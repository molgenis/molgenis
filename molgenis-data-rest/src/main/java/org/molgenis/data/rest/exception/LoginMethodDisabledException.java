package org.molgenis.data.rest.exception;

/**
 * thrown if a loging method was used that is diabled in this MOLGENIS instance
 * for example: the api/v1/login is disabled when 2fa is active on a server
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class LoginMethodDisabledException extends RestApiException
{
	private static final String ERROR_CODE = "R04";

	public LoginMethodDisabledException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
