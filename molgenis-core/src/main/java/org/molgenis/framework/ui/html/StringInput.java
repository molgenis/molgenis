package org.molgenis.framework.ui.html;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for string data. Renders as a <code>textarea</code>.
 */
public class StringInput extends ValidatingInput<String>
{
	public StringInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	public StringInput(String name, String label, String value, boolean nillable, boolean readonly)
	{
		this(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
	}

	public StringInput(String name)
	{
		this(name, null);
	}

	public StringInput(String name, String value)
	{
		super(name, value);
		this.setMinHeight(1);
		this.setMaxHeight(1);
	}

	public StringInput()
	{
	}

	@Override
	public String toHtml(Tuple params) throws HtmlInputException
	{
		return new StringInput(params).render();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "";
	}
}
