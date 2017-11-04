package org.molgenis.security.captcha;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.awt.image.BufferedImage;

@Api("Captcha")
@Controller
@RequestMapping("/captcha")
public class CaptchaController
{
	private static final int CAPTCHA_WIDTH = 220;
	private static final int CAPTCHA_HEIGHT = 50;

	@Autowired
	private CaptchaService captchaService;

	@ApiOperation("Generate captcha")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Returns generated captcha", response = BufferedImage.class)
	})
	@GetMapping(produces = "image/jpeg")
	@ResponseBody
	public BufferedImage getCaptcha()
	{
		return captchaService.createCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
	}

	@ApiOperation("Validate captcha")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Captcha is valid", response = Boolean.class),
			@ApiResponse(code = 400, message = "Captcha is invalid", response = CaptchaException.class)
	})
	@PostMapping
	@ResponseBody
	public Boolean validateCaptcha(@Valid @RequestBody CaptchaRequest captchaRequest) throws CaptchaException
	{
		return captchaService.validateCaptcha(captchaRequest.getCaptcha());
	}

	@ApiOperation("Validate captcha from form")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Captcha is valid", response = Boolean.class),
			@ApiResponse(code = 400, message = "Captcha is invalid", response = CaptchaException.class)
	})
	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@PostMapping(headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public Boolean validateCaptchaFromForm(@Valid @ModelAttribute CaptchaRequest captchaRequest) throws CaptchaException
	{
		return captchaService.validateCaptcha(captchaRequest.getCaptcha());
	}
}
