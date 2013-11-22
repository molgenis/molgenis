package org.molgenis.framework.tupletable;

import java.util.Iterator;
import java.util.List;

import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.Tuple;

/**
 * TupleTable is a simple abstraction for tabular data (columns X rows).
 * <ul>
 * <li>Column names are unique
 * <li>Column meta data (name,type,label,description,..) for currently visible
 * columns are available in getColumns()
 * <li>Row data is available as Tuple (hashMap)
 * </ul>
 * 
 * Purpose: this can be used as facade pattern to make views that are reusable
 * across heterogeneous backends without extra work, such as database tables,
 * Excel files, EAV models, binary formats, etc.
 */
public interface TupleTable extends Iterable<Tuple>
{
	public void hideColumn(String columnName);

	public void showColumn(String columnName);

	public List<Field> getHiddenColumns();

	/** Wether the first column is 'fixed', must always be visible **/
	public void setFirstColumnFixed(boolean firstColumnFixed);

	public boolean isFirstColumnFixed();

	/**
	 * Get meta data describing the columns in current view (within
	 * colLimit/colOffset)
	 */
	public List<Field> getColumns() throws TableException;

	/**
	 * Get all columns, including currently not visible
	 */
	public List<Field> getAllColumns() throws TableException;

	/** Get the data rows in current view (within limit/offset) */
	public List<Tuple> getRows() throws TableException;

	/**
	 * Get the data rows in current view in a streaming fashion (good for large
	 * data sets)
	 */
	@Override
	public Iterator<Tuple> iterator();

	/** Closes the resources from which table reads data (optional) */
	public void close() throws TableException;

	/**
	 * Get count of all records in the TupleTable
	 * 
	 * @throws TableException
	 */
	public int getCount() throws TableException;

	/**
	 * Get count of all columns in the TupleTable
	 * 
	 * @throws TableException
	 */
	public int getColCount() throws TableException;

	/**
	 * get row limit
	 */
	public int getLimit();

	/**
	 * get column limit
	 */
	public int getColLimit();

	/**
	 * set row limit (i.e. limit the current viewport to the full underlying
	 * table). Default: 0 (no limit).
	 */
	public void setLimit(int limit);

	/**
	 * set column limit (i.e. limit the current viewport to the full underlying
	 * table). Default: 0 (no limit)
	 */
	public void setColLimit(int limit);

	/**
	 * get row offset. Default: 0
	 */
	public int getOffset();

	/**
	 * get column offset. Default: 0
	 */
	public int getColOffset();

	/**
	 * set row limit (i.e. limit the current viewport to the full underlying
	 * table). Default: 0 (no offset)
	 */
	public void setOffset(int offset);

	/**
	 * set column limit (i.e. limit the current viewport to the full underlying
	 * table). Default: 0 (no offset)
	 */
	public void setColOffset(int offset);

	/** Reset the table, i.e. remove limits and filters */
	public void reset();

	/**
	 * shorthand for setLimit(),setOffset()
	 * 
	 * @param limit
	 * @param offset
	 */
	public void setLimitOffset(int limit, int offset);
}
