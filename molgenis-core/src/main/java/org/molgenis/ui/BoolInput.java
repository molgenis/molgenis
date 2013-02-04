package org.molgenis.ui;

/**
 * Input for yes/no (boolean) values.
 */
public class BoolInput extends HtmlInput<BoolInput, Boolean>
{
	/** Construct a BoolInput with name and default value */
	public BoolInput(String id, Boolean value)
	{
		super(id, value);
	}
}