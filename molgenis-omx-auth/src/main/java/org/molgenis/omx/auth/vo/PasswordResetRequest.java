package org.molgenis.omx.auth.vo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;

public class PasswordResetRequest
{
	@NotNull
	@Email
	private String email;

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

}
