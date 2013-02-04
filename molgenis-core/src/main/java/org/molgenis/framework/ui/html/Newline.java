package org.molgenis.framework.ui.html;

public class Newline extends HtmlInput<String>
{

	public Newline()
	{
		this.setLabel("");
	}

	@Override
	public String toHtml()
	{
		return "<br/>";
	}

}
