package org.molgenis.compute5.db.api;

public class StartRunRequest
{
	private final String runName;
	private final String username;
	private final String password;

	public StartRunRequest(String runName, String username, String password)
	{
		this.runName = runName;
		this.username = username;
		this.password = password;
	}

	public String getRunName()
	{
		return runName;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

}
