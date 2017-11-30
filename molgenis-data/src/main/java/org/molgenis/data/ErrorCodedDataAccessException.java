package org.molgenis.data;

import javax.annotation.Nullable;
import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * {@link org.springframework.dao.DataAccessException} with error code and without message.
 */
public abstract class ErrorCodedDataAccessException extends org.springframework.dao.DataAccessException
		implements ErrorCoded
{
	private final String errorCode;

	public ErrorCodedDataAccessException(String errorCode)
	{
		this(errorCode, null);
	}

	public ErrorCodedDataAccessException(String errorCode, @Nullable Throwable cause)
	{
		super("", cause);
		this.errorCode = requireNonNull(errorCode);
	}

	@Override
	public String getLocalizedMessage()
	{
		try
		{
			return getLanguageService().map(languageService -> languageService.getMessageFormat(getErrorCode()))
									   .map(format -> format.format(getLocalizedMessageArguments()))
									   .orElseGet(super::getLocalizedMessage);
		}
		catch (RuntimeException ex)
		{
			return MessageFormat.format("FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE {0}.%nFallback message: {1}",
					errorCode, super.getLocalizedMessage());
		}
	}

	protected abstract Object[] getLocalizedMessageArguments();

	@Override
	public String getErrorCode()
	{
		return errorCode;
	}
}
