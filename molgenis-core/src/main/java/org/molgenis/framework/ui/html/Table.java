package org.molgenis.framework.ui.html;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.molgenis.util.Pair;
import org.molgenis.util.tuple.Tuple;

/*
 * Provides an html table with objects, e.g. input components, in the cells.
 * First you add columns (addColumn) and rows (addRows), then you set the contents
 * of the cells (setCell).
 * You can set/override the default CSS styles for the header and for the individual cells
 * (setHeaderCellStyle, setDefaultCellStyle and setCellStyle).
 * Row and colspan are currently not supported.
 */
public class Table extends HtmlWidget
{
	LinkedHashMap<Pair<Integer, Integer>, Object> cells = new LinkedHashMap<Pair<Integer, Integer>, Object>();
	LinkedHashMap<Pair<Integer, Integer>, String> cellStyles = new LinkedHashMap<Pair<Integer, Integer>, String>();
	List<String> cols = new ArrayList<String>();
	List<String> rows = new ArrayList<String>();
	String style = null;
	protected String defaultCellStyle = "border: 1px solid black; padding:2px";
	protected String headerCellStyle = "border: 1px solid black; padding:2px; background-color: #5B82A4; color: white";
	boolean headerColumn = true;
	boolean headerRow = true;

	/**
	 * Constructor with empty label.
	 * 
	 * @param name
	 */
	public Table(String name)
	{
		this(name, "");
	}

	public Table(String name, String label)
	{
		super(name, label);
		this.setLabel(label);
	}

	/**
	 * Constructor used to control the presence of the header column and row
	 * 
	 * @param name
	 * @param label
	 * @param headerColumn
	 *            specify the presence of header column and row, defaults to
	 *            true
	 */
	public Table(String name, String label, boolean headerColumn, boolean headerRow)
	{
		super(name, label);
		this.setLabel(label);
		this.headerColumn = headerColumn;
		this.headerRow = headerRow;

	}

	@Override
	/**
	 * Renders the table.
	 */
	public String toHtml()
	{
		String result = "<table";

		if (style != null)
		{
			result += " style=\"clear:both;" + style + "\"";
		}

		result += " width=\"400\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\""
				+ this.getId() + "\">";
		if (headerRow)
		{
			result += printHeaders();
		}
		result += printBody();
		result += "</table>";

		return result;
	}

	private String printHeaders()
	{
		StringBuilder strBuilder = new StringBuilder("<thead><tr>");
		if (headerColumn)
		{
			strBuilder.append("<th></th>");
		}
		for (String col : cols)
		{
			strBuilder.append("<th style=\"").append(getHeaderCellStyle()).append("\">").append(col).append("</th>");
		}
		strBuilder.append("</tr></thead>");
		return strBuilder.toString();
	}

	private String printBody()
	{
		StringBuilder strBuilder = new StringBuilder("<tbody>");
		int rowCount = 0;
		for (String row : rows)
		{
			strBuilder.append(printRow(row, rowCount));
			rowCount++;
		}
		strBuilder.append("</tbody>");
		return strBuilder.toString();
	}

	// default visibility for subclassing in the same package
	String printRow(String row, int rowCount)
	{
		StringBuilder strBuilder = new StringBuilder("<tr>");
		if (headerColumn)
		{
			strBuilder.append("<th style=\"").append(getHeaderCellStyle()).append("\">").append(row).append("</th>");
		}

		for (int colCount = 0; colCount < cols.size(); colCount++)
		{
			strBuilder.append("<td style=\"").append(getCellStyle(colCount, rowCount)).append("\">");
			strBuilder.append(getCellString(colCount, rowCount)).append("</td>");
		}
		strBuilder.append("</tr>");
		return strBuilder.toString();
	}

	/**
	 * Set the containing div's css style.
	 */
	@Override
	public HtmlInput<String> setStyle(String style)
	{
		this.style = style;
		return this;
	}

	/**
	 * Add a column to the Table.
	 * 
	 * @param colName
	 * @return
	 */
	public Integer addColumn(String colName)
	{
		cols.add(colName);
		return cols.size() - 1;
	}

	/**
	 * Remove the column at 'colNr' from the Table.
	 * 
	 * @param colNr
	 * @return
	 */
	public String removeColumn(int colNr)
	{
		return cols.remove(colNr);
	}

	/**
	 * Add a row to the Table.
	 * 
	 * @param rowName
	 * @return
	 */
	public Integer addRow(String rowName)
	{
		rows.add(rowName);
		return rows.size() - 1;
	}

	/**
	 * Remove the row at 'rowNr' from the Table.
	 * 
	 * @param rowNr
	 * @return
	 */
	public String removeRow(int rowNr)
	{
		return rows.remove(rowNr);
	}

	/**
	 * Set the contents of the cell at col, row.
	 * 
	 * @param col
	 * @param row
	 * @param contents
	 */
	public void setCell(int col, int row, Object contents)
	{
		cells.put(new Pair<Integer, Integer>(col, row), contents);
	}

	/**
	 * Get the contents of the cell at col, row.
	 * 
	 * @param col
	 * @param row
	 * @return
	 */
	public Object getCell(int col, int row)
	{
		return cells.get(new Pair<Integer, Integer>(col, row));
	}

	/**
	 * Get the contents of the cell at col, row as a String.
	 * 
	 * @param col
	 * @param row
	 * @return
	 */
	public String getCellString(int col, int row)
	{
		Object o = cells.get(new Pair<Integer, Integer>(col, row));
		if (o == null)
		{
			return "";
		}
		if (o instanceof HtmlInput<?>)
		{
			return ((HtmlInput<?>) o).toHtml();
		}
		return o.toString();
	}

	/**
	 * Set the default CSS style parameters for all non-header cells.
	 * 
	 * @param defaultCellStyle
	 */
	public void setDefaultCellStyle(String defaultCellStyle)
	{
		this.defaultCellStyle = defaultCellStyle;
	}

	/**
	 * Set CSS style parameters for the cell at col, row. E.g.: setCellStyle(1,
	 * 1, "border: 1px")
	 * 
	 * @param col
	 * @param row
	 * @param cellStyle
	 */
	public void setCellStyle(int col, int row, String cellStyle)
	{
		cellStyles.put(new Pair<Integer, Integer>(col, row), cellStyle);
	}

	/**
	 * Get the CSS style parameters for the cell at col, row.
	 * 
	 * @param col
	 * @param row
	 * @return
	 */
	public String getCellStyle(int col, int row)
	{
		String style = cellStyles.get(new Pair<Integer, Integer>(col, row));
		if (style != null)
		{
			return style;
		}
		else
		{
			return defaultCellStyle;
		}
	}

	/**
	 * Set CSS style parameters for all header cells.
	 * 
	 * @param headerCellStyle
	 */
	public void setHeaderCellStyle(String headerCellStyle)
	{
		this.headerCellStyle = headerCellStyle;
	}

	/**
	 * Get the CSS style parameters for all header cells.
	 * 
	 * @return
	 */
	public String getHeaderCellStyle()
	{
		return headerCellStyle;
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		// TODO?
		throw new UnsupportedOperationException();
	}
}
