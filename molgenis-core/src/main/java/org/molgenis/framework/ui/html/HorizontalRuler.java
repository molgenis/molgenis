package org.molgenis.framework.ui.html;

public class HorizontalRuler extends HtmlInput<String>
{

	public HorizontalRuler()
	{
		this.setLabel("");
	}

	@Override
	public String toHtml()
	{
		return "<hr/>";
	}

}
