package org.molgenis.data.postgresql;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
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
import java.util.stream.Stream;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.lang.String.format;
import static java.util.EnumSet.of;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.RepositoryCollectionCapability.*;
import static org.molgenis.data.i18n.model.LanguageMetaData.*;
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.getJunctionTableAttributes;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.getTableName;
import static org.molgenis.data.support.EntityMetaDataUtils.*;

public class PostgreSqlRepositoryCollection extends AbstractRepositoryCollection
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepositoryCollection.class);

	public static final String POSTGRESQL = "PostgreSQL";

	private final PostgreSqlEntityFactory postgreSqlEntityFactory;
	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	private final DataService dataService;
	private final PlatformTransactionManager transactionManager;

	PostgreSqlRepositoryCollection(PostgreSqlEntityFactory postgreSqlEntityFactory, DataSource dataSource,
			JdbcTemplate jdbcTemplate, DataService dataService, PlatformTransactionManager transactionManager)
	{
		this.postgreSqlEntityFactory = requireNonNull(postgreSqlEntityFactory);
		this.dataSource = requireNonNull(dataSource);
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
		this.dataService = requireNonNull(dataService);
		this.transactionManager = requireNonNull(transactionManager);
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
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		PostgreSqlRepository repository = createPostgreSqlRepository();
		repository.setMetaData(entityMeta);
		if (!isTableExists(entityMeta))
		{
			createTable(entityMeta);
		}
		return repository;
	}

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return isTableExists(entityMeta);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(BACKEND, POSTGRESQL)
				.fetch(getEntityMetaDataFetch()).findAll().map(EntityMetaData::getName)::iterator;
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		EntityMetaData entityMeta = dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(BACKEND, POSTGRESQL)
				.and().eq(FULL_NAME, name).and().eq(ABSTRACT, false).fetch(getEntityMetaDataFetch()).findOne();
		return getRepository(entityMeta);
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMeta)
	{
		PostgreSqlRepository repository = createPostgreSqlRepository();
		repository.setMetaData(entityMeta);
		return repository;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(BACKEND, POSTGRESQL).and()
				.eq(ABSTRACT, false).fetch(getEntityMetaDataFetch()).findAll().map(this::getRepository).iterator();
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		if (entityMeta.isAbstract())
		{
			throw new UnknownRepositoryException(entityMeta.getName());
		}

		getJunctionTableAttributes(entityMeta).forEach(mrefAttr -> dropJunctionTable(entityMeta, mrefAttr));

		String sqlDropTable = getSqlDropTable(entityMeta);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping table for entity [{}]", entityMeta.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropTable);
			}
		}
		jdbcTemplate.execute(sqlDropTable);
	}

	@Override
	public void addAttribute(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		addAttributeRec(entityMeta, attr, true);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (entityMeta.getAttribute(attr.getName()) == null)
		{
			throw new UnknownAttributeException(format("Unknown attribute [%s]", attr.getName()));
		}
		updateAttributeRec(entityMeta, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		if (entityMeta.getAttribute(attr.getName()) == null)
		{
			throw new UnknownAttributeException(format("Unknown attribute [%s]", attr.getName()));
		}
		deleteAttributeRec(entityMeta, attr);
	}

	/**
	 * Recursively add attribute to entity and entities extending from this entity.
	 *
	 * @param entityMeta      entity meta data
	 * @param attr            attribute to add
	 * @param checkAttrExists whether or not to perform a check if the attribute exists for the given entity
	 */
	private void addAttributeRec(EntityMetaData entityMeta, AttributeMetaData attr, boolean checkAttrExists)
	{
		if (attr.getExpression() != null || attr.getDataType() == COMPOUND)
		{
			// computed attributes and compound attributes are not persisted
			return;
		}

		if (checkAttrExists && entityMeta.getAttribute(attr.getName()) != null)
		{
			throw new MolgenisDataException(
					format("Adding attribute operation failed. Attribute already exists [%s]", attr.getName()));
		}

		if (entityMeta.isAbstract())
		{
			// for abstract entities recursively update entities extending the abstract entity
			dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(EXTENDS, entityMeta).findAll()
					.forEach(childEntityMeta -> addAttributeRec(childEntityMeta, attr, checkAttrExists));
		}
		else
		{
			boolean bidirectionalOneToMany = attr.getDataType() == ONE_TO_MANY && attr.isMappedBy();
			if (isMultipleReferenceType(attr) && !bidirectionalOneToMany)
			{
				createJunctionTable(entityMeta, attr);
			}
			else
			{
				createColumn(entityMeta, attr);
			}
		}
	}

	/**
	 * Recursively update attribute of entity and entities extending from this entity.
	 *
	 * @param entityMeta  entity meta data
	 * @param attr        existing attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateAttributeRec(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if ((attr.getExpression() != null && updatedAttr.getExpression() != null) || (attr.getDataType() == COMPOUND
				&& updatedAttr.getDataType() == COMPOUND))
		{
			return;
		}

		if (entityMeta.isAbstract())
		{
			// for abstract entities recursively update entities extending the abstract entity
			dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(EXTENDS, entityMeta).findAll()
					.forEach(childEntityMeta -> updateAttributeRec(childEntityMeta, attr, updatedAttr));
		}
		else
		{
			if ((attr.getExpression() == null && updatedAttr.getExpression() != null) || (attr.getDataType() != COMPOUND
					&& updatedAttr.getDataType() == COMPOUND))
			{
				// computed attributes and compound attributes are not persisted
				deleteAttribute(entityMeta, attr);
			}
			else if ((attr.getExpression() != null && updatedAttr.getExpression() == null) || (
					attr.getDataType() == COMPOUND && updatedAttr.getDataType() != COMPOUND))
			{
				// computed attributes and compound attributes are not persisted
				addAttributeRec(entityMeta, updatedAttr, false);
			}
			else
			{
				updateColumn(entityMeta, attr, updatedAttr);
			}
		}
	}

	/**
	 * Updates database column based on attribute changes.
	 *
	 * @param entityMeta  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateColumn(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		// nullable changes
		if (!Objects.equals(attr.isNillable(), updatedAttr.isNillable()))
		{
			updateNillable(entityMeta, attr, updatedAttr);
		}

		// unique changes
		if (!Objects.equals(attr.isUnique(), updatedAttr.isUnique()))
		{
			updateUnique(entityMeta, attr, updatedAttr);
		}

		// data type changes
		if (!Objects.equals(attr.getDataType(), updatedAttr.getDataType()))
		{
			updateDataType(entityMeta, attr, updatedAttr);
		}

		// ref entity changes
		if (attr.getRefEntity() != null && updatedAttr.getRefEntity() != null && !attr.getRefEntity().getName()
				.equals(updatedAttr.getRefEntity().getName()))
		{
			updateRefEntity(entityMeta, attr, updatedAttr);
		}

		// enum option changes
		if (!Objects.equals(attr.getEnumOptions(), updatedAttr.getEnumOptions()))
		{
			updateEnumOptions(entityMeta, attr, updatedAttr);
		}

		// orderBy change
		if (!Objects.equals(attr.getOrderBy(), updatedAttr.getOrderBy()))
		{
			updateOrderBy(attr, updatedAttr);
		}
	}

	/**
	 * Creates/removes order column associated with a one-to-many attribute.
	 *
	 * @param attr        one-to-many attribute
	 * @param updatedAttr updated one-to-many attribute
	 */
	private void updateOrderBy(AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.getOrderBy() == null && updatedAttr.getOrderBy() != null)
		{
			// remove order column
			dropColumn(attr.getRefEntity(), attr);
		}
		else if (attr.getOrderBy() != null && updatedAttr.getOrderBy() == null)
		{
			// create order column
			createColumn(updatedAttr.getRefEntity(), updatedAttr);
		}
	}

	/**
	 * Updates foreign keys based on referenced entity changes.
	 *
	 * @param entityMeta  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateRefEntity(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (isSingleReferenceType(attr) && isSingleReferenceType(updatedAttr))
		{
			dropForeignKey(entityMeta, attr);

			if (attr.getRefEntity().getIdAttribute().getDataType() != updatedAttr.getRefEntity().getIdAttribute()
					.getDataType())
			{
				updateColumnDataType(entityMeta, updatedAttr);
			}

			createForeignKey(entityMeta, updatedAttr);
		}
		else if (isMultipleReferenceType(attr) && isMultipleReferenceType(updatedAttr))
		{
			throw new MolgenisDataException(
					format("Updating entity [%s] attribute [%s] referenced entity from [%s] to [%s] not allowed for type [%s]",
							entityMeta.getName(), attr.getName(), attr.getRefEntity().getName(),
							updatedAttr.getRefEntity().getName(), updatedAttr.getDataType().toString()));
		}
	}

	/**
	 * Updates check constraint based on enum value changes.
	 *
	 * @param entityMeta  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateEnumOptions(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.getDataType() == ENUM)
		{
			if (updatedAttr.getDataType() == ENUM)
			{
				// update check constraint
				dropCheckConstraint(entityMeta, attr);
				createCheckConstraint(entityMeta, updatedAttr);
			}
			else
			{
				// drop check constraint
				dropCheckConstraint(entityMeta, attr);
			}
		}
		else
		{
			if (updatedAttr.getDataType() == ENUM)
			{
				createCheckConstraint(entityMeta, updatedAttr);
			}
		}
	}

	/**
	 * Updates column data type and foreign key constraints based on data type update.
	 *
	 * @param entityMeta  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateDataType(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		AttributeMetaData idAttr = entityMeta.getIdAttribute();
		if (idAttr != null && idAttr.getName().equals(attr.getName()))
		{
			throw new MolgenisDataException(
					format("Data type of entity [%s] attribute [%s] cannot be modified, because [%s] is an ID attribute.",
							entityMeta.getName(), attr.getName(), attr.getName()));
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
			dropForeignKey(entityMeta, attr);
		}

		updateColumnDataType(entityMeta, updatedAttr);

		// add foreign key on data type updates such as STRING --> XREF
		if (!isReferenceType(attr) && isSingleReferenceType(updatedAttr))
		{
			createForeignKey(entityMeta, updatedAttr);
		}
	}

	/**
	 * Updates unique constraint based on attribute unique changes.
	 *
	 * @param entityMeta  entity meta data
	 * @param attr        current attribute
	 * @param updatedAttr updated attribute
	 */
	private void updateUnique(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.isUnique() && !updatedAttr.isUnique())
		{
			AttributeMetaData idAttr = entityMeta.getIdAttribute();
			if (idAttr != null && idAttr.getName().equals(attr.getName()))
			{
				throw new MolgenisDataException(
						format("ID attribute [%s] of entity [%s] must be unique", attr.getName(),
								entityMeta.getName()));
			}

			dropUniqueKey(entityMeta, updatedAttr);
		}
		else if (!attr.isUnique() && updatedAttr.isUnique())
		{
			createUniqueKey(entityMeta, updatedAttr);
		}
	}

	/**
	 * Recursively delete attribute in entity and entities extending from this entity.
	 *
	 * @param entityMeta entity meta data
	 * @param attr       attribute to delete
	 */
	private void deleteAttributeRec(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		if (attr.getExpression() != null || attr.getDataType() == COMPOUND)
		{
			// computed attributes and compound attributes are not persisted
			return;
		}

		if (entityMeta.isAbstract())
		{
			// for abstract entities recursively update entities extending the abstract entity
			dataService.query(ENTITY_META_DATA, EntityMetaData.class).eq(EXTENDS, entityMeta).findAll()
					.forEach(childEntityMeta -> deleteAttributeRec(childEntityMeta, attr));
		}
		else
		{
			boolean bidirectionalOneToMany = attr.getDataType() == ONE_TO_MANY && attr.isMappedBy();
			if (isMultipleReferenceType(attr) && !bidirectionalOneToMany)
			{
				dropJunctionTable(entityMeta, attr);
			}
			else
			{
				dropColumn(entityMeta, attr);
			}
		}
	}

	/**
	 * Return a new PostgreSQL repository
	 */
	private PostgreSqlRepository createPostgreSqlRepository()
	{
		return new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource, transactionManager);
	}

	private boolean isTableExists(EntityMetaData entityMeta)
	{
		return isTableExists(getTableName(entityMeta, false));
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

	private void updateNillable(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.isNillable() && !updatedAttr.isNillable())
		{
			String sqlSetNotNull = getSqlSetNotNull(entityMeta, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating not null constraint for entity [{}] attribute [{}]", entityMeta.getName(),
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
			AttributeMetaData idAttr = entityMeta.getIdAttribute();
			if (idAttr != null && idAttr.getName().equals(attr.getName()))
			{
				throw new MolgenisDataException(
						format("ID attribute [%s] of entity [%s] cannot be nullable", attr.getName(),
								entityMeta.getName()));
			}

			String sqlDropNotNull = getSqlDropNotNull(entityMeta, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Removing not null constraint for entity [{}] attribute [{}]", entityMeta.getName(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlDropNotNull);
				}
			}
			jdbcTemplate.execute(sqlDropNotNull);
		}
	}

	// @Transactional FIXME enable when bootstrapping transaction issue has been resolved
	private void createTable(EntityMetaData entityMeta)
	{
		// create table
		String createTableSql = getSqlCreateTable(entityMeta);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating table for entity [{}]", entityMeta.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createTableSql);
			}
		}
		jdbcTemplate.execute(createTableSql);

		// create junction tables for attributes referencing multiple entities
		createJunctionTables(entityMeta);
	}

	private void createJunctionTables(EntityMetaData entityMeta)
	{
		getJunctionTableAttributes(entityMeta).filter(attr -> !attr.isInversedBy())
				.forEach(attr -> createJunctionTable(entityMeta, attr));
		getJunctionTableAttributes(entityMeta).filter(AttributeMetaData::isInversedBy)
				.forEach(attr -> createJunctionTable(entityMeta, attr));
	}

	private void createForeignKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String createForeignKeySql = getSqlCreateForeignKey(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating foreign key for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createForeignKeySql);
			}
		}
		jdbcTemplate.execute(createForeignKeySql);
	}

	private void dropForeignKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String dropForeignKeySql = getSqlDropForeignKey(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping foreign key for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropForeignKeySql);
			}
		}
		jdbcTemplate.execute(dropForeignKeySql);
	}

	private void createUniqueKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String createUniqueKeySql = getSqlCreateUniqueKey(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating unique key for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createUniqueKeySql);
			}
		}
		jdbcTemplate.execute(createUniqueKeySql);
	}

	private void dropUniqueKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String dropUniqueKeySql = getSqlDropUniqueKey(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping unique key for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropUniqueKeySql);
			}
		}
		jdbcTemplate.execute(dropUniqueKeySql);
	}

	private void createCheckConstraint(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String sqlCreateCheckConstraint = getSqlCreateCheckConstraint(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating check constraint for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlCreateCheckConstraint);
			}
		}
		jdbcTemplate.execute(sqlCreateCheckConstraint);
	}

	private void dropCheckConstraint(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String sqlDropCheckConstraint = getSqlDropCheckConstraint(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping check constraint for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropCheckConstraint);
			}
		}
		jdbcTemplate.execute(sqlDropCheckConstraint);
	}

	private void createColumn(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String addColumnSql = getSqlAddColumn(entityMeta, attr);
		if (addColumnSql != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating column for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", addColumnSql);
				}
			}
			jdbcTemplate.execute(addColumnSql);
		}
	}

	private void dropColumn(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String dropColumnSql = getSqlDropColumn(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping column for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropColumnSql);
			}
		}
		jdbcTemplate.execute(dropColumnSql);
	}

	private void updateColumnDataType(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String sqlSetDataType = getSqlSetDataType(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Changing data type of entity [{}] attribute [{}] to [{}]", entityMeta.getName(), attr.getName(),
					attr.getDataType().toString());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlSetDataType);
			}
		}
		jdbcTemplate.execute(sqlSetDataType);
	}

	private void createJunctionTable(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		if (attr.isInversedBy())
		{
			createForeignKey(attr.getRefEntity(), attr.getInversedBy());
		}
		else
		{
			String createJunctionTableSql = getSqlCreateJunctionTable(entityMeta, attr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating junction table for entity [{}] attribute [{}]", entityMeta.getName(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", createJunctionTableSql);
				}
			}
			jdbcTemplate.execute(createJunctionTableSql);

			String createJunctionTableIndexSql = getSqlCreateJunctionTableIndex(entityMeta, attr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating junction table index for entity [{}] attribute [{}]", entityMeta.getName(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", createJunctionTableIndexSql);
				}
			}
			jdbcTemplate.execute(createJunctionTableIndexSql);
		}
	}

	private void dropJunctionTable(EntityMetaData entityMeta, AttributeMetaData mrefAttr)
	{
		String sqlDropJunctionTable = getSqlDropJunctionTable(entityMeta, mrefAttr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping junction table for entity [{}] attribute [{}]", entityMeta.getName(),
					mrefAttr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlDropJunctionTable);
			}
		}
		jdbcTemplate.execute(sqlDropJunctionTable);
	}
}