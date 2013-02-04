package org.molgenis.util.tuple;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tuple backed by a {@link java.util.Map}
 */
public class KeyValueTuple extends AbstractTuple implements WritableTuple
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> valueMap;

	public KeyValueTuple()
	{
		this.valueMap = new LinkedHashMap<String, Object>();
	}

	/**
	 * Copy constructor
	 * 
	 * @param t
	 */
	public KeyValueTuple(Tuple t)
	{
		this();
		this.set(t);
	}

	@Override
	public int getNrCols()
	{
		return valueMap.size();
	}

	@Override
	public Iterable<String> getColNames()
	{
		return Collections.unmodifiableSet(valueMap.keySet());
	}

	@Override
	public Object get(String colName)
	{
		return valueMap.get(colName);
	}

	@Override
	public Object get(int col)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(String colName, Object val)
	{
		valueMap.put(colName, val);
	}

	@Override
	public void set(Tuple t)
	{
		for (String col : t.getColNames())
		{
			this.set(col, t.get(col));
		}
	}
}
