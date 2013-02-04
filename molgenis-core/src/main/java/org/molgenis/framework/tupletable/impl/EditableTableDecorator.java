package org.molgenis.framework.tupletable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.tupletable.FilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/** Will change all row values to become edit boxes */
public class EditableTableDecorator implements FilterableTupleTable
{
	FilterableTupleTable decoratedTable;

	public EditableTableDecorator(FilterableTupleTable decoratedTable)
	{
		this.decoratedTable = decoratedTable;
	}

	@Override
	public List<Field> getColumns() throws TableException
	{
		return decoratedTable.getColumns();
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		return decoratedTable.getAllColumns();
	}

	@Override
	public List<Tuple> getRows() throws TableException
	{
		try
		{
			List<Tuple> editableRows = new ArrayList<Tuple>();

			for (Tuple t : this.decoratedTable.getRows())
			{
				WritableTuple tuple = new KeyValueTuple();
				for (String colName : t.getColNames())
				{
					tuple.set(colName,
							"<input style=\"width:100%; padding:0px \" type=\"text\" value=\"" + t.get(colName) + "\">");

				}

				editableRows.add(tuple);
			}

			return editableRows;
		}
		catch (Exception e)
		{
			throw new TableException(e);
		}
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		return decoratedTable.iterator();
	}

	@Override
	public void close() throws TableException
	{
		decoratedTable.close();
	}

	@Override
	public int getCount() throws TableException
	{
		return decoratedTable.getCount();
	}

	@Override
	public int getColCount() throws TableException
	{
		return decoratedTable.getColCount();
	}

	@Override
	public int getLimit()
	{
		return decoratedTable.getLimit();
	}

	@Override
	public int getColLimit()
	{
		return decoratedTable.getColLimit();
	}

	@Override
	public void setLimit(int limit)
	{
		decoratedTable.setLimit(limit);
	}

	@Override
	public void setColLimit(int limit)
	{
		decoratedTable.setColLimit(limit);
	}

	@Override
	public int getOffset()
	{
		return decoratedTable.getOffset();
	}

	@Override
	public int getColOffset()
	{
		return decoratedTable.getColOffset();
	}

	@Override
	public void setOffset(int offset)
	{
		decoratedTable.setOffset(offset);
	}

	@Override
	public void setColOffset(int offset)
	{
		decoratedTable.setColOffset(offset);
	}

	@Override
	public void reset()
	{
		decoratedTable.reset();
	}

	@Override
	public void setLimitOffset(int limit, int offset)
	{
		decoratedTable.setLimitOffset(limit, offset);

	}

	@Override
	public void setFilters(List<QueryRule> rules) throws TableException
	{
		decoratedTable.setFilters(rules);
	}

	@Override
	public List<QueryRule> getFilters()
	{
		return decoratedTable.getFilters();
	}

	@Override
	public QueryRule getSortRule()
	{
		return decoratedTable.getSortRule();
	}

	@Override
	public void hideColumn(String columnName)
	{
		decoratedTable.hideColumn(columnName);
	}

	@Override
	public void showColumn(String columnName)
	{
		decoratedTable.showColumn(columnName);
	}

	@Override
	public List<Field> getHiddenColumns()
	{
		return decoratedTable.getHiddenColumns();
	}

	@Override
	public void setFirstColumnFixed(boolean firstColumnFixed)
	{
		decoratedTable.setFirstColumnFixed(firstColumnFixed);
	}

	@Override
	public boolean isFirstColumnFixed()
	{
		return decoratedTable.isFirstColumnFixed();
	}

}
