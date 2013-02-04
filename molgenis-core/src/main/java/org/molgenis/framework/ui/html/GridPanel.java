package org.molgenis.framework.ui.html;

public class GridPanel extends TablePanel
{
	private int columns = 1;

	public GridPanel()
	{
		super();
	}

	public GridPanel(int columns)
	{
		this.columns = columns;
	}

	public void setColumns(int columns)
	{
		this.columns = columns;
	}

	@Override
	public String toHtml()
	{
		StringBuilder strBuilder = new StringBuilder("<table>\n");
		int cell = 0;

		for (HtmlInput<?> i : this.inputs.values())
		{
			if (i.isHidden()) continue;

			if (cell % columns == 0)
			{
				if (cell != 0) strBuilder.append("</tr>\n");
				strBuilder.append("<tr>");
			}

			strBuilder.append("<td>").append(i.getLabel()).append("</td><td>").append(i.toHtml()).append("</td>");

			cell++;
		}

		if (this.inputs.size() > 0) strBuilder.append("</tr>\n");

		strBuilder.append("</table>\n");

		return strBuilder.toString();
	}
}
