package org.molgenis.framework.tupletable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.Tuple;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public abstract class AbstractTupleTable implements TupleTable
{
	private int limit = 0;
	private int offset = 0;
	private int colOffset = 0;
	private int colLimit = 0;
	private boolean firstColumnFixed;
	private Map<String, Integer> columnByIndex;

	@Override
	public void reset()
	{
		limit = 0;
		offset = 0;
		colOffset = 0;
		colLimit = 0;
	}

	@Override
	public void setFirstColumnFixed(boolean firstColumnFixed)
	{
		this.firstColumnFixed = firstColumnFixed;
	}

	@Override
	public boolean isFirstColumnFixed()
	{
		return firstColumnFixed;
	}

	@Override
	public void hideColumn(String columnName)
	{
		try
		{
			// If the first column is fixed, it can not be hidden
			if (isFirstColumnFixed() && getAllColumns().get(0).getName().equals(columnName))
			{
				return;
			}

			getColumnByName(columnName).setHidden(true);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void showColumn(String columnName)
	{
		try
		{
			getColumnByName(columnName).setHidden(false);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Field> getHiddenColumns()
	{
		try
		{
			Collection<Field> hiddenColumns = Collections2.filter(getAllColumns(), new Predicate<Field>()
			{
				@Override
				public boolean apply(Field f)
				{
					return f.isHidden();
				}

			});

			return new ArrayList<Field>(hiddenColumns);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}

	}

	protected List<Field> getVisibleColumns() throws TableException
	{
		List<Field> visibleColumns = new ArrayList<Field>();
		for (Field column : getAllColumns())
		{
			if (!column.isHidden())
			{
				visibleColumns.add(column);
			}
		}

		return visibleColumns;
	}

	@Override
	public int getLimit()
	{
		return limit;
	}

	@Override
	public void setLimit(int limit)
	{
		if (limit < 0) throw new RuntimeException("limit cannot be < 0");
		this.limit = limit;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	@Override
	public void setOffset(int offset)
	{
		if (offset < 0) throw new RuntimeException("offset cannot be < 0");
		this.offset = offset;
	}

	@Override
	public abstract List<Field> getAllColumns() throws TableException;

	@Override
	public List<Field> getColumns() throws TableException
	{
		List<Field> result;
		List<Field> columns = getVisibleColumns();

		int colCount = columns.size();

		if (getColOffset() > colCount)
		{
			setColOffset(colCount);
		}

		if (isFirstColumnFixed())
		{
			columns.remove(0);
			colCount--;
		}

		int colLimit = this.colLimit == 0 ? colCount - getColOffset() : getCurrentColumnPageSize(colCount);

		if (getColOffset() > 0)
		{
			if (colLimit > 0)
			{
				result = columns.subList(getColOffset(), Math.min(getColOffset() + colLimit, colCount));
			}
			else
			{
				result = columns.subList(getColOffset(), colCount);
			}
		}
		else
		{
			if (colLimit > 0)
			{
				result = columns.subList(0, colLimit);
			}
			else
			{
				result = columns;
			}
		}

		if (isFirstColumnFixed())
		{
			result.add(0, getAllColumns().get(0));
		}

		return result;
	}

	@Override
	public List<Tuple> getRows() throws TableException
	{
		List<Tuple> result = new ArrayList<Tuple>();
		for (Tuple t : this)
		{
			result.add(t);
		}
		return result;
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		try
		{
			return new TupleTableIterator(this);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws TableException
	{
		// close resources if applicable
	}

	@Override
	public abstract int getCount() throws TableException;

	@Override
	public int getColCount() throws TableException
	{
		return getVisibleColumns().size();
	}

	@Override
	public void setColLimit(int limit)
	{
		if (limit < 0) throw new RuntimeException("colLimit cannot be < 0");
		this.colLimit = limit;
	}

	@Override
	public int getColLimit()
	{
		return colLimit;
	}

	@Override
	public int getColOffset()
	{
		return colOffset;
	}

	@Override
	public void setColOffset(int offset)
	{
		if (offset < 0) throw new RuntimeException("colOffset cannot be < 0");
		this.colOffset = offset;
	}

	@Override
	public void setLimitOffset(int limit, int offset)
	{
		this.setLimit(limit);
		this.setOffset(offset);
	}

	protected int getCurrentColumnPageSize(int colCount) throws TableException
	{
		int pageSize = getColLimit();

		if (getColOffset() + pageSize > colCount)
		{
			pageSize = colCount - getColOffset();
		}

		return pageSize;
	}

	/**
	 * Please override in subclass if you use the TupleTableIterator !!!!
	 * 
	 * @throws TableException
	 */
	protected Tuple getValues(int row, List<Field> columns) throws TableException
	{
		return getRows().get(row);
	}

	protected int getColumnIndex(String columnName) throws TableException
	{
		if (columnByIndex == null)
		{
			columnByIndex = new HashMap<String, Integer>();

			List<Field> columns = getAllColumns();
			for (int i = 0; i < columns.size(); i++)
			{
				columnByIndex.put(columns.get(i).getName(), i);
			}

		}

		Integer index = columnByIndex.get(columnName);

		if (index == null)
		{
			throw new TableException("Unknown columnName [" + columnName + "]");
		}

		return index;
	}

	protected Field getColumnByName(String columnName) throws TableException
	{
		for (Field field : getAllColumns())
		{
			if (field.getName().equals(columnName))
			{
				return field;
			}
		}

		throw new TableException("Unknown columnName [" + columnName + "]");
	}

	/**
	 * Checks if the column is in the current view port (the columns that the
	 * user sees in the table on the screen)
	 * 
	 * @param columnName
	 * @return
	 * @throws TableException
	 */
	protected boolean isInViewPort(String columnName) throws TableException
	{
		for (Field field : getColumns())
		{
			if (field.getName().equals(columnName))
			{
				return true;
			}
		}

		return false;
	}
}
