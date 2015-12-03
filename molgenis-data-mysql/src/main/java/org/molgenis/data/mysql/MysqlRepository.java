package org.molgenis.data.mysql;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MysqlRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(MysqlRepository.class);
	public static final int BATCH_SIZE = 1000;
	private EntityMetaData metaData;
	private final JdbcTemplate jdbcTemplate;
	private final AsyncJdbcTemplate asyncJdbcTemplate;
	private final DataService dataService;
	private final MySqlEntityFactory mySqlEntityFactory;
	private final DataSource dataSource;
	private static final String VARCHAR = "VARCHAR(255)";

	/**
	 * Creates a new MysqlRepository.
	 *
	 * @param dataSource
	 *            the datasource to use to execute statements on the Mysql database
	 * @param asyncJdbcTemplate
	 *            {@link AsyncJdbcTemplate} to use to execute DDL statements in an isolated transaction on the Mysql
	 *            database
	 */
	public MysqlRepository(DataService dataService, MySqlEntityFactory mySqlEntityFactory, DataSource dataSource,
			AsyncJdbcTemplate asyncJdbcTemplate)
	{
		this.dataService = requireNonNull(dataService);
		this.mySqlEntityFactory = requireNonNull(mySqlEntityFactory);
		this.dataSource = requireNonNull(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.asyncJdbcTemplate = requireNonNull(asyncJdbcTemplate);
	}

	public void setMetaData(EntityMetaData metaData)
	{
		this.metaData = metaData;
	}

	@Override
	public void drop()
	{
		DataAccessException remembered = null;
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getDataType() instanceof MrefField)
			{
				DataAccessException e = tryExecute("DROP TABLE IF EXISTS `" + getTableName() + "_" + att.getName()
						+ "`");
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
			DataAccessException e = tryExecute(getDropSql());
			remembered = remembered != null ? remembered : e;
		}

		if (remembered != null)
		{
			throw remembered;
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
			asyncJdbcTemplate.execute(sql);
			return null;
		}
		catch (DataAccessException caught)
		{
			return caught;
		}
	}

	public void dropAttribute(String attributeName)
	{
		String sql = String.format("ALTER TABLE `%s` DROP COLUMN `%s`", getTableName(), attributeName);
		asyncJdbcTemplate.execute(sql);

		DefaultEntityMetaData demd = new DefaultEntityMetaData(metaData);
		demd.removeAttributeMetaData(demd.getAttribute(attributeName));
		setMetaData(demd);
	}

	protected String getDropSql()
	{
		return "DROP TABLE IF EXISTS `" + getTableName() + "`";
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
			asyncJdbcTemplate.execute(getCreateSql());

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
					asyncJdbcTemplate.execute(getMrefCreateSql(attr));
				}
				else if (attr.getDataType() instanceof XrefField)
				{
					String backend = dataService.getMeta().getBackend(attr.getRefEntity()).getName();
					if (backend.equalsIgnoreCase(MysqlRepositoryCollection.NAME))
					{
						asyncJdbcTemplate.execute(getCreateFKeySql(attr));
					}
				}

				// text can't be unique, so don't add unique constraint when type is string
				if (attr.isUnique() && !(attr.getDataType() instanceof StringField))
				{
					asyncJdbcTemplate.execute(getUniqueSql(attr));
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception creating MysqlRepository.", e);
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

	/**
	 * Adds an attribute to the repository. Will execute the alter table statement in a different thread so that the
	 * current transaction does not get committed.
	 *
	 * This is needed for adding columns during an import.
	 *
	 * @param attributeMetaData
	 *            the {@link AttributeMetaData} to add
	 */
	protected void addAttribute(AttributeMetaData attributeMetaData)
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
	protected void addAttributeSync(AttributeMetaData attributeMetaData)
	{
		addAttributeInternal(attributeMetaData, true, false);
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
				execute(getMrefCreateSql(attributeMetaData), async);
			}
			else if (!attributeMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				execute(getAlterSql(attributeMetaData), async);
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
			LOG.error("Exception updating MysqlRepository.", e);
			throw new MolgenisDataException(e);
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
	private void execute(String sql, boolean async)
	{
		if (async)
		{
			asyncJdbcTemplate.execute(sql);
		}
		else
		{
			jdbcTemplate.execute(sql);
		}
	}

	protected String getMrefCreateSql(AttributeMetaData att) throws MolgenisModelException
	{
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		StringBuilder sql = new StringBuilder();

		// mysql keys cannot have TEXT value, so change it to VARCHAR when needed
		String idAttrMysqlType = (idAttribute.getDataType() instanceof StringField ? VARCHAR : idAttribute
				.getDataType().getMysqlType());

		String refAttrMysqlType = (att.getRefEntity().getIdAttribute().getDataType() instanceof StringField ? VARCHAR : att
				.getRefEntity().getIdAttribute().getDataType().getMysqlType());

		sql.append(" CREATE TABLE ").append('`').append(getTableName()).append('_').append(att.getName()).append('`')
				.append("(`order` INT,`").append(idAttribute.getName()).append('`').append(' ').append(idAttrMysqlType)
				.append(" NOT NULL, ").append('`').append(att.getName()).append('`').append(' ')
				.append(refAttrMysqlType).append(" NOT NULL, FOREIGN KEY (").append('`').append(idAttribute.getName())
				.append('`').append(") REFERENCES ").append('`').append(getTableName()).append('`').append('(')
				.append('`').append(idAttribute.getName()).append("`) ON DELETE CASCADE");

		// If the refEntity is not of type MySQL do not add a foreign key to it
		String refEntityBackend = dataService.getMeta().getBackend(att.getRefEntity()).getName();
		if (refEntityBackend.equalsIgnoreCase(MysqlRepositoryCollection.NAME))
		{
			sql.append(", FOREIGN KEY (").append('`').append(att.getName()).append('`').append(") REFERENCES ")
					.append('`').append(getTableName(att.getRefEntity())).append('`').append('(').append('`')
					.append(att.getRefEntity().getIdAttribute().getName()).append("`) ON DELETE CASCADE");
		}

		sql.append(") ENGINE=InnoDB;");

		return sql.toString();
	}

	protected String getCreateSql() throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS ").append('`').append(getTableName()).append('`').append('(');

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

		if (idAttribute.getDataType() instanceof XrefField || idAttribute.getDataType() instanceof MrefField) throw new RuntimeException(
				"primary key(" + getTableName() + "." + idAttribute.getName() + ") cannot be XREF or MREF");

		if (idAttribute.isNillable() == true) throw new RuntimeException("idAttribute (" + getTableName() + "."
				+ idAttribute.getName() + ") should not be nillable");

		sql.append("PRIMARY KEY (").append('`').append(getEntityMetaData().getIdAttribute().getName()).append('`')
				.append(')');

		// close
		sql.append(") ENGINE=InnoDB;");

		if (LOG.isTraceEnabled())
		{
			LOG.trace("sql: " + sql);
		}

		return sql.toString();
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
			case IMAGE:
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
				if (att.isLabelAttribute())
				{
					throw new MolgenisDataException("Attribute [" + att.getName() + "] of entity [" + getName()
							+ "] is label attribute and of type [" + att.getDataType()
							+ "]. Label attributes cannot be of type xref, mref, categorical or compound.");
				}

				if (att.isLookupAttribute())
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
			sql.append('`').append(att.getName()).append('`').append(' ');
			// xref adopt type of the identifier of referenced entity
			if (att.getDataType() instanceof XrefField)
			{
				// mysql keys can not be of type TEXT, so don't adopt the field type of a referenced entity when it is
				// of fieldtype STRING
				if (att.getRefEntity().getIdAttribute().getDataType() instanceof StringField)
				{
					sql.append(VARCHAR);
				}
				else
				{
					sql.append(att.getRefEntity().getIdAttribute().getDataType().getMysqlType());
				}
			}
			else
			{
				if (att.isIdAtrribute() && att.getDataType() instanceof StringField)
				{
					// id attributes can not be of type TEXT so we'll change it to VARCHAR
					sql.append(VARCHAR);
				}
				else if (att.isUnique() && att.getDataType() instanceof StringField)
				{
					// mysql TEXT fields cannot be UNIQUE, so use VARCHAR instead
					sql.append(VARCHAR);
				}
				else
				{
					sql.append(att.getDataType().getMysqlType());
				}
			}
			// not null
			if (!att.isNillable() && !EntityUtils.doesExtend(metaData, "Questionnaire")
					&& (att.getVisibleExpression() == null))
			{
				sql.append(" NOT NULL");
			}
			// int + auto = auto_increment
			if (att.getDataType().equals(MolgenisFieldTypes.INT) && att.isAuto())
			{
				sql.append(" AUTO_INCREMENT");
			}
		}
	}

	public String getAlterSql(AttributeMetaData attributeMetaData) throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE ").append('`').append(getTableName()).append('`').append(" ADD ");
		getAttributeSql(sql, attributeMetaData);
		sql.append(";");
		return sql.toString();
	}

	protected String getCreateFKeySql(AttributeMetaData att)
	{
		return new StringBuilder().append("ALTER TABLE ").append('`').append(getTableName()).append('`')
				.append(" ADD FOREIGN KEY (").append('`').append(att.getName()).append('`').append(") REFERENCES ")
				.append('`').append(getTableName(att.getRefEntity())).append('`').append('(').append('`')
				.append(att.getRefEntity().getIdAttribute().getName()).append('`').append(")").toString();
	}

	protected String getUniqueSql(AttributeMetaData att)
	{
		return new StringBuilder().append("ALTER TABLE ").append('`').append(getTableName()).append('`')
				.append(" ADD CONSTRAINT ").append('`').append(att.getName()).append("_unique").append('`')
				.append(" UNIQUE (").append('`').append(att.getName()).append('`').append(")").toString();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	protected String getInsertSql()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append('`').append(getTableName()).append('`').append(" (");
		StringBuilder params = new StringBuilder();
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getExpression() != null)
			{
				continue;
			}
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append('`').append(att.getName()).append('`').append(", ");
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

	private void removeMrefs(final List<Object> ids, final AttributeMetaData att)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		StringBuilder mrefSql = new StringBuilder();
		mrefSql.append("DELETE FROM ").append('`').append(getTableName()).append('_').append(att.getName()).append('`')
				.append(" WHERE ").append('`').append(idAttribute.getName()).append('`').append("= ?");

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
		mrefSql.append("INSERT INTO ").append('`').append(getTableName()).append('_').append(att.getName()).append('`')
				.append(" (`order`,").append('`').append(idAttribute.getName()).append('`').append(',').append('`')
				.append(att.getName()).append('`').append(") VALUES (?,?,?)");

		jdbcTemplate.batchUpdate(mrefSql.toString(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				preparedStatement.setInt(1, i);

				preparedStatement.setObject(2, mrefs.get(i).get(idAttribute.getName()));

				Object value = mrefs.get(i).get(att.getName());
				if (value instanceof Entity)
				{
					preparedStatement.setObject(
							3,
							refEntityIdAttribute.getDataType().convert(
									((Entity) value).get(refEntityIdAttribute.getName())));
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

	protected String getSelectSql(Query q, List<Object> parameters)
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

					// TODO needed when autoids are used to join
					if (att.getDataType() instanceof MrefField)
					{
						select.append("GROUP_CONCAT(DISTINCT(").append('`').append(att.getName()).append('`')
								.append('.').append('`').append(att.getName()).append('`').append(") ORDER BY `")
								.append(att.getName()).append("`.`order`) AS ").append('`').append(att.getName())
								.append('`');
					}
					else
					{
						select.append("this.").append('`').append(att.getName()).append('`');
						if (group.length() > 0) group.append(", this.").append('`').append(att.getName()).append('`');
						else group.append("this.").append('`').append(att.getName()).append('`');
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
		if (select.indexOf("GROUP_CONCAT") != -1 && group.length() > 0) result.append(" GROUP BY ").append(group);
		// order by
		result.append(' ').append(getSortSql(q));
		// limit
		if (q.getPageSize() > 0) result.append(" LIMIT ").append(q.getPageSize());
		if (q.getOffset() > 0) result.append(" OFFSET ").append(q.getOffset());

		return result.toString().trim();
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return new BatchingQueryResult(BATCH_SIZE, q)
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

				RowMapper<Entity> entityMapper = mySqlEntityFactory.createRowMapper(getEntityMetaData(),
						batchQuery.getFetch(), jdbcTemplate, getTableName());
				return jdbcTemplate.query(sql, parameters.toArray(new Object[0]), entityMapper);
			}
		};
	}

	protected String getWhereSql(Query q, List<Object> parameters, int mrefFilterIndex)
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
							search.append(" OR this.").append('`').append(att.getName()).append('`').append(" LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");
						}
						else if (att.getDataType() instanceof XrefField)
						{

							Repository repo = dataService.getRepository(att.getRefEntity().getName());
							if (repo.getCapabilities().contains(QUERYABLE))
							{
								Query refQ = new QueryImpl().like(att.getRefEntity().getLabelAttribute().getName(), r
										.getValue().toString());
								Iterator<Entity> it = repo.findAll(refQ).iterator();
								if (it.hasNext())
								{
									search.append(" OR this.").append('`').append(att.getName()).append('`')
											.append(" IN (");
									while (it.hasNext())
									{
										Entity ref = it.next();
										search.append("?");
										parameters.add(att.getDataType().convert(
												ref.get(att.getRefEntity().getIdAttribute().getName())));
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
							search.append(" OR CAST(").append(att.getName()).append(".`").append(att.getName())
									.append('`').append(" as CHAR) LIKE ?");
							parameters.add("%" + DataConverter.toString(r.getValue()) + "%");

						}
						else
						{
							search.append(" OR CAST(this.").append('`').append(att.getName()).append('`')
									.append(" as CHAR) LIKE ?");
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
						result.append(" this.").append('`').append(attr.getName()).append('`').append(" LIKE ?");
					}
					else
					{
						result.append(" CAST(this.").append('`').append(attr.getName()).append('`')
								.append(" as CHAR) LIKE ?");
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

					if (attr.getDataType() instanceof MrefField) result.append(attr.getName()).append("_filter")
							.append(mrefFilterIndex);
					else result.append("this");

					result.append(".`").append(r.getField()).append("` IN (").append(in).append(')');
					break;
				default:
					// comparable values...
					FieldType type = attr.getDataType();
					if (type instanceof MrefField) predicate.append(attr.getName()).append("_filter")
							.append(mrefFilterIndex);
					else predicate.append("this");

					predicate.append(".`").append(r.getField()).append('`');

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
					parameters.add(attr.getDataType().convert(r.getValue()));

					if (result.length() > 0 && !result.toString().endsWith(" OR ")
							&& !result.toString().endsWith(" AND ")) result.append(" AND ");
					result.append(predicate);
			}
		}

		return result.toString().trim();
	}

	protected String getSortSql(Query q)
	{
		StringBuilder sortSql = new StringBuilder();
		if (q.getSort() != null)
		{
			for (Sort.Order o : q.getSort())
			{
				AttributeMetaData att = getEntityMetaData().getAttribute(o.getAttr());
				if (att.getDataType() instanceof MrefField) sortSql.append(", ").append(att.getName());
				else sortSql.append(", ").append('`').append(att.getName()).append('`');
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

	protected String getUpdateSql()
	{
		// use (readonly) identifier
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		// create sql
		StringBuilder sql = new StringBuilder("UPDATE ").append('`').append(getTableName()).append('`').append(" SET ");
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getExpression() != null)
			{
				// computed attributes are not persisted
				continue;
			}
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append('`').append(att.getName()).append('`').append(" = ?, ");
			}
		}
		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',')
		{
			sql.setLength(sql.length() - 2);
		}
		sql.append(" WHERE ").append('`').append(idAttribute.getName()).append('`').append("= ?");
		return sql.toString();
	}

	@Override
	public void delete(Entity entity)
	{
		this.delete(Arrays.asList(new Entity[]
		{ entity }));

	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// todo, split in subbatchs
		final List<Object> deleteByIdBatch = new ArrayList<Object>();

		this.resetXrefValuesBySelfReference(entities);

		for (Entity e : entities)
		{
			deleteByIdBatch.add(e.getIdValue());
		}
		this.deleteById(deleteByIdBatch);
	}

	/**
	 * Use before a delete action of a entity with XREF data type where the entity and refEntity are the same entities.
	 *
	 * @param entities
	 */
	private void resetXrefValuesBySelfReference(Iterable<? extends Entity> entities)
	{
		List<String> xrefAttributesWithSelfReference = new ArrayList<String>();
		for (AttributeMetaData attributeMetaData : getEntityMetaData().getAttributes())
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
		this.update(updateBatch);
	}

	public String getDeleteSql()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append('`').append(getTableName()).append('`').append(" WHERE ").append('`')
				.append(getEntityMetaData().getIdAttribute().getName()).append('`').append(" = ?");
		return sql.toString();
	}

	@Override
	public void deleteById(Object id)
	{
		this.deleteById(Arrays.asList(new Object[]
		{ id }));
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		final List<Object> idList = new ArrayList<Object>();
		for (Object id : ids)
		{
			idList.add(id);
		}

		jdbcTemplate.batchUpdate(getDeleteSql(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				preparedStatement.setObject(1, idList.get(i));
			}

			@Override
			public int getBatchSize()
			{
				return idList.size();
			}
		});
	}

	@Override
	public void deleteAll()
	{
		delete(this);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		if (entities == null) return 0;
		AtomicInteger count = new AtomicInteger(0);

		final List<Entity> batch = new ArrayList<Entity>();

		Iterator<? extends Entity> it = entities.iterator();
		while (it.hasNext())
		{
			batch.add(it.next());
			count.addAndGet(1);

			if ((batch.size() == BATCH_SIZE) || !it.hasNext())
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
									for (Entity val : batch.get(rowIndex).getEntities(att.getName()))
									{
										Map<String, Object> mref = new HashMap<>();
										mref.put(idAttribute.getName(), batch.get(rowIndex).get(idAttribute.getName()));
										mref.put(att.getName(), val.getIdValue());

										mrefs.get(att.getName()).add(mref);
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
									if (att.isIdAtrribute() && att.isAuto()
											&& (att.getDataType() instanceof StringField))
									{
										throw new MolgenisDataException(
												"Missing auto id value. Please use the 'AutoIdCrudRepositoryDecorator' to add auto id capabilities.");
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

									preparedStatement.setObject(fieldIndex++, att.getRefEntity().getIdAttribute()
											.getDataType().convert(value));
								}
								else
								{
									preparedStatement.setObject(fieldIndex++,
											att.getDataType().convert(batch.get(rowIndex).get(att.getName())));
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

	@Override
	public void add(Entity entity)
	{
		if (entity == null) throw new RuntimeException("MysqlRepository.add() failed: entity was null");
		add(Arrays.asList(new Entity[]
		{ entity }));
	}

	@Override
	public void update(Entity entity)
	{
		update(Arrays.asList(new Entity[]
		{ entity }));
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		// TODO, split in subbatches
		final List<Entity> batch = new ArrayList<Entity>();
		if (entities != null) for (Entity e : entities)
		{
			batch.add(e);
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
								int i = 0;
								for (Entity val : vals)
								{
									Map<String, Object> mref = new HashMap<>();
									mref.put("order", i++);
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
									preparedStatement.setObject(
											fieldIndex++,
											att.getRefEntity()
													.getIdAttribute()
													.getDataType()
													.convert(
															((Entity) value).get(att.getRefEntity().getIdAttribute()
																	.getName())));
								}
								else
								{
									preparedStatement.setObject(fieldIndex++, att.getRefEntity().getIdAttribute()
											.getDataType().convert(value));
								}
							}
							else
							{
								preparedStatement.setObject(fieldIndex++,
										att.getDataType().convert(e.get(att.getName())));
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

	public boolean tableExists()
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

	private String getTableName()
	{
		return getTableName(getEntityMetaData());
	}

	private String getTableName(EntityMetaData emd)
	{
		return emd.getName();
	}

	private boolean columnExists(String column)
	{
		Connection conn = null;
		try
		{
			conn = dataSource.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet columns = dbm.getColumns(null, null, getName(), column);
			return columns.next();
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

	/**
	 * Adds an attribute to the table for this entity. Looks up the type of the attribute in {@link #metaData}.
	 *
	 * @param attributeName
	 *            name of the attribute to add
	 */
	public void addAttributeToTable(String attributeName)
	{
		if (!columnExists(attributeName))
		{
			String sql;
			try
			{
				sql = getAlterSql(metaData.getAttribute(attributeName));
			}
			catch (MolgenisModelException e)
			{
				throw new RuntimeException(e);
			}

			asyncJdbcTemplate.execute(sql);
		}
	}

	/**
	 * Creates the table for this repository if it does not already exist.
	 *
	 * @return boolean indicating if the table was created
	 */
	public boolean createTableIfNotExists()
	{
		if (!tableExists())
		{
			create();
			return true;
		}
		return false;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(WRITABLE, MANAGABLE);
	}

	@Override
	public long count(Query q)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Fetching MySQL [{}] data for query [{}]", getName(), q);
		}

		List<Object> parameters = Lists.newArrayList();
		String sql = getCountSql(q, parameters);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("sql: {}, parameters: {}", sql, parameters);
		}

		return jdbcTemplate.queryForObject(sql, parameters.toArray(new Object[0]), Long.class);
	}

	protected String getFromSql(Query q)
	{
		StringBuilder from = new StringBuilder();
		from.append(" FROM ").append('`').append(getTableName()).append('`').append(" AS this");

		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		List<String> mrefQueryFields = Lists.newArrayList();
		getMrefQueryFields(q.getRules(), mrefQueryFields);

		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (q.getFetch() == null || q.getFetch().hasField(att.getName()))
			{
				if (att.getDataType() instanceof MrefField)
				{
					from.append(" LEFT JOIN ").append('`').append(getTableName()).append('_').append(att.getName())
							.append('`').append(" AS ").append('`').append(att.getName()).append('`')
							.append(" ON (this.").append('`').append(idAttribute.getName()).append('`').append(" = ")
							.append('`').append(att.getName()).append('`').append('.').append('`')
							.append(idAttribute.getName()).append('`').append(')');

				}
			}
		}

		for (int i = 0; i < mrefQueryFields.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData att = getEntityMetaData().getAttribute(mrefQueryFields.get(i));

			from.append(" LEFT JOIN ").append('`').append(getTableName()).append('_').append(att.getName()).append('`')
					.append(" AS ").append('`').append(att.getName()).append("_filter").append(i + 1)
					.append("` ON (this.").append('`').append(idAttribute.getName()).append('`').append(" = ")
					.append('`').append(att.getName()).append("_filter").append(i + 1).append("`.").append('`')
					.append(idAttribute.getName()).append('`').append(')');
		}

		return from.toString();
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

	protected String getCountSql(Query q, List<Object> parameters)
	{
		String where = getWhereSql(q, parameters, 0);
		String from = getFromSql(q);
		String idAttribute = getEntityMetaData().getIdAttribute().getName();

		if (where.length() > 0) return new StringBuilder("SELECT COUNT(DISTINCT this.").append('`').append(idAttribute)
				.append('`').append(')').append(from).append(" WHERE ").append(where).toString();

		return new StringBuilder("SELECT COUNT(DISTINCT this.").append('`').append(idAttribute).append('`').append(')')
				.append(from).toString();
	}

}
