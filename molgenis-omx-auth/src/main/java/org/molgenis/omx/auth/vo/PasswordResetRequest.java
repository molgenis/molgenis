package org.molgenis.omx.auth.vo;

import javax.validation.constraints.NotNull;

public class PasswordResetRequest
{
	@NotNull
	private String username;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

}
