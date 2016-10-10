package org.molgenis.data.postgresql;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Stopwatch.createStarted;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ONE_TO_MANY;
import static org.molgenis.MolgenisFieldTypes.AttributeType.getValueString;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
import static org.molgenis.data.postgresql.PostgreSqlUtils.getPostgreSqlValue;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;

/**
 * Repository that persists entities in a PostgreSQL database
 * <ul>
 * <li>Attributes with expression are not persisted</li>
 * <li>Cross-backend attribute references are supported</li>
 * <li>Query operators DIS_MAX, FUZZY_MATCH, FUZZY_MATCH_NGRAM, SEARCH, SHOULD are not supported</li>
 * </ul>
 */
public class PostgreSqlRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepository.class);

	/**
	 * JDBC batch operation size
	 */
	private static final int BATCH_SIZE = 1000;
	/**
	 * Repository capabilities
	 */
	private static final Set<RepositoryCapability> REPO_CAPABILITIES = unmodifiableSet(
			EnumSet.of(WRITABLE, MANAGABLE, QUERYABLE, VALIDATE_REFERENCE_CONSTRAINT, VALIDATE_UNIQUE_CONSTRAINT,
					VALIDATE_NOTNULL_CONSTRAINT, CACHEABLE));

	/**
	 * Supported query operators
	 */
	private static final Set<Operator> QUERY_OPERATORS = unmodifiableSet(
			EnumSet.of(EQUALS, IN, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, RANGE, LIKE, NOT, AND, OR, NESTED));

	private final PostgreSqlEntityFactory postgreSqlEntityFactory;
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	private final PlatformTransactionManager transactionManager;

	private EntityType entityType;

	public PostgreSqlRepository(PostgreSqlEntityFactory postgreSqlEntityFactory, JdbcTemplate jdbcTemplate,
			DataSource dataSource, PlatformTransactionManager transactionManager)
	{
		this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
		this.dataSource = requireNonNull(dataSource);
		this.transactionManager = requireNonNull(transactionManager);
	}

	void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setReadOnly(true);

		return transactionTemplate.execute((status) ->
		{
			Query<Entity> q = new QueryImpl<>();
			return findAllBatching(q).iterator();
		});
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return REPO_CAPABILITIES;
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return QUERY_OPERATORS;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public long count(Query<Entity> q)
	{
		List<Object> parameters = Lists.newArrayList();
		String sql = getSqlCount(entityType, q, parameters);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Counting [{}] rows for query [{}]", getName(), q);
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}, parameters: {}", sql, parameters);
			}
		}
		return jdbcTemplate.queryForObject(sql, parameters.toArray(new Object[parameters.size()]), Long.class);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setReadOnly(true);

		return transactionTemplate.execute((status) -> stream(findAllBatching(q).spliterator(), false));
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		Iterator<Entity> iterator = findAll(q).iterator();
		if (iterator.hasNext())
		{
			return iterator.next();
		}
		return null;
	}

	@Override
	public Entity findOneById(Object id)
	{
		if (id == null)
		{
			return null;
		}
		return findOne(new QueryImpl<>().eq(entityType.getIdAttribute().getName(), id));
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		if (id == null)
		{
			return null;
		}
		return findOne(new QueryImpl<>().eq(entityType.getIdAttribute().getName(), id).fetch(fetch));
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void update(Entity entity)
	{
		update(Stream.of(entity));
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void update(Stream<Entity> entities)
	{
		updateBatching(entities.iterator());
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void delete(Entity entity)
	{
		this.delete(Stream.of(entity));
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void delete(Stream<Entity> entities)
	{
		deleteAll(entities.map(Entity::getIdValue));
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void deleteById(Object id)
	{
		this.deleteAll(Stream.of(id));
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void deleteAll(Stream<Object> ids)
	{
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(idsBatch ->
		{
			String sql = getSqlDelete(entityType);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Deleting {} [{}] entities", idsBatch.size(), getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sql);
				}
			}
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
			{
				@Override
				public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
				{
					preparedStatement.setObject(1, idsBatch.get(i));
				}

				@Override
				public int getBatchSize()
				{
					return idsBatch.size();
				}
			});
		});
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void deleteAll()
	{
		String deleteAllSql = getSqlDeleteAll(entityType);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleting all [{}] entities", getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", deleteAllSql);
			}
		}
		jdbcTemplate.update(deleteAllSql);
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void add(Entity entity)
	{
		if (entity == null)
		{
			throw new RuntimeException("PostgreSqlRepository.add() failed: entity was null");
		}
		add(Stream.of(entity));
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public Integer add(Stream<Entity> entities)
	{
		return addBatching(entities.iterator());
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		final Stopwatch stopwatch = createStarted();
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		template.setFetchSize(batchSize);

		final Query<Entity> query = new QueryImpl<>();
		if (fetch != null)
		{
			query.fetch(fetch);
		}
		final EntityType entityType = this.entityType;
		final String allRowsSelect = getSqlSelect(entityType, query, emptyList(), false);
		LOG.debug("Fetching [{}] data...", getName());
		LOG.trace("SQL: {}", allRowsSelect);
		RowMapper<Entity> rowMapper = postgreSqlEntityFactory.createRowMapper(entityType, fetch);
		template.query(allRowsSelect,
				(ResultSetExtractor) resultSet -> processResultSet(consumer, batchSize, entityType, rowMapper,
						resultSet));
		LOG.debug("Streamed entire repository in batches of size {} in {}.", batchSize, stopwatch);
	}

	private Object processResultSet(Consumer<List<Entity>> consumer, int batchSize, EntityType entityType,
			RowMapper<Entity> rowMapper, ResultSet resultSet) throws SQLException
	{
		int rowNum = 0;
		Map<Object, Entity> batch = newHashMap();
		while (resultSet.next())
		{
			Entity entity = rowMapper.mapRow(resultSet, rowNum++);
			batch.put(entity.getIdValue(), entity);
			if (rowNum % batchSize == 0)
			{
				handleBatch(consumer, entityType, batch);
				batch = newHashMap();
			}
		}
		if (!batch.isEmpty())
		{
			handleBatch(consumer, entityType, batch);
		}
		return null;
	}

	/**
	 * Handles a batch of Entities. Looks up the values for MREF ID attributes and sets them as references in the
	 * entities. Then feeds the entities to the {@link Consumer}
	 *
	 * @param consumer   {@link Consumer} to feed the batch to after setting the MREF ID values
	 * @param entityType EntityType for the {@link Entity}s in the batch
	 * @param batch      {@link Map} mapping entity ID to entity for all {@link Entity}s in the batch
	 */
	private void handleBatch(Consumer<List<Entity>> consumer, EntityType entityType, Map<Object, Entity> batch)
	{
		AttributeType idAttributeDataType = entityType.getIdAttribute().getDataType();
		LOG.debug("Select ID values for a batch of MREF attributes...");
		for (Attribute mrefAttr : entityType.getAtomicAttributes())
		{
			if (mrefAttr.getExpression() == null && isMultipleReferenceType(mrefAttr) && !(mrefAttr.getDataType() == ONE_TO_MANY && mrefAttr.isMappedBy()))
			{
				EntityType refEntityType = mrefAttr.getRefEntity();
				Multimap<Object, Object> mrefIDs = selectMrefIDsForAttribute(entityType, idAttributeDataType, mrefAttr,
						batch.keySet(), refEntityType.getIdAttribute().getDataType());
				for (Map.Entry entry : batch.entrySet())
				{
					batch.get(entry.getKey()).set(mrefAttr.getName(), postgreSqlEntityFactory
							.getReferences(refEntityType, newArrayList(mrefIDs.get(entry.getKey()))));
				}
			}
		}
		LOG.trace("Feeding batch of {} rows to consumer.", batch.size());
		consumer.accept(batch.values().stream().collect(toList()));
	}

	/**
	 * Selects MREF IDs for an MREF attribute from the junction table, in the order of the MREF attribute value.
	 *
	 * @param entityType          EntityType for the entities
	 * @param idAttributeDataType {@link AttributeType} of the ID attribute of the entity
	 * @param mrefAttr            Attribute of the MREF attribute to select the values for
	 * @param ids                 {@link Set} of {@link Object}s containing the values for the ID attribute of the entity
	 * @param refIdDataType       {@link AttributeType} of the ID attribute of the refEntity of the attribute
	 * @return Multimap mapping entity ID to a list containing the MREF IDs for the values in the attribute
	 */
	private Multimap<Object, Object> selectMrefIDsForAttribute(EntityType entityType, AttributeType idAttributeDataType,
			Attribute mrefAttr, Set<Object> ids, AttributeType refIdDataType)
	{
		Stopwatch stopwatch = null;
		if (LOG.isTraceEnabled()) stopwatch = createStarted();

		String junctionTableSelect = getSqlJunctionTableSelect(entityType, mrefAttr, ids.size());
		LOG.trace("SQL: {}", junctionTableSelect);

		Multimap<Object, Object> mrefIDs = ArrayListMultimap.create();
		jdbcTemplate.query(junctionTableSelect, (RowCallbackHandler) row -> mrefIDs
						.put(convert(idAttributeDataType, row.getObject(1)), convert(refIdDataType, row.getObject(3))),
				ids.toArray());

		if (LOG.isTraceEnabled()) LOG.trace("Selected {} ID values for MREF attribute {} in {}",
				mrefIDs.values().stream().collect(counting()), mrefAttr.getName(), stopwatch);
		return mrefIDs;
	}

	private BatchingQueryResult<Entity> findAllBatching(Query<Entity> q)
	{
		return new BatchingQueryResult<Entity>(BATCH_SIZE, q)
		{
			@Override
			protected List<Entity> getBatch(Query<Entity> batchQuery)
			{
				List<Object> parameters = new ArrayList<>();

				String sql = getSqlSelect(getEntityType(), batchQuery, parameters, true);
				RowMapper<Entity> entityMapper = postgreSqlEntityFactory
						.createRowMapper(getEntityType(), batchQuery.getFetch());
				LOG.debug("Fetching [{}] data for query [{}]", getName(), batchQuery);
				LOG.trace("SQL: {}, parameters: {}", sql, parameters);
				Stopwatch sw = createStarted();
				List<Entity> result = jdbcTemplate
						.query(sql, parameters.toArray(new Object[parameters.size()]), entityMapper);
				LOG.trace("That took {}", sw);
				return result;
			}
		};
	}

	private Integer addBatching(Iterator<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger();

		final Attribute idAttr = entityType.getIdAttribute();
		final List<Attribute> tableAttrs = getTableAttributes(entityType).collect(toList());
		final List<Attribute> junctionTableAttrs = getJunctionTableAttributes(entityType).collect(toList());
		final String insertSql = getSqlInsert(entityType);

		Iterators.partition(entities, BATCH_SIZE).forEachRemaining(entitiesBatch ->
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Adding {} [{}] entities", entitiesBatch.size(), getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", insertSql);
				}
			}

			// persist values in entity table
			jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter()
			{
				@Override
				public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
				{
					Entity entity = entitiesBatch.get(rowIndex);

					int fieldIndex = 1;
					for (Attribute attr : tableAttrs)
					{
						Object postgreSqlValue = getPostgreSqlValue(entity, attr);
						preparedStatement.setObject(fieldIndex++, postgreSqlValue);
					}
				}

				@Override
				public int getBatchSize()
				{
					return entitiesBatch.size();
				}
			});

			// persist values in entity junction table
			if (!junctionTableAttrs.isEmpty())
			{
				Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

				for (Entity entity : entitiesBatch)
				{
					for (Attribute attr : junctionTableAttrs)
					{
						mrefs.putIfAbsent(attr.getName(), new ArrayList<>());
						if (entity.get(attr.getName()) != null)
						{
							AtomicInteger seqNr = new AtomicInteger();
							Iterable<Entity> refEntities;
							if (isSingleReferenceType(attr) && attr.isInversedBy())
							{
								refEntities = singletonList(entity.getEntity(attr.getName()));
							}
							else
							{
								refEntities = entity.getEntities(attr.getName());
							}
							for (Entity val : refEntities)
							{
								if (val != null)
								{
									Map<String, Object> mref = new HashMap<>();
									mref.put(JUNCTION_TABLE_ORDER_ATTR_NAME, seqNr.getAndIncrement());
									mref.put(idAttr.getName(), entity.get(idAttr.getName()));
									mref.put(attr.getName(), val);
									mrefs.get(attr.getName()).add(mref);
								}
							}
						}
					}
				}

				for (Attribute attr : junctionTableAttrs)
				{
					List<Map<String, Object>> attrMrefs = mrefs.get(attr.getName());
					if (attrMrefs != null && !attrMrefs.isEmpty())
					{
						addMrefs(attrMrefs, attr);
					}
				}
			}

			count.addAndGet(entitiesBatch.size());
		});

		return count.get();
	}

	private void updateBatching(Iterator<? extends Entity> entities)
	{
		final Attribute idAttr = entityType.getIdAttribute();
		final List<Attribute> tableAttrs = getTableAttributes(entityType).collect(toList());
		final List<Attribute> junctionTableAttrs = getJunctionTableAttributes(entityType).collect(toList());
		final String updateSql = getSqlUpdate(entityType);

		// update values in entity table
		Iterators.partition(entities, BATCH_SIZE).forEachRemaining(entitiesBatch ->
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Updating {} [{}] entities", entitiesBatch.size(), getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", updateSql);
				}
			}
			jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter()
			{
				@Override
				public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
				{
					Entity entity = entitiesBatch.get(rowIndex);

					int fieldIndex = 1;
					for (Attribute attr : tableAttrs)
					{
						Object postgreSqlValue = getPostgreSqlValue(entity, attr);
						preparedStatement.setObject(fieldIndex++, postgreSqlValue);
					}

					preparedStatement.setObject(fieldIndex, getPostgreSqlValue(entity, idAttr));
				}

				@Override
				public int getBatchSize()
				{
					return entitiesBatch.size();
				}
			});

			// update values in entity junction table
			if (!junctionTableAttrs.isEmpty())
			{
				Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

				for (Entity entity : entitiesBatch)
				{
					// create the mref records
					for (Attribute attr : junctionTableAttrs)
					{
						mrefs.putIfAbsent(attr.getName(), new ArrayList<>());
						if (entity.get(attr.getName()) != null)
						{
							AtomicInteger seqNr = new AtomicInteger();
							Iterable<Entity> refEntities;
							if (isSingleReferenceType(attr) && attr.isInversedBy())
							{
								refEntities = singletonList(entity.getEntity(attr.getName()));
							}
							else
							{
								refEntities = entity.getEntities(attr.getName());
							}
							for (Entity val : refEntities)
							{
								Map<String, Object> mref = new HashMap<>();
								mref.put(JUNCTION_TABLE_ORDER_ATTR_NAME, seqNr.getAndIncrement());
								mref.put(idAttr.getName(), entity.get(idAttr.getName()));
								mref.put(attr.getName(), val);
								mrefs.get(attr.getName()).add(mref);
							}
						}
					}
				}
				// update mrefs
				List<Object> ids = entitiesBatch.stream().map(entity -> getPostgreSqlValue(entity, idAttr))
						.collect(toList());
				for (Attribute attr : junctionTableAttrs)
				{
					removeMrefs(ids, attr);
					addMrefs(mrefs.get(attr.getName()), attr);
				}
			}
		});
	}

	private void addMrefs(final List<Map<String, Object>> mrefs, final Attribute attr)
	{
		// database doesn't validate NOT NULL constraint for attribute values referencing multiple entities,
		// so validate it ourselves
		if (!attr.isNillable() && mrefs.isEmpty())
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Entity [%s] attribute [%s] value cannot be null", entityType.getName(), attr.getName())));
		}

		final Attribute idAttr = entityType.getIdAttribute();
		String insertMrefSql = getSqlInsertJunction(entityType, attr);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Adding junction table entries for entity [{}] attribute [{}]", getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", insertMrefSql);
			}
		}
		jdbcTemplate.batchUpdate(insertMrefSql, new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				Map<String, Object> mref = mrefs.get(i);

				Object idValue0, idValue1;
				if (attr.isMappedBy())
				{
					Entity mrefEntity = (Entity) mref.get(attr.getName());
					idValue0 = getPostgreSqlValue(mrefEntity, attr.getRefEntity().getIdAttribute());
					idValue1 = mref.get(idAttr.getName());
				}
				else
				{
					idValue0 = mref.get(idAttr.getName());
					Entity mrefEntity = (Entity) mref.get(attr.getName());
					idValue1 = getPostgreSqlValue(mrefEntity, mrefEntity.getEntityType().getIdAttribute());
				}
				preparedStatement.setInt(1, (int) mref.get(JUNCTION_TABLE_ORDER_ATTR_NAME));
				preparedStatement.setObject(2, idValue0);
				preparedStatement.setObject(3, idValue1);
			}

			@Override
			public int getBatchSize()
			{
				return mrefs.size();
			}
		});
	}

	private void removeMrefs(final List<Object> ids, final Attribute attr)
	{
		final Attribute idAttr = attr.isMappedBy() ? attr.getMappedBy() : entityType.getIdAttribute();
		String deleteMrefSql = getSqlDelete(getJunctionTableName(entityType, attr), idAttr);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Removing junction table entries for entity [{}] attribute [{}]", getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", deleteMrefSql);
			}
		}
		jdbcTemplate.batchUpdate(deleteMrefSql, new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				preparedStatement.setObject(1, ids.get(i));
			}

			@Override
			public int getBatchSize()
			{
				return ids.size();
			}
		});
	}

	@Deprecated
	private static Object convert(AttributeType attrType, Object value)
	{
		FieldType fieldType = MolgenisFieldTypes.getType(getValueString(attrType));
		return fieldType.convert(value);
	}
}