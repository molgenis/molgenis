package org.molgenis.util.tuple;

/**
 * {@link KeyValueTuple} with case insensitive column names
 */
public class CaseInsensitiveKeyValueTuple extends KeyValueTuple
{
	private static final long serialVersionUID = 1L;

	@Override
	public Object get(String colName)
	{
		return super.get(colName.toLowerCase());
	}

	@Override
	public void set(String colName, Object val)
	{
		super.set(colName.toLowerCase(), val);
	}
}
