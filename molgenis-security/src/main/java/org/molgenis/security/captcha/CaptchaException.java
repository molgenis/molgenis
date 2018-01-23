package org.molgenis.security.captcha;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class CaptchaException extends Exception
{
	private static final long serialVersionUID = 1L;

	public CaptchaException(String message)
	{
		super(message);
	}
}