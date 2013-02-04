package org.molgenis.framework.ui.commands;

import org.molgenis.framework.ui.html.HtmlInput;

/**
 * Decorates an input with a checkbox to disable/enable the input
 * 
 * @param <E>
 */
public class EditSelectedInput extends HtmlInput<Object>
{
	private HtmlInput<?> input;

	public EditSelectedInput(HtmlInput<?> input)
	{
		this.input = input;
	}

	@Override
	public String getLabel()
	{
		return input.getLabel();
	}

	@Override
	public String toHtml()
	{
		input.setNillable(true);
		return "<input type=\"checkbox\" name=\"use_" + input.getName() + "\" />" + input.toHtml();
	}

}
