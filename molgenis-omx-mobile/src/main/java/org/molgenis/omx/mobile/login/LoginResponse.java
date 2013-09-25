package org.molgenis.omx.mobile.login;

public class LoginResponse
{
	private final String errorMessage;
	private final boolean success;

	public LoginResponse(String errorMessage)
	{
		this.errorMessage = errorMessage;
		this.success = errorMessage == null;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public boolean isSuccess()
	{
		return success;
	}
}
