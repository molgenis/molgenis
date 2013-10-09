package org.molgenis.omx.workflow;

import java.util.List;

public class DataRow
{
	private final List<Object> values;

	public DataRow(List<Object> values)
	{
		if (values == null) throw new IllegalArgumentException("Values is null");
		this.values = values;
	}

	public List<Object> getValues()
	{
		return values;
	}
}