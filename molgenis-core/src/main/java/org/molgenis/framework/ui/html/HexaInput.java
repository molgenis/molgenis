package org.molgenis.framework.ui.html;

/**
 * Input for hexadecimal data fields.
 */
@Deprecated
public class HexaInput extends StringInput
{
	public HexaInput(String name)
	{
		this(name, null);
	}

	public HexaInput(String name, String value)
	{
		super(name, value);
	}
}
