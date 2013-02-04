package org.molgenis.util.tuple;

public interface WritableTuple extends Tuple
{
	public void set(String colName, Object value);

	public void set(Tuple t);
}
