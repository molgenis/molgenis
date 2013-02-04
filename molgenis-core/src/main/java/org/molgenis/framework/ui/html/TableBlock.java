package org.molgenis.framework.ui.html;

import org.apache.log4j.Logger;

/**
 * Helper class for Table to repesent header, footer or body block.
 */
public class TableBlock
{
	Logger logger = Logger.getLogger(TableBlock.class);
	int rowSize = 0;
	int colSize = 0;
	TableCell[][] values = new TableCell[][]
	{};

	public TableCell get(int row, int col)
	{
		if (row < values.length)
		{
			if (col < values[row].length) return values[row][col];
		}
		return null;
	}

	/** Set the cel(row,col) to value including row and/or column spanning */
	public void set(TableCell value)
	{
		int col = value.getCol();
		int row = value.getRow();

		// make sure size is right
		if (row + value.getRowspan() >= rowSize || col + value.getColspan() >= colSize)
		{
			this.resize(Math.max(row + value.getRowspan(), rowSize), Math.max(col + value.getColspan(), colSize));
		}

		// remove all cells affected
		for (int i = row; i < row + value.getRowspan(); i++)
		{
			for (int j = col; j < col + value.getColspan(); j++)
			{
				// remove oldvalues
				TableCell oldValue = get(row, col);
				if (oldValue != null)
				{
					logger.warn("removing table cell because overwriting: row = " + oldValue.getCol() + ", col="
							+ oldValue.getRow());
					remove(oldValue);
				}
			}
		}

		// set all cell references (incl spanning)
		for (int i = row; i < row + value.getRowspan(); i++)
		{
			for (int j = col; j < col + value.getColspan(); j++)
			{
				values[i][j] = value;
			}
		}
	}

	public void remove(TableCell cell)
	{
		// remove all reference to this cell.
		TableCell oldValue = values[cell.getRow()][cell.getCol()];

		if (oldValue != null)
		{
			for (int rowIndex = oldValue.getRow(); rowIndex < oldValue.getRow() + oldValue.getRowspan(); rowIndex++)
			{
				for (int colIndex = oldValue.getCol(); colIndex < oldValue.getCol() + oldValue.getColspan(); colIndex++)
				{
					values[rowIndex][colIndex] = new TableCell(rowIndex, colIndex, new CustomHtml(""));
				}
			}
		}

		// TODO check if this means the table can shrink
	}

	public String renderRows()
	{
		StringBuilder strBuilder = new StringBuilder();
		// iterate through the rows and cols; render unless it is spanning
		for (int row = 0; row < values.length; row++)
		{
			// only open tr if it is not complete in 'spanning'.
			boolean tr = false;
			if (values[row] != null)
			{
				for (int col = 0; col < values[row].length; col++)
				{
					TableCell td = get(row, col);
					if (td != null && row == td.getRow() && col == td.getCol())
					{
						if (tr == false)
						{
							strBuilder.append("\t\n<tr>");
							tr = true;
						}

						// create the <td with optional colspan, rowspane
						String colspan = td.getColspan() > 1 ? " colspan=" + td.getColspan() : "";
						String rowspan = td.getRowspan() > 1 ? " rowspan=" + td.getRowspan() : "";
						String value = td.getValue() != null ? td.getValue().render() : "";
						strBuilder.append("<td").append(colspan).append(rowspan).append('>');
						strBuilder.append(value).append("</td>");
					}
				}
			}
			if (tr) strBuilder.append("</tr>");
		}

		return strBuilder.toString();
	}

	/** Helper method to resize the values array */
	private void resize(int rows, int cols)
	{
		values = (TableCell[][]) resizeArray(values, rows);
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] == null)
			{
				values[i] = new TableCell[cols];
			}
			else
			{
				values[i] = (TableCell[]) resizeArray(values[i], cols);
			}
			for (int j = 0; j < values[i].length; j++)
			{
				if (values[i][j] == null) values[i][j] = new TableCell(i, j, new CustomHtml(""), 1, 1);
			}
		}
		this.rowSize = rows;
		this.colSize = cols;
	}

	/** Helper method to resize an array */
	private Object resizeArray(Object oldArray, int newSize)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	public void set(int row, int col, HtmlElement value, int rowspan, int colspan)
	{
		set(new TableCell(row, col, value, rowspan, colspan));

	}
}
