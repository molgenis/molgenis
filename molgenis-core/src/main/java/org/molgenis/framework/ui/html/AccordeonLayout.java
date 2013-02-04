package org.molgenis.framework.ui.html;

/**
 * Accordeon layout is a MultiPanel layout that shows multiple panels underneath
 * each other. A button can be used to show one of the panels.
 */
public class AccordeonLayout extends MultipanelLayout implements Layout
{
	public AccordeonLayout(String id)
	{
		super(id);
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "";
	}

	@Override
	public String render()
	{
		if (this.style == UiToolkit.JQUERY)
		{
			StringBuilder strBuilder = new StringBuilder("<div class=\"jquery_accordion\">");

			for (String title : elements.keySet())
			{
				strBuilder.append("<h3><a href=\"#\">").append(title).append("</a></h3><div>");
				strBuilder.append(elements.get(title).render());
				strBuilder.append("</div>");
			}
			strBuilder.append("</div><script>$(\".jquery_accordion\").accordion();</script>");
			return strBuilder.toString();
		}
		else
		{
			return "ERROR";
		}
	}
}
