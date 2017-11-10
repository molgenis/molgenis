package org.molgenis.util;

import org.molgenis.data.MolgenisRuntimeException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public abstract class LocalizedRuntimeException extends MolgenisRuntimeException
{
	private static final String BUNDLE_ID_PREFIX = "exception";

	private final String bundleId;
	private final String errorCode;

	protected LocalizedRuntimeException(String bundleId, String errorCode)
	{
		this.bundleId = requireNonNull(bundleId);
		this.errorCode = requireNonNull(errorCode);
	}

	/**
	 * Create exception message returned by {@link #getMessage()}.
	 *
	 * @return exception message
	 */
	protected abstract String createMessage();

	/**
	 * Create localized exception message returned by {@link #getLocalizedMessage()}.
	 *
	 * @param resourceBundle resource bundle containing exception messages
	 * @param locale         locale for which to generate message
	 * @return localized exception message
	 */
	protected abstract String createLocalizedMessage(ResourceBundle resourceBundle, Locale locale);

	/**
	 * Returns an error code that differs for each child class (e.g. D34)
	 *
	 * @return error code, never <tt>null</tt>
	 */
	public String getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Implement {@link #createMessage()} to change behavior of this method.
	 *
	 * @return exception message (e.g. to log), never null
	 */
	@Override
	public final String getMessage()
	{
		return createMessage() + ' ' + getErrorCodeMessage();
	}

	/**
	 * Implement {@link #createLocalizedMessage(ResourceBundle, Locale)} to change behavior of this method.
	 *
	 * @return localized exception message (e.g. to display to the user), never null
	 */
	@Override
	public final String getLocalizedMessage()
	{
		return createLocalizedMessage(getResourceBundle(), getLocale()) + ' ' + getErrorCodeMessage();
	}

	private ResourceBundle getResourceBundle()
	{
		String baseName = BUNDLE_ID_PREFIX + '_' + bundleId;
		return ResourceBundle.getBundle(baseName, getLocale());
	}

	private Locale getLocale()
	{
		return LocaleContextHolder.getLocale();
	}

	/**
	 * Returns message part for the error code
	 */
	private String getErrorCodeMessage()
	{
		return '(' + errorCode + ')';
	}
}
