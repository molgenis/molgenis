package org.molgenis.framework.tupletable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.fieldtypes.StringField;
import org.molgenis.framework.tupletable.AbstractTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/**
 * Wrap a List<Tuple> into a TupleTable
 */
public class MemoryTable extends AbstractTupleTable
{
	private List<Field> columns = new ArrayList<Field>();
	private List<Tuple> rows = new ArrayList<Tuple>();

	/**
	 * Construct from list of tuples. Field will be derived based on column
	 * names and value type of first tuple. Otherwise field type will be String.
	 */
	public MemoryTable(List<Tuple> rows)
	{
		if (rows == null) throw new NullPointerException("Creation of MemoryTable failed: rows == null");

		this.rows = rows;

		// use first row
		if (rows.size() > 0)
		{
			for (String field : rows.get(0).getColNames())
			{
				Field f = new Field(field);
				f.setType(new StringField());
				columns.add(f);
			}
		}
	}

	@Override
	public List<Field> getAllColumns()
	{
		return this.columns;
	}

	@Override
	public List<Tuple> getRows() throws TableException
	{
		List<String> columns = new ArrayList<String>();
		for (Field f : getColumns())
			columns.add(f.getName());

		List<Tuple> result = new ArrayList<Tuple>();
		if (getLimit() > 0 || getOffset() > 0)
		{
			int count = 0;
			int index = 1;
			for (Tuple row : this.rows)
			{
				if (index > getOffset())
				{
					KeyValueTuple tuple = new KeyValueTuple();
					for (String col : columns)
						tuple.set(col, row.get(col));
					result.add(tuple);

					count++;
					if (count >= getLimit()) break;
				}
				index++;
			}
		}
		else
		{
			for (Tuple row : this.rows)
			{
				WritableTuple tuple = new KeyValueTuple();
				for (String col : columns)
					tuple.set(col, row.get(col));
				result.add(tuple);
			}
		}
		return result;
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		try
		{
			return this.getRows().iterator();
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getCount() throws TableException
	{
		return rows.size();
	}
}
