package org.molgenis.col7a1;

import java.util.ArrayList;
import java.util.List;

public class Cell
{
	private final List<Value> values = new ArrayList<Value>();

	void add(Value value)
	{
		this.values.add(value);
	}

	void addAll(List<Value> values)
	{
		this.values.addAll(values);
	}

	public List<Value> getValues()
	{
		return this.values;
	}
}
