package org.molgenis.data.mysql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.*;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class MysqlRepository implements Repository, Writable, Queryable, Manageable, CrudRepository
{
	public static final int BATCH_SIZE = 100000;
	private EntityMetaData metaData;
	private DataSource ds;
	private JdbcTemplate jdbcTemplate;

	public MysqlRepository(DataSource ds, EntityMetaData metaData)
	{
		if (metaData == null) throw new IllegalArgumentException("DataSource is null");
		if (metaData == null) throw new IllegalArgumentException("metaData is null");
		this.metaData = metaData;
		this.ds = ds;
		this.jdbcTemplate = new JdbcTemplate(ds);
	}

	@Autowired
	public void setDataSource(DataSource dataSource)
	{
		this.ds = dataSource;
		this.jdbcTemplate = new JdbcTemplate(ds);
		System.out.println("BLAAAT:" + dataSource);
	}

	@Override
	public void drop()
	{
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (att.getDataType() == MolgenisFieldTypes.MREF)
			{
				jdbcTemplate.execute("DROP TABLE IF EXISTS " + getEntityMetaData().getName() + "_" + att.getName());
			}
		}
		jdbcTemplate.execute(this.getDropSql());
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
			jdbcTemplate.execute(this.getCreateSql());
			// add mref tables
			for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			{
				if (att.getDataType() == MolgenisFieldTypes.MREF)
				{
					jdbcTemplate.execute(this.getMrefCreateSql(att));
				}
			}

		}
		catch (Exception e)
		{
            e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected String getMrefCreateSql(AttributeMetaData att) throws MolgenisModelException
	{
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		return " CREATE TABLE " + getEntityMetaData().getName() + "_" + att.getName() + "(" + idAttribute.getName()
				+ " " + idAttribute.getDataType().getMysqlType() + " NOT NULL, " + att.getName() + " "
				+ att.getRefEntity().getIdAttribute().getDataType().getMysqlType() + " NOT NULL, FOREIGN KEY ("
				+ idAttribute.getName() + ") REFERENCES " + getEntityMetaData().getName() + "(" + idAttribute.getName()
				+ ") ON DELETE CASCADE, FOREIGN KEY (" + att.getName() + ") REFERENCES " + att.getRefEntity().getName()
				+ "(" + att.getRefEntity().getIdAttribute().getName() + ") ON DELETE CASCADE);";
	}

	protected String getCreateSql() throws MolgenisModelException
	{
		String sql = "CREATE TABLE IF NOT EXISTS " + getEntityMetaData().getName() + "(";
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (!(att.getDataType() instanceof MrefField))
			{
				sql += att.getName() + " ";
				// xref adopt type of the identifier of referenced entity
				if (att.getDataType().equals(MolgenisFieldTypes.XREF))
				{
					sql += att.getRefEntity().getIdAttribute().getDataType().getMysqlType();
				}
				else
				{
					sql += att.getDataType().getMysqlType();
				}
				// not null
				if (!att.isNillable())
				{
					sql += " NOT NULL";
				}
				// int + auto = auto_increment
				if (att.getDataType().equals(MolgenisFieldTypes.INT) && att.isAuto())
				{
					sql += " AUTO_INCREMENT";
				}
				sql += ", ";
			}
		}
		// primary key is first attribute unless otherwise indicate
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		if (idAttribute.getDataType() == MolgenisFieldTypes.XREF
				|| idAttribute.getDataType() == MolgenisFieldTypes.MREF) throw new RuntimeException("primary key("
				+ getEntityMetaData().getName() + "." + idAttribute.getName() + ") cannot be XREF or MREF");
		if (idAttribute.isNillable() == true) throw new RuntimeException("primary key(" + getEntityMetaData().getName()
				+ "." + idAttribute.getName() + ") must be NOT NULL");
		sql += "PRIMARY KEY (" + getEntityMetaData().getIdAttribute().getName() + ")";

		// foreign keys
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (att.getDataType().equals(MolgenisFieldTypes.XREF))
			{
				sql += ", FOREIGN KEY (" + att.getName() + ") REFERENCES " + att.getRefEntity().getName() + "("
						+ att.getRefEntity().getIdAttribute().getName() + ")";
			}

		// close
		sql += ") ENGINE=InnoDB;";

		return sql;
	}

	@Override
	public String getName()
	{
		return metaData.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	protected String iteratorSql()
	{
		String sql = "SELECT ";
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			sql += att.getName() + ", ";
		}
		if (sql.endsWith(", ")) sql = sql.substring(0, sql.length() - 2);
		else sql += "*";
		sql += " FROM " + getEntityMetaData().getName();
		return sql;
	}

	@Override
	public String getUrl()
	{
		throw new UnsupportedOperationException();
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

	@Override
	public Integer add(Entity entity)
	{
		if (entity == null) throw new RuntimeException("MysqlRepository.add() failed: entity was null");
		this.add(Arrays.asList(new Entity[]
		{ entity }));
		return 1;
	}

	protected String getInsertSql()
	{
		String sql = "INSERT INTO " + this.getName() + " (";
		String params = "";
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (att.getDataType() != MolgenisFieldTypes.MREF)
			{
				sql += att.getName() + ", ";
				params += "?, ";
			}
		if (sql.endsWith(", "))
		{
			sql = sql.substring(0, sql.length() - 2);
			params = params.substring(0, params.length() - 2);
		}
		sql += ") VALUES (" + params + ")";
		return sql;
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		// todo, split in subbatches
		final List<Entity> batch = new ArrayList<Entity>();
		for (Entity e : entities)
		{
			batch.add(e);
		}
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		final Map<String, List<Entity>> mrefs = new HashMap<String, List<Entity>>();

		jdbcTemplate.batchUpdate(this.getInsertSql(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
			{
				int fieldIndex = 1;
				for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
				{
					// create the mref records
					if (att.getDataType() == MolgenisFieldTypes.MREF)
					{
						if (mrefs.get(att.getName()) == null) mrefs.put(att.getName(), new ArrayList<Entity>());
						if (batch.get(rowIndex).get(att.getName()) != null) for (Object val : batch.get(rowIndex)
								.getList(att.getName()))
						{
							Entity mref = new MapEntity();
							mref.set(idAttribute.getName(), batch.get(rowIndex).get(idAttribute.getName()));
							mref.set(att.getName(), val);
							mrefs.get(att.getName()).add(mref);
						}
					}
					else
					{
						if (batch.get(rowIndex).get(att.getName()) == null)
						{
							preparedStatement.setObject(fieldIndex++, att.getDefaultValue());
						}
						else
						{
							if (att.getDataType() == MolgenisFieldTypes.XREF)
							{
								preparedStatement.setObject(fieldIndex++, att.getRefEntity().getIdAttribute()
										.getDataType().convert(batch.get(rowIndex).get(att.getName())));
							}
							else
							{
								preparedStatement.setObject(fieldIndex++,
										att.getDataType().convert(batch.get(rowIndex).get(att.getName())));
							}
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
			if (att.getDataType() == MolgenisFieldTypes.MREF)
			{
				addMref(mrefs.get(att.getName()), att);
			}
		}

	}

	private void addMref(final List<Entity> mrefs, final AttributeMetaData att)
	{

		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		String mrefSql = "INSERT INTO " + getEntityMetaData().getName() + "_" + att.getName() + " ("
				+ idAttribute.getName() + "," + att.getName() + ") VALUES (?,?)";
		jdbcTemplate.batchUpdate(mrefSql, new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				preparedStatement.setObject(1, mrefs.get(i).get(idAttribute.getName()));
				preparedStatement.setObject(2, mrefs.get(i).get(att.getName()));
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
	public long count(Query q)
	{
		return jdbcTemplate.queryForObject(getCountSql(q), Long.class);
	}

	protected String getSelectSql(Query q)
	{
		String select = "SELECT ";
		String group = "";
		int count = 0;
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
		{
			if (count > 0) select += ", ";

			// TODO needed when autoids are used to join
			// if(att.getDataType() == MolgenisFieldTypes.XREF)
			// {
			// String refEntity = att.getRefEntity().getName();
			// String refId = att.getRefEntity().getIdAttribute().getName();
			// select += att.getName()+"."+refId+" AS "+att.getName();
			// from += " LEFT JOIN "+refEntity+" AS "+att.getName()+" ON ("+refEntity+"."+refId+"=this."+att.getName();
			// }
			// else
			// {
			if (att.getDataType() == MolgenisFieldTypes.MREF)
			{
				select += "GROUP_CONCAT(DISTINCT(" + att.getName() + "." + att.getName() + ")) AS " + att.getName();

			}
			else
			{
				select += "this." + att.getName();
				if (group.length() > 0) group += ", this." + att.getName();
				else group += "this." + att.getName();
			}
			// }
			count++;

		}
		String where = getWhereSql(q);
		String result = select + getFromSql();
		if (where.length() > 0) result += " " + where;
		if (select.contains("GROUP_CONCAT") && group.length() > 0) result += " GROUP BY " + group;
		return result;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		String sql = getSelectSql(q);

		// tmp:
		System.out.println("query: " + q);
		System.out.println("sql: " + sql);

		return jdbcTemplate.query(sql, new EntityMapper());
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return null;
	}

	@Override
	public Entity findOne(Query q)
	{
		return null;
	}

	@Override
	public Entity findOne(Integer id)
	{
		return null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		return null;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		return null;
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		return findAll(Arrays.asList(new Integer[]
		{ id }), clazz).iterator().next();
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		return findAll(q, clazz).iterator().next();
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	protected String getFromSql()
	{
		String from = " FROM " + getEntityMetaData().getName() + " AS this";
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (att.getDataType() == MolgenisFieldTypes.MREF)
			{
				// extra join so we can filter on the mrefs
				from += " LEFT JOIN " + getEntityMetaData().getName() + "_" + att.getName() + " AS " + att.getName()
						+ "_filter ON (this." + idAttribute.getName() + " = " + att.getName() + "_filter."
						+ idAttribute.getName() + ") LEFT JOIN " + getEntityMetaData().getName() + "_" + att.getName()
						+ " AS " + att.getName() + " ON (this." + idAttribute.getName() + " = " + att.getName() + "."
						+ idAttribute.getName() + ")";
			}

		return from;
	}

	protected String getCountSql(Query q)
	{
		String where = getWhereSql(q);
		String from = getFromSql();
		String idAttribute = getEntityMetaData().getIdAttribute().getName();
		if (where.length() > 0) return "SELECT count(this." + idAttribute + ")" + from + " " + where;
		return "SELECT count(this." + idAttribute + ")" + from;
	}

	protected String getWhereSql(Query q)
	{
		String result = "";
		for (QueryRule r : q.getRules())
		{
			String predicate = "";
			switch (r.getOperator())
			{
				case SEARCH:
					String search = "";
					for (AttributeMetaData att : getEntityMetaData().getAttributes())
					{
						// TODO: other data types???
						if (att.getDataType() instanceof StringField || att.getDataType() instanceof TextField)
						{
							search += " OR this." + att.getName() + " LIKE '%" + r.getValue() + "%'";
						}
                        else
                        {
                            search += " OR CAST(this." + att.getName() + " as CHAR) LIKE '%" + r.getValue() + "%'";
                        }
					}
					if (search.length() > 0) result += "(" + search.substring(4) + ")";
					break;
				case AND:
					break;
				case NESTED:
					throw new UnsupportedOperationException();
					// break;
				case OR:
					result += " OR ";
					break;
				case IN:
					throw new UnsupportedOperationException();
					// break;
				default:
					// comparable values...
					AttributeMetaData att = getEntityMetaData().getAttribute(r.getField());
					if (att == null) throw new RuntimeException("Query failed: attribute '" + r.getField()
							+ "' unknown");
					FieldType type = att.getDataType();
					if (type == MolgenisFieldTypes.MREF) predicate += att.getName() + "_filter." + r.getField();
					else predicate += "this." + r.getField();
					switch (r.getOperator())
					{
						case EQUALS:
							predicate += " =";
							break;
						case GREATER:
							predicate += " >";
							break;
						case LESS:
							predicate += " <";
							break;
						case GREATER_EQUAL:
							predicate += " >=";
							break;
						case LESS_EQUAL:
							predicate += " <=";
							break;
						default:
							throw new RuntimeException("cannot solve query rule:  " + r);
					}
					if (type instanceof IntField || type instanceof BoolField) predicate += " " + r.getValue() + "";
					else predicate += " '" + r.getValue() + "'";

					if (result.length() > 0 && !result.endsWith(" OR ")) result += " AND ";
					result += predicate;
			}
		}
		String sortSql = "";
		if (q.getSort() != null)
		{
			for (Sort.Order o : q.getSort())
			{
				sortSql += ", " + o.getProperty();
				if (o.getDirection().equals(Sort.Direction.DESC))
				{
					sortSql += " DESC";
				}
				else
				{
					sortSql += " ASC";
				}
			}

			if (sortSql.length() > 0) sortSql = " ORDER BY " + sortSql.substring(2);

		}
		if (result.length() > 0) return "WHERE " + result + sortSql;
		else return sortSql.trim();
	}

	private String formatValue(AttributeMetaData att, Object value)
	{
		if (att.getDataType() instanceof StringField || att.getDataType() instanceof TextField)
		{
			return "'" + value + "'";
		}
		else
		{
			return value.toString();
		}
	}

	public MysqlRepositoryQuery query()
	{
		return new MysqlRepositoryQuery(this);
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		throw new UnsupportedOperationException();

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
		final List<Entity> batch = new ArrayList<Entity>();
		for (Entity e : entities)
			batch.add(e);
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		jdbcTemplate.batchUpdate(getDeleteSql(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				if (batch.get(i).get(idAttribute.getName()) == null)
				{
					throw new IllegalArgumentException("idAttribute cannot be null");
				}
				else
				{
					preparedStatement.setObject(1, batch.get(i).get(idAttribute.getName()));
				}
			}

			@Override
			public int getBatchSize()
			{
				return batch.size();
			}
		});
	}

	public String getDeleteSql()
	{
		return "DELETE FROM " + getName() + " WHERE " + getEntityMetaData().getIdAttribute().getName() + " = ?";
	}

	@Override
	public void deleteById(Integer id)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		throw new UnsupportedOperationException();

	}

	private class EntityMapper implements RowMapper
	{

		@Override
		public Entity mapRow(ResultSet resultSet, int i) throws SQLException
		{
			Entity e = new MapEntity();
			for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			{
				if (att.getDataType() == MolgenisFieldTypes.MREF)
				{
					// TODO: convert to typed lists (or arrays?)
					e.set(att.getName(), resultSet.getObject(att.getName()));
				}
				else if (att.getDataType() == MolgenisFieldTypes.XREF)
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
