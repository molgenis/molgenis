package org.molgenis.framework.ui.html;

import org.molgenis.util.tuple.Tuple;

/**
 * Input for decimal data. DecimalInput is a ValidatingInput checking if data
 * entered is conform decimal notation.
 */
public class DecimalInput extends ValidatingInput<Double>
{
	/**
	 * Construct DecimalInput with name
	 * 
	 * @param name
	 */
	public DecimalInput(String name)
	{
		this(name, null);
	}

	/**
	 * Construct DecimalInput with name, value
	 * 
	 * @param name
	 * @param value
	 */
	public DecimalInput(String name, Double value)
	{
		super(name, value);
		this.validationString = "number";
	}

	/**
	 * Construct DecimalInput with name, label, value
	 * 
	 * @param name
	 * @param value
	 */
	public DecimalInput(String name, String label, Double value)
	{
		super(name, label, value);
		this.validationString = "number";
	}

	public DecimalInput(String name, String label, Double value, boolean nillable, boolean readonly)
	{
		super(name, value);
		this.setLabel(label);
		this.setNillable(nillable);
		this.setReadonly(readonly);
		this.validationString = "number";
	}

	public DecimalInput(Tuple params) throws HtmlInputException
	{
		set(params);
		this.validationString = "number";
	}

	protected DecimalInput()
	{
		this.validationString = "number";
	}
}
