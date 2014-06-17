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
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (this == obj)
		{
			return true;
		}

		if (this.getClass() != obj.getClass())
		{
			return false;
		}

		final Cell cell = (Cell) obj;

		List<Value> cellValues = cell.getValues();
		for (int j = 0; j < this.values.size(); j++)
		{
			if(!this.values.get(j).equals(cellValues.get(j))){
				return false;
			}
		}
		return true;
	}
}
