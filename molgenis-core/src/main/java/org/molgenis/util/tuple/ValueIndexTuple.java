package org.molgenis.util.tuple;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Values row backed by a {@link java.util.List} with an optional column names
 * index
 */
public class ValueIndexTuple extends AbstractTuple
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Integer> colNamesMap;
	private final List<? extends Object> values;

	public ValueIndexTuple(Map<String, Integer> colNamesMap, List<? extends Object> values)
	{
		if (colNamesMap == null) throw new IllegalArgumentException("column names map is null");
		if (values == null) throw new IllegalArgumentException("values is null");
		this.colNamesMap = colNamesMap;
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
		return true;
	}

	@Override
	public Iterable<String> getColNames()
	{
		return Collections.unmodifiableSet(colNamesMap.keySet());
	}

	@Override
	public Object get(String colName)
	{
		Integer pos = colNamesMap.get(colName);
		try
		{
			return pos != null ? values.get(pos) : null;
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new IndexOutOfBoundsException("missing required value for column " + colName);
		}
	}

	@Override
	public Object get(int col)
	{
		return values.get(col);
	}
}
