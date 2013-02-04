package org.molgenis.util.tuple;

import java.util.Collections;

/**
 * Tuple backed by a single value
 */
public class SingletonTuple<T> extends AbstractTuple
{
	private static final long serialVersionUID = 1L;

	private final String colName;
	private final T value;

	public SingletonTuple(String colName, T value)
	{
		if (colName == null) throw new IllegalArgumentException("col name is null");
		this.colName = colName;
		this.value = value;
	}

	@Override
	public int getNrCols()
	{
		return 1;
	}

	@Override
	public Iterable<String> getColNames()
	{
		return Collections.singletonList(colName);
	}

	@Override
	public Object get(String colName)
	{
		return this.colName.equals(colName) ? value : null;
	}

	@Override
	public Object get(int col)
	{
		throw new UnsupportedOperationException();
	}
}
