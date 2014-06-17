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

		final Row row = (Row) obj;

		List<Cell> rowCells = row.getCells();
		for (int j = 0; j < this.cells.size(); j++)
		{
			if (!this.cells.get(j).equals(rowCells.get(j)))
			{
				return false;
			}
		}
		return true;
	}
}
