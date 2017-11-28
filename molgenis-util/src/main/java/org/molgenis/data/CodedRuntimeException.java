package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * {@link RuntimeException} with error code.
 */
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
		return getLanguageService().map(languageService -> languageService.getMessageFormat(errorCode))
								   .map(format -> format.format(getLocalizedMessageArguments()))
								   .orElseGet(super::getLocalizedMessage);
	}

	/**
	 * @return the arguments for both the message format and the localized message to use
	 */
	protected abstract Object[] getLocalizedMessageArguments();
}
