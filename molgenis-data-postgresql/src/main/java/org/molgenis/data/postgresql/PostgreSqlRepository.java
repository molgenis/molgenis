package org.molgenis.data.postgresql;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisReferencedEntityException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.Sort;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.BatchingQueryResult;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.DateField;
import org.molgenis.fieldtypes.DatetimeField;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PostgreSqlRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepository.class);

	private static final int BATCH_SIZE = 1000;
	private static final String JUNCTION_TABLE_ORDER_ATTR_NAME = "order";

	private final DataService dataService;
	private final PostgreSqlEntityFactory postgreSqlEntityFactory;
	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	private EntityMetaData metaData;

	/**
	 * Creates a new PostgreSqlRepository.
	 *
	 * @param dataSource
	 *            the datasource to use to execute statements on the PostgreSQL database
	 * @param jdbcTemplate
	 *            {@link AsyncJdbcTemplate} to use to execute DDL statements in an isolated transaction on the
	 *            PostgreSQL database
	 */
	public PostgreSqlRepository(DataService dataService, PostgreSqlEntityFactory postgreSqlEntityFactory,
			DataSource dataSource)
	{
		this.dataService = requireNonNull(dataService);
		this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
		this.dataSource = requireNonNull(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setMetaData(EntityMetaData metaData)
	{
		this.metaData = metaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		Query q = new QueryImpl();
		return findAllBatching(q).iterator();
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		Query q = new QueryImpl();
		if (fetch != null)
		{
			q.fetch(fetch);
		}
		return StreamSupport.stream(findAllBatching(q).spliterator(), false);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(WRITABLE, MANAGABLE, QUERYABLE);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	@Override
	public long count(Query q)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Counting MySQL [{}] rows for query [{}]", getName(), q);
		}

		List<Object> parameters = Lists.newArrayList();
		String sql = getCountSql(q, parameters);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("sql: {}, parameters: {}", sql, parameters);
		}

		return jdbcTemplate.queryForObject(sql, parameters.toArray(new Object[0]), Long.class);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return StreamSupport.stream(findAllBatching(q).spliterator(), false);
	}

	@Override
	public Entity findOne(Query q)
	{
		Iterator<Entity> iterator = findAll(q).iterator();
		if (iterator.hasNext()) return iterator.next();
		return null;
	}

	@Override
	public Entity findOne(Object id)
	{
		if (id == null) return null;
		return findOne(new QueryImpl().eq(getEntityMetaData().getIdAttribute().getName(), id));
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		if (id == null) return null;
		return findOne(new QueryImpl().eq(getEntityMetaData().getIdAttribute().getName(), id).fetch(fetch));
	}

	@Override
	public void update(Entity entity)
	{
		update(Stream.of(entity));
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		update(entities.iterator());
	}

	@Override
	public void delete(Entity entity)
	{
		this.delete(Stream.of(entity));
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
			resetXrefValuesBySelfReference(batch);
			deleteById(batch.stream().map(Entity::getIdValue));
		});
	}

	@Override
	public void deleteById(Object id)
	{
		this.deleteById(Stream.of(id));
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(idsBatch -> {
			jdbcTemplate.batchUpdate(getDeleteSql(), new BatchPreparedStatementSetter()
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

	@Override
	public void deleteAll()
	{
		Stream<AttributeMetaData> selfReferencingAttrs = StreamSupport
				.stream(getEntityMetaData().getAtomicAttributes().spliterator(), false)
				.filter(attr -> attr.getDataType() instanceof XrefField
						&& attr.getRefEntity().getName().equals(getEntityMetaData().getName()));

		selfReferencingAttrs.forEach(selfReferencingXrefAttr -> {
			if (!selfReferencingXrefAttr.isNillable())
			{
				// update value with id attribute name (instead of NULL) won't work due to
				// http://bugs.mysql.com/bug.php?id=7412. For more information read the paragraph "Until InnoDB
				// implements deferred constraint checking, some things will be impossible, such as deleting a
				// record that refers to itself using a foreign key." in
				// http://dev.mysql.com/doc/refman/5.1/en/innodb-foreign-key-constraints.html
				throw new MolgenisDataException(
						format("Self-referencing not-null attribute [%s] of entity [%s] cannot be deleted",
								selfReferencingXrefAttr.getName(), getName()));
			}

			String updateSql = getUpdateColumnToNullSql(selfReferencingXrefAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Updating nillable self-referencing xref attribute: " + updateSql);
			}
			jdbcTemplate.update(updateSql);
		});

		String deleteSql = getDeleteAllSql();
		if (LOG.isDebugEnabled())
		{
			LOG.debug(format("Deleting all [%s] entities: %s", getName(), deleteSql));
		}
		jdbcTemplate.update(deleteSql);
	}

	@Override
	public void add(Entity entity)
	{
		if (entity == null) throw new RuntimeException("PostgreSqlRepository.add() failed: entity was null");
		add(Stream.of(entity));
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		return add(entities.iterator());
	}

	// TODO move to repository collection
	@Override
	public void create()
	{
		if (tableExists())
		{
			LOG.debug("Table for entity {} already exists. Skipping creation", getName());
			return;
		}
		try
		{
			jdbcTemplate.execute(getCreateTableSql());
			LOG.debug("Created table [{}]", getTableName());

			for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
			{
				if (attr.getExpression() != null)
				{
					// computed attributes are not persisted
					continue;
				}

				// add mref tables

				if (attr.getDataType() instanceof MrefField)
				{
					jdbcTemplate.execute(getCreateJunctionTableSql(attr));
					LOG.debug("Created junction table [{}]", getJunctionTableName(attr));
				}
				else if (attr.getDataType() instanceof XrefField)
				{
					// String backend = dataService.getMeta().getBackend(attr.getRefEntity()).getName();
					// if (backend.equalsIgnoreCase(PostgreSqlRepositoryCollection.NAME)) // FIXME
					// {
					jdbcTemplate.execute(getCreateFKeySql(attr));
					// }
				}

				// text can't be unique, so don't add unique constraint when type is string
				if (attr.isUnique() && !(attr.getDataType() instanceof StringField))
				{
					jdbcTemplate.execute(getUniqueSql(attr));
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception creating PostgreSqlRepository.", e);
			try
			{
				drop();
			}
			catch (Exception ignored)
			{
			}
			throw new MolgenisDataException(e);
		}
	}

	// TODO move to repository collection
	@Override
	public void drop()
	{
		DataAccessException remembered = null;
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getDataType() instanceof MrefField)
			{
				DataAccessException e = tryExecute(getDropTableSql(getJunctionTableName(att)));
				remembered = remembered != null ? remembered : e;
			}
		}

		// Deleting entites that are referenced won't work due to failing key constraints
		// Find out if the entity is referenced and if it is, report those entities
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingEntities = EntityUtils
				.getReferencingEntityMetaData(getEntityMetaData(), dataService);
		List<Pair<EntityMetaData, List<AttributeMetaData>>> nonSelfReferencingEntities = referencingEntities.stream()
				.filter(ref -> !getEntityMetaData().getName().equals(referencingEntities.get(0).getA().getName()))
				.collect(Collectors.toList());

		if (!nonSelfReferencingEntities.isEmpty())
		{
			List<String> entityNames = Lists.newArrayList();
			nonSelfReferencingEntities.forEach(pair -> entityNames.add(pair.getA().getName()));

			StringBuilder msg = new StringBuilder("Cannot delete entity '").append(getEntityMetaData().getName())
					.append("' because it is referenced by the following entities: ").append(entityNames.toString());

			throw new MolgenisReferencedEntityException(msg.toString());
		}
		else
		{
			DataAccessException e = tryExecute(getDropTableSql());
			remembered = remembered != null ? remembered : e;
		}

		if (remembered != null)
		{
			throw remembered;
		}
	}

	void dropAttribute(String attributeName)
	{
		jdbcTemplate.execute(getDropColumnSql(attributeName));

		DefaultEntityMetaData demd = new DefaultEntityMetaData(metaData);
		demd.removeAttributeMetaData(demd.getAttribute(attributeName));
		setMetaData(demd);
	}

	/**
	 * Adds an attribute to the repository. Will execute the alter table statement in a different thread so that the
	 * current transaction does not get committed.
	 *
	 * This is needed for adding columns during an import.
	 *
	 * @param attributeMetaData
	 *            the {@link AttributeMetaData} to add
	 */
	void addAttribute(AttributeMetaData attributeMetaData)
	{
		addAttributeInternal(attributeMetaData, true, true);
	}

	/**
	 * Adds an attribute to the repository. Will excecute the alter table statement in the current thread. Please note
	 * that this *will* commit any existing transactions.
	 *
	 * This is needed for adding columns in the annotator.
	 *
	 * @param attributeMetaData
	 *            the {@link AttributeMetaData} to add
	 */
	void addAttributeSync(AttributeMetaData attributeMetaData)
	{
		addAttributeInternal(attributeMetaData, true, false);
	}

	static String getSelectMrefSql(EntityMetaData entityMeta, AttributeMetaData att)
	{
		return new StringBuilder().append("SELECT ").append(getColumnName(att)).append(" FROM ")
				.append(getJunctionTableName(entityMeta, att)).append(" WHERE ")
				.append(getColumnName(entityMeta.getIdAttribute())).append(" = ?").append(" ORDER BY ")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).toString();
	}

	/**
	 * Adds an attribute to the repository.
	 *
	 * @param attributeMetaData
	 *            the {@link AttributeMetaData} to add
	 * @param addToEntityMetaData
	 *            boolean indicating if the repository's {@link EntityMetaData} should be updated as well. This should
	 *            not happen for parts of a compound attribute.
	 * @param async
	 *            boolean indicating if the alter table statement should be executed in a different thread or not.
	 */
	private void addAttributeInternal(AttributeMetaData attributeMetaData, boolean addToEntityMetaData, boolean async)
	{
		try
		{
			if (attributeMetaData.getExpression() != null)
			{
				// computed attributes are not persisted
				return;
			}
			if (attributeMetaData.getDataType() instanceof MrefField)
			{
				execute(getCreateJunctionTableSql(attributeMetaData), async);
			}
			else if (!attributeMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				execute(getAddColumnSql(attributeMetaData), async);
			}

			if (attributeMetaData.getDataType() instanceof XrefField)
			{
				execute(getCreateFKeySql(attributeMetaData), async);
			}

			// TEXT cannot be UNIQUE, don't add constraint when field type is string
			if (attributeMetaData.isUnique() && !(attributeMetaData.getDataType() instanceof StringField))
			{
				execute(getUniqueSql(attributeMetaData), async);
			}

			if (attributeMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				for (AttributeMetaData attrPart : attributeMetaData.getAttributeParts())
				{
					addAttributeInternal(attrPart, false, async);
				}
			}
			DefaultEntityMetaData demd = new DefaultEntityMetaData(metaData);
			if (addToEntityMetaData)
			{
				demd.addAttributeMetaData(attributeMetaData);
			}
			setMetaData(demd);
		}
		catch (Exception e)
		{
			LOG.error("Exception updating PostgreSqlRepository.", e);
			throw new MolgenisDataException(e);
		}
	}

	/**
	 * Tries to execute a piece of SQL.
	 *
	 * @param sql
	 *            the SQL to execute
	 * @return Exception if one was caught, or null if all went well
	 */
	private DataAccessException tryExecute(String sql)
	{
		try
		{
			jdbcTemplate.execute(sql);
			return null;
		}
		catch (DataAccessException caught)
		{
			return caught;
		}
	}

	/**
	 * Executes a SQL string.
	 *
	 * @param sql
	 *            the String to execute
	 * @param async
	 *            indication if the string should be executed on a different thread or not
	 */
	@Deprecated
	private void execute(String sql, boolean async)
	{
		if (async)
		{
			jdbcTemplate.execute(sql);
		}
		else
		{
			jdbcTemplate.execute(sql);
		}
	}

	private BatchingQueryResult findAllBatching(Query q)
	{
		BatchingQueryResult batchingQueryResult = new BatchingQueryResult(BATCH_SIZE, q)
		{
			@Override
			protected List<Entity> getBatch(Query batchQuery)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Fetching MySQL [{}] data for query [{}]", getName(), batchQuery);
				}

				List<Object> parameters = Lists.newArrayList();
				String sql = getSelectSql(batchQuery, parameters);
				if (LOG.isTraceEnabled())
				{
					LOG.trace("sql: {}, parameters: {}", sql, parameters);
				}

				RowMapper<Entity> entityMapper = postgreSqlEntityFactory.createRowMapper(getEntityMetaData(),
						batchQuery.getFetch(), jdbcTemplate);
				return jdbcTemplate.query(sql, parameters.toArray(new Object[0]), entityMapper);
			}
		};
		return batchingQueryResult;
	}

	/**
	 * Use before a delete action of a entity with XREF data type where the entity and refEntity are the same entities.
	 *
	 * @param entities
	 */
	private void resetXrefValuesBySelfReference(Iterable<? extends Entity> entities)
	{
		List<String> xrefAttributesWithSelfReference = new ArrayList<String>();
		for (AttributeMetaData attributeMetaData : getEntityMetaData().getAtomicAttributes())
		{
			if (attributeMetaData.getDataType().getEnumType().equals(FieldTypeEnum.XREF)
					&& getEntityMetaData().getName().equals(attributeMetaData.getRefEntity().getName()))
			{
				xrefAttributesWithSelfReference.add(attributeMetaData.getName());
			}
		}

		final List<Entity> updateBatch = new ArrayList<Entity>();
		for (Entity e : entities)
		{
			for (String attributeName : xrefAttributesWithSelfReference)
			{
				Entity en = e.getEntity(attributeName);
				if (null != en)
				{
					en.set(attributeName, null);
					updateBatch.add(en);
					break;
				}
			}
		}
		this.update(updateBatch.iterator());
	}

	private Integer add(Iterator<? extends Entity> entitiesIterator)
	{
		if (entitiesIterator == null) return 0;
		AtomicInteger count = new AtomicInteger(0);

		final List<Entity> batch = new ArrayList<Entity>();

		while (entitiesIterator.hasNext())
		{
			batch.add(entitiesIterator.next());
			count.addAndGet(1);

			if ((batch.size() == BATCH_SIZE) || !entitiesIterator.hasNext())
			{
				final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
				final Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

				jdbcTemplate.batchUpdate(getInsertSql(), new BatchPreparedStatementSetter()
				{
					@Override
					public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
					{
						int fieldIndex = 1;
						for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
						{
							// create the mref records
							if (att.getDataType() instanceof MrefField)
							{
								if (mrefs.get(att.getName()) == null) mrefs.put(att.getName(), new ArrayList<>());
								if (batch.get(rowIndex).get(att.getName()) != null)
								{
									AtomicInteger seqNr = new AtomicInteger();
									for (Entity val : batch.get(rowIndex).getEntities(att.getName()))
									{
										if (val != null)
										{
											Map<String, Object> mref = new HashMap<>();
											mref.put(JUNCTION_TABLE_ORDER_ATTR_NAME, seqNr.getAndIncrement());
											mref.put(idAttribute.getName(),
													batch.get(rowIndex).get(idAttribute.getName()));
											mref.put(att.getName(), val.getIdValue());
											mrefs.get(att.getName()).add(mref);
										}
									}
								}
							}
							else
							{
								if (att.getExpression() != null)
								{
									continue;
								}

								if (batch.get(rowIndex).get(att.getName()) == null)
								{
									if (att.equals(getEntityMetaData().getIdAttribute()) && att.isAuto()
											&& (att.getDataType() instanceof StringField))
									{
										throw new MolgenisDataException(
												"Missing auto id value. Please use the 'AutoValueRepositoryDecorator' to add auto id capabilities.");
									}
									preparedStatement.setObject(fieldIndex++, null);
								}
								else if (att.getDataType() instanceof XrefField)
								{
									Object value = batch.get(rowIndex).get(att.getName());
									if (value instanceof Entity)
									{
										value = ((Entity) value).get(att.getRefEntity().getIdAttribute().getName());
									}

									preparedStatement.setObject(fieldIndex++,
											att.getRefEntity().getIdAttribute().getDataType().convert(value));
								}
								else
								{
									Object value = att.getDataType().convert(batch.get(rowIndex).get(att.getName()));
									if (att.getDataType() instanceof DateField)
									{
										value = new java.sql.Date(((java.util.Date) value).getTime());
									}
									else if (att.getDataType() instanceof DatetimeField)
									{
										value = new java.sql.Timestamp(((java.util.Date) value).getTime());
									}
									preparedStatement.setObject(fieldIndex++, value);
								}
							}
						}
					}

					@Override
					public int getBatchSize()
					{
						return batch.size();
					}
				});

				// add mrefs as well
				for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
				{
					if (att.getDataType() instanceof MrefField)
					{
						addMrefs(mrefs.get(att.getName()), att);
					}
				}

				LOG.debug("Added " + count.get() + " " + getTableName() + " entities.");
				batch.clear();
			}
		}

		return count.get();
	}

	private void update(Iterator<? extends Entity> entities)
	{
		// TODO, split in subbatches
		final List<Entity> batch = new ArrayList<Entity>();
		if (entities != null) while (entities.hasNext())
		{
			batch.add(entities.next());
		}
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final List<Object> ids = new ArrayList<Object>();
		final Map<String, List<Map<String, Object>>> mrefs = new HashMap<>();

		jdbcTemplate.batchUpdate(getUpdateSql(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
			{
				Entity e = batch.get(rowIndex);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("updating: " + e);
				}

				Object idValue = idAttribute.getDataType().convert(e.get(idAttribute.getName()));
				ids.add(idValue);
				int fieldIndex = 1;
				for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
				{
					// create the mref records
					if (att.getDataType() instanceof MrefField)
					{
						if (mrefs.get(att.getName()) == null) mrefs.put(att.getName(), new ArrayList<>());
						if (e.get(att.getName()) != null)
						{
							List<Entity> vals = Lists.newArrayList(e.getEntities(att.getName()));
							if (vals != null)
							{
								AtomicInteger seqNr = new AtomicInteger();
								for (Entity val : vals)
								{
									Map<String, Object> mref = new HashMap<>();
									mref.put(JUNCTION_TABLE_ORDER_ATTR_NAME, seqNr.getAndIncrement());
									mref.put(idAttribute.getName(), idValue);
									mref.put(att.getName(), val.get(att.getRefEntity().getIdAttribute().getName()));
									mrefs.get(att.getName()).add(mref);
								}
							}
						}
					}
					else
					{
						if (att.getExpression() != null)
						{
							// computed attributes are not persisted
							continue;
						}
						if (e.get(att.getName()) == null)
						{
							// repository should not fill in default value, the form should
							preparedStatement.setObject(fieldIndex++, null);
						}
						else
						{
							if (att.getDataType() instanceof XrefField)
							{
								Object value = e.get(att.getName());
								if (value instanceof Entity)
								{
									preparedStatement.setObject(fieldIndex++,
											att.getRefEntity().getIdAttribute().getDataType().convert(((Entity) value)
													.get(att.getRefEntity().getIdAttribute().getName())));
								}
								else
								{
									preparedStatement.setObject(fieldIndex++,
											att.getRefEntity().getIdAttribute().getDataType().convert(value));
								}
							}
							else
							{
								Object value = att.getDataType().convert(e.get(att.getName()));
								if (att.getDataType() instanceof DateField)
								{
									value = new java.sql.Date(((java.util.Date) value).getTime());
								}
								else if (att.getDataType() instanceof DatetimeField)
								{
									value = new java.sql.Timestamp(((java.util.Date) value).getTime());
								}
								preparedStatement.setObject(fieldIndex++, value);
							}
						}
					}
				}
				preparedStatement.setObject(fieldIndex++, idValue);
			}

			@Override
			public int getBatchSize()
			{
				return batch.size();
			}
		});

		// update mrefs
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getDataType() instanceof MrefField)
			{
				removeMrefs(ids, att);
				addMrefs(mrefs.get(att.getName()), att);
			}
		}
	}

	private boolean tableExists()
	{
		Connection conn = null;
		try
		{
			conn = dataSource.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, getTableName(), null);
			return tables.next();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}

	private void removeMrefs(final List<Object> ids, final AttributeMetaData att)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		StringBuilder mrefSql = new StringBuilder();
		mrefSql.append(getDeleteSql(getJunctionTableName(att), idAttribute));

		jdbcTemplate.batchUpdate(mrefSql.toString(), new BatchPreparedStatementSetter()
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

	private void addMrefs(final List<Map<String, Object>> mrefs, final AttributeMetaData att)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final AttributeMetaData refEntityIdAttribute = att.getRefEntity().getIdAttribute();

		StringBuilder mrefSql = new StringBuilder();
		mrefSql.append(getInsertMrefSql(att, idAttribute));

		jdbcTemplate.batchUpdate(mrefSql.toString(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				Map<String, Object> mref = mrefs.get(i);

				preparedStatement.setInt(1, (int) mref.get(JUNCTION_TABLE_ORDER_ATTR_NAME));

				preparedStatement.setObject(2, mref.get(idAttribute.getName()));

				Object value = mref.get(att.getName());
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

	private void getAttributeSql(StringBuilder sql, AttributeMetaData att) throws MolgenisModelException
	{
		if (att.getExpression() != null)
		{
			return;
		}
		switch (att.getDataType().getEnumType())
		{
			case BOOL:
				break;
			case DATE:
				break;
			case DATE_TIME:
				break;
			case DECIMAL:
				break;
			case EMAIL:
				break;
			case ENUM:
				break;
			case HTML:
				break;
			case HYPERLINK:
				break;
			case INT:
				break;
			case LONG:
				break;
			case SCRIPT:
				break;
			case STRING:
				break;
			case TEXT:
				break;
			case COMPOUND:
				break;
			case MREF:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case XREF:
			case FILE:
				if (att.equals(getEntityMetaData().getLabelAttribute()))
				{
					throw new MolgenisDataException("Attribute [" + att.getName() + "] of entity [" + getName()
							+ "] is label attribute and of type [" + att.getDataType()
							+ "]. Label attributes cannot be of type xref, mref, categorical or compound.");
				}

				if (getEntityMetaData().getLookupAttribute(att.getName()) != null)
				{
					throw new MolgenisDataException("Attribute [" + att.getName() + "] of entity [" + getName()
							+ "] is lookup attribute and of type [" + att.getDataType()
							+ "]. Lookup attributes cannot be of type xref, mref, categorical or compound.");
				}

				break;
			default:
				throw new RuntimeException("Unknown datatype [" + att.getDataType().getEnumType() + "]");

		}

		if (!(att.getDataType() instanceof MrefField))
		{
			sql.append(getColumnName(att)).append(' ');
			// xref adopt type of the identifier of referenced entity
			if (att.getDataType() instanceof XrefField)
			{
				sql.append(att.getRefEntity().getIdAttribute().getDataType().getPostgreSqlType());
			}
			else
			{
				sql.append(att.getDataType().getPostgreSqlType());
			}
			// TODO remove Questionnaire entity hack
			// not null
			if (!att.isNillable() && !EntityUtils.doesExtend(metaData, "Questionnaire")
					&& (att.getVisibleExpression() == null))
			{
				sql.append(" NOT NULL");
			}
			if (att.getDataType() instanceof EnumField)
			{
				sql.append(" CHECK (" + getColumnName(att) + " IN (").append(
						att.getEnumOptions().stream().map(enumOption -> "'" + enumOption + "'").collect(joining(",")))
						.append("))");
			}
		}
	}

	private String getCreateFKeySql(AttributeMetaData att)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName()).append(" ADD FOREIGN KEY (")
				.append(getColumnName(att)).append(") REFERENCES ").append(getTableName(att.getRefEntity())).append('(')
				.append(getColumnName(att.getRefEntity().getIdAttribute())).append(")").toString();
	}

	private String getUniqueSql(AttributeMetaData att)
	{
		// PostgreSQL name convention
		return new StringBuilder().append("ALTER TABLE ").append(getTableName()).append(" ADD CONSTRAINT ")
				.append(getUniqueKeyName(att)).append(" UNIQUE (").append(getColumnName(att)).append(")").toString();
	}

	private void getMrefQueryFields(List<QueryRule> rules, List<String> fields)
	{
		for (QueryRule rule : rules)
		{
			if (rule.getField() != null)
			{
				AttributeMetaData attr = this.getEntityMetaData().getAttribute(rule.getField());
				if (attr != null && attr.getDataType() instanceof MrefField)
				{
					fields.add(rule.getField());
				}
			}

			if (rule.getNestedRules() != null && !rule.getNestedRules().isEmpty())
			{
				getMrefQueryFields(rule.getNestedRules(), fields);
			}
		}
	}

	private String getAddColumnSql(AttributeMetaData attributeMetaData) throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE ").append(getTableName()).append(" ADD ");
		getAttributeSql(sql, attributeMetaData);
		return sql.toString();
	}

	private String getCreateTableSql() throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS ").append(getTableName()).append('(');

		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			getAttributeSql(sql, att);
			if (att.getExpression() == null && !(att.getDataType() instanceof MrefField))
			{
				sql.append(", ");
			}
		}
		// primary key is first attribute unless otherwise indicated
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		if (idAttribute == null) throw new MolgenisDataException("Missing idAttribute for entity [" + getName() + "]");

		if (idAttribute.getDataType() instanceof XrefField || idAttribute.getDataType() instanceof MrefField)
			throw new RuntimeException(
					"primary key(" + getTableName() + "." + getColumnName(idAttribute) + ") cannot be XREF or MREF");

		if (idAttribute.isNillable() == true) throw new RuntimeException(
				"idAttribute (" + getTableName() + "." + getColumnName(idAttribute) + ") should not be nillable");

		sql.append("PRIMARY KEY (").append(getColumnName(getEntityMetaData().getIdAttribute())).append(')');

		// close
		sql.append(')');

		if (LOG.isTraceEnabled())
		{
			LOG.trace("sql: " + sql);
		}

		return sql.toString();
	}

	private String getCreateJunctionTableSql(AttributeMetaData att) throws MolgenisModelException
	{
		AttributeMetaData idAttr = getEntityMetaData().getIdAttribute();
		StringBuilder sql = new StringBuilder();

		sql.append(" CREATE TABLE IF NOT EXISTS ").append(getJunctionTableName(att)).append(" (")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(" INT,").append(getColumnName(idAttr))
				.append(' ').append(idAttr.getDataType().getPostgreSqlType()).append(" NOT NULL, ")
				.append(getColumnName(att)).append(' ')
				.append(att.getRefEntity().getIdAttribute().getDataType().getPostgreSqlType())
				.append(" NOT NULL, FOREIGN KEY (").append(getColumnName(idAttr)).append(") REFERENCES ")
				.append(getTableName()).append('(').append(getColumnName(idAttr)).append(") ON DELETE CASCADE");

		// FIXME
		// If the refEntity is not of type MySQL do not add a foreign key to it
		// String refEntityBackend = dataService.getMeta().getBackend(att.getRefEntity()).getName();
		// if (refEntityBackend.equalsIgnoreCase(PostgreSqlRepositoryCollection.NAME))
		// {
		sql.append(", FOREIGN KEY (").append(getColumnName(att)).append(") REFERENCES ")
				.append(getTableName(att.getRefEntity())).append('(')
				.append(getColumnName(att.getRefEntity().getIdAttribute())).append(") ON DELETE CASCADE");
		// }

		sql.append(", UNIQUE (").append(getColumnName(att)).append(',').append(getColumnName(idAttr)).append(')');
		sql.append(", UNIQUE (").append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(',')
				.append(getColumnName(idAttr)).append(')');

		sql.append(')');

		return sql.toString();
	}

	private String getDropTableSql()
	{
		return getDropTableSql(getTableName());
	}

	private String getDropTableSql(String tableName)
	{
		return new StringBuilder("DROP TABLE IF EXISTS ").append(tableName).toString();
	}

	@SuppressWarnings("unused")
	private String getDropColumnSql(AttributeMetaData attr)
	{
		return getDropColumnSql(attr.getName());
	}

	private String getDropColumnSql(String attrName)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName()).append(" DROP COLUMN ")
				.append(getColumnName(attrName)).toString();
	}

	private String getInsertSql()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(getTableName()).append(" (");
		StringBuilder params = new StringBuilder();
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getExpression() != null)
			{
				continue;
			}
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append(getColumnName(att)).append(", ");
				params.append("?, ");
			}
		}
		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',')
		{
			sql.setLength(sql.length() - 2);
			params.setLength(params.length() - 2);
		}
		sql.append(") VALUES (").append(params).append(")");
		return sql.toString();
	}

	private String getInsertMrefSql(AttributeMetaData attr, AttributeMetaData idAttr)
	{
		return new StringBuilder().append("INSERT INTO ").append(getJunctionTableName(attr)).append(" (")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(',').append(getColumnName(idAttr))
				.append(',').append(getColumnName(attr)).append(") VALUES (?,?,?)").toString();
	}

	private String getDeleteAllSql()
	{
		return new StringBuilder().append("DELETE FROM ").append(getTableName()).toString();
	}

	private String getDeleteSql()
	{
		return getDeleteSql(getTableName(), getEntityMetaData().getIdAttribute());
	}

	private static String getDeleteSql(String tableName, AttributeMetaData attr)
	{
		return new StringBuilder().append("DELETE FROM ").append(tableName).append(" WHERE ")
				.append(getColumnName(attr)).append(" = ?").toString();
	}

	private String getSelectSql(Query q, List<Object> parameters)
	{
		StringBuilder select = new StringBuilder("SELECT ");
		StringBuilder group = new StringBuilder();
		int count = 0;
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (q.getFetch() == null || q.getFetch().hasField(att.getName()))
			{
				if (att.getExpression() == null)
				{
					if (count > 0) select.append(", ");

					if (att.getDataType() instanceof MrefField)
					{
						// TODO retrieve mref values in seperate queries to allow specifying limit and offset
						select.append("array_agg(distinct array[").append(getColumnName(att)).append('.')
								.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append("::text,")
								.append(getColumnName(att)).append("::text]) AS ").append(getColumnName(att));
					}
					else
					{
						select.append("this.").append(getColumnName(att));
						if (group.length() > 0) group.append(", this.").append(getColumnName(att));
						else group.append("this.").append(getColumnName(att));
					}
					count++;
				}
			}
		}

		// from
		StringBuilder result = new StringBuilder().append(select).append(getFromSql(q));
		// where
		String where = getWhereSql(q, parameters, 0);
		if (where.length() > 0) result.append(" WHERE ").append(where);
		// group by
		if (select.indexOf("array_agg") != -1 && group.length() > 0) result.append(" GROUP BY ").append(group);
		// order by
		result.append(' ').append(getSortSql(q));
		// limit
		if (q.getPageSize() > 0) result.append(" LIMIT ").append(q.getPageSize());
		if (q.getOffset() > 0) result.append(" OFFSET ").append(q.getOffset());

		return result.toString().trim();
	}

	private String getWhereSql(Query q, List<Object> parameters, int mrefFilterIndex)
	{
		StringBuilder result = new StringBuilder();
		for (QueryRule r : q.getRules())
		{
			AttributeMetaData attr = null;
			if (r.getField() != null)
			{
				attr = getEntityMetaData().getAttribute(r.getField());
				if (attr == null)
				{
					throw new MolgenisDataException("Unknown attribute [" + r.getField() + "]");
				}
				if (attr.getDataType() instanceof MrefField)
				{
					mrefFilterIndex++;
				}
			}

			StringBuilder predicate = new StringBuilder();
			switch (r.getOperator())
			{
				case SEARCH:
					StringBuilder search = new StringBuilder();
					for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
					{
						// TODO: other data types???
						if (att.getDataType() instanceof StringField || att.getDataType() instanceof TextField)
						{
							search.append(" OR this.").append(getColumnName(att)).append(" LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");
						}
						else if (att.getDataType() instanceof XrefField)
						{

							Repository repo = dataService.getRepository(att.getRefEntity().getName());
							if (repo.getCapabilities().contains(QUERYABLE))
							{
								Query refQ = new QueryImpl().like(att.getRefEntity().getLabelAttribute().getName(),
										r.getValue().toString());
								Iterator<Entity> it = repo.findAll(refQ).iterator();
								if (it.hasNext())
								{
									search.append(" OR this.").append(getColumnName(att)).append(" IN (");
									while (it.hasNext())
									{
										Entity ref = it.next();
										search.append("?");
										parameters.add(att.getDataType()
												.convert(ref.get(att.getRefEntity().getIdAttribute().getName())));
										if (it.hasNext())
										{
											search.append(",");
										}
									}
									search.append(")");
								}
							}
						}
						else if (att.getDataType() instanceof MrefField)
						{
							// FIXME required for postgres?
							search.append(" OR CAST(").append(getColumnName(att)).append(".").append(getColumnName(att))
									.append(" as CHAR) LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");

						}
						else
						{
							search.append(" OR CAST(this.").append(getColumnName(att)).append(" as CHAR) LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");

						}
					}
					if (search.length() > 0) result.append('(').append(search.substring(4)).append(')');
					break;
				case AND:
					result.append(" AND ");
					break;
				case NESTED:
					result.append("(");
					result.append(getWhereSql(new QueryImpl(r.getNestedRules()), parameters, mrefFilterIndex));
					result.append(")");
					break;
				case OR:
					result.append(" OR ");
					break;
				case LIKE:

					if (attr.getDataType() instanceof StringField || attr.getDataType() instanceof TextField)
					{
						result.append(" this.").append(getColumnName(attr)).append(" LIKE ?");
					}
					else
					{
						result.append(" CAST(this.").append(getColumnName(attr)).append(" as CHAR) LIKE ?");
					}
					parameters.add("%" + DataConverter.toString(r.getValue()) + "%");
					break;
				case IN:
					StringBuilder in = new StringBuilder();
					List<Object> values = new ArrayList<Object>();
					if (r.getValue() == null)
					{
						throw new MolgenisDataException("Missing value for IN query");
					}
					else if (!(r.getValue() instanceof Iterable<?>))
					{
						for (String str : r.getValue().toString().split(","))
							values.add(str);
					}
					else
					{
						Iterables.addAll(values, (Iterable<?>) r.getValue());
					}

					for (int i = 0; i < values.size(); i++)
					{
						if (i > 0)
						{
							in.append(",");
						}

						in.append("?");
						parameters.add(attr.getDataType().convert(values.get(i)));
					}

					if (attr.getDataType() instanceof MrefField)
						result.append(getFilterColumnName(attr)).append(mrefFilterIndex);
					else result.append("this");

					result.append(".").append(getColumnName(r.getField())).append(" IN (").append(in).append(')');
					break;
				default:
					// comparable values...
					FieldType type = attr.getDataType();
					if (type instanceof MrefField) predicate.append(getFilterColumnName(attr)).append(mrefFilterIndex);
					else predicate.append("this");

					predicate.append(".").append(getColumnName(r.getField()));

					switch (r.getOperator())
					{
						case EQUALS:
							predicate.append(" =");
							break;
						case GREATER:
							predicate.append(" >");
							break;
						case LESS:
							predicate.append(" <");
							break;
						case GREATER_EQUAL:
							predicate.append(" >=");
							break;
						case LESS_EQUAL:
							predicate.append(" <=");
							break;
						default:
							throw new MolgenisDataException("cannot solve query rule:  " + r);
					}
					predicate.append(" ? ");
					Object convertedVal = attr.getDataType().convert(r.getValue());
					if (convertedVal instanceof Entity)
					{
						convertedVal = ((Entity) convertedVal).getIdValue();
					}
					parameters.add(convertedVal);

					if (result.length() > 0 && !result.toString().endsWith(" OR ")
							&& !result.toString().endsWith(" AND "))
						result.append(" AND ");
					result.append(predicate);
			}
		}

		return result.toString().trim();
	}

	private String getSortSql(Query q)
	{
		StringBuilder sortSql = new StringBuilder();
		if (q.getSort() != null)
		{
			for (Sort.Order o : q.getSort())
			{
				AttributeMetaData att = getEntityMetaData().getAttribute(o.getAttr());
				if (att.getDataType() instanceof MrefField) sortSql.append(", ").append(getColumnName(att));
				else sortSql.append(", ").append(getColumnName(att));
				if (o.getDirection().equals(Sort.Direction.DESC))
				{
					sortSql.append(" DESC");
				}
				else
				{
					sortSql.append(" ASC");
				}
			}

			if (sortSql.length() > 0) sortSql = new StringBuilder("ORDER BY ").append(sortSql.substring(2));
		}
		return sortSql.toString();
	}

	private String getUpdateSql()
	{
		// use (readonly) identifier
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		// create sql
		StringBuilder sql = new StringBuilder("UPDATE ").append(getTableName()).append(" SET ");
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getExpression() != null)
			{
				// computed attributes are not persisted
				continue;
			}
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append(getColumnName(att)).append(" = ?, ");
			}
		}
		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',')
		{
			sql.setLength(sql.length() - 2);
		}
		sql.append(" WHERE ").append(getColumnName(idAttribute)).append("= ?");
		return sql.toString();
	}

	private String getUpdateColumnToNullSql(AttributeMetaData attr)
	{
		return new StringBuilder().append("UPDATE ").append(getTableName()).append(" SET ").append(getColumnName(attr))
				.append(" = ").append("NULL").toString();
	}

	private String getFromSql(Query q)
	{
		StringBuilder from = new StringBuilder();
		from.append(" FROM ").append(getTableName()).append(" AS this");

		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		List<String> mrefQueryFields = Lists.newArrayList();
		getMrefQueryFields(q.getRules(), mrefQueryFields);

		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (q.getFetch() == null || q.getFetch().hasField(att.getName()))
			{
				if (att.getDataType() instanceof MrefField)
				{
					from.append(" LEFT JOIN ").append(getJunctionTableName(att)).append(" AS ")
							.append(getColumnName(att)).append(" ON (this.").append(getColumnName(idAttribute))
							.append(" = ").append(getColumnName(att)).append('.').append(getColumnName(idAttribute))
							.append(')');

				}
			}
		}

		for (int i = 0; i < mrefQueryFields.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData att = getEntityMetaData().getAttribute(mrefQueryFields.get(i));

			from.append(" LEFT JOIN ").append(getJunctionTableName(att)).append(" AS ").append(getFilterColumnName(att))
					.append(i + 1).append(" ON (this.").append(getColumnName(idAttribute)).append(" = ")
					.append(getFilterColumnName(att)).append(i + 1).append(".").append(getColumnName(idAttribute))
					.append(')');
		}

		return from.toString();
	}

	private String getCountSql(Query q, List<Object> parameters)
	{
		String idAttribute = getColumnName(getEntityMetaData().getIdAttribute());

		// both queries ignore offset/pageSize
		List<QueryRule> queryRules = q.getRules();
		if (queryRules == null || queryRules.isEmpty())
		{
			return new StringBuilder("SELECT COUNT(*) FROM ").append(getTableName()).toString();
		}
		else
		{
			String where = getWhereSql(q, parameters, 0);
			String from = getFromSql(q);

			// TODO do not DISTINCT if no joining took place
			if (where.length() > 0) return new StringBuilder("SELECT COUNT(DISTINCT this.").append(idAttribute)
					.append(')').append(from).append(" WHERE ").append(where).toString();

			return new StringBuilder("SELECT COUNT(DISTINCT this.").append(idAttribute).append(')').append(from)
					.toString();
		}
	}

	private String getTableName()
	{
		return getTableName(getEntityMetaData());
	}

	private String getTableName(EntityMetaData emd)
	{
		return new StringBuilder().append("\"").append(emd.getName()).append("\"").toString();
	}

	private String getJunctionTableName(AttributeMetaData attr)
	{
		return getJunctionTableName(getEntityMetaData(), attr);
	}

	private static String getJunctionTableName(EntityMetaData emd, AttributeMetaData attr)
	{
		return new StringBuilder().append("\"").append(emd.getName()).append('_').append(attr.getName()).append("\"")
				.toString();
	}

	private static String getColumnName(AttributeMetaData attr)
	{
		return getColumnName(attr.getName());
	}

	private static String getColumnName(String attrName)
	{
		return new StringBuilder().append("\"").append(attrName).append("\"").toString();
	}

	private static String getFilterColumnName(AttributeMetaData attr)
	{
		return new StringBuilder().append("\"").append(attr.getName()).append("_filter\"").toString();
	}

	private String getUniqueKeyName(AttributeMetaData attr)
	{
		return getUniqueKeyName(getEntityMetaData(), attr);
	}

	private static String getUniqueKeyName(EntityMetaData emd, AttributeMetaData attr)
	{
		return new StringBuilder().append("\"").append(emd.getName()).append('_').append(attr.getName()).append("_key")
				.append("\"").toString();
	}
}