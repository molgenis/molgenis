package org.molgenis.charts.data;

import java.util.Collections;
import java.util.List;

public class DataMatrix
{
	private final List<Target> columnTargets;
	private final List<Target> rowTargets;
	private final List<List<Number>> values;

	public DataMatrix(List<Target> columnTargets, List<Target> rowTargets, List<List<Number>> values)
	{
		if (columnTargets.size() != values.get(0).size())
			throw new IllegalArgumentException("Nr columnTargets not the same as the nr of columns of the values");

		if (rowTargets.size() != values.size())
			throw new IllegalArgumentException("Nr rowTargets not the same as the nr of rows of the values");

		for (int i = 0; i < columnTargets.size(); i++)
		{
			if (values.get(i).size() != values.get(0).size())
			{
				throw new IllegalArgumentException("Row [" + i + "] has a different length");
			}
		}

		this.columnTargets = columnTargets;
		this.rowTargets = rowTargets;
		this.values = values;
	}

	public List<Target> getColumnTargets()
	{
		return Collections.unmodifiableList(columnTargets);
	}

	public List<Target> getRowTargets()
	{
		return Collections.unmodifiableList(rowTargets);
	}

	public List<List<Number>> getValues()
	{
		return Collections.unmodifiableList(values);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnTargets == null) ? 0 : columnTargets.hashCode());
		result = prime * result + ((rowTargets == null) ? 0 : rowTargets.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DataMatrix other = (DataMatrix) obj;
		if (columnTargets == null)
		{
			if (other.columnTargets != null) return false;
		}
		else if (!columnTargets.equals(other.columnTargets)) return false;
		if (rowTargets == null)
		{
			if (other.rowTargets != null) return false;
		}
		else if (!rowTargets.equals(other.rowTargets)) return false;
		if (values == null)
		{
			if (other.values != null) return false;
		}
		else if (!values.equals(other.values)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "DataMatrix [columnTargets=" + columnTargets + ", rowTargets=" + rowTargets + ", values=" + values + "]";
	}

}
