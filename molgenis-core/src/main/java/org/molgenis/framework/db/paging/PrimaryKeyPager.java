package org.molgenis.framework.db.paging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.util.Entity;

/**
 * Page a database using the primary key.
 * 
 * This pager was added to allow paging through large datasets which is much
 * faster than OFFSET, given that it uses indexes of the primary key. The
 * benefits depend on the implementation of Database.
 * 
 * WARNING only faster when sorted by indexed fields!!
 * 
 * @param <E>
 */
@Deprecated
public class PrimaryKeyPager<E extends Entity> extends AbstractPager<E>
{
	private static final long serialVersionUID = 1707494068232123242L;

	private static final Logger logger = Logger.getLogger(PrimaryKeyPager.class);

	/**
	 * Constructor.
	 * 
	 * @param entityClass
	 *            class of the entity to be paged
	 * @param primaryKeyField
	 *            the primary key of the entity (must be unique and indexed)
	 * @throws DatabaseException
	 */
	public PrimaryKeyPager(Class<E> entityClass, String primaryKeyField) throws DatabaseException
	{
		super(entityClass, primaryKeyField);
		this.primaryKeyField = primaryKeyField;
		this.setPagingState(State.FIRST);
		// this.refresh();
	}

	/**
	 * Secondary sorting field. This is necessary if orderByField is not an
	 * indexed field. Typically this is the primary key of the entity.
	 */
	private String primaryKeyField;

	/** remember index thresholds */
	private Object nextOrderByThreshold;
	private Object prevOrderByThreshold;
	private Object nextPKeyThreshold;
	private Object prevPKeyThreshold;

	/**
	 * {@inheritDoc}. Implemented using 'primary key' QueryRules. This means
	 * that at least the primary key is used to sort the data in the database
	 * underlying the paging.
	 */
	@Override
	public void refresh(Database db) throws DatabaseException
	{
		// don't use getters and setters!!! these will call refresh resulting in
		// endless loops
		if (this.pagingState == State.UPTODATE) return;

		// get the rules
		List<QueryRule> rules = new ArrayList<QueryRule>();
		rules.addAll(Arrays.asList(this.getFilters()));

		logger.debug("refresh started with state '" + pagingState + "'");
		reloadCount(db, rules.toArray(new QueryRule[rules.size()]));

		// first: add sorting of order by field
		if (getOrderByField() != null || "".equals(getOrderByField()))
		{
			logger.debug("adding order by on " + getOrderByField());
			if (getOrderByOperator().equals(Operator.SORTASC)) rules.add(new QueryRule(Operator.SORTASC,
					getOrderByField()));
			else
				rules.add(new QueryRule(Operator.SORTDESC, getOrderByField()));
		}

		// second: add sorting by primary key (ensuring predictable ordering)
		if (!getOrderByField().equals(primaryKeyField))
		{
			logger.debug("adding order by on " + primaryKeyField);
			if (getOrderByOperator().equals(Operator.SORTASC)) rules.add(new QueryRule(Operator.SORTASC,
					primaryKeyField));
			else
				rules.add(new QueryRule(Operator.SORTDESC, primaryKeyField));

		}

		// based on the pagingState we add additional filterings
		switch (pagingState)
		{
			case REFRESH:
				if (count > offset)
				{
					rules.add(new QueryRule(Operator.LIMIT, limit));
					rules.add(new QueryRule(getOrderByField(), Operator.GREATER_EQUAL, prevOrderByThreshold));
					if (!getOrderByField().equals(primaryKeyField)) rules.add(new QueryRule(primaryKeyField,
							Operator.GREATER_EQUAL, prevPKeyThreshold));
					// FIXME how can we distinguish page when sort field is not
					// unique!!
					logger.debug("loaded filters for refresh. Added operator: limit=" + limit + ", offset=" + offset);
					break;
				}
				else
				{
					logger.debug("refresh is delegated to 'last' operation because count < offset (maybe cause of deletes)");
					pagingState = State.LAST;
					this.refresh(db);
					return;
				}
			case NEXT:
				if (limit + offset < count)
				{
					offset = limit + offset;
					rules.add(new QueryRule(Operator.LIMIT, Math.min(limit, count - offset)));
					rules.add(new QueryRule(getOrderByField(), Operator.GREATER, nextOrderByThreshold));
					if (!getOrderByField().equals(primaryKeyField)) rules.add(new QueryRule(primaryKeyField,
							Operator.GREATER, nextPKeyThreshold));
					// FIXME how can we distinguish page when sort field is not
					// unique!!

					// get first from next threshold (exclusive)
					logger.debug("loaded filters for next. Added operator: " + getOrderByField() + " > "
							+ nextOrderByThreshold + ". Offset is: " + offset + ") and " + primaryKeyField
							+ " greater than: " + nextPKeyThreshold);
					break;
				}
				else
				// page in 'last' range, go last
				{
					pagingState = State.LAST;
					logger.debug("next, is already in 'last' range (offset=" + offset + "), refresh to last.");
					this.refresh(db);
					return;
				}
			case LAST:
				// get last, limit to remaining from count
				if (count % limit != 0) rules.add(new QueryRule(Operator.LIMIT, count % limit));
				else
					rules.add(new QueryRule(Operator.LIMIT, limit));
				if (count > limit) offset = (int) Math.round(Math.floor((count - 1) / limit) * limit);
				else
					offset = 0;
				rules.add(new QueryRule(Operator.LAST));
				logger.debug("loaded filters for last. Added operator: 'last'. Offset is: " + offset);
				break;
			case PREV:
				// get last before prev threshold (exclusive)
				// set it to be the previous valid offset
				if (offset - limit >= 0)
				{
					offset = offset - limit;
					rules.add(new QueryRule(Operator.LIMIT, limit));
					rules.add(new QueryRule(Operator.LAST));
					rules.add(new QueryRule(getOrderByField(), Operator.LESS, prevOrderByThreshold));
					if (!getOrderByField().equals(primaryKeyField)) rules.add(new QueryRule(primaryKeyField,
							Operator.LESS, prevPKeyThreshold));
					// FIXME how can we distinguish page when sort field is not
					// unique!!
					logger.debug("prev, offset: " + offset + ", " + getOrderByField() + " < " + prevOrderByThreshold
							+ " and " + primaryKeyField + " greater than: " + prevPKeyThreshold);
					break;
				}
				else
				{ // delegate to first
					logger.debug("prev, is already in 'first' range (offset=" + offset + "), refresh to first");
					pagingState = State.FIRST;
					this.refresh(db);
					return;
				}
			case FIRST:
				// get first, no additional filtering needed
				offset = 0;
				rules.add(new QueryRule(Operator.LIMIT, limit));
				logger.debug("loaded filters for first: no filters needed");
				break;
		}

		reloadPage(db, rules.toArray(new QueryRule[rules.size()]));

		// remember prevThresholds and nextThresholds for next time (only for
		// prev/next)
		if (page.size() > 0)
		{
			prevOrderByThreshold = page.get(0).get(getOrderByField());
			prevPKeyThreshold = page.get(0).get(primaryKeyField);
			nextOrderByThreshold = page.get(page.size() - 1).get(getOrderByField());
			nextPKeyThreshold = page.get(page.size() - 1).get(this.primaryKeyField);
		}
		else
		{
			logger.error("should never happen unless the db was changed between count and find");
		}

		// don't forget!
		pagingState = State.UPTODATE;
	}
}
