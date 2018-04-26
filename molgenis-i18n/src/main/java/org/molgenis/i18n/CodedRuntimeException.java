package org.molgenis.i18n;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

/**
 * {@link RuntimeException} with error code.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class CodedRuntimeException extends RuntimeException implements ErrorCoded
{
	private final String errorCode;

	protected CodedRuntimeException(String errorCode)
	{
		this.errorCode = requireNonNull(errorCode);
	}

	protected CodedRuntimeException(String errorCode, Throwable cause)
	{
		super(cause);
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorCode()
	{
		return errorCode;
	}

	@Override
	public String getLocalizedMessage()
	{
		try
		{
			return getMessageSource().getMessage(getErrorCode(), getLocalizedMessageArguments(),
					super.getLocalizedMessage(), getLocale());
		}
		catch (RuntimeException ex)
		{
			return MessageFormat.format("FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE {0}.%nFallback message: {1}",
					errorCode, super.getLocalizedMessage());
		}
	}

	/**
	 * @return the arguments for both the message format and the localized message to use
	 */
	protected abstract Object[] getLocalizedMessageArguments();
}
