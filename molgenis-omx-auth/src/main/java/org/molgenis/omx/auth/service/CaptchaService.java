package org.molgenis.omx.auth.service;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpSession;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Scope(WebApplicationContext.SCOPE_SESSION)
@Service
public class CaptchaService
{
	@Autowired
	private HttpSession session;

	public BufferedImage createCaptcha(int width, int height)
	{
		Captcha captcha = new Captcha.Builder(width, height).addText().addBackground(new GradiatedBackgroundProducer())
				.gimp().addNoise().addBorder().build();
		session.setAttribute(Captcha.NAME, captcha);
		return captcha.getImage();
	}

	public boolean validateCaptcha(String captchaAnswer) throws CaptchaException
	{
		Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
		if (captcha == null) throw new CaptchaException("no captcha to validate");
		return captcha.isCorrect(captchaAnswer);
	}

	public static class CaptchaException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public CaptchaException(String message)
		{
			super(message);
		}
	}
}
