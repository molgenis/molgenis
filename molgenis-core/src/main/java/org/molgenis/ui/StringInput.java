package org.molgenis.ui;

/**
 * Input for string data. Renders as a <code>textarea</code>.
 */
public class StringInput extends HtmlInput<StringInput, String>
{
	public StringInput(String name, String value)
	{
		super(name, value);
	}

	public StringInput(String name)
	{
		this(name, null);
	}
}
