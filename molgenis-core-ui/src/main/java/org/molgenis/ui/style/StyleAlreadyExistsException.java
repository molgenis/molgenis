package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class StyleAlreadyExistsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C12";
	private final String styleId;

	public StyleAlreadyExistsException(String styleId)
	{
		super(ERROR_CODE);
		this.styleId = requireNonNull(styleId);
	}

	@Override
	public String getMessage()
	{
		return String.format("styleId:%s", styleId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { styleId };
	}
}
