package org.molgenis.omx.auth.controller;

import java.awt.image.BufferedImage;

import javax.validation.Valid;

import org.molgenis.omx.auth.service.CaptchaService;
import org.molgenis.omx.auth.service.CaptchaService.CaptchaException;
import org.molgenis.omx.auth.vo.CaptchaRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
public class CaptchaController
{
	private static final int CAPTCHA_WIDTH = 220;
	private static final int CAPTCHA_HEIGHT = 50;

	@Autowired
	private CaptchaService captchaService;

	@RequestMapping(value = "/captcha", method = RequestMethod.GET, produces = "image/jpeg")
	@ResponseBody
	public BufferedImage getCaptcha()
	{
		return captchaService.createCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
	}

	@RequestMapping(value = "/captcha", method = RequestMethod.POST)
	@ResponseBody
	public Boolean validateCaptcha(@Valid @RequestBody CaptchaRequest captchaRequest) throws CaptchaException
	{
		return captchaService.validateCaptcha(captchaRequest.getCaptcha());
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/captcha", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public Boolean validateCaptchaFromForm(@Valid @ModelAttribute CaptchaRequest captchaRequest)
			throws CaptchaException
	{
		return captchaService.validateCaptcha(captchaRequest.getCaptcha());
	}
}
