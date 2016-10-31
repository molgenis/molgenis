package org.molgenis.data.postgresql;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.lang.String.format;
import static java.util.EnumSet.of;
import static java.util.Objects.requireNonNull;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.RepositoryCollectionCapability.*;
import static org.molgenis.data.meta.MetaUtils.getEntityTypeFetch;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.getJunctionTableAttributes;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.getTableName;
import static org.molgenis.data.support.EntityTypeUtils.*;

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
		PostgreSqlRepository repository = createPostgreSqlRepository();
		repository.setEntityType(entityType);
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
	public Iterable<String> getEntityNames()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class).eq(BACKEND, POSTGRESQL).fetch(getEntityTypeFetch())
				.findAll().map(EntityType::getName)::iterator;
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		EntityType entityType = dataService.query(ENTITY_TYPE_META_DATA, EntityType.class).eq(BACKEND, POSTGRESQL).and()
				.eq(FULL_NAME, name).and().eq(IS_ABSTRACT, false).fetch(getEntityTypeFetch()).findOne();
		return getRepository(entityType);
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		PostgreSqlRepository repository = createPostgreSqlRepository();
		repository.setEntityType(entityType);
		return repository;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class).eq(BACKEND, POSTGRESQL).and().eq(IS_ABSTRACT, false)
				.fetch(getEntityTypeFetch()).findAll().map(this::getRepository).iterator();
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		if (entityType.isAbstract())
		{
			throw new UnknownRepositoryException(entityType.getName());
		}
		getJunctionTableAttributes(entityType).forEach(mrefAttr -> dropJunctionTable(entityType, mrefAttr));

		String sqlDropTable = getSqlDropTable(entityType);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping table for entity [{}]", entityType.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropTable);
			}
		}
		jdbcTemplate.execute(sqlDropTable);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attr)
	{
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Cannot add attribute [%s] to abstract entity type [%s].", attr.getName(),
							entityType.getName()));
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
							entityType.getName()));
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
							entityType.getName()));
		}
		if (entityType.getAttribute(attr.getName()) == null)
		{
			throw new UnknownAttributeException(format("Unknown attribute [%s]", attr.getName()));
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
	 *  @param entityType      the {@link EntityType} to add attribute to
	 * @param attr            attribute to add
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
			}
			else
			{
				createColumn(entityType, attr);
			}
		}
	}

	/**
	 * Indicates if the attribute is persisted in the database.
	 * Compound attributes and computed attributes with an expression are not persisted.
	 *
	 * @param attr the attribute to check
	 * @return boolean indicating if the entity is persisted in the database.
	 */
	private boolean isPersisted(Attribute attr)
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

		// data type changes
		if (!Objects.equals(attr.getDataType(), updatedAttr.getDataType()))
		{
			updateDataType(entityType, attr, updatedAttr);
		}

		// ref entity changes
		if (attr.getRefEntity() != null && updatedAttr.getRefEntity() != null && !attr.getRefEntity().getName()
				.equals(updatedAttr.getRefEntity().getName()))
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

			if (attr.getRefEntity().getIdAttribute().getDataType() != updatedAttr.getRefEntity().getIdAttribute()
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
							entityType.getName(), attr.getName(), attr.getRefEntity().getName(),
							updatedAttr.getRefEntity().getName(), updatedAttr.getDataType().toString()));
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
							entityType.getName(), attr.getName(), attr.getName()));
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
						format("ID attribute [%s] of entity [%s] must be unique", attr.getName(),
								entityType.getName()));
			}

			dropUniqueKey(entityType, updatedAttr);
		}
		else if (!attr.isUnique() && updatedAttr.isUnique())
		{
			createUniqueKey(entityType, updatedAttr);
		}
	}

	/**
	 * Return a new PostgreSQL repository
	 */
	private PostgreSqlRepository createPostgreSqlRepository()
	{
		return new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource);
	}

	private boolean isTableExists(EntityType entityType)
	{
		return isTableExists(getTableName(entityType, false));
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
			try
			{
				if (conn != null)
				{
					conn.close();
				}
			}
			catch (Exception e)
			{
				//noinspection ThrowFromFinallyBlock
				throw new RuntimeException(e);
			}
		}
	}

	private void updateNillable(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		if (attr.isNillable() && !updatedAttr.isNillable())
		{
			String sqlSetNotNull = getSqlSetNotNull(entityType, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating not null constraint for entity [{}] attribute [{}]", entityType.getName(),
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
								entityType.getName()));
			}

			String sqlDropNotNull = getSqlDropNotNull(entityType, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Removing not null constraint for entity [{}] attribute [{}]", entityType.getName(),
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
			LOG.debug("Creating table for entity [{}]", entityType.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createTableSql);
			}
		}
		jdbcTemplate.execute(createTableSql);

		// create junction tables for attributes referencing multiple entities
		createJunctionTables(entityType);
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
			LOG.debug("Creating foreign key for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
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
			LOG.debug("Dropping foreign key for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
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
			LOG.debug("Creating unique key for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
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
			LOG.debug("Dropping unique key for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
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
			LOG.debug("Creating check constraint for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
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
			LOG.debug("Dropping check constraint for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropCheckConstraint);
			}
		}
		jdbcTemplate.execute(sqlDropCheckConstraint);
	}

	private void createColumn(EntityType entityType, Attribute attr)
	{
		String addColumnSql = getSqlAddColumn(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating column for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", addColumnSql);
			}
		}
		jdbcTemplate.execute(addColumnSql);
		}

	private void dropColumn(EntityType entityType, Attribute attr)
	{
		String dropColumnSql = getSqlDropColumn(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping column for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
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
			LOG.debug("Changing data type of entity [{}] attribute [{}] to [{}]", entityType.getName(), attr.getName(),
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
			LOG.debug("Creating junction table for entity [{}] attribute [{}]", entityType.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createJunctionTableSql);
			}
		}
		jdbcTemplate.execute(createJunctionTableSql);

		String createJunctionTableIndexSql = getSqlCreateJunctionTableIndex(entityType, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating junction table index for entity [{}] attribute [{}]", entityType.getName(),
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
			LOG.debug("Dropping junction table for entity [{}] attribute [{}]", entityType.getName(),
					mrefAttr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropJunctionTable);
			}
		}
		jdbcTemplate.execute(sqlDropJunctionTable);
	}
}