package org.molgenis.security.captcha;

@Deprecated // FIXME extend from CodedRuntimeException
public class CaptchaException extends Exception
{
	private static final long serialVersionUID = 1L;

	public CaptchaException(String message)
	{
		super(message);
	}
}