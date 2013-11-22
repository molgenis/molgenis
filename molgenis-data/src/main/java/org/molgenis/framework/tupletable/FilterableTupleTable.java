package org.molgenis.framework.tupletable;

import java.util.List;

import org.molgenis.data.QueryRule;

/**
 * Extension of TupleTable that allows filterings, sortings, etc on the table.
 */
public interface FilterableTupleTable extends TupleTable
{
	/**
	 * Set the row filters, based on column names. Not allowed are LIMIT, OFFSET and SORT filters (@see setLimit,
	 * setOffset), which will throw TableException.
	 * 
	 * @throws TableException
	 */
	public void setFilters(List<QueryRule> rules) throws TableException;

	/**
	 * Get the current set of filters
	 */
	public List<QueryRule> getFilters();

}