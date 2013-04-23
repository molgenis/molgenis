package org.molgenis.omx.auth.vo;

import javax.validation.constraints.NotNull;

public class CaptchaRequest
{
	@NotNull
	private String captcha;

	public String getCaptcha()
	{
		return captcha;
	}

	public void setCaptcha(String captcha)
	{
		this.captcha = captcha;
	}
}