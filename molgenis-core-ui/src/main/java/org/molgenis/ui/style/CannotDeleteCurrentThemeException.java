package org.molgenis.ui.style;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class CannotDeleteCurrentThemeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C04";
	private final String themeId;

	public CannotDeleteCurrentThemeException(String themeId)
	{
		super(ERROR_CODE);
		this.themeId = requireNonNull(themeId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", themeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { themeId };
	}
}
