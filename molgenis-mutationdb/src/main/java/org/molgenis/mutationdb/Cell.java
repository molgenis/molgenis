package org.molgenis.mutationdb;

import java.util.ArrayList;
import java.util.List;

public class Cell
{
	private final List<Value> values = new ArrayList<Value>();

	Cell add(Value value)
	{
		this.values.add(value);
		return this;
	}

	Cell addAll(List<Value> values)
	{
		this.values.addAll(values);
		return this;
	}

	public List<Value> getValues()
	{
		return this.values;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Cell other = (Cell) obj;
		if (values == null)
		{
			if (other.values != null) return false;
		}
		else if (!values.equals(other.values)) return false;
		return true;
	}
}
