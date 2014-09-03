package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Manageable;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.AbstractAggregateableCrudRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MysqlRepository extends AbstractAggregateableCrudRepository implements Manageable
{
	public static final String URL_PREFIX = "mysql://";
	public static final int BATCH_SIZE = 100000;
	private static final Logger logger = Logger.getLogger(MysqlRepository.class);
	private EntityMetaData metaData;
	private final JdbcTemplate jdbcTemplate;
	private RepositoryCollection repositoryCollection;

	public MysqlRepository(DataSource dataSource, EntityValidator entityValidator)
	{
		super(null, entityValidator);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setMetaData(EntityMetaData metaData)
	{
		this.metaData = metaData;
	}

	public void setRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		this.repositoryCollection = repositoryCollection;
	}

	@Override
	public void drop()
	{
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getDataType() instanceof MrefField)
			{
				jdbcTemplate.execute("DROP TABLE IF EXISTS " + getEntityMetaData().getName() + "_" + att.getName());
			}
		}
		jdbcTemplate.execute(getDropSql());
	}

	public void dropAttribute(String attributeName)
	{
		String sql = String.format("ALTER TABLE %s DROP COLUMN %s", getName(), attributeName);
		jdbcTemplate.execute(sql);

	}

	protected String getDropSql()
	{
		return "DROP TABLE IF EXISTS " + getEntityMetaData().getName();
	}

	@Override
	public void create()
	{
		try
		{
			jdbcTemplate.execute(getCreateSql());
			for (String fkeySql : getCreateFKeySql())
				jdbcTemplate.execute(fkeySql);

			// add mref tables
			for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			{
				if (att.getDataType() instanceof MrefField)
				{
					jdbcTemplate.execute(getMrefCreateSql(att));
				}
			}

		}
		catch (Exception e)
		{
			logger.error("Exception creating MysqlRepository.", e);
			throw new MolgenisDataException(e);
		}
	}

	public void addAttribute(AttributeMetaData attributeMetaData)
	{
		try
		{
			if (attributeMetaData.getDataType() instanceof MrefField)
			{
				jdbcTemplate.execute(getMrefCreateSql(attributeMetaData));
			}
			else
			{
				jdbcTemplate.execute(getAlterSql(attributeMetaData));
			}
		}
		catch (Exception e)
		{
			logger.error("Exception updating MysqlRepository.", e);
			throw new MolgenisDataException(e);
		}
	}

	protected String getMrefCreateSql(AttributeMetaData att) throws MolgenisModelException
	{
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		StringBuilder sql = new StringBuilder();
		sql.append(" CREATE TABLE ").append('`').append(getEntityMetaData().getName()).append('_')
				.append(att.getName()).append('`').append('(').append('`').append(idAttribute.getName()).append('`')
				.append(' ').append(idAttribute.getDataType().getMysqlType()).append(" NOT NULL, ").append('`')
				.append(att.getName()).append('`').append(' ')
				.append(att.getRefEntity().getIdAttribute().getDataType().getMysqlType())
				.append(" NOT NULL, FOREIGN KEY (").append('`').append(idAttribute.getName()).append('`')
				.append(") REFERENCES ").append('`').append(getEntityMetaData().getName()).append('`').append('(')
				.append('`').append(idAttribute.getName()).append('`').append(") ON DELETE CASCADE, FOREIGN KEY (")
				.append('`').append(att.getName()).append('`').append(") REFERENCES ").append('`')
				.append(att.getRefEntity().getName()).append('`').append('(').append('`')
				.append(att.getRefEntity().getIdAttribute().getName()).append('`').append(") ON DELETE CASCADE);");

		return sql.toString();
	}

	protected String getCreateSql() throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS ").append('`').append(getEntityMetaData().getName()).append('`')
				.append('(');

		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			getAttributeSql(sql, att);
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append(", ");
			}
		}
		// primary key is first attribute unless otherwise indicate
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		if (idAttribute == null) throw new MolgenisDataException("Missing idAttribute for entity [" + getName() + "]");

		if (idAttribute.getDataType() instanceof XrefField || idAttribute.getDataType() instanceof MrefField) throw new RuntimeException(
				"primary key(" + getEntityMetaData().getName() + "." + idAttribute.getName()
						+ ") cannot be XREF or MREF");

		if (idAttribute.isNillable() == true) throw new RuntimeException("idAttribute ("
				+ getEntityMetaData().getName() + "." + idAttribute.getName() + ") should not be nillable");

		sql.append("PRIMARY KEY (").append('`').append(getEntityMetaData().getIdAttribute().getName()).append('`')
				.append(')');

		// close
		sql.append(") ENGINE=InnoDB;");

		if (logger.isDebugEnabled())
		{
			logger.debug("sql: " + sql);
		}

		return sql.toString();
	}

	private void getAttributeSql(StringBuilder sql, AttributeMetaData att) throws MolgenisModelException
	{
		if (!(att.getDataType() instanceof MrefField))
		{
			sql.append('`').append(att.getName()).append('`').append(' ');
			// xref adopt type of the identifier of referenced entity
			if (att.getDataType() instanceof XrefField)
			{
				sql.append(att.getRefEntity().getIdAttribute().getDataType().getMysqlType());
			}
			else
			{
				sql.append(att.getDataType().getMysqlType());
			}
			// not null
			if (!att.isNillable())
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

	protected String getAlterSql(AttributeMetaData attributeMetaData) throws MolgenisModelException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE ").append('`').append(getEntityMetaData().getName()).append('`').append(" ADD ");
		getAttributeSql(sql, attributeMetaData);
		sql.append(";");
		return sql.toString();
	}

	protected List<String> getCreateFKeySql()
	{
		List<String> sql = new ArrayList<String>();
		// foreign keys
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (att.getDataType() instanceof XrefField)
			{
				sql.add(new StringBuilder().append("ALTER TABLE ").append(getEntityMetaData().getName())
						.append(" ADD FOREIGN KEY (").append('`').append(att.getName()).append('`')
						.append(") REFERENCES ").append('`').append(att.getRefEntity().getName()).append('`')
						.append('(').append('`').append(att.getRefEntity().getIdAttribute().getName()).append('`')
						.append(")").toString());
			}

		return sql;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	protected String iteratorSql()
	{
		StringBuilder sql = new StringBuilder("SELECT ");
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			sql.append(att.getName()).append(", ");
		}
		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',') sql.setLength(sql.length() - 2);
		else sql.append('*');
		sql.append(" FROM ").append(getEntityMetaData().getName());

		return sql.toString();
	}

	@Override
	public void close() throws IOException
	{
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	protected String getInsertSql()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append('`').append(this.getName()).append('`').append(" (");
		StringBuilder params = new StringBuilder();
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append('`').append(att.getName()).append('`').append(", ");
				params.append("?, ");
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
		mrefSql.append("DELETE FROM ").append('`').append(getEntityMetaData().getName()).append('_')
				.append(att.getName()).append('`').append(" WHERE ").append('`').append(idAttribute.getName())
				.append('`').append("= ?");

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

	private void addMrefs(final List<Entity> mrefs, final AttributeMetaData att)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final AttributeMetaData refEntityIdAttribute = att.getRefEntity().getIdAttribute();

		StringBuilder mrefSql = new StringBuilder();
		mrefSql.append("INSERT INTO ").append(getEntityMetaData().getName()).append('_').append(att.getName())
				.append(" (").append('`').append(idAttribute.getName()).append('`').append(',').append('`')
				.append(att.getName()).append('`').append(") VALUES (?,?)");

		jdbcTemplate.batchUpdate(mrefSql.toString(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{

				if (logger.isDebugEnabled())
				{
					logger.debug("mref: " + mrefs.get(i).get(idAttribute.getName()) + ", "
							+ mrefs.get(i).get(att.getName()));
				}

				preparedStatement.setObject(1, mrefs.get(i).get(idAttribute.getName()));

				Object value = mrefs.get(i).get(att.getName());
				if (value instanceof Entity)
				{
					preparedStatement.setObject(
							2,
							refEntityIdAttribute.getDataType().convert(
									((Entity) value).get(refEntityIdAttribute.getName())));
				}
				else
				{
					preparedStatement.setObject(2, refEntityIdAttribute.getDataType().convert(value));
				}
			}

			@Override
			public int getBatchSize()
			{
				return mrefs.size();
			}
		});
	}

	@Override
	public void flush()
	{

	}

	@Override
	public void clearCache()
	{

	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		List<Object> parameters = Lists.newArrayList();
		String sql = getCountSql(q, parameters);

		if (logger.isDebugEnabled())
		{
			logger.debug("sql: " + sql + ",parameters:" + parameters);
		}

		return jdbcTemplate.queryForObject(sql, parameters.toArray(new Object[0]), Long.class);
	}

	protected String getSelectSql(Query q, List<Object> parameters)
	{
		StringBuilder select = new StringBuilder("SELECT ");
		StringBuilder group = new StringBuilder();
		int count = 0;
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (count > 0) select.append(", ");

			// TODO needed when autoids are used to join
			if (att.getDataType() instanceof MrefField)
			{
				select.append("GROUP_CONCAT(DISTINCT(").append('`').append(att.getName()).append('`').append('.')
						.append('`').append(att.getName()).append('`').append(")) AS ").append('`')
						.append(att.getName()).append('`');
			}
			else
			{
				select.append("this.").append('`').append(att.getName()).append('`');
				if (group.length() > 0) group.append(", this.").append('`').append(att.getName()).append('`');
				else group.append("this.").append('`').append(att.getName()).append('`');
			}
			count++;
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
		List<Object> parameters = Lists.newArrayList();
		String sql = getSelectSql(q, parameters);

		if (logger.isDebugEnabled())
		{
			logger.debug("query: " + q);
			logger.debug("sql: " + sql + ",parameters:" + parameters);
		}

		return jdbcTemplate.query(sql, parameters.toArray(new Object[0]), new EntityMapper(getEntityMetaData()));
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
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		if (ids == null) return Collections.emptyList();
		return findAll(new QueryImpl().in(getEntityMetaData().getIdAttribute().getName(), ids));
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		return findAll(Arrays.asList(new Object[]
		{ id }), clazz).iterator().next();
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		Iterator<E> it = findAll(q, clazz).iterator();
		if (it.hasNext())
		{
			return it.next();
		}

		return null;
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	protected String getFromSql(Query q)
	{
		StringBuilder from = new StringBuilder();
		from.append(" FROM ").append('`').append(getEntityMetaData().getName()).append('`').append(" AS this");

		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		List<String> mrefQueryFields = Lists.newArrayList();
		getMrefQueryFields(q.getRules(), mrefQueryFields);

		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (att.getDataType() instanceof MrefField)
			{
				from.append(" LEFT JOIN ").append('`').append(getEntityMetaData().getName()).append('_')
						.append(att.getName()).append('`').append(" AS ").append('`').append(att.getName()).append('`')
						.append(" ON (this.").append('`').append(idAttribute.getName()).append('`').append(" = ")
						.append('`').append(att.getName()).append('`').append('.').append('`')
						.append(idAttribute.getName()).append('`').append(')');

			}

		for (int i = 0; i < mrefQueryFields.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData att = getEntityMetaData().getAttribute(mrefQueryFields.get(i));

			from.append(" LEFT JOIN ").append('`').append(getEntityMetaData().getName()).append('_')
					.append(att.getName()).append('`').append(" AS ").append('`').append(att.getName())
					.append("_filter").append(i + 1).append("` ON (this.").append('`').append(idAttribute.getName())
					.append('`').append(" = ").append('`').append(att.getName()).append("_filter").append(i + 1)
					.append("`.").append('`').append(idAttribute.getName()).append('`').append(')');
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
							Repository repo = repositoryCollection.getRepositoryByEntityName(att.getRefEntity()
									.getName());
							if (repo instanceof Queryable)
							{
								Query refQ = new QueryImpl().like(att.getRefEntity().getLabelAttribute().getName(),
										r.getValue());
								Iterator<Entity> it = ((Queryable) repo).findAll(refQ).iterator();
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
				AttributeMetaData att = getEntityMetaData().getAttribute(o.getProperty());
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
		StringBuilder sql = new StringBuilder("UPDATE ").append('`').append(this.getName()).append('`').append(" SET ");
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (!(att.getDataType() instanceof MrefField))
			{
				sql.append('`').append(att.getName()).append('`').append(" = ?, ");
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
		final List<Object> batch = new ArrayList<Object>();
		for (Entity e : entities)
			batch.add(e.getIdValue());
		this.deleteById(batch);
	}

	public String getDeleteSql()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append('`').append(getName()).append('`').append(" WHERE ").append('`')
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
			idList.add(id);

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
	public void updateInternal(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		if ((entities == null) || entities.isEmpty()) return;
		if (getEntityMetaData().getIdAttribute() == null) throw new MolgenisDataException("Missing is attribute for ["
				+ getName() + "]");

		String idAttributeName = getEntityMetaData().getIdAttribute().getName();

		// Split in existing and new entities
		Map<Object, Entity> existingEntities = Maps.newLinkedHashMap();
		List<Entity> newEntities = Lists.newArrayList();

		List<Object> ids = Lists.newArrayList();
		for (Entity entity : entities)
		{
			Object id = entity.get(idAttributeName);
			if (id != null)
			{
				ids.add(id);
			}
		}

		if (!ids.isEmpty())
		{
			List<Object> existingIds = Lists.newArrayList();

			Query q = new QueryImpl();
			for (int i = 0; i < ids.size(); i++)
			{
				if (i > 0)
				{
					q.or();
				}
				q.eq(idAttributeName, ids.get(i));
			}

			for (Entity existing : findAll(q))
			{
				existingIds.add(existing.getIdValue());
			}

			FieldType dataType = getEntityMetaData().getIdAttribute().getDataType();
			for (Entity entity : entities)
			{
				Object id = entity.get(idAttributeName);
				if ((id != null) && existingIds.contains(dataType.convert(id)))
				{
					existingEntities.put(id, entity);
				}
				else
				{
					newEntities.add(entity);
				}
			}
		}

		switch (dbAction)
		{
			case ADD:
				if (!existingEntities.isEmpty())
				{
					List<Object> keys = Lists.newArrayList(existingEntities.keySet());

					StringBuilder msg = new StringBuilder();
					msg.append("Trying to add existing ").append(getName()).append(" entities as new insert: ");
					msg.append(keys.subList(0, Math.min(5, keys.size())));
					if (keys.size() > 5)
					{
						msg.append(" and ").append(keys.size() - 5).append(" more.");
					}

					throw new MolgenisDataException(msg.toString());
				}

				addInternal(entities);
				break;

			case ADD_IGNORE_EXISTING:
				if (!newEntities.isEmpty())
				{
					addInternal(newEntities);
				}
				break;

			case ADD_UPDATE_EXISTING:
				if (!newEntities.isEmpty())
				{
					addInternal(newEntities);
				}
				if (!existingEntities.isEmpty())
				{
					updateInternal(existingEntities.values());
				}
				break;

			case REMOVE:
				if (!newEntities.isEmpty())
				{
					List<Object> keys = Lists.newArrayList();
					for (Entity newEntity : newEntities)
					{
						keys.add(newEntity.get(idAttributeName));
						if (keys.size() == 5)
						{
							break;
						}
					}

					StringBuilder msg = new StringBuilder();
					msg.append("Trying to remove not exsisting ").append(getName()).append(" entities:").append(keys);
					if (newEntities.size() > 5)
					{
						msg.append(" and ").append(newEntities.size() - 5).append(" more.");
					}

					throw new MolgenisDataException(msg.toString());
				}

				deleteById(existingEntities.keySet());
				break;

			case REMOVE_IGNORE_MISSING:
				deleteById(existingEntities.keySet());
				break;

			case UPDATE:
				if (!newEntities.isEmpty())
				{
					List<Object> keys = Lists.newArrayList();
					for (Entity newEntity : newEntities)
					{
						keys.add(newEntity.get(idAttributeName));
						if (keys.size() == 5)
						{
							break;
						}
					}

					StringBuilder msg = new StringBuilder();
					msg.append("Trying to update not exsisting ").append(getName()).append(" entities:").append(keys);
					if (newEntities.size() > 5)
					{
						msg.append(" and ").append(newEntities.size() - 5).append(" more.");
					}

					throw new MolgenisDataException(msg.toString());
				}
				updateInternal(existingEntities.values());
				break;

			case UPDATE_IGNORE_MISSING:
				updateInternal(existingEntities.values());
				break;

			default:
				break;

		}
	}

	public RepositoryCollection getRepositoryCollection()
	{
		return repositoryCollection;
	}

	@Override
	public Integer addInternal(Iterable<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger(0);

		// TODO, split in subbatches
		final List<Entity> batch = new ArrayList<Entity>();
		if (entities != null) for (Entity e : entities)
		{
			batch.add(e);
			count.addAndGet(1);
		}
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final Map<String, List<Entity>> mrefs = new HashMap<String, List<Entity>>();

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
						if (mrefs.get(att.getName()) == null) mrefs.put(att.getName(), new ArrayList<Entity>());
						if (batch.get(rowIndex).get(att.getName()) != null)
						{
							for (Object val : batch.get(rowIndex).getList(att.getName()))
							{
								Entity mref = new MapEntity();
								mref.set(idAttribute.getName(), batch.get(rowIndex).get(idAttribute.getName()));
								mref.set(att.getName(), val);

								mrefs.get(att.getName()).add(mref);
							}
						}
					}
					else
					{
						// default value, if any
						if (batch.get(rowIndex).get(att.getName()) == null)
						{
							preparedStatement.setObject(fieldIndex++, att.getDefaultValue());
						}
						else if (att.getDataType() instanceof XrefField)
						{
							Object value = batch.get(rowIndex).get(att.getName());
							if (value instanceof Entity)
							{
								value = ((Entity) value).get(att.getRefEntity().getIdAttribute().getName());
							}

							preparedStatement.setObject(fieldIndex++, att.getRefEntity().getIdAttribute().getDataType()
									.convert(value));
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

		return count.get();
	}

	@Override
	public void addInternal(Entity entity)
	{
		if (entity == null) throw new RuntimeException("MysqlRepository.add() failed: entity was null");
		addInternal(Arrays.asList(new Entity[]
		{ entity }));
	}

	@Override
	public void updateInternal(Entity entity)
	{
		updateInternal(Arrays.asList(new Entity[]
		{ entity }));
	}

	@Override
	public void updateInternal(Iterable<? extends Entity> entities)
	{
		// TODO, split in subbatches
		final List<Entity> batch = new ArrayList<Entity>();
		if (entities != null) for (Entity e : entities)
		{
			batch.add(e);
		}
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final List<Object> ids = new ArrayList<Object>();
		final Map<String, List<Entity>> mrefs = new HashMap<String, List<Entity>>();

		jdbcTemplate.batchUpdate(getUpdateSql(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
			{
				Entity e = batch.get(rowIndex);

				if (logger.isDebugEnabled())
				{
					logger.debug("updating: " + e);
				}

				Object idValue = idAttribute.getDataType().convert(e.get(idAttribute.getName()));
				ids.add(idValue);
				int fieldIndex = 1;
				for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
				{
					// create the mref records
					if (att.getDataType() instanceof MrefField)
					{
						if (mrefs.get(att.getName()) == null) mrefs.put(att.getName(), new ArrayList<Entity>());
						if (e.get(att.getName()) != null) for (Object val : e.getList(att.getName()))
						{
							Entity mref = new MapEntity();
							mref.set(idAttribute.getName(), idValue);
							mref.set(att.getName(), val);
							mrefs.get(att.getName()).add(mref);
						}
					}
					else
					{
						// default value, if any
						if (e.get(att.getName()) == null)
						{
							preparedStatement.setObject(fieldIndex++, att.getDefaultValue());
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

	@Override
	protected void addAggregateValuesAndLabels(AttributeMetaData attr, List<Object> values, Set<String> labels)
	{
		if (attr.getDataType().getEnumType() == BOOL)
		{
			values.add(Boolean.TRUE);
			values.add(Boolean.FALSE);
			labels.add(attr.getName() + ": true");
			labels.add(attr.getName() + ": false");
		}
		else
		{
			for (Object value : getDistinctColumnValues(attr))
			{
				String valueStr = DataConverter.toString(value);
				labels.add(valueStr);
				values.add(valueStr);
			}
		}
	}

	private List<Object> getDistinctColumnValues(AttributeMetaData attr)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT(`").append(attr.getName()).append("`) FROM `").append(getName())
				.append("` AS this ");

		if (attr.getDataType() instanceof MrefField)
		{
			AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

			sql.append(" LEFT JOIN ").append('`').append(getEntityMetaData().getName()).append('_')
					.append(attr.getName()).append('`').append(" AS ").append('`').append(attr.getName()).append('`')
					.append(" ON (this.").append('`').append(idAttribute.getName()).append('`').append(" = ")
					.append('`').append(attr.getName()).append('`').append('.').append('`')
					.append(idAttribute.getName()).append('`').append(')');
		}

		sql.append(" WHERE `").append(attr.getName()).append("` IS NOT NULL");

		return jdbcTemplate.query(sql.toString(), new RowMapper<Object>()
		{
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				return rs.getObject(1);
			}
		});
	}

	private class EntityMapper implements RowMapper<Entity>
	{
		private final EntityMetaData entityMetaData;

		private EntityMapper(EntityMetaData entityMetaData)
		{
			this.entityMetaData = entityMetaData;
		}

		@Override
		public Entity mapRow(ResultSet resultSet, int i) throws SQLException
		{
			Entity e = new MysqlEntity(entityMetaData, repositoryCollection);

			for (AttributeMetaData att : entityMetaData.getAtomicAttributes())
			{
				if (att.getDataType() instanceof MrefField)
				{
					// TODO: convert to typed lists (or arrays?)
					if (att.getRefEntity().getIdAttribute().getDataType() instanceof IntField)
					{
						e.set(att.getName(), DataConverter.toIntList(resultSet.getString(att.getName())));
					}
					else
					{
						e.set(att.getName(), DataConverter.toObjectList(resultSet.getString(att.getName())));
					}

				}
				else if (att.getDataType() instanceof XrefField)
				{
					e.set(att.getName(),
							att.getRefEntity().getIdAttribute().getDataType()
									.convert(resultSet.getObject(att.getName())));
				}
				else
				{
					e.set(att.getName(), att.getDataType().convert(resultSet.getObject(att.getName())));
				}
			}
			return e;

		}
	}

}
