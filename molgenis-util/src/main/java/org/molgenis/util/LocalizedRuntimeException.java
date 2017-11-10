package org.molgenis.util;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Root exception for all exceptions displayed to user.
 */
public abstract class LocalizedRuntimeException extends RuntimeException
{
	private static final String BUNDLE_ID_PREFIX = "exception";

	private final String bundleId;
	private final String errorCode;

	protected LocalizedRuntimeException(String bundleId, String errorCode)
	{
		this.bundleId = requireNonNull(bundleId);
		this.errorCode = requireNonNull(errorCode);
	}

	protected LocalizedRuntimeException(String bundleId, String errorCode, Throwable cause)
	{
		super(cause);
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
	 * @param messageFormat
	 * @return localized exception message
	 */
	protected abstract String createLocalizedMessage(String messageFormat);

	/**
	 * Returns an error code that differs for each child class (e.g. D34)
	 *
	 * @return error code, never <tt>null</tt>
	 */
	@SuppressWarnings("unused")
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
		return "code: " + getErrorCode() + " " + createMessage();
	}

	/**
	 * Implement {@link #createLocalizedMessage(String)} to change behavior of this method.
	 *
	 * @return localized exception message (e.g. to display to the user), never null
	 */
	@Override
	public final String getLocalizedMessage()
	{
		ResourceBundle resourceBundle = getResourceBundle();
		String format = resourceBundle.getString(getErrorCode());

		return createLocalizedMessage(format) + ' ' + '(' + getErrorCode() + ')';
	}

	protected ResourceBundle getResourceBundle()
	{
		String baseName = BUNDLE_ID_PREFIX + '_' + bundleId;
		return ResourceBundle.getBundle(baseName, getLocale());
	}

	protected Locale getLocale()
	{
		return LocaleContextHolder.getLocale();
	}
}
