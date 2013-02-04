package org.molgenis.framework.db.paging;

import java.io.Serializable;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.util.Entity;

/**
 * Page through the entities in a Database.
 * <p>
 * Very useful if one doesn't want to retrieve all data at once into memory.
 * Instead, one can just iterate through the data while leaving the bulk of data
 * safely and efficiently on disk. The DatabasePager takes care of (re)querying
 * the Database.
 * <p>
 * TODO: add a method to go to a certain page.
 * 
 * @param <E>
 *            the specific entity type to be paged.
 * 
 */
public interface DatabasePager<E extends Entity> extends Serializable
{
	/**
	 * Go to first page and return current page.
	 * 
	 * @return refreshed page
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<E> first(Database db) throws DatabaseException;

	/**
	 * Go to previous page and return current page.
	 * 
	 * @return refreshed page
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<E> prev(Database db) throws DatabaseException;

	/**
	 * Go to next page and return current page.
	 * 
	 * @return refreshed page
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<E> next(Database db) throws DatabaseException;

	/**
	 * Go to last page and return current page.
	 * 
	 * @return refreshed page
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<E> last(Database db) throws DatabaseException;

	/**
	 * Refresh page, reloading dat from database db.
	 * 
	 * @param db
	 * @throws DatabaseException
	 */
	public void refresh(Database db) throws DatabaseException;

	/**
	 * Retrieve current limit, that is, the number of entities to be retrieved
	 * in one page.
	 * 
	 * @return current limit
	 */
	public int getLimit();

	/**
	 * Changes the limit, that is, the number of entities to be retrieved in one
	 * page. If necessary the offset will be updated to make sure that each page
	 * is of 'limit' size (except the last).
	 * 
	 * @throws DatabaseException
	 */
	public void setLimit(int limit) throws DatabaseException;

	/**
	 * Retrieve the current offset, that is, the index of the first entity to be
	 * retrieved in the current page.
	 * 
	 * @return current offset.
	 */
	public int getOffset();

	/**
	 * Update the offset to match index.
	 * <p>
	 * If necessary the offset will be rounded down to ensure that it is a
	 * multiplication of limit, that is, offset % limit == 0.
	 * 
	 * @param index
	 */
	public void setOffset(int index);

	/**
	 * Retrieve the name of the field that the pages are currently ordered by.
	 * 
	 * @return current order by field name.
	 */
	public String getOrderByField();

	/**
	 * Set the field to order the page by. If changed, the offset will be re-set
	 * to first().
	 * 
	 * @param orderByField
	 *            name
	 * @throws DatabaseException
	 */
	public void setOrderByField(String orderByField) throws DatabaseException;

	/**
	 * Retrieve current order-by operator, either
	 * {@link org.molgenis.framework.db.QueryRule.Operator#SORTASC} or
	 * {@link org.molgenis.framework.db.QueryRule.Operator#SORTDESC}
	 * 
	 * @return Operator
	 */
	public Operator getOrderByOperator();

	/**
	 * Set the order-by operator, , either
	 * {@link org.molgenis.framework.db.QueryRule.Operator#SORTASC} or
	 * {@link org.molgenis.framework.db.QueryRule.Operator#SORTDESC}. If
	 * changed, the offset will be re-set to first() as all pages are
	 * re-ordered.
	 * 
	 * @param orderByOperator
	 * @throws DatabaseException
	 */
	public void setOrderByOperator(Operator orderByOperator) throws DatabaseException;

	/**
	 * Add a filter to the filter list. If not yet in, the offset will be re-st
	 * to first();
	 * 
	 * @param rule
	 * @throws DatabaseException
	 */
	public void addFilter(QueryRule rule) throws DatabaseException;

	/**
	 * Retrieve the current list of filters as array. The index of this filters
	 * can be used to remove specific filters. @see #removeFilter(int)
	 * 
	 * @return current filters.
	 */
	public QueryRule[] getFilters();

	/**
	 * Remove a specific filter by index.
	 * 
	 * @param index
	 *            of the filter to be removed.
	 * @throws DatabaseException
	 */
	public void removeFilter(int index) throws DatabaseException;

	/**
	 * Reset the orderByField and orderByOperator to default as passed during
	 * construction of this DatabasePager.
	 * 
	 * @throws DatabaseException
	 */
	public void resetOrderBy() throws DatabaseException;

	/**
	 * Reset the filters to default as passed during construction of this
	 * DatabasePager (effectively removing all user defined filters).
	 */
	public void resetFilters();

	/**
	 * Reset the filters to the given filters
	 * 
	 * @param filters
	 */
	public void resetFilters(List<QueryRule> filters);

	/**
	 * Retrieve the current number of entities in the database, after filtering.
	 * 
	 * @return current count of entities in the Database.
	 * @throws DatabaseException
	 */
	public int getCount(Database db) throws DatabaseException;

	/**
	 * Retrieve the current number of entities in the database, without
	 * reloading the database.
	 */
	public int getCount();

	/**
	 * Retrieve the current page as based on offset and limit, that is,
	 * entity[offset | offset >= 0 && offset < limit] until entity[offset+limit
	 * || count]
	 * 
	 * @return current page of entities with length getLimit().
	 * @throws DatabaseException
	 */
	public List<E> getPage(Database db) throws DatabaseException;

	/**
	 * Force reload.
	 * 
	 * @param dirty
	 */
	void setDirty(boolean dirty);
}