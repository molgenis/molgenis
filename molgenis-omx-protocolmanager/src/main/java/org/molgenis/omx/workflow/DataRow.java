package org.molgenis.omx.workflow;

import java.util.List;

public class DataRow
{
	private final List<Object> values;
	private final List<DataRow> linkedDataRows;

	public DataRow(List<Object> values)
	{
		this(values, null);
	}

	public DataRow(List<Object> values, List<DataRow> linkedDataRows)
	{
		if (values == null) throw new IllegalArgumentException("Values is null");
		this.values = values;
		this.linkedDataRows = linkedDataRows;
	}

	public List<Object> getValues()
	{
		return values;
	}

	public List<DataRow> getLinkedDataRows()
	{
		return linkedDataRows;
	}
}