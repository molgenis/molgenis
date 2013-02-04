package org.molgenis.framework.db.paging;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.util.Entity;

/**
 * Common implementation for implementing a pager.
 * 
 * @param <E>
 */
public abstract class AbstractPager<E extends Entity> implements DatabasePager<E>
{
	private static final long serialVersionUID = -5113663340351891980L;

	/**
	 * State of paging, that is, what is the last known event that has to be
	 * processed.
	 */
	protected enum State
	{
		FIRST, PREV, NEXT, LAST, UPTODATE, REFRESH;
	}

	/** Class of this pager */
	private Class<E> entityClass;

	/** Name of the field to order by */
	private String orderByField;

	/** default order by field (to allow a reset) */
	String defaultOrderByField;

	/** limit of the pager */
	protected int limit = 10;

	/** ASC or DESC */
	private Operator orderByOperator = Operator.SORTASC;

	/** Current state of the pager */
	protected State pagingState = State.FIRST;

	/** offset of the pager */
	protected int offset = 0;

	/** entity count */
	protected int count = 0;

	/** rules */
	private List<QueryRule> filters = new ArrayList<QueryRule>();

	/** Logger **/
	private final static Logger logger = Logger.getLogger(AbstractPager.class);

	/**
	 * @param entityClass
	 *            definition of an entity
	 * @param defaultOrderByField
	 *            name of field that will be used to order database entities by.
	 * @throws DatabaseException
	 */
	public AbstractPager(Class<E> entityClass, String defaultOrderByField)
	{
		assert entityClass != null && defaultOrderByField != null && !defaultOrderByField.equals("");

		this.entityClass = entityClass;
		this.orderByField = defaultOrderByField;
		this.defaultOrderByField = defaultOrderByField;

		logger.debug("created for " + entityClass.getCanonicalName() + ", ordered by " + defaultOrderByField);
	}

	/** cache of current page */
	protected List<E> page = new ArrayList<E>();

	@Override
	public int getCount(Database db) throws DatabaseException
	{
		this.refresh(db);
		return count;
	}

	@Override
	public int getCount()
	{
		return count;
	}

	@Override
	public List<E> getPage(Database db) throws DatabaseException
	{
		this.refresh(db);
		return page;
	}

	@Override
	public List<E> first(Database db) throws DatabaseException
	{
		pagingState = State.FIRST;
		logger.debug("go to first " + this.getLimit());
		return this.getPage(db);
	}

	@Override
	public List<E> prev(Database db) throws DatabaseException
	{
		// relative, so need to refresh first
		this.refresh(db);
		pagingState = State.PREV;
		logger.debug("go to previous " + this.getLimit());
		return this.getPage(db);
	}

	@Override
	public List<E> next(Database db) throws DatabaseException
	{
		// relative, so need to refresh first
		this.refresh(db);
		pagingState = State.NEXT;
		logger.debug("go to next " + this.getLimit());
		return this.getPage(db);
	}

	@Override
	public List<E> last(Database db) throws DatabaseException
	{
		pagingState = State.LAST;
		logger.debug("go to last " + this.getLimit());
		return this.getPage(db);
	}

	@Override
	public int getLimit()
	{
		return limit;
	}

	@Override
	public void setLimit(int limit) throws DatabaseException
	{
		// FIXME: need safety break!
		if (limit <= 0) throw new IllegalArgumentException("limit must be a positive number");
		if (this.limit != limit)
		{
			this.limit = limit;
			logger.debug("set limit to " + this.getLimit());
			this.pagingState = State.REFRESH;
		}
	}

	@Override
	public String getOrderByField()
	{
		return orderByField;
	}

	@Override
	public void setOrderByField(String orderByField) throws DatabaseException
	{
		if (orderByField == null || orderByField.equals("")) throw new DatabaseException("orderByField cannot be null");

		// pagingState = State.FIRST;
		this.orderByField = orderByField;
		logger.debug("set order by field to '" + orderByField + "'");
	}

	@Override
	public Operator getOrderByOperator()
	{
		return orderByOperator;
	}

	@Override
	public void setOrderByOperator(Operator orderByOperator) throws DatabaseException
	{
		if (!orderByOperator.equals(Operator.SORTASC) && !orderByOperator.equals(Operator.SORTDESC)) throw new IllegalArgumentException(
				"orderByOperator cannot be " + orderByOperator);

		this.orderByOperator = orderByOperator;
		logger.debug("set order by operator to '" + orderByOperator + "'");
	}

	@Override
	public void resetOrderBy() throws DatabaseException
	{
		this.setOrderByField(this.defaultOrderByField);
		this.setOrderByOperator(Operator.SORTASC);
		// pagingState = State.FIRST;
		logger.debug("reset order by to default: field '" + orderByField + "', operator '" + orderByOperator + "'");
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	@Override
	public void setDirty(boolean dirty)
	{
		if (dirty) this.pagingState = State.REFRESH;
	}

	@Override
	public void setOffset(int offset)
	{
		this.offset = offset;
		this.pagingState = State.REFRESH;
		logger.debug("changed offset to: " + offset);
		// FIXME what are the consequences for keybased???
	}

	@Override
	public void addFilter(QueryRule filter) throws DatabaseException
	{
		if (filter != null)
		{
			filters.add(filter);
			logger.debug("added filter: " + filter);
		}
	}

	@Override
	public QueryRule[] getFilters()
	{
		return filters.toArray(new QueryRule[filters.size()]);
	}

	@Override
	public void removeFilter(int index) throws DatabaseException
	{
		if (index >= 0 && index < filters.size())
		{
			filters.remove(index);
			logger.debug("removed filter: " + filters.get(index));
		}
	}

	@Override
	public void resetFilters()
	{
		resetFilters(new ArrayList<QueryRule>());
	}

	@Override
	public void resetFilters(List<QueryRule> filters)
	{
		this.filters = filters;
	}

	// PROTECTED HELPERS
	protected void reloadCount(Database db, QueryRule... rules) throws DatabaseException
	{
		count = db.count(entityClass, rules);
	}

	protected void reloadPage(Database db, QueryRule... rules) throws DatabaseException
	{
		setPage(db.find(entityClass, rules));
	}

	/** will check whether it is dirty, the refresh */
	@Override
	public abstract void refresh(Database db) throws DatabaseException;

	protected State getPagingState()
	{
		return pagingState;
	}

	protected void setPagingState(State pageState)
	{
		this.pagingState = pageState;
	}

	protected void setPage(List<E> page)
	{
		this.page = page;
	}

	public Class<E> getEntityClass()
	{
		return entityClass;
	}

	public void setEntityClass(Class<E> entityClass)
	{
		this.entityClass = entityClass;
	}
}
