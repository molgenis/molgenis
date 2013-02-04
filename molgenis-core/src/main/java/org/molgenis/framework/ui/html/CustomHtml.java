package org.molgenis.framework.ui.html;

public class CustomHtml extends HtmlWidget
{
	private String html;

	public CustomHtml(String html)
	{
		super(null);
		this.html = html;
	}

	@Override
	public String toHtml()
	{
		return html;
	}

}
