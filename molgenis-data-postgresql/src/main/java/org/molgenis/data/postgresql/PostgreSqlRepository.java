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

/**
 * Repository that persists entities in a PostgreSQL database
 */
public class PostgreSqlRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepository.class);

	/** JDBC batch operation size */
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
		if (iterator.hasNext())
		{
			return iterator.next();
		}
		return null;
	}

	@Override
	public Entity findOne(Object id)
	{
		if (id == null)
		{
			return null;
		}
		return findOne(new QueryImpl().eq(getEntityMetaData().getIdAttribute().getName(), id));
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		if (id == null)
		{
			return null;
		}
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
		if (entity == null)
		{
			throw new RuntimeException("PostgreSqlRepository.add() failed: entity was null");
		}
		add(Stream.of(entity));
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		return add(entities.iterator());
	}

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

			String idAttrName = getEntityMetaData().getIdAttribute().getName();
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
				else if (attr.getDataType() instanceof XrefField
						&& attr.getRefEntity().getBackend().equals(PostgreSqlRepositoryCollection.NAME))
				{
					jdbcTemplate.execute(getCreateFKeySql(attr));
				}

				if (attr.isUnique() && !attr.getName().equals(idAttrName))
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

	@Override
	public void drop()
	{
		DataAccessException remembered = null;
		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (attr.getDataType() instanceof MrefField)
			{
				DataAccessException e = tryExecute(getDropTableSql(getJunctionTableName(attr)));
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

			throw new MolgenisReferencedEntityException(
					format("Cannot delete entity '%s' because it is referenced by the following entities: %s",
							getEntityMetaData().getName(), entityNames.toString()));
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
	 * @param attr
	 *            the {@link AttributeMetaData} to add
	 */
	void addAttribute(AttributeMetaData attr)
	{
		addAttributeInternal(attr, true, true);
	}

	/**
	 * Adds an attribute to the repository. Will excecute the alter table statement in the current thread. Please note
	 * that this *will* commit any existing transactions.
	 *
	 * This is needed for adding columns in the annotator.
	 *
	 * @param attr
	 *            the {@link AttributeMetaData} to add
	 */
	void addAttributeSync(AttributeMetaData attr)
	{
		addAttributeInternal(attr, true, false);
	}

	static String getSelectMrefSql(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		return new StringBuilder().append("SELECT ").append(getColumnName(attr)).append(" FROM ")
				.append(getJunctionTableName(entityMeta, attr)).append(" WHERE ")
				.append(getColumnName(entityMeta.getIdAttribute())).append(" = ?").append(" ORDER BY ")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).toString();
	}

	/**
	 * Adds an attribute to the repository.
	 *
	 * @param attr
	 *            the {@link AttributeMetaData} to add
	 * @param addToEntityMetaData
	 *            boolean indicating if the repository's {@link EntityMetaData} should be updated as well. This should
	 *            not happen for parts of a compound attribute.
	 * @param async
	 *            boolean indicating if the alter table statement should be executed in a different thread or not.
	 */
	private void addAttributeInternal(AttributeMetaData attr, boolean addToEntityMetaData, boolean async)
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
				jdbcTemplate.execute(getCreateJunctionTableSql(attr));
			}
			else if (!attr.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				jdbcTemplate.execute(getAddColumnSql(attr));
			}

			if (attr.getDataType() instanceof XrefField
					&& attr.getRefEntity().getBackend().equals(PostgreSqlRepositoryCollection.NAME))
			{
				jdbcTemplate.execute(getCreateFKeySql(attr));
			}

			String idAttrName = getEntityMetaData().getIdAttribute().getName();
			if (attr.isUnique() && !attr.getName().equals(idAttrName))
			{
				jdbcTemplate.execute(getUniqueSql(attr));
			}

			if (attr.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				for (AttributeMetaData attrPart : attr.getAttributeParts())
				{
					addAttributeInternal(attrPart, false, async);
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
		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (attr.getDataType().getEnumType().equals(FieldTypeEnum.XREF)
					&& getEntityMetaData().getName().equals(attr.getRefEntity().getName()))
			{
				xrefAttributesWithSelfReference.add(attr.getName());
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
		if (entitiesIterator == null)
		{
			return 0;
		}
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
						for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
						{
							// create the mref records
							if (attr.getDataType() instanceof MrefField)
							{
								if (mrefs.get(attr.getName()) == null)
								{
									mrefs.put(attr.getName(), new ArrayList<>());
								}
								if (batch.get(rowIndex).get(attr.getName()) != null)
								{
									AtomicInteger seqNr = new AtomicInteger();
									for (Entity val : batch.get(rowIndex).getEntities(attr.getName()))
									{
										if (val != null)
										{
											Map<String, Object> mref = new HashMap<>();
											mref.put(JUNCTION_TABLE_ORDER_ATTR_NAME, seqNr.getAndIncrement());
											mref.put(idAttribute.getName(),
													batch.get(rowIndex).get(idAttribute.getName()));
											mref.put(attr.getName(), val.getIdValue());
											mrefs.get(attr.getName()).add(mref);
										}
									}
								}
							}
							else
							{
								if (attr.getExpression() != null)
								{
									continue;
								}

								if (batch.get(rowIndex).get(attr.getName()) == null)
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
									Object value = batch.get(rowIndex).get(attr.getName());
									if (value instanceof Entity)
									{
										value = ((Entity) value).get(attr.getRefEntity().getIdAttribute().getName());
									}

									preparedStatement.setObject(fieldIndex++,
											attr.getRefEntity().getIdAttribute().getDataType().convert(value));
								}
								else
								{
									Object value = attr.getDataType().convert(batch.get(rowIndex).get(attr.getName()));
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
					}

					@Override
					public int getBatchSize()
					{
						return batch.size();
					}
				});

				// add mrefs as well
				for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
				{
					if (attr.getDataType() instanceof MrefField)
					{
						addMrefs(mrefs.get(attr.getName()), attr);
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
		// TODO batch updating just like add and delete
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
				for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
				{
					// create the mref records
					if (attr.getDataType() instanceof MrefField)
					{
						if (mrefs.get(attr.getName()) == null)
						{
							mrefs.put(attr.getName(), new ArrayList<>());
						}
						if (e.get(attr.getName()) != null)
						{
							List<Entity> vals = Lists.newArrayList(e.getEntities(attr.getName()));
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
					else
					{
						if (attr.getExpression() != null)
						{
							// computed attributes are not persisted
							continue;
						}
						if (e.get(attr.getName()) == null)
						{
							// repository should not fill in default value, the form should
							preparedStatement.setObject(fieldIndex++, null);
						}
						else
						{
							if (attr.getDataType() instanceof XrefField)
							{
								Object value = e.get(attr.getName());
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
								Object value = attr.getDataType().convert(e.get(attr.getName()));
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
		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (attr.getDataType() instanceof MrefField)
			{
				removeMrefs(ids, attr);
				addMrefs(mrefs.get(attr.getName()), attr);
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
			// DatabaseMetaData.getTables() requires table name without double quotes
			ResultSet tables = dbm.getTables(null, null, getTableName(false), null);
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

	private void removeMrefs(final List<Object> ids, final AttributeMetaData attr)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		StringBuilder mrefSql = new StringBuilder();
		mrefSql.append(getDeleteSql(getJunctionTableName(attr), idAttribute));

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

	private void addMrefs(final List<Map<String, Object>> mrefs, final AttributeMetaData attr)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final AttributeMetaData refEntityIdAttribute = attr.getRefEntity().getIdAttribute();

		StringBuilder mrefSql = new StringBuilder();
		mrefSql.append(getInsertMrefSql(attr, idAttribute));

		jdbcTemplate.batchUpdate(mrefSql.toString(), new BatchPreparedStatementSetter()
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

	private void getAttributeSql(StringBuilder sql, AttributeMetaData attr) throws MolgenisModelException
	{
		if (attr.getExpression() != null)
		{
			return;
		}
		switch (attr.getDataType().getEnumType())
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
				if (attr.equals(getEntityMetaData().getLabelAttribute()))
				{
					throw new MolgenisDataException(
							format("Attribute [%s] of entity [%s] is label attribute and of type [%s]. Label attributes cannot be of type xref, mref, categorical or compound.",
									attr.getName(), getName(), attr.getDataType().getEnumType().toString()));
				}

				if (getEntityMetaData().getLookupAttribute(attr.getName()) != null)
				{
					throw new MolgenisDataException(
							format("Attribute [%s] of entity [%s] is lookup attribute and of type [%s]. Lookup attributes cannot be of type xref, mref, categorical or compound.",
									attr.getName(), getName(), attr.getDataType().getEnumType().toString()));
				}

				break;
			default:
				throw new RuntimeException(format("Unknown datatype [%s]", attr.getDataType().getEnumType().toString()));
		}

		if (!(attr.getDataType() instanceof MrefField))
		{
			sql.append(getColumnName(attr)).append(' ');
			// xref adopt type of the identifier of referenced entity
			if (attr.getDataType() instanceof XrefField)
			{
				sql.append(attr.getRefEntity().getIdAttribute().getDataType().getPostgreSqlType());
			}
			else
			{
				sql.append(attr.getDataType().getPostgreSqlType());
			}
			// TODO remove Questionnaire entity hack
			// not null
			if (!attr.isNillable() && !EntityUtils.doesExtend(metaData, "Questionnaire")
					&& (attr.getVisibleExpression() == null))
			{
				sql.append(" NOT NULL");
			}
			if (attr.getDataType() instanceof EnumField)
			{
				sql.append(" CHECK (" + getColumnName(attr) + " IN (").append(
						attr.getEnumOptions().stream().map(enumOption -> "'" + enumOption + "'").collect(joining(",")))
						.append("))");
			}
		}
	}

	private String getCreateFKeySql(AttributeMetaData attr)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName()).append(" ADD FOREIGN KEY (")
				.append(getColumnName(attr)).append(") REFERENCES ").append(getTableName(attr.getRefEntity())).append('(')
				.append(getColumnName(attr.getRefEntity().getIdAttribute())).append(")").toString();
	}

	private String getUniqueSql(AttributeMetaData attr)
	{
		// PostgreSQL name convention
		return new StringBuilder().append("ALTER TABLE ").append(getTableName()).append(" ADD CONSTRAINT ")
				.append(getUniqueKeyName(attr)).append(" UNIQUE (").append(getColumnName(attr)).append(")").toString();
	}

	private List<AttributeMetaData> getMrefQueryAttrs(Query q)
	{
		List<AttributeMetaData> mrefAttrsInQuery = new ArrayList<>();
		getMrefQueryFieldsRec(q.getRules(), mrefAttrsInQuery);
		return mrefAttrsInQuery;
	}

	private void getMrefQueryFieldsRec(List<QueryRule> rules, List<AttributeMetaData> mrefAttrsInQuery)
	{
		for (QueryRule rule : rules)
		{
			if (rule.getField() != null)
			{
				AttributeMetaData attr = this.getEntityMetaData().getAttribute(rule.getField());
				if (attr != null && attr.getDataType() instanceof MrefField)
				{
					mrefAttrsInQuery.add(attr);
				}
			}

			if (rule.getNestedRules() != null && !rule.getNestedRules().isEmpty())
			{
				getMrefQueryFieldsRec(rule.getNestedRules(), mrefAttrsInQuery);
			}
		}
	}

	private String getAddColumnSql(AttributeMetaData attr) throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE ").append(getTableName()).append(" ADD ");
		getAttributeSql(sql, attr);
		return sql.toString();
	}

	private String getCreateTableSql() throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS ").append(getTableName()).append('(');

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			getAttributeSql(sql, attr);
			if (attr.getExpression() == null && !(attr.getDataType() instanceof MrefField))
			{
				sql.append(", ");
			}
		}
		// primary key is first attribute unless otherwise indicated
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		if (idAttribute == null)
		{
			throw new MolgenisDataException(format("Missing idAttribute for entity [%s]", getName()));
		}

		if (idAttribute.getDataType() instanceof XrefField || idAttribute.getDataType() instanceof MrefField)
		{
			throw new RuntimeException(
					format("primary key(%s.%s) cannot be XREF or MREF", getTableName(), getColumnName(idAttribute)));
		}

		if (idAttribute.isNillable() == true)
		{
			throw new RuntimeException(
					format("idAttribute (%s.%s) should not be nillable", getTableName(), getColumnName(idAttribute)));
		}

		sql.append("PRIMARY KEY (").append(getColumnName(getEntityMetaData().getIdAttribute())).append(')');
		sql.append(')');

		if (LOG.isTraceEnabled())
		{
			LOG.trace("sql: " + sql);
		}

		return sql.toString();
	}

	private String getCreateJunctionTableSql(AttributeMetaData attr) throws MolgenisModelException
	{
		AttributeMetaData idAttr = getEntityMetaData().getIdAttribute();
		StringBuilder sql = new StringBuilder();

		sql.append(" CREATE TABLE IF NOT EXISTS ").append(getJunctionTableName(attr)).append(" (")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(" INT,").append(getColumnName(idAttr))
				.append(' ').append(idAttr.getDataType().getPostgreSqlType()).append(" NOT NULL, ")
				.append(getColumnName(attr)).append(' ')
				.append(attr.getRefEntity().getIdAttribute().getDataType().getPostgreSqlType())
				.append(" NOT NULL, FOREIGN KEY (").append(getColumnName(idAttr)).append(") REFERENCES ")
				.append(getTableName()).append('(').append(getColumnName(idAttr)).append(") ON DELETE CASCADE");

		if (attr.getRefEntity().getBackend().equals(PostgreSqlRepositoryCollection.NAME))
		{
			sql.append(", FOREIGN KEY (").append(getColumnName(attr)).append(") REFERENCES ")
					.append(getTableName(attr.getRefEntity())).append('(')
					.append(getColumnName(attr.getRefEntity().getIdAttribute())).append(") ON DELETE CASCADE");
		}

		sql.append(", UNIQUE (").append(getColumnName(attr)).append(',').append(getColumnName(idAttr)).append(')');
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
		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (attr.getExpression() != null)
			{
				continue;
			}
			if (!(attr.getDataType() instanceof MrefField))
			{
				sql.append(getColumnName(attr)).append(", ");
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
		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (q.getFetch() == null || q.getFetch().hasField(attr.getName()))
			{
				if (attr.getExpression() == null)
				{
					if (count > 0)
					{
						select.append(", ");
					}

					if (attr.getDataType() instanceof MrefField)
					{
						// TODO retrieve mref values in seperate queries to allow specifying limit and offset
						select.append("array_agg(distinct array[").append(getColumnName(attr)).append('.')
								.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append("::text,")
								.append(getColumnName(attr)).append('.').append(getColumnName(attr))
								.append("::text]) AS ").append(getColumnName(attr));
					}
					else
					{
						select.append("this.").append(getColumnName(attr));
						if (group.length() > 0)
						{
							group.append(", this.").append(getColumnName(attr));
						}
						else
						{
							group.append("this.").append(getColumnName(attr));
						}
					}
					count++;
				}
			}
		}

		// from
		StringBuilder result = new StringBuilder().append(select).append(getFromSql(q));
		// where
		String where = getWhereSql(q, parameters, 0);
		if (where.length() > 0)
		{
			result.append(" WHERE ").append(where);
		}
		// group by
		if (select.indexOf("array_agg") != -1 && group.length() > 0)
		{
			result.append(" GROUP BY ").append(group);
		}
		// order by
		result.append(' ').append(getSortSql(q));
		// limit
		if (q.getPageSize() > 0)
		{
			result.append(" LIMIT ").append(q.getPageSize());
		}
		if (q.getOffset() > 0)
		{
			result.append(" OFFSET ").append(q.getOffset());
		}

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
					throw new MolgenisDataException(format("Unknown attribute [%s]", r.getField()));
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
					for (AttributeMetaData searchAttr : getEntityMetaData().getAtomicAttributes())
					{
						// TODO: other data types???
						if (searchAttr.getDataType() instanceof StringField || searchAttr.getDataType() instanceof TextField)
						{
							search.append(" OR this.").append(getColumnName(searchAttr)).append(" LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");
						}
						else if (searchAttr.getDataType() instanceof XrefField)
						{

							Repository repo = dataService.getRepository(searchAttr.getRefEntity().getName());
							if (repo.getCapabilities().contains(QUERYABLE))
							{
								Query refQ = new QueryImpl().like(searchAttr.getRefEntity().getLabelAttribute().getName(),
										r.getValue().toString());
								Iterator<Entity> it = repo.findAll(refQ).iterator();
								if (it.hasNext())
								{
									search.append(" OR this.").append(getColumnName(searchAttr)).append(" IN (");
									while (it.hasNext())
									{
										Entity ref = it.next();
										search.append("?");
										parameters.add(searchAttr.getDataType()
												.convert(ref.get(searchAttr.getRefEntity().getIdAttribute().getName())));
										if (it.hasNext())
										{
											search.append(",");
										}
									}
									search.append(")");
								}
							}
						}
						else if (searchAttr.getDataType() instanceof MrefField)
						{
							// TODO check if casting is required for postgres
							search.append(" OR CAST(").append(getColumnName(searchAttr)).append(".").append(getColumnName(searchAttr))
									.append(" as CHAR) LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");

						}
						else
						{
							// TODO check if casting is required for postgres
							search.append(" OR CAST(this.").append(getColumnName(searchAttr)).append(" as CHAR) LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");
						}
					}
					if (search.length() > 0)
					{
						result.append('(').append(search.substring(4)).append(')');
					}
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
					{
						result.append(getFilterColumnName(attr, mrefFilterIndex));
					}
					else
					{
						result.append("this");
					}

					result.append(".").append(getColumnName(r.getField())).append(" IN (").append(in).append(')');
					break;
				default:
					// comparable values...
					FieldType type = attr.getDataType();
					if (type instanceof MrefField)
					{
						predicate.append(getFilterColumnName(attr, mrefFilterIndex));
					}
					else
					{
						predicate.append("this");
					}

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
							throw new MolgenisDataException(format("cannot solve query rule:  %s", r.toString()));
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
					{
						result.append(" AND ");
					}
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
				AttributeMetaData attr = getEntityMetaData().getAttribute(o.getAttr());
				if (attr.getDataType() instanceof MrefField)
				{
					sortSql.append(", ").append(getColumnName(attr));
				}
				else
				{
					sortSql.append(", ").append(getColumnName(attr));
				}
				if (o.getDirection().equals(Sort.Direction.DESC))
				{
					sortSql.append(" DESC");
				}
				else
				{
					sortSql.append(" ASC");
				}
			}

			if (sortSql.length() > 0)
			{
				sortSql = new StringBuilder("ORDER BY ").append(sortSql.substring(2));
			}
		}
		return sortSql.toString();
	}

	private String getUpdateSql()
	{
		// use (readonly) identifier
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		// create sql
		StringBuilder sql = new StringBuilder("UPDATE ").append(getTableName()).append(" SET ");
		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (attr.getExpression() != null)
			{
				// computed attributes are not persisted
				continue;
			}
			if (!(attr.getDataType() instanceof MrefField))
			{
				sql.append(getColumnName(attr)).append(" = ?, ");
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
		StringBuilder from = new StringBuilder().append(" FROM ").append(getTableName()).append(" AS this");

		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			if (q.getFetch() == null || q.getFetch().hasField(attr.getName()))
			{
				if (attr.getDataType() instanceof MrefField)
				{
					from.append(" LEFT JOIN ").append(getJunctionTableName(attr)).append(" AS ")
							.append(getColumnName(attr)).append(" ON (this.").append(getColumnName(idAttribute))
							.append(" = ").append(getColumnName(attr)).append('.').append(getColumnName(idAttribute))
							.append(')');
				}
			}
		}

		List<AttributeMetaData> mrefAttrsInQuery = getMrefQueryAttrs(q);
		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData mrefAttr = mrefAttrsInQuery.get(i);

			from.append(" LEFT JOIN ").append(getJunctionTableName(mrefAttr)).append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1)).append(" ON (this.")
					.append(getColumnName(idAttribute)).append(" = ").append(getFilterColumnName(mrefAttr, i + 1))
					.append('.').append(getColumnName(idAttribute)).append(')');
		}

		return from.toString();
	}

	private String getFromCountSql(Query q, List<AttributeMetaData> mrefAttrsInQuery)
	{
		StringBuilder from = new StringBuilder().append(" FROM ").append(getTableName()).append(" AS this");

		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData mrefAttr = mrefAttrsInQuery.get(i);

			from.append(" LEFT JOIN ").append(getJunctionTableName(mrefAttr)).append(" AS ")
					.append(getColumnName(mrefAttr)).append(" ON (this.").append(getColumnName(idAttribute))
					.append(" = ").append(getColumnName(mrefAttr)).append('.').append(getColumnName(idAttribute))
					.append(')');

			from.append(" LEFT JOIN ").append(getJunctionTableName(mrefAttr)).append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1)).append(" ON (this.")
					.append(getColumnName(idAttribute)).append(" = ").append(getFilterColumnName(mrefAttr, i + 1))
					.append('.').append(getColumnName(idAttribute)).append(')');
		}

		return from.toString();
	}

	// FIXME both queries ignore offset/pageSize
	private String getCountSql(Query q, List<Object> parameters)
	{
		StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT");
		String idAttribute = getColumnName(getEntityMetaData().getIdAttribute());

		List<QueryRule> queryRules = q.getRules();
		if (queryRules == null || queryRules.isEmpty())
		{
			sqlBuilder.append("(*) FROM ").append(getTableName());
		}
		else
		{
			List<AttributeMetaData> mrefAttrsInQuery = getMrefQueryAttrs(q);
			if (!mrefAttrsInQuery.isEmpty())
			{
				// distinct count in case query contains one or more rules refering to MREF attributes.
				sqlBuilder.append("(DISTINCT this.").append(idAttribute).append(')');
			}
			else
			{
				sqlBuilder.append("(*)");
			}

			String from = getFromCountSql(q, mrefAttrsInQuery);
			String where = getWhereSql(q, parameters, 0);
			sqlBuilder.append(from).append(" WHERE ").append(where);
		}
		return sqlBuilder.toString();
	}

	private String getTableName()
	{
		return getTableName(getEntityMetaData());
	}

	private String getTableName(boolean quoteSystemIdentifiers)
	{
		return getTableName(getEntityMetaData(), quoteSystemIdentifiers);
	}

	private String getTableName(EntityMetaData emd)
	{
		return getTableName(emd, true);
	}

	private String getTableName(EntityMetaData emd, boolean quoteSystemIdentifiers)
	{
		StringBuilder strBuilder = new StringBuilder();
		if (quoteSystemIdentifiers)
		{
			strBuilder.append("\"");
		}
		strBuilder.append(emd.getName());
		if (quoteSystemIdentifiers)
		{
			strBuilder.append("\"");
		}
		return strBuilder.toString();
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

	private static String getFilterColumnName(AttributeMetaData attr, int filterIndex)
	{
		return new StringBuilder().append("\"").append(attr.getName()).append("_filter").append(filterIndex)
				.append("\"").toString();
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