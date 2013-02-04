package org.molgenis.framework.ui.html;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * An extension of Table that renders as a jQuery DataTable instead of a plain
 * HTML table.
 * 
 * @author erikroos
 * 
 */
public class EditableJQueryDataTable extends JQueryDataTable
{

	public final static String MATRIX_EDIT_VALUE = "EditableJQueryDataTable-edit-value";
	public final static String MATRIX_EDIT_ACTION = "EditableJQueryDataTable-edit-action";

	public EditableJQueryDataTable(String name)
	{
		this(name, null);
	}

	public EditableJQueryDataTable(String name, String label)
	{
		super(name, label);
		this.setLabel(label);
		super.setDefaultCellStyle("");
		super.setHeaderCellStyle("");
	}

	@Override
	String printRow(String row, int rowCount)
	{
		String result = "<tr>";
		if (headerColumn)
		{
			result += ("<th style=\"" + getHeaderCellStyle() + "\">" + row + "</th>");
		}

		for (int colCount = 0; colCount < cols.size(); colCount++)
		{
			result += ("<td style=\""
					+ getCellStyle(colCount, rowCount)
					+ "\">"
					+ (getCellString(colCount, rowCount).contains("<") ? getCellString(colCount, rowCount) : "<input name=\""
							+ MATRIX_EDIT_VALUE
							+ "_"
							+ colCount
							+ "_"
							+ rowCount
							+ "\" type=\"text\" value=\""
							+ StringEscapeUtils.escapeHtml(getCellString(colCount, rowCount)) + "\"/>") + "</td>");
		}
		result += "</tr>";
		return result;
	}

}
