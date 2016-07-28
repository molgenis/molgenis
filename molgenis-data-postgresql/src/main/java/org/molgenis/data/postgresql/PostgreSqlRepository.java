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
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.EntityMetaDataUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.annotation.Transactional;

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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.getValueString;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
import static org.molgenis.data.support.EntityMetaDataUtils.isMultipleReferenceType;

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
	private static final Set<RepositoryCapability> REPO_CAPABILITIES = unmodifiableSet(new HashSet<>(
			asList(WRITABLE, MANAGABLE, QUERYABLE, VALIDATE_REFERENCE_CONSTRAINT, VALIDATE_UNIQUE_CONSTRAINT,
					VALIDATE_NOTNULL_CONSTRAINT, CACHEABLE)));

	/**
	 * Supported query operators
	 */
	private static final Set<Operator> QUERY_OPERATORS = unmodifiableSet(
			EnumSet.of(EQUALS, IN, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, RANGE, LIKE, NOT, AND, OR, NESTED));

	private final PostgreSqlEntityFactory postgreSqlEntityFactory;
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

	private EntityMetaData metaData;

	public PostgreSqlRepository(PostgreSqlEntityFactory postgreSqlEntityFactory, JdbcTemplate jdbcTemplate,
			DataSource dataSource)
	{
		this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
		this.dataSource = requireNonNull(dataSource);
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
	}

	void setMetaData(EntityMetaData metaData)
	{
		this.metaData = metaData;
	}

	@Override
	@Transactional(readOnly = true)
	public Iterator<Entity> iterator()
	{
		Query<Entity> q = new QueryImpl<>();
		return findAllBatching(q).iterator();
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

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	@Override
	public long count(Query<Entity> q)
	{
		List<Object> parameters = Lists.newArrayList();
		String sql = getSqlCount(metaData, q, parameters);

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
	@Transactional(readOnly = true)
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return stream(findAllBatching(q).spliterator(), false);
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
		return findOne(new QueryImpl<>().eq(metaData.getIdAttribute().getName(), id));
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		if (id == null)
		{
			return null;
		}
		return findOne(new QueryImpl<>().eq(metaData.getIdAttribute().getName(), id).fetch(fetch));
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
			String sql = getSqlDelete(metaData);
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
		String deleteAllSql = getSqlDeleteAll(metaData);
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
		final EntityMetaData entityMeta = metaData;
		final String allRowsSelect = getSqlSelect(entityMeta, query, emptyList(), false);
		LOG.debug("Fetching [{}] data...", getName());
		LOG.trace("SQL: {}", allRowsSelect);
		RowMapper<Entity> rowMapper = postgreSqlEntityFactory.createRowMapper(entityMeta, fetch);
		template.query(allRowsSelect,
				(ResultSetExtractor) resultSet -> processResultSet(consumer, batchSize, entityMeta, rowMapper,
						resultSet));
		LOG.debug("Streamed entire repository in batches of size {} in {}.", batchSize, stopwatch);
	}

	private Object processResultSet(Consumer<List<Entity>> consumer, int batchSize, EntityMetaData entityMeta,
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
				handleBatch(consumer, entityMeta, batch);
				batch = newHashMap();
			}
		}
		if (!batch.isEmpty())
		{
			handleBatch(consumer, entityMeta, batch);
		}
		return null;
	}

	/**
	 * Handles a batch of Entities. Looks up the values for MREF ID attributes and sets them as references in the
	 * entities. Then feeds the entities to the {@link Consumer}
	 *
	 * @param consumer   {@link Consumer} to feed the batch to after setting the MREF ID values
	 * @param entityMeta EntityMetaData for the {@link Entity}s in the batch
	 * @param batch      {@link Map} mapping entity ID to entity for all {@link Entity}s in the batch
	 */
	private void handleBatch(Consumer<List<Entity>> consumer, EntityMetaData entityMeta, Map<Object, Entity> batch)
	{
		AttributeType idAttributeDataType = entityMeta.getIdAttribute().getDataType();
		LOG.debug("Select ID values for a batch of MREF attributes...");
		for (AttributeMetaData mrefAttr : entityMeta.getAtomicAttributes())
		{
			if (mrefAttr.getExpression() == null && isMultipleReferenceType(mrefAttr))
			{
				EntityMetaData refEntityMeta = mrefAttr.getRefEntity();
				Multimap<Object, Object> mrefIDs = selectMrefIDsForAttribute(entityMeta, idAttributeDataType, mrefAttr,
						batch.keySet(), refEntityMeta.getIdAttribute().getDataType());
				for (Map.Entry entry : batch.entrySet())
				{
					batch.get(entry.getKey()).set(mrefAttr.getName(), postgreSqlEntityFactory
							.getReferences(refEntityMeta, newArrayList(mrefIDs.get(entry.getKey()))));
				}
			}
		}
		LOG.trace("Feeding batch of {} rows to consumer.", batch.size());
		consumer.accept(batch.values().stream().collect(toList()));
	}

	/**
	 * Selects MREF IDs for an MREF attribute from the junction table, in the order of the MREF attribute value.
	 *
	 * @param entityMeta          EntityMetaData for the entities
	 * @param idAttributeDataType {@link AttributeType} of the ID attribute of the entity
	 * @param mrefAttr            AttributeMetaData of the MREF attribute to select the values for
	 * @param ids                 {@link Set} of {@link Object}s containing the values for the ID attribute of the entity
	 * @param refIdDataType       {@link AttributeType} of the ID attribute of the refEntity of the attribute
	 * @return Multimap mapping entity ID to a list containing the MREF IDs for the values in the attribute
	 */
	private Multimap<Object, Object> selectMrefIDsForAttribute(EntityMetaData entityMeta,
			AttributeType idAttributeDataType, AttributeMetaData mrefAttr, Set<Object> ids, AttributeType refIdDataType)
	{
		Stopwatch stopwatch = createStarted();
		Multimap<Object, Object> mrefIDs = ArrayListMultimap.create();
		String junctionTableSelect = getJunctionTableSelect(entityMeta, mrefAttr, ids.size());
		LOG.trace("SQL: {}", junctionTableSelect);
		jdbcTemplate.query(junctionTableSelect, (RowCallbackHandler) row -> mrefIDs
						.put(convert(idAttributeDataType, row.getObject(1)), convert(refIdDataType, row.getObject(3))),
				ids.toArray());

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Selected {} ID values for MREF attribute {} in {}",
					mrefIDs.values().stream().collect(counting()), mrefAttr.getName(), stopwatch);
		}
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

				String sql = getSqlSelect(getEntityMetaData(), batchQuery, parameters, true);
				RowMapper<Entity> entityMapper = postgreSqlEntityFactory
						.createRowMapper(getEntityMetaData(), batchQuery.getFetch());
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

	/**
	 * Returns the PostgreSQL value for the given entity attribute
	 *
	 * @param entity entity
	 * @param attr   attribute
	 * @return PostgreSQL value
	 */
	private static Object getPostgreSqlValue(Entity entity, AttributeMetaData attr)
	{
		String attrName = attr.getName();
		AttributeType attrType = attr.getDataType();

		switch (attrType)
		{
			case BOOL:
				return entity.getBoolean(attrName);
			case CATEGORICAL:
			case FILE:
			case XREF:
				Entity xrefEntity = entity.getEntity(attrName);
				return xrefEntity != null ? getPostgreSqlValue(xrefEntity,
						xrefEntity.getEntityMetaData().getIdAttribute()) : null;
			case CATEGORICAL_MREF:
			case MREF:
				Iterable<Entity> entities = entity.getEntities(attrName);
				return stream(entities.spliterator(), false).map(mrefEntity -> getPostgreSqlValue(mrefEntity,
						mrefEntity.getEntityMetaData().getIdAttribute())).collect(toList());
			case DATE:
				Date date = entity.getDate(attrName);
				return date != null ? new java.sql.Date(date.getTime()) : null;
			case DATE_TIME:
				Date dateTime = entity.getDate(attrName);
				return dateTime != null ? new java.sql.Timestamp(dateTime.getTime()) : null;
			case DECIMAL:
				return entity.getDouble(attrName);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return entity.getString(attrName);
			case INT:
				return entity.getInt(attrName);
			case LONG:
				return entity.getLong(attrName);
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	private Integer addBatching(Iterator<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger();

		final AttributeMetaData idAttr = metaData.getIdAttribute();
		List<AttributeMetaData> persistedAttrs = getPersistedAttributes(metaData).collect(toList());
		final List<AttributeMetaData> persistedNonMrefAttrs = persistedAttrs.stream()
				.filter(attr -> !isMultipleReferenceType(attr)).collect(toList());
		final List<AttributeMetaData> persistedMrefAttrs = persistedAttrs.stream()
				.filter(EntityMetaDataUtils::isMultipleReferenceType).collect(toList());
		final String insertSql = getSqlInsert(metaData);

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
					for (AttributeMetaData attr : persistedNonMrefAttrs)
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
			if (!persistedMrefAttrs.isEmpty())
			{
				Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

				for (Entity entity : entitiesBatch)
				{
					for (AttributeMetaData attr : persistedMrefAttrs)
					{
						mrefs.putIfAbsent(attr.getName(), new ArrayList<>());
						if (entity.get(attr.getName()) != null)
						{
							AtomicInteger seqNr = new AtomicInteger();
							for (Entity val : entity.getEntities(attr.getName()))
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

				for (AttributeMetaData attr : persistedMrefAttrs)
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
		final AttributeMetaData idAttr = metaData.getIdAttribute();
		List<AttributeMetaData> persistedAttrs = getPersistedAttributes(metaData).collect(toList());
		final List<AttributeMetaData> persistedNonMrefAttrs = persistedAttrs.stream()
				.filter(attr -> !isMultipleReferenceType(attr)).collect(toList());
		final List<AttributeMetaData> persistedMrefAttrs = persistedAttrs.stream()
				.filter(EntityMetaDataUtils::isMultipleReferenceType).collect(toList());
		final String updateSql = getSqlUpdate(metaData);

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
					for (AttributeMetaData attr : persistedNonMrefAttrs)
					{
						Object postgreSqlValue = getPostgreSqlValue(entity, attr);
						preparedStatement.setObject(fieldIndex++, postgreSqlValue);
					}

					preparedStatement.setObject(fieldIndex++, getPostgreSqlValue(entity, idAttr));
				}

				@Override
				public int getBatchSize()
				{
					return entitiesBatch.size();
				}
			});

			// update values in entity junction table
			if (!persistedMrefAttrs.isEmpty())
			{
				Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

				for (Entity entity : entitiesBatch)
				{
					// create the mref records
					for (AttributeMetaData attr : persistedMrefAttrs)
					{
						mrefs.putIfAbsent(attr.getName(), new ArrayList<>());
						if (entity.get(attr.getName()) != null)
						{
							AtomicInteger seqNr = new AtomicInteger();
							for (Entity val : entity.getEntities(attr.getName()))
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
				for (AttributeMetaData attr : persistedMrefAttrs)
				{
					if (isMultipleReferenceType(attr))
					{
						removeMrefs(ids, attr);
						addMrefs(mrefs.get(attr.getName()), attr);
					}
				}
			}
		});
	}

	private void addMrefs(final List<Map<String, Object>> mrefs, final AttributeMetaData attr)
	{
		AttributeMetaData idAttribute = metaData.getIdAttribute();
		String insertMrefSql = getSqlInsertMref(metaData, attr, idAttribute);

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

				preparedStatement.setInt(1, (int) mref.get(JUNCTION_TABLE_ORDER_ATTR_NAME));
				preparedStatement.setObject(2, mref.get(idAttribute.getName()));
				Entity mrefEntity = (Entity) mref.get(attr.getName());
				preparedStatement
						.setObject(3, getPostgreSqlValue(mrefEntity, mrefEntity.getEntityMetaData().getIdAttribute()));
			}

			@Override
			public int getBatchSize()
			{
				return mrefs.size();
			}
		});
	}

	private void removeMrefs(final List<Object> ids, final AttributeMetaData attr)
	{
		final AttributeMetaData idAttribute = metaData.getIdAttribute();
		String deleteMrefSql = getSqlDelete(getJunctionTableName(metaData, attr), idAttribute);

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
	private static Object convert(AttributeMetaData attr, Object value)
	{
		FieldType fieldType = MolgenisFieldTypes.getType(getValueString(attr.getDataType()));
		return fieldType.convert(value);
	}

	@Deprecated
	private static Object convert(AttributeType attrType, Object value)
	{
		FieldType fieldType = MolgenisFieldTypes.getType(getValueString(attrType));
		return fieldType.convert(value);
	}
}