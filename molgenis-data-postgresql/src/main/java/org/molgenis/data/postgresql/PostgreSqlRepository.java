package org.molgenis.data.postgresql;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.*;

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
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.postgresql.PostgreSqlExceptionTranslator.VALUE_TOO_LONG_MSG;
import static org.molgenis.data.postgresql.PostgreSqlNameGenerator.getJunctionTableOrderColumnName;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.getJunctionTableAttributes;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.getTableAttributes;
import static org.molgenis.data.postgresql.PostgreSqlUtils.getPostgreSqlValue;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;

/**
 * Repository that persists entities in a PostgreSQL database
 * <ul>
 * <li>Attributes with expression are not persisted</li>
 * <li>Cross-backend attribute references are supported</li>
 * <li>Query operators DIS_MAX, FUZZY_MATCH, FUZZY_MATCH_NGRAM, SEARCH, SHOULD are not supported</li>
 * </ul>
 */
class PostgreSqlRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepository.class);

	/**
	 * JDBC batch operation size
	 */
	static final int BATCH_SIZE = 1000;
	/**
	 * Repository capabilities
	 */
	private static final Set<RepositoryCapability> REPO_CAPABILITIES = unmodifiableSet(
			EnumSet.of(WRITABLE, MANAGABLE, QUERYABLE, VALIDATE_REFERENCE_CONSTRAINT, VALIDATE_UNIQUE_CONSTRAINT,
					VALIDATE_NOTNULL_CONSTRAINT, VALIDATE_READONLY_CONSTRAINT, CACHEABLE));

	/**
	 * Supported query operators
	 */
	private static final Set<Operator> QUERY_OPERATORS = unmodifiableSet(
			EnumSet.of(EQUALS, IN, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, RANGE, LIKE, NOT, AND, OR, NESTED));

	private final PostgreSqlEntityFactory postgreSqlEntityFactory;
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	private final EntityType entityType;

	PostgreSqlRepository(PostgreSqlEntityFactory postgreSqlEntityFactory, JdbcTemplate jdbcTemplate,
			DataSource dataSource, EntityType entityType)
	{
		this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
		this.dataSource = requireNonNull(dataSource);
		this.entityType = requireNonNull(entityType);
	}

	@Override
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

	@Override
	public void update(Entity entity)
	{
		update(Stream.of(entity));
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		updateBatching(entities.iterator());
	}

	@Override
	public void delete(Entity entity)
	{
		this.delete(Stream.of(entity));
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		deleteAll(entities.map(Entity::getIdValue));
	}

	@Override
	public void deleteById(Object id)
	{
		this.deleteAll(Stream.of(id));
	}

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
			jdbcTemplate.batchUpdate(sql, new BatchDeletePreparedStatementSetter(idsBatch));
		});
	}

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

	@Override
	public void add(Entity entity)
	{
		if (entity == null)
		{
			throw new NullPointerException("PostgreSqlRepository.add() failed: entity was null");
		}
		add(Stream.of(entity));
	}

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
		final String allRowsSelect = getSqlSelect(entityType, query, emptyList(), false);
		LOG.debug("Fetching [{}] data...", getName());
		LOG.trace("SQL: {}", allRowsSelect);
		RowMapper<Entity> rowMapper = postgreSqlEntityFactory.createRowMapper(entityType, fetch);
		template.query(allRowsSelect,
				(ResultSetExtractor<Object>) resultSet -> processResultSet(consumer, batchSize, entityType, rowMapper,
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
			if (mrefAttr.getExpression() == null && isMultipleReferenceType(mrefAttr) && !(
					mrefAttr.getDataType() == ONE_TO_MANY && mrefAttr.isMappedBy()))
			{
				EntityType refEntityType = mrefAttr.getRefEntity();
				Multimap<Object, Object> mrefIDs = selectMrefIDsForAttribute(entityType, idAttributeDataType, mrefAttr,
						batch.keySet(), refEntityType.getIdAttribute().getDataType());
				for (Map.Entry entry : batch.entrySet())
				{
					batch.get(entry.getKey())
						 .set(mrefAttr.getName(), postgreSqlEntityFactory.getReferences(refEntityType,
								 newArrayList(mrefIDs.get(entry.getKey()))));
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
		jdbcTemplate.query(junctionTableSelect,
				getJunctionTableRowCallbackHandler(idAttributeDataType, refIdDataType, mrefIDs), ids.toArray());

		if (LOG.isTraceEnabled()) LOG.trace("Selected {} ID values for MREF attribute {} in {}",
				mrefIDs.values().stream().collect(counting()), mrefAttr.getName(), stopwatch);
		return mrefIDs;
	}

	RowCallbackHandler getJunctionTableRowCallbackHandler(AttributeType idAttributeDataType,
			AttributeType refIdDataType, Multimap<Object, Object> mrefIDs)
	{
		return row ->
		{
			Object id;
			switch (idAttributeDataType)
			{
				case EMAIL:
				case HYPERLINK:
				case STRING:
					id = row.getString(1);
					break;
				case INT:
					id = row.getInt(1);
					break;
				case LONG:
					id = row.getLong(1);
					break;
				default:
					throw new UnexpectedEnumException(idAttributeDataType);
			}

			Object refId;
			switch (refIdDataType)
			{
				case EMAIL:
				case HYPERLINK:
				case STRING:
					refId = row.getString(3);
					break;
				case INT:
					refId = row.getInt(3);
					break;
				case LONG:
					refId = row.getLong(3);
					break;
				default:
					throw new UnexpectedEnumException(refIdDataType);
			}
			mrefIDs.put(id, refId);
		};
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
				RowMapper<Entity> entityMapper = postgreSqlEntityFactory.createRowMapper(getEntityType(),
						batchQuery.getFetch());
				LOG.debug("Fetching [{}] data for query [{}]", getName(), batchQuery);
				LOG.trace("SQL: {}, parameters: {}", sql, parameters);
				Stopwatch sw = createStarted();
				List<Entity> result = jdbcTemplate.query(sql, parameters.toArray(new Object[parameters.size()]),
						entityMapper);
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
			jdbcTemplate.batchUpdate(insertSql, new BatchAddPreparedStatementSetter(entitiesBatch, tableAttrs));

			// persist values in entity junction table
			if (!junctionTableAttrs.isEmpty())
			{
				Map<String, List<Map<String, Object>>> mrefs = createMrefMap(idAttr, junctionTableAttrs, entitiesBatch);

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

	private static Map<String, List<Map<String, Object>>> createMrefMap(Attribute idAttr,
			List<Attribute> junctionTableAttrs, List<? extends Entity> entitiesBatch)
	{
		Map<String, List<Map<String, Object>>> mrefs = Maps.newHashMapWithExpectedSize(junctionTableAttrs.size());

		AtomicInteger seqNr = new AtomicInteger();
		for (Entity entity : entitiesBatch)
		{
			for (Attribute attr : junctionTableAttrs)
			{
				Iterable<Entity> refEntities = entity.getEntities(attr.getName());

				// Not-Null constraint doesn't exist for MREF attributes since they are stored in junction tables,
				// so validate manually.
				if (!attr.isNillable() && Iterables.isEmpty(refEntities))
				{
					throw new MolgenisValidationException(new ConstraintViolation(
							format("The attribute [%s] of entity [%s] with id [%s] can not be null.", attr.getName(),
									attr.getEntity().getId(), entity.getIdValue().toString())));
				}

				mrefs.putIfAbsent(attr.getName(), new ArrayList<>());

				seqNr.set(0);
				for (Entity val : refEntities)
				{
					Map<String, Object> mref = createJunctionTableRowData(seqNr.getAndIncrement(), idAttr, val, attr,
							entity);
					mrefs.get(attr.getName()).add(mref);
				}
			}
		}

		return mrefs;
	}

	static Map<String, Object> createJunctionTableRowData(int seqNr, Attribute idAttr, Entity refEntity,
			Attribute junctionTableAttr, Entity entity)
	{
		Map<String, Object> mref = Maps.newHashMapWithExpectedSize(3);
		mref.put(getJunctionTableOrderColumnName(), seqNr);
		mref.put(idAttr.getName(), entity.get(idAttr.getName()));
		mref.put(junctionTableAttr.getName(), refEntity);
		return mref;
	}

	private void updateBatching(Iterator<? extends Entity> entities)
	{
		final Attribute idAttr = entityType.getIdAttribute();
		final List<Attribute> tableAttrs = getTableAttributes(entityType).collect(toList());
		final List<Attribute> junctionTableAttrs = getJunctionTableAttributes(entityType).filter(
				attr -> !attr.isReadOnly()).collect(toList());
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
			int[] counts = jdbcTemplate.batchUpdate(updateSql,
					new BatchUpdatePreparedStatementSetter(entitiesBatch, tableAttrs, idAttr));
			verifyUpdate(entitiesBatch, counts, idAttr);

			// update values in entity junction table
			if (!junctionTableAttrs.isEmpty())
			{
				Map<String, List<Map<String, Object>>> mrefs = createMrefMap(idAttr, junctionTableAttrs, entitiesBatch);

				// update mrefs
				List<Object> ids = entitiesBatch.stream()
												.map(entity -> getPostgreSqlValue(entity, idAttr))
												.collect(toList());
				for (Attribute attr : junctionTableAttrs)
				{
					removeMrefs(ids, attr);
					addMrefs(mrefs.get(attr.getName()), attr);
				}
			}
		});
	}

	private void verifyUpdate(List<? extends Entity> entitiesBatch, int[] counts, Attribute idAttr)
	{
		int nrUpdatedEntities = Arrays.stream(counts).sum();
		if (nrUpdatedEntities < entitiesBatch.size())
		{
			Set<Object> existingEntityIds = findAll(entitiesBatch.stream().map(Entity::getIdValue),
					new Fetch().field(idAttr.getName())).map(Entity::getIdValue).collect(toSet());
			Object nonExistingEntityId = entitiesBatch.stream()
													  .map(Entity::getIdValue)
													  .filter(entityId -> !existingEntityIds.contains(entityId))
													  .findFirst()
													  .orElseThrow(() -> new IllegalStateException(
															  "Not all entities in batch were updated but all are present in the repository."));
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Cannot update [%s] with id [%s] because it does not exist", entityType.getId(),
							nonExistingEntityId.toString())));
		}
	}

	void addMrefs(final List<Map<String, Object>> mrefs, final Attribute attr)
	{
		// database doesn't validate NOT NULL constraint for attribute values referencing multiple entities,
		// so validate it ourselves
		if (!attr.isNillable() && mrefs.isEmpty())
		{
			throw new MolgenisValidationException(new ConstraintViolation(
					format("Entity [%s] attribute [%s] value cannot be null", entityType.getId(), attr.getName())));
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

		try
		{
			jdbcTemplate.batchUpdate(insertMrefSql,
					new BatchJunctionTableAddPreparedStatementSetter(mrefs, attr, idAttr));

		}
		catch (MolgenisValidationException mve)
		{
			if (mve.getMessage().equals(VALUE_TOO_LONG_MSG))
			{
				mve = new MolgenisValidationException(new ConstraintViolation(
						format("One of the mref values in entity type [%s] attribute [%s] is too long.",
								getEntityType().getId(), attr.getName())));
			}
			throw mve;
		}
	}

	private void removeMrefs(final List<Object> ids, final Attribute attr)
	{
		final Attribute idAttr = attr.isMappedBy() ? attr.getMappedBy() : entityType.getIdAttribute();
		String deleteMrefSql = getSqlDelete(PostgreSqlNameGenerator.getJunctionTableName(entityType, attr), idAttr);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Removing junction table entries for entity [{}] attribute [{}]", getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", deleteMrefSql);
			}
		}
		jdbcTemplate.batchUpdate(deleteMrefSql, new BatchJunctionTableDeletePreparedStatementSetter(ids));
	}

	private static class BatchAddPreparedStatementSetter implements BatchPreparedStatementSetter
	{
		private final List<? extends Entity> entities;
		private final List<Attribute> tableAttrs;

		BatchAddPreparedStatementSetter(List<? extends Entity> entities, List<Attribute> tableAttrs)
		{
			this.entities = entities;
			this.tableAttrs = tableAttrs;
		}

		@Override
		public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
		{
			Entity entity = entities.get(rowIndex);

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
			return entities.size();
		}
	}

	private static class BatchUpdatePreparedStatementSetter implements BatchPreparedStatementSetter
	{
		private final List<? extends Entity> entities;
		private final List<Attribute> tableAttrs;
		private final Attribute idAttr;

		BatchUpdatePreparedStatementSetter(List<? extends Entity> entities, List<Attribute> tableAttrs,
				Attribute idAttr)
		{
			this.entities = entities;
			this.tableAttrs = tableAttrs;
			this.idAttr = idAttr;
		}

		@Override
		public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
		{
			Entity entity = entities.get(rowIndex);

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
			return entities.size();
		}
	}

	private static class BatchDeletePreparedStatementSetter implements BatchPreparedStatementSetter
	{
		private final List<Object> entityIds;

		BatchDeletePreparedStatementSetter(List<Object> entityIds)
		{
			this.entityIds = entityIds;
		}

		@Override
		public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
		{
			preparedStatement.setObject(1, entityIds.get(i));
		}

		@Override
		public int getBatchSize()
		{
			return entityIds.size();
		}
	}

	private static class BatchJunctionTableAddPreparedStatementSetter implements BatchPreparedStatementSetter
	{
		private final List<Map<String, Object>> mrefs;
		private final Attribute attr;
		private final Attribute idAttr;

		BatchJunctionTableAddPreparedStatementSetter(List<Map<String, Object>> mrefs, Attribute attr, Attribute idAttr)
		{
			this.mrefs = mrefs;
			this.attr = attr;
			this.idAttr = idAttr;
		}

		@Override
		public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
		{
			Map<String, Object> mref = mrefs.get(i);

			Object idValue0;
			Object idValue1;
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
			preparedStatement.setInt(1, (int) mref.get(getJunctionTableOrderColumnName()));
			preparedStatement.setObject(2, idValue0);
			preparedStatement.setObject(3, idValue1);
		}

		@Override
		public int getBatchSize()
		{
			return mrefs.size();
		}
	}

	private static class BatchJunctionTableDeletePreparedStatementSetter implements BatchPreparedStatementSetter
	{
		private final List<Object> entityIds;

		BatchJunctionTableDeletePreparedStatementSetter(List<Object> entityIds)
		{
			this.entityIds = entityIds;
		}

		@Override
		public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
		{
			preparedStatement.setObject(1, entityIds.get(i));
		}

		@Override
		public int getBatchSize()
		{
			return entityIds.size();
		}
	}
}