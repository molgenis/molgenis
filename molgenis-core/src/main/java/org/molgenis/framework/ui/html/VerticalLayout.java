package org.molgenis.framework.ui.html;

public class VerticalLayout extends FlowLayout
{
	@Override
	public String render()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (HtmlElement i : this.getElements())
		{
			if (i instanceof HtmlInput<?>)
			{
				HtmlInput<?> input = (HtmlInput<?>) i;
				strBuilder.append("<label>").append(input.getLabel()).append("</label><br/>");
				strBuilder.append(input.toHtml()).append("<br/>");
			}
			else
			{
				strBuilder.append(i.render()).append("<br/>");
			}

		}
		return strBuilder.toString();
	}
}
