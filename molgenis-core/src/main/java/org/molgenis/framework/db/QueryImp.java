package org.molgenis.framework.db;

import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.io.TupleWriter;
import org.molgenis.util.Entity;

/**
 * Simple implementation of the Query interface.
 */
public class QueryImp<E extends Entity> implements Query<E>
{
	public Class<E> getKlazz()
	{
		return klazz;
	}

	private Vector<QueryRule> rules = new Vector<QueryRule>();
	private Class<E> klazz;
	private Database database;

	/** Construct a Query that is not bound to a database and entity-type */
	public QueryImp()
	{
	}

	/**
	 * Construct a Query that is bound to a database and entity-type. This
	 * enables use of the {@link #find()} method.
	 * 
	 * @param db
	 *            the database this Query is bound to.
	 * @param klazz
	 *            the Entity class that this Query will filter.
	 */
	public QueryImp(Database db, Class<E> klazz)
	{
		this.database = db;
		this.klazz = klazz;
	}

	public QueryImp(Class<E> klazz)
	{
		this.klazz = klazz;
	}

	@Override
	public Query<E> filter(String filter)
	{
		String[] args = filter.split(" "); // FIXME
		this.rules.add(new QueryRule(args[0], QueryRule.Operator.valueOf(args[1]), args[2]));
		return this;
	}

	@Override
	public Query<E> equals(String field, Object value)
	{
		rules.add(new QueryRule(field, Operator.EQUALS, value));
		return this;
	}

	@Override
	public Query<E> search(String searchTerms)
	{
		rules.add(new QueryRule(Operator.SEARCH, searchTerms));
		return this;
	}

	@Override
	public Query<E> in(String field, List<?> values)
	{
		if (values.size() > 0)
		{
			rules.add(new QueryRule(field, Operator.IN, values.toArray()));
		}
		return this;
	}

	@Override
	public Query<E> subquery(String field, String sql)
	{
		rules.add(new QueryRule(field, Operator.IN_SUBQUERY, sql));
		return this;
	}

	@Override
	public Query<E> subQuery(SubQueryRule subQueryRule)
	{
		rules.add(subQueryRule);
		return this;
	}

	@Override
	public Query<E> greater(String field, Object value)
	{
		rules.add(new QueryRule(field, Operator.GREATER, value));
		return this;
	}

	@Override
	public Query<E> greaterOrEqual(String field, Object value)
	{
		rules.add(new QueryRule(field, Operator.GREATER_EQUAL, value));
		return this;
	}

	@Override
	public Query<E> less(String field, Object value)
	{
		rules.add(new QueryRule(field, Operator.LESS, value));
		return this;
	}

	@Override
	public Query<E> lessOrEqual(String field, Object value)
	{
		rules.add(new QueryRule(field, Operator.LESS_EQUAL, value));
		return this;
	}

	@Override
	public Query<E> between(String field, Object min, Object max)
	{
		return this.lessOrEqual(field, max).greaterOrEqual(field, min);
	}

	@Override
	public Query<E> like(String field, Object value)
	{
		rules.add(new QueryRule(field, Operator.LIKE, value));
		return this;
	}

	@Override
	public Query<E> last()
	{
		rules.add(new QueryRule(Operator.LAST));
		return this;
	}

	@Override
	public Query<E> or()
	{
		rules.add(new QueryRule(Operator.OR));
		return this;
	}

	@Override
	public Query<E> and()
	{
		rules.add(new QueryRule(Operator.AND));
		return this;
	}

	@Override
	public Query<E> limit(int limit)
	{
		rules.add(new QueryRule(QueryRule.Operator.LIMIT, limit));
		return this;
	}

	@Override
	public Query<E> offset(int offset)
	{
		rules.add(new QueryRule(QueryRule.Operator.OFFSET, offset));
		return this;
	}

	@Override
	public Query<E> sortASC(String orderByField)
	{
		rules.add(new QueryRule(Operator.SORTASC, orderByField));
		return this;
	}

	@Override
	public Query<E> sortDESC(String orderByField)
	{
		rules.add(new QueryRule(Operator.SORTDESC, orderByField));
		return this;
	}

	@Override
	public QueryRule[] getRules()
	{
		return rules.toArray(new QueryRule[rules.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (QueryRule rule : this.getRules())
		{
			strBuilder.append(rule.toString());
		}
		return strBuilder.toString();
	}

	public void addRules(List<QueryRule> addRules)
	{
		if (addRules != null)
		{
			this.addRules(addRules.toArray(new QueryRule[addRules.size()]));
		}
	}

	@Override
	public void addRules(QueryRule... addRules)
	{
		for (QueryRule rule : addRules)
		{
			// Logger.getLogger("test").debug("added rule " + rule);
			rules.add(rule);
		}
	}

	@Override
	public List<E> find() throws DatabaseException
	{
		if (this.klazz != null && this.database != null)
		{
			return this.find(this.database, this.klazz);
		}
		throw new UnsupportedOperationException(
				"Cannot execute this find query because no database and entity is provided. Use find(Database,Class)");
	}

	@Override
	public List<E> find(Database db, Class<E> klazz) throws DatabaseException
	{
		return db.find(klazz, this.getRules());
	}

	@Override
	public int count() throws DatabaseException
	{
		if (this.klazz != null && this.database != null)
		{
			return this.count(this.database, this.klazz);
		}
		throw new UnsupportedOperationException(
				"Cannot execute this count query because no database and entity is provided. Use count(Database,Class)");
	}

	@Override
	public int count(Database db, Class<E> klazz) throws DatabaseException
	{
		return db.count(klazz, this.getRules());
	}

	@Override
	public void find(TupleWriter writer) throws DatabaseException, ParseException
	{
		find(writer, null);
	}

	@Override
	public void find(TupleWriter writer, List<String> fieldsToExport) throws DatabaseException, ParseException
	{
		if (this.klazz != null && this.database != null)
		{
			database.find(this.klazz, writer, fieldsToExport, this.getRules());
		}
		else
		{
			throw new UnsupportedOperationException(
					"Cannot execute this count query because no database and entity is provided. Use count(Database,Class)");
		}
	}

	@Override
	public void find(TupleWriter writer, boolean skipAutoId) throws DatabaseException, ParseException,
			InstantiationException, IllegalAccessException
	{
		this.find(writer, this.klazz.newInstance().getFields(skipAutoId));
	}

	@Override
	public Query<E> eq(String field, Object value)
	{
		return this.equals(field, value);
	}

	@Override
	public Query<E> gt(String field, Object value)
	{
		return this.greater(field, value);
	}

	@Override
	public Query<E> lt(String field, Object value)
	{
		return this.less(field, value);
	}

	@Override
	public Database getDatabase()
	{
		return database;
	}

	@Override
	public void setDatabase(Database db)
	{
		this.database = db;
	}

	@Override
	public Query<E> example(Entity example)
	{
		for (String field : example.getFields())
		{
			if (example.get(field) != null)
			{
				if (example.get(field) instanceof List<?>)
				{
					if (((List<?>) example.get(field)).size() > 0) this.in(field, (List<?>) example.get(field));
				}
				else
					this.equals(field, example.get(field));
			}
		}
		return this;
	}

	@Override
	public void removeRule(QueryRule ruleToBeRemoved)
	{
		try
		{
			rules.remove(ruleToBeRemoved);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.println("ArrayIndexOutOfBoundsException " + e.getLocalizedMessage());
		}
	}

	@Override
	public String createFindSql() throws DatabaseException
	{
		return database.createFindSql(klazz, this.getRules());

	}
}
