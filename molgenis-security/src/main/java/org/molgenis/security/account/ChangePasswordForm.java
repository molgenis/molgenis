package org.molgenis.security.account;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.ScriptAssert;

@ScriptAssert(lang = "jexl", script = "_this.password1 == _this.password2")
public class ChangePasswordForm
{
	@NotBlank
	private String password1;

	@NotBlank
	private String password2;

	public String getPassword1()
	{
		return password1;
	}

	public void setPassword1(String password1)
	{
		this.password1 = password1;
	}

	public String getPassword2()
	{
		return password2;
	}

	public void setPassword2(String password2)
	{
		this.password2 = password2;
	}

}
