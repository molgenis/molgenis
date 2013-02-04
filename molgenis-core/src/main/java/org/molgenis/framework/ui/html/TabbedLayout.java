package org.molgenis.framework.ui.html;

import java.util.Map;
import java.util.Map.Entry;

public class TabbedLayout extends MultipanelLayout
{
	public enum Position
	{
		TOPLEFT, TOPRIGHT, LEFTTOP
	};

	public TabbedLayout(String id)
	{
		super(id);
	}

	public void addHyperlink(String name, String hyperlink)
	{
		super.add(new HyperlinkInput(name, hyperlink));
	}

	@Override
	public String render()
	{
		Map<String, HtmlElement> elements = this.getElements();

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<div id=\"").append(this.getId()).append("\">");

		// create tabs
		strBuilder.append("<ul>");
		int i = 0;
		for (Entry<String, HtmlElement> entry : elements.entrySet())
		{
			HtmlElement htmlElement = entry.getValue();
			if (htmlElement instanceof HyperlinkInput)
			{
				strBuilder.append("<li><a href=\"").append(((HyperlinkInput) htmlElement).getValue());
				strBuilder.append("\">").append("</a></li>");
			}
			else
			{
				strBuilder.append("<li><a href=\"#").append(getId()).append('-').append(i++).append("\">");
				strBuilder.append(entry.getKey()).append("</a></li>");
			}
		}
		strBuilder.append("</ul>");

		// create bodies
		i = 0;
		for (String label : elements.keySet())
		{
			strBuilder.append("<div id=\"").append(getId()).append('-').append(i++).append("\">");
			strBuilder.append(elements.get(label).render()).append("</div>");
		}
		strBuilder.append("</div>");

		strBuilder.append("</div><script>$(\"#").append(getId()).append("\").tabs();</script>");

		return strBuilder.toString();
	}
}
