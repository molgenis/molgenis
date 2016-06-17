package org.molgenis.data.postgresql;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Stopwatch.createStarted;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;

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

	/** JDBC batch operation size */
	private static final int BATCH_SIZE = 1000;

	/** Repository capabilities */
	private static final Set<RepositoryCapability> REPO_CAPABILITIES = unmodifiableSet(EnumSet.of(WRITABLE, MANAGABLE,
			QUERYABLE, VALIDATE_REFERENCE_CONSTRAINT, VALIDATE_UNIQUE_CONSTRAINT, VALIDATE_NOTNULL_CONSTRAINT));

	/** Supported query operators */
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

	public void setMetaData(EntityMetaData metaData)
	{
		this.metaData = metaData;
	}

	@Override
	@Transactional(readOnly = true)
	public Iterator<Entity> iterator()
	{
		Query<Entity> q = new QueryImpl<Entity>();
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
		String sql = getSqlCount(getEntityMetaData(), q, parameters);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Counting [{}] rows for query [{}]", getName(), q);
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}, parameters: {}", sql, parameters);
			}
		}
		return jdbcTemplate.queryForObject(sql, parameters.toArray(new Object[0]), Long.class);
	}

	@Override
	@Transactional(readOnly = true)
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return StreamSupport.stream(findAllBatching(q).spliterator(), false);
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
		return findOne(new QueryImpl<Entity>().eq(getEntityMetaData().getIdAttribute().getName(), id));
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		if (id == null)
		{
			return null;
		}
		return findOne(new QueryImpl<Entity>().eq(getEntityMetaData().getIdAttribute().getName(), id).fetch(fetch));
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
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(idsBatch -> {
			String sql = getSqlDelete(getEntityMetaData());
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
		String deleteAllSql = getSqlDeleteAll(getEntityMetaData());
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

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void create()
	{
		String createTableSql = getSqlCreateTable(getEntityMetaData());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating table for entity [{}]", getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createTableSql);
			}
		}
		jdbcTemplate.execute(createTableSql);

		String idAttrName = getEntityMetaData().getIdAttribute().getName();
		getPersistedAttributes(getEntityMetaData()).forEach(attr -> {
			// add mref tables
			if (attr.getDataType() instanceof MrefField)
			{
				String createJunctionTableSql = getSqlCreateJunctionTable(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating junction table for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createJunctionTableSql);
					}
				}
				jdbcTemplate.execute(createJunctionTableSql);
				String createIndexJunctionTableSql = getSqlCreateJunctionTableIndex(getEntityMetaData(), attr);
				LOG.debug("Creating index on junction table for entity [{}] attribute [{}]", getName(), attr.getName());
				LOG.trace("SQL: {}", createIndexJunctionTableSql);
				jdbcTemplate.execute(createIndexJunctionTableSql);
			}
			else if (attr.getDataType() instanceof XrefField && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				String createForeignKeySql = getSqlCreateForeignKey(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating foreign key for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createForeignKeySql);
					}
				}
				jdbcTemplate.execute(createForeignKeySql);
			}

			if (attr.isUnique() && !attr.getName().equals(idAttrName))
			{
				String createUniqueSql = getSqlCreateUniqueKey(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating unique key for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createUniqueSql);
					}
				}
				jdbcTemplate.execute(createUniqueSql);
			}
		});
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	@Override
	public void drop()
	{
		getPersistedAttributesMref(getEntityMetaData()).forEach(mrefAttr -> {
			String sqlDropJunctionTable = getSqlDropJunctionTable(getEntityMetaData(), mrefAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Dropping junction table for entity [{}] attribute [{}]", getName(), mrefAttr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlDropJunctionTable);
				}
			}
			jdbcTemplate.execute(sqlDropJunctionTable);
		});

		String sqlDropTable = getSqlDropTable(getEntityMetaData());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping table for entity [{}]", getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropTable);
			}
		}
		jdbcTemplate.execute(sqlDropTable);
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	void dropAttribute(String attrName)
	{
		String dropColumnSql = getSqlDropColumn(getEntityMetaData(), attrName);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping column for entity [{}] attribute [{}]", getName(), attrName);
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropColumnSql);
			}
		}
		jdbcTemplate.execute(dropColumnSql);

		DefaultEntityMetaData demd = new DefaultEntityMetaData(metaData);
		demd.removeAttributeMetaData(demd.getAttribute(attrName));
		setMetaData(demd);
	}

	/**
	 * Adds an attribute to the repository.
	 * 
	 * @param attr
	 *            the {@link AttributeMetaData} to add
	 */
	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	void addAttribute(AttributeMetaData attr)
	{
		addAttributeRec(attr, true);
	}

	// FIXME code duplication with create table
	/**
	 * Adds an attribute to the repository.
	 *
	 * @param attr
	 *            the {@link AttributeMetaData} to add
	 * @param addToEntityMetaData
	 *            boolean indicating if the repository's {@link EntityMetaData} should be updated as well. This should
	 *            not happen for parts of a compound attribute.
	 */
	private void addAttributeRec(AttributeMetaData attr, boolean addToEntityMetaData)
	{
		try
		{
			if (attr.getExpression() != null)
			{
				// computed attributes are not persisted
				return;
			}
			if (attr.getDataType() instanceof MrefField)
			{
				String createJunctionTableSql = getSqlCreateJunctionTable(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating junction table for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createJunctionTableSql);
					}
				}
				jdbcTemplate.execute(createJunctionTableSql);
				String createIndexJunctionTableSql = getSqlCreateJunctionTableIndex(getEntityMetaData(), attr);
				LOG.debug("Creating index on junction table for entity [{}] attribute [{}]", getName(), attr.getName());
				LOG.trace("SQL: {}", createIndexJunctionTableSql);
				jdbcTemplate.execute(createIndexJunctionTableSql);
			}
			else if (!attr.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				String addColumnSql = getSqlAddColumn(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating column for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", addColumnSql);
					}
				}
				jdbcTemplate.execute(addColumnSql);
			}

			if (attr.getDataType() instanceof XrefField && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				String createForeignKeySql = getSqlCreateForeignKey(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating foreign key for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createForeignKeySql);
					}
				}
				jdbcTemplate.execute(createForeignKeySql);
			}

			String idAttrName = getEntityMetaData().getIdAttribute().getName();
			if (attr.isUnique() && !attr.getName().equals(idAttrName))
			{
				String createUniqueKeySql = getSqlCreateUniqueKey(getEntityMetaData(), attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating unique key for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createUniqueKeySql);
					}
				}
				jdbcTemplate.execute(createUniqueKeySql);
			}

			if (attr.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				for (AttributeMetaData attrPart : attr.getAttributeParts())
				{
					addAttributeRec(attrPart, false);
				}
			}
			DefaultEntityMetaData demd = new DefaultEntityMetaData(metaData);
			if (addToEntityMetaData)
			{
				demd.addAttributeMetaData(attr);
			}
			setMetaData(demd);
		}
		catch (Exception e)
		{
			LOG.error("Exception updating PostgreSqlRepository.", e);
			throw new MolgenisDataException(e);
		}
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
		final EntityMetaData entityMeta = getEntityMetaData();
		final String allRowsSelect = getSqlSelect(entityMeta, query, emptyList(), false);
		LOG.debug("Fetching [{}] data...", getName());
		LOG.trace("SQL: {}", allRowsSelect);
		RowMapper<Entity> rowMapper = postgreSqlEntityFactory.createRowMapper(entityMeta, fetch);
		template.query(allRowsSelect, (ResultSetExtractor) resultSet ->
				processResultSet(consumer, batchSize, entityMeta, rowMapper, resultSet));
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
		FieldType idAttributeDataType = entityMeta.getIdAttribute().getDataType();
		LOG.debug("Select ID values for a batch of MREF attributes...");
		for (AttributeMetaData mrefAttr : entityMeta.getAtomicAttributes())
		{
			if (mrefAttr.getExpression() == null && mrefAttr.getDataType() instanceof MrefField)
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
	 * @param idAttributeDataType {@link FieldType} of the ID attribute of the entity
	 * @param mrefAttr            AttributeMetaData of the MREF attribute to select the values for
	 * @param ids                 {@link Set} of {@link Object}s containing the values for the ID attribute of the entity
	 * @param refIdDataType       {@link FieldType} of the ID attribute of the refEntity of the attribute
	 * @return Multimap mapping entity ID to a list containing the MREF IDs for the values in the attribute
	 */
	private Multimap<Object, Object> selectMrefIDsForAttribute(EntityMetaData entityMeta, FieldType idAttributeDataType,
			AttributeMetaData mrefAttr, Set<Object> ids, FieldType refIdDataType)
	{
		Stopwatch stopwatch = createStarted();
		Multimap<Object, Object> mrefIDs = ArrayListMultimap.create();
		String junctionTableSelect = getJunctionTableSelect(entityMeta, mrefAttr, ids.size());
		LOG.trace("SQL: {}", junctionTableSelect);
		jdbcTemplate.query(junctionTableSelect, (RowCallbackHandler) row -> mrefIDs
						.put(idAttributeDataType.convert(row.getObject(1)), refIdDataType.convert(row.getObject(3))),
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
				List<Entity> result = jdbcTemplate.query(sql, parameters.toArray(new Object[0]), entityMapper);
				LOG.trace("That took {}", sw);
				return result;
			}
		};
	}

	private Integer addBatching(Iterator<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger();

		final AttributeMetaData idAttr = getEntityMetaData().getIdAttribute();
		List<AttributeMetaData> persistedAttrs = getPersistedAttributes(getEntityMetaData()).collect(toList());
		final List<AttributeMetaData> persistedNonMrefAttrs = persistedAttrs.stream()
				.filter(attr -> !(attr.getDataType() instanceof MrefField)).collect(toList());
		final List<AttributeMetaData> persistedMrefAttrs = persistedAttrs.stream()
				.filter(attr -> attr.getDataType() instanceof MrefField).collect(toList());
		final String insertSql = getSqlInsert(getEntityMetaData());

		Iterators.partition(entities, BATCH_SIZE).forEachRemaining(entitiesBatch -> {
			final Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

			LOG.debug("Adding {} [{}] entities", entitiesBatch.size(), getName());
			LOG.trace("SQL: {}", insertSql);
			jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter()
			{
				@Override
				public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
				{
					Entity entity = entitiesBatch.get(rowIndex);

					int fieldIndex = 1;
					for (AttributeMetaData attr : persistedNonMrefAttrs)
					{
						if (entity.get(attr.getName()) == null)
						{
							if (attr.equals(getEntityMetaData().getIdAttribute()) && attr.isAuto()
									&& (attr.getDataType() instanceof StringField))
							{
								throw new MolgenisDataException(
										"Missing auto id value. Please use the 'AutoValueRepositoryDecorator' to add auto id capabilities.");
							}
							preparedStatement.setObject(fieldIndex++, null);
						}
						else if (attr.getDataType() instanceof XrefField)
						{
							Object value = entity.get(attr.getName());
							if (value instanceof Entity)
							{
								value = ((Entity) value).get(attr.getRefEntity().getIdAttribute().getName());
							}

							preparedStatement.setObject(fieldIndex++,
									attr.getRefEntity().getIdAttribute().getDataType().convert(value));
						}
						else
						{
							Object value = attr.getDataType().convert(entity.get(attr.getName()));
							if (attr.getDataType() instanceof DateField)
							{
								value = new java.sql.Date(((java.util.Date) value).getTime());
							}
							else if (attr.getDataType() instanceof DatetimeField)
							{
								value = new java.sql.Timestamp(((java.util.Date) value).getTime());
							}
							preparedStatement.setObject(fieldIndex++, value);
						}
					}

					// create the mref records
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
									mref.put(attr.getName(), val.getIdValue());
									mrefs.get(attr.getName()).add(mref);
								}
							}
						}
					}
				}

				@Override
				public int getBatchSize()
				{
					return entitiesBatch.size();
				}
			});

			// add mrefs as well
			for (AttributeMetaData attr : persistedMrefAttrs)
			{
				addMrefs(mrefs.get(attr.getName()), attr);
			}

			count.addAndGet(entitiesBatch.size());
		});

		return count.get();
	}

	private void updateBatching(Iterator<? extends Entity> entities)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		List<AttributeMetaData> persistedAttrs = getPersistedAttributes(getEntityMetaData()).collect(toList());
		final List<AttributeMetaData> persistedNonMrefAttrs = persistedAttrs.stream()
				.filter(attr -> !(attr.getDataType() instanceof MrefField)).collect(toList());
		final List<AttributeMetaData> persistedMrefAttrs = persistedAttrs.stream()
				.filter(attr -> attr.getDataType() instanceof MrefField).collect(toList());
		final String updateSql = getSqlUpdate(getEntityMetaData());

		Iterators.partition(entities, BATCH_SIZE).forEachRemaining(entitiesBatch -> {
			final List<Object> ids = new ArrayList<Object>();
			final Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

			LOG.debug("Updating {} [{}] entities", entitiesBatch.size(), getName());
			LOG.trace("SQL: {}", updateSql);
			jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter()
			{
				@Override
				public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
				{
					Entity entity = entitiesBatch.get(rowIndex);

					Object idValue = idAttribute.getDataType().convert(entity.get(idAttribute.getName()));
					ids.add(idValue);
					int fieldIndex = 1;
					for (AttributeMetaData attr : persistedNonMrefAttrs)
					{
						if (entity.get(attr.getName()) == null)
						{
							// repository should not fill in default value, the form should
							preparedStatement.setObject(fieldIndex++, null);
						}
						else
						{
							if (attr.getDataType() instanceof XrefField)
							{
								Object value = entity.get(attr.getName());
								if (value instanceof Entity)
								{
									preparedStatement.setObject(fieldIndex++,
											attr.getRefEntity().getIdAttribute().getDataType().convert(((Entity) value)
													.get(attr.getRefEntity().getIdAttribute().getName())));
								}
								else
								{
									preparedStatement.setObject(fieldIndex++,
											attr.getRefEntity().getIdAttribute().getDataType().convert(value));
								}
							}
							else
							{
								Object value = attr.getDataType().convert(entity.get(attr.getName()));
								if (attr.getDataType() instanceof DateField)
								{
									value = new java.sql.Date(((java.util.Date) value).getTime());
								}
								else if (attr.getDataType() instanceof DatetimeField)
								{
									value = new java.sql.Timestamp(((java.util.Date) value).getTime());
								}
								preparedStatement.setObject(fieldIndex++, value);
							}
						}
					}

					// create the mref records
					for (AttributeMetaData attr : persistedMrefAttrs)
					{
						mrefs.putIfAbsent(attr.getName(), new ArrayList<>());
						if (entity.get(attr.getName()) != null)
						{
							List<Entity> vals = Lists.newArrayList(entity.getEntities(attr.getName()));
							if (vals != null)
							{
								AtomicInteger seqNr = new AtomicInteger();
								for (Entity val : vals)
								{
									Map<String, Object> mref = new HashMap<>();
									mref.put(JUNCTION_TABLE_ORDER_ATTR_NAME, seqNr.getAndIncrement());
									mref.put(idAttribute.getName(), idValue);
									mref.put(attr.getName(), val.get(attr.getRefEntity().getIdAttribute().getName()));
									mrefs.get(attr.getName()).add(mref);
								}
							}
						}
					}
					preparedStatement.setObject(fieldIndex++, idValue);
				}

				@Override
				public int getBatchSize()
				{
					return entitiesBatch.size();
				}
			});

			// update mrefs
			for (AttributeMetaData attr : persistedMrefAttrs)
			{
				if (attr.getDataType() instanceof MrefField)
				{
					removeMrefs(ids, attr);
					addMrefs(mrefs.get(attr.getName()), attr);
				}
			}
		});
	}

	private void addMrefs(final List<Map<String, Object>> mrefs, final AttributeMetaData attr)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final AttributeMetaData refEntityIdAttribute = attr.getRefEntity().getIdAttribute();

		String insertMrefSql = getSqlInsertMref(getEntityMetaData(), attr, idAttribute);

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

				Object value = mref.get(attr.getName());
				if (value instanceof Entity)
				{
					preparedStatement.setObject(3, refEntityIdAttribute.getDataType()
							.convert(((Entity) value).get(refEntityIdAttribute.getName())));
				}
				else
				{
					preparedStatement.setObject(3, refEntityIdAttribute.getDataType().convert(value));
				}
			}

			@Override
			public int getBatchSize()
			{
				if (null == mrefs)
				{
					return 0;
				}
				return mrefs.size();
			}
		});
	}

	private void removeMrefs(final List<Object> ids, final AttributeMetaData attr)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		String deleteMrefSql = getSqlDelete(getJunctionTableName(getEntityMetaData(), attr), idAttribute);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Removing junction table entries for entity [{}] attribute [{}]", getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", deleteMrefSql);
			}
		}
		jdbcTemplate.batchUpdate(deleteMrefSql.toString(), new BatchPreparedStatementSetter()
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
}