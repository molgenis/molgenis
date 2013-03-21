package org.molgenis.ngs.plugins.worksheet;

import java.util.List;

import org.molgenis.framework.ui.html.TupleTable;
import org.molgenis.util.tuple.Tuple;

public class WorksheetModel
{
	private List<Tuple> rows;

	public WorksheetModel(List<Tuple> currentRows)
	{
		this.rows = currentRows;
	}

	public void setRows(List<Tuple> rows)
	{
		this.rows = rows;
	}

	public List<Tuple> getRows()
	{
		return rows;
	}

	public TupleTable getTable()
	{
		TupleTable t = new TupleTable("worksheet", getRows());

		return t;
	}
}
