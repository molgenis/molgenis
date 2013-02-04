package org.molgenis.framework.ui.html;

/**
 * Wrapper of an HTML table.
 * 
 * Features:
 * <ul>
 * <li>A table consist of blocks header, body and footer (THEAD,TBODY and TFOOT
 * respectively)
 * <li>Each block constist of cells which are identified by row/col index (TD
 * grouped in TR)
 * <li>The contents of each cell can be either simple String or rich
 * HtmlElement.
 * <li>Some cells may be set to span multiple rows/cols.
 * <li>If a new value overlaps with an existing (spanned) cell the original cell
 * is removed and a warning logged.
 * </ul>
 */
public class TableBeta extends HtmlWidget
{
	TableBlock header = new TableBlock();
	TableBlock footer = new TableBlock();
	TableBlock body = new TableBlock();

	/**
	 * Construct a Table with unique name/id
	 * 
	 * @param name
	 */
	public TableBeta(String name)
	{
		super(name);
	}

	/**
	 * Get the widget that has been loaded at a particular row/col index.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public TableCell get(int row, int col)
	{
		return body.get(row, col);
	}

	/**
	 * Set the cel(row,col) to value including row and/or column spanning
	 * 
	 * @param row
	 *            index
	 * @param col
	 *            index
	 * @param value
	 * @param rowspan
	 *            the number of row elements to span
	 * @param colspan
	 *            the number of col elements to span
	 */
	public void set(int row, int col, HtmlElement value, int rowspan, int colspan)
	{
		body.set(row, col, value, rowspan, colspan);
	}

	/**
	 * Set the cel(row,col) to String value including row and/or column spanning
	 */
	public void set(int row, int col, String value)
	{
		set(row, col, new CustomHtml(value));
	}

	/** Set the cel(row,col) to value */
	public void set(int row, int col, HtmlElement value)
	{
		set(row, col, value, 1, 1);
	}

	public void set(int row, int col, String string, int rowspan, int colspan)
	{
		this.set(row, col, new CustomHtml(string), rowspan, colspan);
	}

	/**
	 * Set the cel(row,col) to value including row and/or column spanning
	 * 
	 * @param row
	 *            index
	 * @param col
	 *            index
	 * @param value
	 * @param rowspan
	 *            the number of row elements to span
	 * @param colspan
	 *            the number of col elements to span
	 */
	public void setHead(int row, int col, HtmlElement value, int rowspan, int colspan)
	{
		header.set(row, col, value, rowspan, colspan);
	}

	/**
	 * Set the cel(row,col) to String value including row and/or column spanning
	 */
	public void setHead(int row, int col, String value)
	{
		setHead(row, col, new CustomHtml(value));
	}

	/** Set the cel(row,col) to value */
	public void setHead(int row, int col, HtmlElement value)
	{
		setHead(row, col, value, 1, 1);
	}

	public void setHead(int row, int col, String string, int rowspan, int colspan)
	{
		setHead(row, col, new CustomHtml(string), rowspan, colspan);
	}

	/**
	 * Set the cel(row,col) to value including row and/or column spanning
	 * 
	 * @param row
	 *            index
	 * @param col
	 *            index
	 * @param value
	 * @param rowspan
	 *            the number of row elements to span
	 * @param colspan
	 *            the number of col elements to span
	 */
	public void setFoot(int row, int col, HtmlElement value, int rowspan, int colspan)
	{
		footer.set(row, col, value, rowspan, colspan);
	}

	/**
	 * Set the cel(row,col) to String value including row and/or column spanning
	 */
	public void setFoot(int row, int col, String value)
	{
		setFoot(row, col, new CustomHtml(value));
	}

	/** Set the cel(row,col) to value */
	public void setFoot(int row, int col, HtmlElement value)
	{
		setFoot(row, col, value, 1, 1);
	}

	public void setFoot(int row, int col, String string, int rowspan, int colspan)
	{
		setFoot(row, col, new CustomHtml(string), rowspan, colspan);
	}

	@Override
	public String toHtml()
	{
		String attributes = this.getStyle() != null ? " style=\"" + this.getStyle() + "\"" : "";
		attributes += this.getClazz() != null ? " class=\"" + this.getClazz() + "\"" : "";

		String result = "\n<table id=\"" + getName() + "\"" + attributes + ">";

		result += "<thead>" + header.renderRows() + "</thead>";
		result += "<tbody>" + body.renderRows() + "</tbody>";

		result += "\n</table>";

		return result;
	}

}
