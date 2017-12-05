package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class StyleNotFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C10";
	private final String styleName;

	public StyleNotFoundException(String styleName)
	{
		super(ERROR_CODE);
		this.styleName = requireNonNull(styleName);
	}

	@Override
	public String getMessage()
	{
		return String.format("styleName:%s", styleName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { styleName };
	}
}
