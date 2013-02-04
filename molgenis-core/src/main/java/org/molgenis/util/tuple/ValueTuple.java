package org.molgenis.util.tuple;

import java.util.List;

/**
 * Values row backed by a {@link java.util.List} with an optional column names
 * index
 */
public class ValueTuple extends AbstractTuple
{
	private static final long serialVersionUID = 1L;

	private final List<? extends Object> values;

	public ValueTuple(List<? extends Object> values)
	{
		if (values == null) throw new IllegalArgumentException("values is null");
		this.values = values;
	}

	@Override
	public int getNrCols()
	{
		return values.size();
	}

	@Override
	public boolean hasColNames()
	{
		return false;
	}

	@Override
	public Iterable<String> getColNames()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(String colName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(int col)
	{
		return values.get(col);
	}
}
