package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for string data. Renders as a normal <code>input</code>.
 */
public class TextLineInput<E> extends HtmlInput<E>
{
	private boolean disabled = false;

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	public TextLineInput(String name)
	{
		super(name, null);
	}

	@Deprecated
	public TextLineInput(String name, String label, E value, boolean nillable, boolean readonly)
	{
		this(name, label, value, nillable, readonly, null);
	}

	public TextLineInput(String name, String label, E value, boolean nillable, boolean readonly, String description)
	{
		super(name, label, value, nillable, readonly, description);
	}

	public TextLineInput(String name, E value)
	{
		super(name, value);
	}

	public TextLineInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	protected TextLineInput()
	{
	}

	@Override
	public String toHtml()
	{
		String classAtt = (this.getClazz() != null ? this.getClazz() : "");
		classAtt += (this.isReadonly()) ? " readonly " : "";

		String disabledProperty = (disabled ? " disabled=\"disabled\"" : "");

		// 'disabled' doesn't send the value. We need the value if it is
		// key...therefore we use 'readonly'.

		if (this.isHidden())
		{
			return "<input type=\"hidden\" id=\"" + getId() + "\" name=\"" + getName() + "\" value=\"" + getValue()
					+ disabledProperty + "\">";
		}

		String attributes = "";

		return "<input type=\"text\" id=\"" + getId() + "\" class=\"" + classAtt + "\" name=\"" + getName()
				+ "\" value=\"" + getValue() + "\" " + attributes + tabIndex + disabledProperty + " />";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new TextLineInput<E>(params).render();
	}
}
