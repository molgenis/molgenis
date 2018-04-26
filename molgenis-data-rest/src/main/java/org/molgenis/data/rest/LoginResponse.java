package org.molgenis.data.rest;

public class LoginResponse
{
	private final String token;
	private final String username;
	private final String firstname;
	private final String lastname;

	public LoginResponse(String token, String username, String firstname, String lastname)
	{
		this.token = token;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public String getToken()
	{
		return token;
	}

	public String getUsername()
	{
		return username;
	}

	public String getFirstname()
	{
		return firstname;
	}

	public String getLastname()
	{
		return lastname;
	}

}
