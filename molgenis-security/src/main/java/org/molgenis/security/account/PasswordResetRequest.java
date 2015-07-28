package org.molgenis.security.account;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;

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
