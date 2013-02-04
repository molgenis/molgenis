package org.molgenis.framework.ui.html;

/**
 * Input for Long integer data.
 */
public class LongInput extends ValidatingInput<Long>
{

	public LongInput(String name, Long value)
	{
		super(name, value);
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

	public LongInput(String name)
	{
		this(name, null);
		this.validationString = "digits";
		this.setMaxWidth(10);
	}

}
