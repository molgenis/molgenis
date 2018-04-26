package org.molgenis.data.postgresql;

import com.google.common.collect.Iterables;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepositoryCollection;
import org.molgenis.data.support.AttributeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.lang.String.format;
import static java.util.EnumSet.of;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.RepositoryCollectionCapability.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.MetaUtils.getEntityTypeFetch;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
import static org.molgenis.data.postgresql.PostgreSqlRepository.BATCH_SIZE;
import static org.molgenis.data.postgresql.PostgreSqlRepository.createJunctionTableRowData;
import static org.molgenis.data.support.EntityTypeUtils.*;
import static org.springframework.jdbc.support.JdbcUtils.closeConnection;

public class PostgreSqlRepositoryCollection extends AbstractRepositoryCollection
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepositoryCollection.class);

	public static final String POSTGRESQL = "PostgreSQL";

	private final PostgreSqlEntityFactory postgreSqlEntityFactory;
	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	private final DataService dataService;

	PostgreSqlRepositoryCollection(PostgreSqlEntityFactory postgreSqlEntityFactory, DataSource dataSource,
			JdbcTemplate jdbcTemplate, DataService dataService)
	{
		this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
		this.dataSource = requireNonNull(dataSource);
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public String getName()
	{
		return POSTGRESQL;
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return immutableEnumSet(of(WRITABLE, UPDATABLE, META_DATA_PERSISTABLE));
	}

	@Override
	public boolean hasRepository(String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		PostgreSqlRepository repository = createPostgreSqlRepository(entityType);
		if (!isTableExists(entityType))
		{
			createTable(entityType);
		}
		return repository;
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return isTableExists(entityType);
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						  .eq(BACKEND, POSTGRESQL)
						  .fetch(getEntityTypeFetch())
						  .findAll()
						  .map(EntityType::getId)::iterator;
	}

	@Override
	public Repository<Entity> getRepository(String id)
	{
		EntityType entityType = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
										   .eq(BACKEND, POSTGRESQL)
										   .and()
										   .eq(ID, id)
										   .and()
										   .eq(IS_ABSTRACT, false)
										   .fetch(getEntityTypeFetch())
										   .findOne();
		return getRepository(entityType);
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		return createPostgreSqlRepository(entityType);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						  .eq(BACKEND, POSTGRESQL)
						  .and()
						  .eq(IS_ABSTRACT, false)
						  .fetch(getEntityTypeFetch())
						  .findAll()
						  .map(this::getRepository)
						  .iterator();
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		if (entityType.isAbstract())
		{
			throw new UnknownRepositoryException(entityType.getId());
		}
		dropTables(entityType);
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		//  no actions needed
	}

	private void dropTables(EntityType entityType)
	{
		getJunctionTableAttributes(entityType).forEach(mrefAttr -> dropJunctionTable(entityType, mrefAttr));

		String sqlDropTable = getSqlDropTable(entityType);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping table for entity [{}]", entityType.getId());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropTable);
			}
		}
		jdbcTemplate.execute(sqlDropTable);

		if (getTableAttributesReadonly(entityType).findAny().isPresent())
		{
			String sqlDropFunctionValidateUpdate = getSqlDropFunctionValidateUpdate(entityType);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Dropping trigger function for entity [{}]", entityType.getId());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlDropFunctionValidateUpdate);
				}
			}
			jdbcTemplate.execute(sqlDropFunctionValidateUpdate);
		}
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attr)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Cannot add attribute [%s] to abstract entity type [%s].", attr.getName(),
							entityType.getId()));
		}
		if (entityType.getAttribute(attr.getName()) != null)
		{
			throw new MolgenisDataException(
					format("Adding attribute operation failed. Attribute already exists [%s]", attr.getName()));
		}
		addAttributeInternal(entityType, attr);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Cannot update attribute [%s] for abstract entity type [%s].", attr.getName(),
							entityType.getId()));
		}
		if (!isPersisted(attr) && !isPersisted(updatedAttr))
		{
			return;
		}

		if (isPersisted(attr) && !isPersisted(updatedAttr))
		{
			deleteAttribute(entityType, attr);
		}
		else if (!isPersisted(attr) && isPersisted(updatedAttr))
		{
			addAttributeInternal(entityType, updatedAttr);
		}
		else
		{
			updateColumn(entityType, attr, updatedAttr);
		}
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Cannot delete attribute [%s] from abstract entity type [%s].", attr.getName(),
							entityType.getId()));
		}
		if (entityType.getAttribute(attr.getName()) == null)
		{
			throw new UnknownAttributeException(entityType, attr.getName());
		}
		if (!isPersisted(attr))
		{
			return;
		}

		if (!(attr.getDataType() == ONE_TO_MANY && attr.isMappedBy()))
		{
			if (isMultipleReferenceType(attr))
			{
				dropJunctionTable(entityType, attr);
			}
			else
			{
				dropColumn(entityType, attr);
			}
		}
	}

	/**
	 * Add attribute to entityType.
	 *
	 * @param entityType the {@link EntityType} to add attribute to
	 * @param attr       attribute to add
	 */
	private void addAttributeInternal(EntityType entityType, Attribute attr)
	{
		if (!isPersisted(attr))
		{
			return;
		}

		if (!(attr.getDataType() == ONE_TO_MANY && attr.isMappedBy()))
		{
			if (isMultipleReferenceType(attr))
			{
				createJunctionTable(entityType, attr);

				if (attr.getDefaultValue() != null && !attr.isNillable())
				{
					@SuppressWarnings("unchecked")
					Iterable<Entity> defaultRefEntities = (Iterable<Entity>) AttributeUtils.getDefaultTypedValue(attr);
					if (!Iterables.isEmpty(defaultRefEntities))
					{
						createJunctionTableRows(entityType, attr, defaultRefEntities);
					}
				}
			}
			else
			{
				createColumn(entityType, attr);
			}
		}
	}

	private void createJunctionTableRows(EntityType entityType, Attribute attr, Iterable<Entity> defaultRefEntities)
	{
		int nrRefEntities = Iterables.size(defaultRefEntities);

		PostgreSqlRepository postgreSqlRepository = createPostgreSqlRepository(entityType);

		Attribute idAttribute = entityType.getIdAttribute();
		String idAttributeName = idAttribute.getName();
		postgreSqlRepository.forEachBatched(new Fetch().field(idAttributeName), entities ->
		{
			List<Map<String, Object>> mrefs = new ArrayList<>(entities.size() * nrRefEntities);
			entities.forEach(entity ->
			{
				AtomicInteger seqNr = new AtomicInteger(0);
				defaultRefEntities.forEach(defaultRefEntity -> mrefs.add(
						createJunctionTableRowData(seqNr.getAndIncrement(), idAttribute, defaultRefEntity, attr,
								entity)));
			});
			postgreSqlRepository.addMrefs(mrefs, attr);
		}, BATCH_SIZE);
	}

	/**
	 * Indicates if the attribute is persisted in the database.
	 * Compound attributes and computed attributes with an expression are not persisted.
	 *
	 * @param attr the attribute to check
	 * @return boolean indicating if the entity is persisted in the database.
	 */
	private static boolean isPersisted(Attribute attr)
	{
		return !attr.hasExpression() && attr.getDataType() != COMPOUND;
	}

	/**
	 * Updates database column based on attribute changes.
	 *
	 * @param entityType  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateColumn(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		// nullable changes
		if (!Objects.equals(attr.isNillable(), updatedAttr.isNillable()))
		{
			updateNillable(entityType, attr, updatedAttr);
		}

		// unique changes
		if (!Objects.equals(attr.isUnique(), updatedAttr.isUnique()))
		{
			updateUnique(entityType, attr, updatedAttr);
		}

		// readonly changes
		if (!Objects.equals(attr.isReadOnly(), updatedAttr.isReadOnly()))
		{
			updateReadonly(entityType, attr, updatedAttr);
		}

		// data type changes
		if (!Objects.equals(attr.getDataType(), updatedAttr.getDataType()))
		{
			updateDataType(entityType, attr, updatedAttr);
		}

		// ref entity changes
		if (attr.getRefEntity() != null && updatedAttr.getRefEntity() != null && !attr.getRefEntity()
																					  .getId()
																					  .equals(updatedAttr.getRefEntity()
																										 .getId()))
		{
			updateRefEntity(entityType, attr, updatedAttr);
		}

		// enum option changes
		if (!Objects.equals(attr.getEnumOptions(), updatedAttr.getEnumOptions()))
		{
			updateEnumOptions(entityType, attr, updatedAttr);
		}
	}

	/**
	 * Updates foreign keys based on referenced entity changes.
	 *
	 * @param entityType  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateRefEntity(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		if (isSingleReferenceType(attr) && isSingleReferenceType(updatedAttr))
		{
			dropForeignKey(entityType, attr);

			if (attr.getRefEntity().getIdAttribute().getDataType() != updatedAttr.getRefEntity()
																				 .getIdAttribute()
																				 .getDataType())
			{
				updateColumnDataType(entityType, updatedAttr);
			}

			createForeignKey(entityType, updatedAttr);
		}
		else if (isMultipleReferenceType(attr) && isMultipleReferenceType(updatedAttr))
		{
			throw new MolgenisDataException(
					format("Updating entity [%s] attribute [%s] referenced entity from [%s] to [%s] not allowed for type [%s]",
							entityType.getId(), attr.getName(), attr.getRefEntity().getId(),
							updatedAttr.getRefEntity().getId(), updatedAttr.getDataType().toString()));
		}
	}

	/**
	 * Updates check constraint based on enum value changes.
	 *
	 * @param entityType  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateEnumOptions(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		if (attr.getDataType() == ENUM)
		{
			if (updatedAttr.getDataType() == ENUM)
			{
				// update check constraint
				dropCheckConstraint(entityType, attr);
				createCheckConstraint(entityType, updatedAttr);
			}
			else
			{
				// drop check constraint
				dropCheckConstraint(entityType, attr);
			}
		}
		else
		{
			if (updatedAttr.getDataType() == ENUM)
			{
				createCheckConstraint(entityType, updatedAttr);
			}
		}
	}

	private void dropColumnDefaultValue(EntityType entityType, Attribute attr)
	{
		String dropColumnDefaultValueSql = getSqlDropColumnDefault(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping column default constraint for entity [{}] attribute [{}]", entityType.getId(),
					attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropColumnDefaultValueSql);
			}
		}
		jdbcTemplate.execute(dropColumnDefaultValueSql);
	}

	/**
	 * Updates column data type and foreign key constraints based on data type update.
	 *
	 * @param entityType  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateDataType(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		Attribute idAttr = entityType.getIdAttribute();
		if (idAttr != null && idAttr.getName().equals(attr.getName()))
		{
			throw new MolgenisDataException(
					format("Data type of entity [%s] attribute [%s] cannot be modified, because [%s] is an ID attribute.",
							entityType.getId(), attr.getName(), attr.getName()));
		}

		// do nothing on representation changes XREF --> CATEGORICAL
		if (isSingleReferenceType(attr) && isSingleReferenceType(updatedAttr))
		{
			return;
		}

		// do nothing on representation changes MREF --> CATEGORICAL_MREF
		if (isMultipleReferenceType(attr) && isMultipleReferenceType(updatedAttr))
		{
			return;
		}

		// remove foreign key on data type updates such as XREF --> STRING
		if (isSingleReferenceType(attr) && !isReferenceType(updatedAttr))
		{
			dropForeignKey(entityType, attr);
		}

		updateColumnDataType(entityType, updatedAttr);

		// add foreign key on data type updates such as STRING --> XREF
		if (!isReferenceType(attr) && isSingleReferenceType(updatedAttr))
		{
			createForeignKey(entityType, updatedAttr);
		}
	}

	/**
	 * Updates unique constraint based on attribute unique changes.
	 *
	 * @param entityType  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateUnique(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		if (attr.isUnique() && !updatedAttr.isUnique())
		{
			Attribute idAttr = entityType.getIdAttribute();
			if (idAttr != null && idAttr.getName().equals(attr.getName()))
			{
				throw new MolgenisDataException(
						format("ID attribute [%s] of entity [%s] must be unique", attr.getName(), entityType.getId()));
			}

			dropUniqueKey(entityType, updatedAttr);
		}
		else if (!attr.isUnique() && updatedAttr.isUnique())
		{
			createUniqueKey(entityType, updatedAttr);
		}
	}

	/**
	 * Updates triggers and functions based on attribute readonly changes.
	 *
	 * @param entityType  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateReadonly(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		LinkedHashMap<String, Attribute> readonlyTableAttrs = getTableAttributesReadonly(entityType).collect(
				toMap(Attribute::getName, Function.identity(), (u, v) ->
				{
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				}, LinkedHashMap::new));
		if (!readonlyTableAttrs.isEmpty())
		{
			dropTableTriggers(entityType);
		}

		if (attr.isReadOnly() && !updatedAttr.isReadOnly())
		{
			readonlyTableAttrs.remove(attr.getName());
		}
		else if (!attr.isReadOnly() && updatedAttr.isReadOnly())
		{
			readonlyTableAttrs.put(updatedAttr.getName(), updatedAttr);
		}

		if (!readonlyTableAttrs.isEmpty())
		{
			createTableTriggers(entityType, readonlyTableAttrs.values());
		}
	}

	/**
	 * Return a new PostgreSQL repository
	 */
	private PostgreSqlRepository createPostgreSqlRepository(EntityType entityType)
	{
		return new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource, entityType);
	}

	private boolean isTableExists(EntityType entityType)
	{
		return isTableExists(PostgreSqlNameGenerator.getTableName(entityType, false));
	}

	private boolean isTableExists(String tableName)
	{
		Connection conn = null;
		try
		{
			conn = dataSource.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			// DatabaseMetaData.getTables() requires table name without double quotes, only search TABLE table type to
			// avoid matches with system tables
			ResultSet tables = dbm.getTables(null, null, tableName, new String[] { "TABLE" });
			return tables.next();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			closeConnection(conn);
		}
	}

	private void updateNillable(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		if (attr.isNillable() && !updatedAttr.isNillable())
		{
			String sqlSetNotNull = getSqlSetNotNull(entityType, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating not null constraint for entity [{}] attribute [{}]", entityType.getId(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlSetNotNull);
				}
			}
			jdbcTemplate.execute(sqlSetNotNull);
		}
		else if (!attr.isNillable() && updatedAttr.isNillable())
		{
			Attribute idAttr = entityType.getIdAttribute();
			if (idAttr != null && idAttr.getName().equals(attr.getName()))
			{
				throw new MolgenisDataException(
						format("ID attribute [%s] of entity [%s] cannot be nullable", attr.getName(),
								entityType.getId()));
			}

			String sqlDropNotNull = getSqlDropNotNull(entityType, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Removing not null constraint for entity [{}] attribute [{}]", entityType.getId(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlDropNotNull);
				}
			}
			jdbcTemplate.execute(sqlDropNotNull);
		}
	}

	private void createTable(EntityType entityType)
	{
		// create table
		String createTableSql = getSqlCreateTable(entityType);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating table for entity [{}]", entityType.getId());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createTableSql);
			}
		}
		jdbcTemplate.execute(createTableSql);

		createTableTriggers(entityType);

		// create junction tables for attributes referencing multiple entities
		createJunctionTables(entityType);
	}

	private void createTableTriggers(EntityType entityType)
	{
		List<Attribute> readonlyTableAttrs = getTableAttributesReadonly(entityType).collect(toList());
		if (!readonlyTableAttrs.isEmpty())
		{
			createTableTriggers(entityType, readonlyTableAttrs);
		}
	}

	private void createTableTriggers(EntityType entityType, Collection<Attribute> readonlyTableAttrs)
	{
		String createFunctionSql = getSqlCreateFunctionValidateUpdate(entityType, readonlyTableAttrs);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating update trigger function for entity [{}]", entityType.getId());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createFunctionSql);
			}
		}
		jdbcTemplate.execute(createFunctionSql);

		String createUpdateTriggerSql = getSqlCreateUpdateTrigger(entityType, readonlyTableAttrs);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating update trigger for entity [{}]", entityType.getId());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createUpdateTriggerSql);
			}
		}
		jdbcTemplate.execute(createUpdateTriggerSql);
	}

	private void updateTableTriggers(EntityType entityType, Collection<Attribute> readonlyTableAttrs)
	{
		dropTableTriggers(entityType);
		createTableTriggers(entityType, readonlyTableAttrs);
	}

	private void dropTableTriggers(EntityType entityType)
	{
		String dropUpdateTriggerSql = getSqlDropUpdateTrigger(entityType);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleting update trigger for entity [{}]", entityType.getId());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropUpdateTriggerSql);
			}
		}
		jdbcTemplate.execute(dropUpdateTriggerSql);

		String dropFunctionValidateUpdateSql = getSqlDropFunctionValidateUpdate(entityType);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Deleting update trigger function for entity [{}]", entityType.getId());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropFunctionValidateUpdateSql);
			}
		}
		jdbcTemplate.execute(dropFunctionValidateUpdateSql);
	}

	private void createJunctionTables(EntityType entityType)
	{
		getJunctionTableAttributes(entityType).forEach(attr -> createJunctionTable(entityType, attr));
	}

	private void createForeignKey(EntityType entityType, Attribute attr)
	{
		String createForeignKeySql = getSqlCreateForeignKey(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating foreign key for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createForeignKeySql);
			}
		}
		jdbcTemplate.execute(createForeignKeySql);
	}

	private void dropForeignKey(EntityType entityType, Attribute attr)
	{
		String dropForeignKeySql = getSqlDropForeignKey(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping foreign key for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropForeignKeySql);
			}
		}
		jdbcTemplate.execute(dropForeignKeySql);
	}

	private void createUniqueKey(EntityType entityType, Attribute attr)
	{
		String createUniqueKeySql = getSqlCreateUniqueKey(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating unique key for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createUniqueKeySql);
			}
		}
		jdbcTemplate.execute(createUniqueKeySql);
	}

	private void dropUniqueKey(EntityType entityType, Attribute attr)
	{
		String dropUniqueKeySql = getSqlDropUniqueKey(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping unique key for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropUniqueKeySql);
			}
		}
		jdbcTemplate.execute(dropUniqueKeySql);
	}

	private void createCheckConstraint(EntityType entityType, Attribute attr)
	{
		String sqlCreateCheckConstraint = getSqlCreateCheckConstraint(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating check constraint for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlCreateCheckConstraint);
			}
		}
		jdbcTemplate.execute(sqlCreateCheckConstraint);
	}

	private void dropCheckConstraint(EntityType entityType, Attribute attr)
	{
		String sqlDropCheckConstraint = getSqlDropCheckConstraint(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping check constraint for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropCheckConstraint);
			}
		}
		jdbcTemplate.execute(sqlDropCheckConstraint);
	}

	private void createColumn(EntityType entityType, Attribute attr)
	{
		String addColumnSql = getSqlAddColumn(entityType, attr, ColumnMode.INCLUDE_DEFAULT_CONSTRAINT);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating column for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", addColumnSql);
			}
		}
		jdbcTemplate.execute(addColumnSql);

		if (generateSqlColumnDefaultConstraint(attr))
		{
			dropColumnDefaultValue(entityType, attr);
		}

		if (attr.isReadOnly())
		{
			Stream<Attribute> updatedTableAttrsReadonly;
			Stream<Attribute> tableAttrsReadonly = getTableAttributesReadonly(entityType);
			if (isTableAttribute(attr))
			{
				updatedTableAttrsReadonly = Stream.concat(tableAttrsReadonly, Stream.of(attr));
			}
			else
			{
				updatedTableAttrsReadonly = tableAttrsReadonly;
			}
			updateTableTriggers(entityType, updatedTableAttrsReadonly.collect(toList()));
		}
	}

	private void dropColumn(EntityType entityType, Attribute attr)
	{
		if (attr.isReadOnly())
		{
			LinkedHashMap<String, Attribute> updatedReadonlyTableAttrs = getTableAttributesReadonly(entityType).collect(
					toMap(Attribute::getName, Function.identity(), (u, v) ->
					{
						throw new IllegalStateException(String.format("Duplicate key %s", u));
					}, LinkedHashMap::new));
			updatedReadonlyTableAttrs.remove(attr.getName());

			updateTableTriggers(entityType, updatedReadonlyTableAttrs.values());
		}

		String dropColumnSql = getSqlDropColumn(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping column for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropColumnSql);
			}
		}
		jdbcTemplate.execute(dropColumnSql);
	}

	private void updateColumnDataType(EntityType entityType, Attribute attr)
	{
		String sqlSetDataType = getSqlSetDataType(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Changing data type of entity [{}] attribute [{}] to [{}]", entityType.getId(), attr.getName(),
					attr.getDataType().toString());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlSetDataType);
			}
		}
		jdbcTemplate.execute(sqlSetDataType);
	}

	private void createJunctionTable(EntityType entityType, Attribute attr)
	{
		String createJunctionTableSql = getSqlCreateJunctionTable(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating junction table for entity [{}] attribute [{}]", entityType.getId(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createJunctionTableSql);
			}
		}
		jdbcTemplate.execute(createJunctionTableSql);

		String createJunctionTableIndexSql = getSqlCreateJunctionTableIndex(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating junction table index for entity [{}] attribute [{}]", entityType.getId(),
					attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createJunctionTableIndexSql);
			}
		}
		jdbcTemplate.execute(createJunctionTableIndexSql);
	}

	private void dropJunctionTable(EntityType entityType, Attribute mrefAttr)
	{
		String sqlDropJunctionTable = getSqlDropJunctionTable(entityType, mrefAttr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping junction table for entity [{}] attribute [{}]", entityType.getId(), mrefAttr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropJunctionTable);
			}
		}
		jdbcTemplate.execute(sqlDropJunctionTable);
	}
}