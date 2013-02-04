package org.molgenis.framework.tupletable;

import java.util.Iterator;
import java.util.List;

import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.Tuple;

/**
 * Iterator to be used with an AbstractTupleTable. Takes into account limit,
 * offset, colLimit and colOffset of the TupleTable
 * 
 * If you use this make sure to override getValues(row, colStart, colEnd) in
 * AbstractTupleTable
 * 
 * @author erwin
 * 
 */
public class TupleTableIterator implements Iterator<Tuple>
{
	private AbstractTupleTable tupleTable;
	private int row = 0;

	public TupleTableIterator(AbstractTupleTable tupleTable) throws TableException
	{
		this.tupleTable = tupleTable;
	}

	@Override
	public boolean hasNext()
	{
		try
		{
			if (tupleTable.getOffset() + row >= tupleTable.getCount()
					|| (tupleTable.getLimit() > 0 && row >= tupleTable.getLimit()))
			{
				return false;
			}

			return true;
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Tuple next()
	{

		Tuple tuple;
		try
		{
			List<Field> columns = tupleTable.getColumns();

			if (tupleTable.isFirstColumnFixed())
			{
				columns.add(tupleTable.getAllColumns().get(0));
			}

			tuple = tupleTable.getValues(tupleTable.getOffset() + row, columns);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}

		row++;

		return tuple;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
