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
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.RepositoryCollectionCapability.*;
import static org.molgenis.data.i18n.model.LanguageMetaData.*;
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
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
	public Stream<String> getLanguageCodes()
	{
		if (isTableExists(LANGUAGE))
		{
			String sql = "SELECT \"" + CODE + "\" FROM \"" + LANGUAGE + '"';
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Fetching languages");
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sql);
				}
			}
			return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(CODE)).stream();
		}
		else
		{
			return Stream.of(DEFAULT_LANGUAGE_CODE);
		}
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		PostgreSqlRepository repository = createPostgreSqlRepository();
		repository.setMetaData(entityMeta);
		if (!isTableExists(entityMeta))
		{
			create(entityMeta);
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
		EntityMetaData entityMetaData = dataService.query(ENTITY_META_DATA, EntityMetaData.class)
				.eq(BACKEND, POSTGRESQL).and().eq(FULL_NAME, name).and().eq(ABSTRACT, false)
				.fetch(getEntityMetaDataFetch()).findOne();
		return getRepository(entityMetaData);
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

		getPersistedAttributesMref(entityMeta).forEach(mrefAttr ->
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
		});

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
			if (isMultipleReferenceType(attr))
			{
				createJunctionTable(entityMeta, attr);
			}
			else if (attr.getDataType() != COMPOUND)
			{
				createColumn(entityMeta, attr);
			}

			if (isSingleReferenceType(attr) && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				createForeignKey(entityMeta, attr);
			}

			String idAttrName = entityMeta.getIdAttribute().getName();
			if (attr.isUnique() && !attr.getName().equals(idAttrName))
			{
				createUniqueKey(entityMeta, attr);
			}
		}
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
			}
		}
	}

	private void updateDataType(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		AttributeMetaData idAttr = entityMeta.getIdAttribute();
		if (idAttr != null && idAttr.getName().equals(attr.getName()))
		{
			throw new MolgenisDataException(
					format("Data type of entity [%s] attribute [%s] cannot be modified, because [%s] is an ID attribute.",
							entityMeta.getName(), attr.getName(), attr.getName()));
		}

		// remove foreign key on data type updates such as XREF --> STRING
		if (isSingleReferenceType(attr) && !isReferenceType(updatedAttr))
		{
			String sqlDropForeignKey = PostgreSqlQueryGenerator.getSqlDropForeignKey(entityMeta, attr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Dropping foreign key for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlDropForeignKey);
				}
			}
			jdbcTemplate.execute(sqlDropForeignKey);
		}

		String sqlSetDataType = getSqlSetDataType(entityMeta, updatedAttr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Changing data type of entity [{}] attribute [{}] from [{}] to [{}]", entityMeta.getName(),
					attr.getName(), attr.getDataType().toString(), updatedAttr.getDataType().toString());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlSetDataType);
			}
		}
		jdbcTemplate.execute(sqlSetDataType);

		// add foreign key on data type updates such as STRING --> XREF
		if (!isReferenceType(attr) && isSingleReferenceType(updatedAttr))
		{
			createForeignKey(entityMeta, updatedAttr);
		}
	}

	private void updateUnique(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.isUnique() && !updatedAttr.isUnique())
		{
			AttributeMetaData idAttr = entityMetaData.getIdAttribute();
			if (idAttr != null && idAttr.getName().equals(attr.getName()))
			{
				throw new MolgenisDataException(
						format("ID attribute [%s] of entity [%s] must be unique", attr.getName(),
								entityMetaData.getName()));
			}

			String sqlDropUniqueKey = getSqlDropUniqueKey(entityMetaData, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Removing unique constraint for entity [{}] attribute [{}]", entityMetaData.getName(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlDropUniqueKey);
				}
			}
			jdbcTemplate.execute(sqlDropUniqueKey);
		}
		else if (!attr.isUnique() && updatedAttr.isUnique())
		{
			String sqlCreateUniqueKey = getSqlCreateUniqueKey(entityMetaData, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating unique constraint for entity [{}] attribute [{}]", entityMetaData.getName(),
						attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sqlCreateUniqueKey);
				}
			}
			jdbcTemplate.execute(sqlCreateUniqueKey);
		}
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

	private void updateNillable(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.isNillable() && !updatedAttr.isNillable())
		{
			String sqlSetNotNull = getSqlSetNotNull(entityMetaData, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating not null constraint for entity [{}] attribute [{}]", entityMetaData.getName(),
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
			AttributeMetaData idAttr = entityMetaData.getIdAttribute();
			if (idAttr != null && idAttr.getName().equals(attr.getName()))
			{
				throw new MolgenisDataException(
						format("ID attribute [%s] of entity [%s] cannot be nullable", attr.getName(),
								entityMetaData.getName()));
			}

			String sqlDropNotNull = getSqlDropNotNull(entityMetaData, updatedAttr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Removing not null constraint for entity [{}] attribute [{}]", entityMetaData.getName(),
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
	private void create(EntityMetaData entityMeta)
	{
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

		String idAttrName = entityMeta.getIdAttribute().getName();
		getPersistedAttributes(entityMeta).forEach(attr ->
		{
			// add mref tables
			if (isMultipleReferenceType(attr))
			{
				createJunctionTable(entityMeta, attr);
			}
			else if (isSingleReferenceType(attr) && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				createForeignKey(entityMeta, attr);
			}

			if (attr.isUnique() && !attr.getName().equals(idAttrName))
			{
				createUniqueKey(entityMeta, attr);
			}
		});
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

	private void createColumn(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String addColumnSql = getSqlAddColumn(entityMeta, attr);
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

	private void createJunctionTable(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String createJunctionTableSql = getSqlCreateJunctionTable(entityMeta, attr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Creating junction table for entity [{}] attribute [{}]", entityMeta.getName(), attr.getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createJunctionTableSql);
			}
		}
		jdbcTemplate.execute(createJunctionTableSql);
	}
}