package org.molgenis.mutationdb;

import java.util.ArrayList;
import java.util.List;

public class Row
{
	private final List<Cell> cells = new ArrayList<Cell>();

	public void add(Cell cell)
	{
		this.cells.add(cell);
	}

	public List<Cell> getCells()
	{
		return this.cells;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cells == null) ? 0 : cells.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Row other = (Row) obj;
		if (cells == null)
		{
			if (other.cells != null) return false;
		}
		else if (!cells.equals(other.cells)) return false;
		return true;
	}
}
