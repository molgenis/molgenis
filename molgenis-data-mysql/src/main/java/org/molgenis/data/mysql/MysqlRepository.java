package org.molgenis.data.mysql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
	private MysqlRepositoryCollection repositoryCollection;
	private DataSource ds;
	private JdbcTemplate jdbcTemplate;

	protected MysqlRepository(MysqlRepositoryCollection collection, EntityMetaData metaData)
	{
		if (metaData == null) throw new IllegalArgumentException("DataSource is null");
		if (metaData == null) throw new IllegalArgumentException("metaData is null");
		this.metaData = metaData;
		this.repositoryCollection = collection;
		this.ds = collection.getDataSource();
		this.jdbcTemplate = new JdbcTemplate(ds);
	}

	@Autowired
	public void setDataSource(DataSource dataSource)
	{
		this.ds = dataSource;
		this.jdbcTemplate = new JdbcTemplate(ds);
		System.out.println("set:" + dataSource);
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
			for (String fkeySql : this.getCreateFKeySql())
				jdbcTemplate.execute(fkeySql);
			// add mref tables
			for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			{
				if (att.getDataType() instanceof MrefField)
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
				if (att.getDataType() instanceof XrefField)
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
		if (idAttribute.getDataType() instanceof XrefField || idAttribute.getDataType() instanceof MrefField) throw new RuntimeException(
				"primary key(" + getEntityMetaData().getName() + "." + idAttribute.getName()
						+ ") cannot be XREF or MREF");
		if (idAttribute.isNillable() == true) throw new RuntimeException("primary key(" + getEntityMetaData().getName()
				+ "." + idAttribute.getName() + ") must be NOT NULL");
		sql += "PRIMARY KEY (" + getEntityMetaData().getIdAttribute().getName() + ")";

		// close
		sql += ") ENGINE=InnoDB;";

		return sql;
	}

	protected List<String> getCreateFKeySql()
	{
		List<String> sql = new ArrayList<String>();
		// foreign keys
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (att.getDataType().equals(MolgenisFieldTypes.XREF))
			{
				sql.add("ALTER TABLE " + getEntityMetaData().getName() + " ADD FOREIGN KEY (" + att.getName()
						+ ") REFERENCES " + att.getRefEntity().getName() + "("
						+ att.getRefEntity().getIdAttribute().getName() + ")");
			}
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
	public void add(Entity entity)
	{
		if (entity == null) throw new RuntimeException("MysqlRepository.add() failed: entity was null");
		this.add(Arrays.asList(new Entity[]
		{ entity }));
	}

	protected String getInsertSql()
	{
		String sql = "INSERT INTO " + this.getName() + " (";
		String params = "";
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (!(att.getDataType() instanceof MrefField))
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
	public Integer add(Iterable<? extends Entity> entities)
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

		jdbcTemplate.batchUpdate(this.getInsertSql(), new BatchPreparedStatementSetter()
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
						else
						{
							if (att.getDataType() instanceof XrefField)
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
			if (att.getDataType() instanceof MrefField)
			{
				addMrefs(mrefs.get(att.getName()), att);
			}
		}

		return count.get();
	}

	private void removeMrefs(final List<Object> ids, final AttributeMetaData att)
	{
		final AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();
		String mrefSql = "DELETE FROM " + getEntityMetaData().getName() + "_" + att.getName() + " WHERE "
				+ idAttribute.getName() + "= ?";
		jdbcTemplate.batchUpdate(mrefSql, new BatchPreparedStatementSetter()
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
		final AttributeMetaData refAttribute = att.getRefEntity().getIdAttribute();
		String mrefSql = "INSERT INTO " + getEntityMetaData().getName() + "_" + att.getName() + " ("
				+ idAttribute.getName() + "," + att.getName() + ") VALUES (?,?)";
		jdbcTemplate.batchUpdate(mrefSql, new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException
			{
				System.out.println("mref: " + mrefs.get(i).get(idAttribute.getName()) + ", "
						+ mrefs.get(i).get(att.getName()));

				preparedStatement.setObject(1, mrefs.get(i).get(idAttribute.getName()));
				Object value = mrefs.get(i).get(att.getName());
				if (value instanceof Entity)
				{
					preparedStatement.setObject(2,
							refAttribute.getDataType().convert(((Entity) value).get(idAttribute.getName())));
				}
				else
				{
					preparedStatement.setObject(2, refAttribute.getDataType().convert(value));
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
	public long count(Query q)
	{
		String sql = getCountSql(q);
		System.out.println(sql);
		return jdbcTemplate.queryForObject(sql, Long.class);
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
			if (att.getDataType() instanceof MrefField)
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

		// from
		String result = select + getFromSql();
		// where
		String where = getWhereSql(q);
		if (where.length() > 0) result += " " + where;
		// group by
		if (select.contains("GROUP_CONCAT") && group.length() > 0) result += " GROUP BY " + group;
		// order by
		result += " " + getSortSql(q);
		// limit
		if (q.getPageSize() > 0) result += " LIMIT " + q.getPageSize();
		if (q.getOffset() > 0) result += " OFFSET " + q.getOffset();
		return result.trim();
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
		if (ids == null) return new ArrayList();
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
			if (att.getDataType() instanceof MrefField)
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
		if (where.length() > 0) return "SELECT COUNT(DISTINCT this." + idAttribute + ")" + from + " " + where;
		return "SELECT COUNT(DISTINCT this." + idAttribute + ")" + from;
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
					AttributeMetaData att = getEntityMetaData().getAttribute(r.getField());
					String in = "'UNKNOWN VALUE'";
					List<Object> values = new ArrayList<Object>();
					if (!(r.getValue() instanceof List))
					{
						for (String str : r.getValue().toString().split(","))
							values.add(str);
					}
					else
					{
						values.addAll((Collection<?>) r.getValue());
					}
					boolean quotes = att.getDataType() instanceof StringField || att.getDataType() instanceof TextField;
					for (Object o : values)
					{
						if (quotes) in += ",'" + o + "'";
						else in += "," + o;
					}

					if (att.getDataType() instanceof MrefField) result += att.getName() + "_filter." + r.getField()
							+ " IN(" + in + ")";
					else result += "this." + r.getField() + " IN(" + in + ")";
					break;
				default:
					// comparable values...
					att = getEntityMetaData().getAttribute(r.getField());
					if (att == null) throw new RuntimeException("Query failed: attribute '" + r.getField()
							+ "' unknown");
					FieldType type = att.getDataType();
					if (type instanceof MrefField) predicate += att.getName() + "_filter." + r.getField();
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
		if (result.length() > 0) return "WHERE " + result.trim();
		else return "";
	}

	protected String getSortSql(Query q)
	{
		String sortSql = "";
		if (q.getSort() != null)
		{
			for (Sort.Order o : q.getSort())
			{
				AttributeMetaData att = getEntityMetaData().getAttribute(o.getProperty());
				if (att.getDataType() instanceof MrefField) sortSql += ", " + att.getName();
				else sortSql += ", " + att.getName();
				if (o.getDirection().equals(Sort.Direction.DESC))
				{
					sortSql += " DESC";
				}
				else
				{
					sortSql += " ASC";
				}
			}

			if (sortSql.length() > 0) sortSql = "ORDER BY " + sortSql.substring(2);
		}
		return sortSql;
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
		update(Arrays.asList(new Entity[]
		{ entity }));
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
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
		final List<Object> ids = new ArrayList<Object>();
		final Map<String, List<Entity>> mrefs = new HashMap<String, List<Entity>>();

		jdbcTemplate.batchUpdate(this.getUpdateSql(), new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement preparedStatement, int rowIndex) throws SQLException
			{
				Entity e = batch.get(rowIndex);
				System.out.println("updating: " + e);
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

		// return count.get();
	}

	protected String getUpdateSql()
	{
		// use (readonly) identifier
		AttributeMetaData idAttribute = getEntityMetaData().getIdAttribute();

		// create sql
		String sql = "UPDATE " + this.getName() + " SET ";
		String params = "";
		for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
			if (!(att.getDataType() instanceof MrefField))
			{
				sql += att.getName() + " = ?, ";
			}
		if (sql.endsWith(", "))
		{
			sql = sql.substring(0, sql.length() - 2);
		}
		sql += " WHERE " + idAttribute.getName() + "= ?";
		return sql;
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
		return "DELETE FROM " + getName() + " WHERE " + getEntityMetaData().getIdAttribute().getName() + " = ?";
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
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		throw new UnsupportedOperationException();
	}

	public MysqlRepositoryCollection getRepositoryCollection()
	{
		return repositoryCollection;
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	private class EntityMapper implements RowMapper
	{

		@Override
		public Entity mapRow(ResultSet resultSet, int i) throws SQLException
		{
			Entity e = new MysqlEntity(getEntityMetaData(), getRepositoryCollection());

			for (AttributeMetaData att : getEntityMetaData().getAtomicAttributes())
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
