package org.molgenis.security.account;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

class PasswordResetRequest
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
