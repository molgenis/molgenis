package org.molgenis.col7a1;

import java.util.ArrayList;
import java.util.List;

public class Row
{
	private final List<Cell> cells = new ArrayList<Cell>();

	void add(Cell cell)
	{
		this.cells.add(cell);
	}

	public List<Cell> getCells()
	{
		return this.cells;
	}
}
