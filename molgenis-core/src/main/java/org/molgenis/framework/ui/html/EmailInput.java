package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for integer data.
 */
public class EmailInput extends ValidatingInput<Integer>
{

	public EmailInput(String name)
	{
		super(name, null);
		this.validationString = "email";
		this.setMaxWidth(10);
	}

	public EmailInput(String name, String label)
	{
		this(name);
		this.setLabel(label);
		this.validationString = "email";
		this.setMaxWidth(10);
	}

	public EmailInput(String name, Integer value)
	{
		super(name, value);
		this.validationString = "email";
		this.setMaxWidth(10);
	}

	public EmailInput(String name, String label, Integer value, boolean nillable, boolean readonly, String description)
	{
		super(name, label, value, nillable, readonly, description);
		this.validationString = "email";
		this.setMaxWidth(10);
	}

	protected EmailInput()
	{
		super();
		this.validationString = "email";
		this.setMaxWidth(10);
	}

	public EmailInput(Tuple params) throws HtmlInputException
	{
		super(params);
		this.validationString = "email";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException, HtmlInputException
	{
		return new EmailInput(params).render();
	}
}
