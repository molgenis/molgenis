package org.molgenis.framework.ui.html;

public class MakeLabelforContainer extends HtmlWidget
{
	private String label;

	public MakeLabelforContainer(String label)
	{
		super(null);
		this.label = label;

	}

	@Override
	public String toHtml()
	{
		return "<label>" + label + "</label>";
	}

}
