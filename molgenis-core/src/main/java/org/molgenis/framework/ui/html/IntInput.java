package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for integer data.
 */
public class IntInput extends ValidatingInput<Integer>
{

	public IntInput(String name)
	{
		super(name, null);
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

	public IntInput(String name, String label)
	{
		this(name);
		this.setLabel(label);
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

	public IntInput(String name, Integer value)
	{
		super(name, value);
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

	public IntInput(String name, String label, Integer value, boolean nillable, boolean readonly, String description)
	{
		super(name, label, value, nillable, readonly, description);
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

	protected IntInput()
	{
		super();
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

	public IntInput(Tuple params) throws HtmlInputException
	{
		super(params);
		this.validationString = "digits";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new IntInput(params).render();
	}
}
