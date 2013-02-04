package org.molgenis.framework.db;

import java.text.ParseException;
import java.util.List;

import org.molgenis.io.TupleWriter;
import org.molgenis.util.Entity;

/**
 * Easily build complex QueryRules for Database.
 * 
 * <p>
 * Query is a wrapper around QueryRule. You can add or remove queryrules much
 * simpeler than by creating "new QueryRule(...)". Because each call returns the
 * Query, you can make complex queries in one sentence.
 * <p>
 * For example:
 * 
 * <pre>
 *  Database db = ...;
 *  Query q = db.query(Person.class).equals(&quot;type&quot;, &quot;Employee&quot;).greater(&quot;age&quot;, 20).limit(10).offset(10);
 *  q.execute();
 * </pre>
 * 
 * 
 * This example returns the second 10 Persons that are of type employee and
 * older than 20 years.
 */
public interface Query<E extends Entity>
{
	/** Translates a String into a QueryRule and adds it */
	public Query<E> filter(String filter);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *        'new QueryRule(field, Operator.EQUALS, value)'
	 * </pre>
	 */
	public Query<E> equals(String field, Object value);

	/**
	 * Shorthand for equals
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public Query<E> eq(String field, Object value);

	/**
	 * Shorthand for search all fields. Typically translates to 'like' on all
	 * textual fields
	 * 
	 * @param searchTerms
	 *            the terms to search on, space seperated
	 */
	public Query<E> search(String searchTerms) throws DatabaseException;

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *        'new QueryRule(field, Operator.IN, objectList)'
	 * </pre>
	 */
	public Query<E> in(String field, List<?> objectList);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(field, Operator.GREATER, value)'
	 * </pre>
	 */
	public Query<E> greater(String field, Object value);

	/**
	 * Shorthand for greater
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public Query<E> gt(String field, Object value);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(field, Operator.GREATER_EQUAL, value)'
	 * </pre>
	 */
	public Query<E> greaterOrEqual(String field, Object value);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *       'new QueryRule(field, Operator.LESS, value)'
	 * </pre>
	 */
	public Query<E> less(String field, Object value);

	/**
	 * Shorthand for lessThan
	 */
	public Query<E> lt(String field, Object value);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(field, Operator.LESS_EQUAL, value)'
	 * </pre>
	 */
	public Query<E> lessOrEqual(String field, Object value);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(field, Operator.LIKE, value)'
	 * </pre>
	 */
	public Query<E> like(String field, Object value);

	/**
	 * Between, inclusive
	 * 
	 * @param field
	 * @param min
	 *            minimum valid value
	 * @param max
	 *            maximum valid value
	 * @return Query
	 */
	public Query<E> between(String field, Object min, Object max);

	/** Add the 'or' option on last queryrule */
	public Query<E> or();

	/**
	 * Add the 'and' option on last query rule (default)
	 */
	public Query<E> and();

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(Operator.LAST)'
	 * </pre>
	 */
	public Query<E> last();

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(Operator.LIMIT, limit)'
	 * </pre>
	 */
	public Query<E> limit(int limit);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(Operator.OFFSET, offset)'
	 * </pre>
	 */
	public Query<E> offset(int offset);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(Operator.ORDER_ASC, orderByField)'
	 * </pre>
	 */
	public Query<E> sortASC(String orderByField);

	/**
	 * Shorthand for
	 * 
	 * <pre>
	 *      'new QueryRule(Operator.ORDER_DESC, orderByField)'
	 * </pre>
	 */
	public Query<E> sortDESC(String orderByField);

	/**
	 * Execute the query on a database.
	 * 
	 * Will return a List of E if the Query knows the Database and Class<E
	 * extends Entity> to use. Otherwise it will throw an
	 * UnsupportedOperationException().
	 * 
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<E> find() throws DatabaseException;

	/** Finder for csv data */
	public void find(TupleWriter writer) throws DatabaseException, ParseException;

	/** Finder for csv data with added option only export particular fields */
	public void find(TupleWriter writer, List<String> fieldsToExport) throws DatabaseException, ParseException;

	/**
	 * Finder for csv data with added option to only skip ids
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void find(TupleWriter writer, boolean skipAutoIds) throws DatabaseException, ParseException,
			InstantiationException, IllegalAccessException;

	/**
	 * Execute the query on a database.
	 * 
	 * Will return a List of E.
	 * 
	 * @param db
	 *            the Database to run this query on.
	 * @param klazz
	 *            the Class<E extends Entity> to run this query on.
	 * @throws ParseException
	 */
	public List<E> find(Database db, Class<E> klazz) throws DatabaseException, ParseException;

	/**
	 * Execute the query on a database and count the results.
	 * 
	 * Will return integer if the Query knows the Database and Class<E extends
	 * Entity> to use. Otherwise it will throw an
	 * UnsupportedOperationException().
	 * 
	 * @throws DatabaseException
	 */
	public int count() throws DatabaseException;

	/**
	 * Execute the query on a database and count the results.
	 * 
	 * Will return a List of E.
	 * 
	 * @param db
	 *            the Database to run this query on.
	 * @param klazz
	 *            the Class<E extends Entity> to run this query on.
	 */
	public int count(Database db, Class<E> klazz) throws DatabaseException;

	/** Retrieve array of current QueryRules in this query */
	public QueryRule[] getRules();

	/** Add rules to the query */
	public void addRules(QueryRule... addRules);

	/** Set the database to use for this query */
	public void setDatabase(Database db);

	/** Get the database to use for this query */
	public Database getDatabase();

	/** Create a query based on the non-null values of an example */
	public Query<E> example(Entity example);

	public void removeRule(QueryRule ruleToBeRemoved);

	/**
	 * Produce the sql that would be used to query
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public String createFindSql() throws DatabaseException;

	/**
	 * Method to create 'where field in (select * from ...)' subquery as
	 * condition
	 */
	Query<E> subquery(String field, String sql);

	public Query<E> subQuery(SubQueryRule subQueryRule);
}