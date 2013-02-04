package org.molgenis.framework.ui.html;

/**
 * Helper class for Tabel to wrap one 'td' within a html table.
 * 
 * Do we need to make this a HtmlWidget so we can css and style?
 */
public class TableCell
{
	int row;
	int col;
	int rowspan;
	int colspan;
	HtmlElement value;

	public TableCell(int row, int col, HtmlElement value)
	{
		this.row = row;
		this.col = col;
		this.value = value;
	}

	public TableCell(int row, int col, HtmlElement value, int rowspan, int colspan)
	{
		this(row, col, value);
		this.rowspan = rowspan;
		this.colspan = colspan;
	}

	public int getRow()
	{
		return row;
	}

	public void setRow(int row)
	{
		this.row = row;
	}

	public int getCol()
	{
		return col;
	}

	public void setCol(int col)
	{
		this.col = col;
	}

	public int getRowspan()
	{
		return rowspan;
	}

	public void setRowspan(int rowspan)
	{
		this.rowspan = rowspan;
	}

	public int getColspan()
	{
		return colspan;
	}

	public void setColspan(int colspan)
	{
		this.colspan = colspan;
	}

	public HtmlElement getValue()
	{
		return value;
	}

	public void setValue(HtmlElement value)
	{
		this.value = value;
	}
}
