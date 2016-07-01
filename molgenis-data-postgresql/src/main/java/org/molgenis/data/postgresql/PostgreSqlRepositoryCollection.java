package org.molgenis.data.postgresql;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AbstractRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static autovalue.shaded.com.google.common.common.collect.Sets.immutableEnumSet;
import static java.lang.String.format;
import static java.util.EnumSet.of;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.RepositoryCollectionCapability.*;
import static org.molgenis.data.i18n.LanguageMetaData.*;
import static org.molgenis.data.meta.MetaUtils.getEntityMetaDataFetch;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
import static org.molgenis.data.support.EntityMetaDataUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityMetaDataUtils.isSingleReferenceType;

public abstract class PostgreSqlRepositoryCollection extends AbstractRepositoryCollection
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlRepositoryCollection.class);

	public static final String POSTGRESQL = "PostgreSQL";

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	private final DataService dataService;

	public PostgreSqlRepositoryCollection(DataSource dataSource, JdbcTemplate jdbcTemplate, DataService dataService)
	{
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
		return immutableEnumSet(of(WRITABLE, UPDATABLE, META_DATA_PERSISTABLE, CACHEABLE));
	}

	@Override
	public Stream<String> getLanguageCodes()
	{
		if (isTableExists(LANGUAGE))
		{
			String sql = new StringBuilder("SELECT \"").append(CODE).append("\" FROM \"").append(LANGUAGE).append('"')
					.toString();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Fetching languages");
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", sql);
				}
			}
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				return rs.getString(CODE);
			}).stream();
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

		getPersistedAttributesMref(entityMeta).forEach(mrefAttr -> {
			String sqlDropJunctionTable = getSqlDropJunctionTable(entityMeta, mrefAttr);
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

		String sqlDropTable = getSqlDropTable(entityMeta);
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

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		if (entityMetaData == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityName));
		}
		addAttributeRec(entityMetaData, attribute);
	}

	/**
	 * Adds an attribute to the repository.
	 *
	 * @param entityMetaData
	 * @param attr           the {@link AttributeMetaData} to add
	 */
	private void addAttributeRec(EntityMetaData entityMetaData, AttributeMetaData attr)
	{
		// FIXME code duplication with create table
		if (attr.getExpression() != null)
		{
			// computed attributes are not persisted
			return;
		}
		if (isMultipleReferenceType(attr))
		{
			String createJunctionTableSql = getSqlCreateJunctionTable(entityMetaData, attr);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating junction table for entity [{}] attribute [{}]", getName(), attr.getName());
				if (LOG.isTraceEnabled())
				{
					LOG.trace("SQL: {}", createJunctionTableSql);
				}
			}
			jdbcTemplate.execute(createJunctionTableSql);
		}
		else if (attr.getDataType() != COMPOUND)
		{
			String addColumnSql = getSqlAddColumn(entityMetaData, attr);
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

		// FIXME Code duplication
		if (isSingleReferenceType(attr) && isPersistedInPostgreSql(attr.getRefEntity()))
		{
			String createForeignKeySql = getSqlCreateForeignKey(entityMetaData, attr);
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

		String idAttrName = entityMetaData.getIdAttribute().getName();
		if (attr.isUnique() && !attr.getName().equals(idAttrName))
		{
			String createUniqueKeySql = getSqlCreateUniqueKey(entityMetaData, attr);
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

		if (attr.getDataType() == COMPOUND)
		{
			for (AttributeMetaData attrPart : attr.getAttributeParts())
			{
				addAttributeRec(entityMetaData, attrPart);
			}
		}
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		// FIXME updating attributes in abstract entities
		// nullable changes
		if (!Objects.equals(attr.isNillable(), updatedAttr.isNillable()))
		{
			updateNillable(entityMetaData, attr, updatedAttr);
		}

		// unique changes
		if (!Objects.equals(attr.isUnique(), updatedAttr.isUnique()))
		{
			updateUnique(entityMetaData, attr, updatedAttr);
		}

		// data type changes
		if (!Objects.equals(attr.getDataType(), updatedAttr.getDataType()))
		{
			updateDataType(entityMetaData, attr, updatedAttr);
		}
	}

	private void updateDataType(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (entityMetaData.getIdAttribute().getName().equals(attr.getName()))
		{
			throw new MolgenisDataException(
					format("Data type of entity [%s] attribute [%s] cannot be modified, because [%s] is an ID attribute.",
							entityMetaData.getName(), attr.getName(), attr.getName()));
		}

		String sqlSetDataType = getSqlSetDataType(entityMetaData, updatedAttr);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Changing data type of entity [{}] attribute [{}] from [{}] to [{}]", entityMetaData.getName(),
					attr.getName(), attr.getDataType().toString(), updatedAttr.getDataType().toString());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", sqlSetDataType);
			}
		}
		jdbcTemplate.execute(sqlSetDataType);
	}

	private void updateUnique(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		if (attr.isUnique() && !updatedAttr.isUnique())
		{
			if (entityMetaData.getIdAttribute().getName().equals(attr.getName()))
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
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		if (entityMetaData == null)
		{
			throw new UnknownEntityException(format("Unknown entity [%s]", entityName));
		}
		deleteAttribute(entityMetaData, attributeName);
	}

	private void deleteAttribute(EntityMetaData entityMetaData, String attrName)
	{
		String dropColumnSql = getSqlDropColumn(entityMetaData, attrName);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Dropping column for entity [{}] attribute [{}]", getName(), attrName);
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", dropColumnSql);
			}
		}
		jdbcTemplate.execute(dropColumnSql);
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract PostgreSqlRepository createPostgreSqlRepository();

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
			if (entityMetaData.getIdAttribute().getName().equals(attr.getName()))
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
			LOG.debug("Creating table for entity [{}]", getName());
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SQL: {}", createTableSql);
			}
		}
		jdbcTemplate.execute(createTableSql);

		String idAttrName = entityMeta.getIdAttribute().getName();
		getPersistedAttributes(entityMeta).forEach(attr -> {
			// add mref tables
			if (isMultipleReferenceType(attr))
			{
				String createJunctionTableSql = getSqlCreateJunctionTable(entityMeta, attr);
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Creating junction table for entity [{}] attribute [{}]", getName(), attr.getName());
					if (LOG.isTraceEnabled())
					{
						LOG.trace("SQL: {}", createJunctionTableSql);
					}
				}
				jdbcTemplate.execute(createJunctionTableSql);
			}
			// FIXME Code duplication
			else if (isSingleReferenceType(attr) && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				String createForeignKeySql = getSqlCreateForeignKey(entityMeta, attr);
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
				String createUniqueSql = getSqlCreateUniqueKey(entityMeta, attr);
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
}