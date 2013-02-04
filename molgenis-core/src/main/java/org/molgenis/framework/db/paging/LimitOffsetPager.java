package org.molgenis.framework.db.paging;

import java.text.ParseException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryImp;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.model.elements.Field;
import org.molgenis.util.Entity;

/**
 * Page a database using limit and offset.
 * 
 * This works perfectly for small data sets. However, this becomes expensive if
 * one has large datasets with large offsets because then much data has to be
 * "thrown away" until the offset of desired data is reached.
 * 
 * @param <E>
 */
public class LimitOffsetPager<E extends Entity> extends AbstractPager<E>
{
	private static final long serialVersionUID = -1859965580920588085L;
	private static final Logger logger = Logger.getLogger(LimitOffsetPager.class);

	public LimitOffsetPager(Class<E> entityClass, String defaultOrderByField) throws DatabaseException
	{
		super(entityClass, defaultOrderByField);
		this.defaultOrderByField = defaultOrderByField;
		// ensure it is loaded on first use
		this.pagingState = State.REFRESH;
	}

	/**
	 * {@inheritDoc}. Implemented using 'limit' and 'offset' QueryRules. These
	 * rules are then passed to the Database underlying this pager.
	 * 
	 * @throws ParseException
	 */
	@Override
	public void refresh(Database db) throws DatabaseException
	{
		// don't use getters and setters here, or everything refreshes like
		// crazy!
		// if still updated, return
		// FIXME add a way to force refresh to see wether db changed (in
		// practice not an issue).
		if (this.pagingState == State.UPTODATE) return;

		// get the rules
		QueryRule[] rules = this.getFilters();

		// reload the count
		Query<E> q = new QueryImp<E>();
		q.addRules(rules);
		try
		{
			for (String fieldName : this.getEntityClass().newInstance().getFields())
			{
				if (fieldName.equals(Field.TYPE_FIELD)) q.equals(Field.TYPE_FIELD, this.getEntityClass()
						.getSimpleName());
			}
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		reloadCount(db, q.getRules());

		// correct current offset to be in line with limit (with limit > 0)
		int oldOffset = offset;
		offset = ((int) Math.floor(offset / limit) * limit);
		if (offset != oldOffset) logger.warn("corrected offset from " + oldOffset + " to " + offset);

		// handle state transition
		switch (pagingState)
		{
			case FIRST:
				offset = 0;
				logger.debug("handled first, offset: " + offset + ", limit: " + limit + ", count: " + count);
				break;
			case NEXT:
				// set it to the next valid offset
				if (count > limit + offset) offset = limit + offset;
				logger.debug("handled next, offset: " + offset + ", limit: " + limit + ", count: " + count);
				break;
			case PREV:
				// set it to be the previous valid offset
				if (offset - limit >= 0) offset = offset - limit;
				logger.debug("handled prev, offset: " + offset + ", limit: " + limit + ", count: " + count);
				break;
			case REFRESH:
				// check if we need to page prev because of deletes, then fall
				// through to 'last' else break
				if (count > offset)
				{
					// nothing to do here :-)
					logger.debug("handled refresh, offset: " + offset + ", limit: " + limit + ", count: " + count);
					break;
				}
				else
				{
					logger.debug("handled refresh, count was lower than offset so moving to show last");
				}
			case LAST:
				// set it to be the last valid offset
				if (count > limit) offset = (int) Math.round(Math.floor((count - 1) / limit) * limit);
				else
					offset = 0;
				logger.debug("handled last, offset: " + offset + ", limit: " + limit + ", count: " + count);
				break;
		}

		// build a query
		q.offset(offset);
		q.limit(limit);

		// ordering rules
		if (this.getOrderByField() != null)
		{
			if (this.getOrderByOperator().equals(Operator.SORTASC)) q.sortASC(getOrderByField());
			else
				q.sortDESC(getOrderByField());
		}

		// reload the page and return
		if (this.count > 0)
		{
			reloadPage(db, q.getRules());
		}
		else
		{
			this.setPage(new ArrayList<E>());
		}
		logger.debug("COUNT IS " + count);

		this.pagingState = State.UPTODATE;
	}
}
