package org.molgenis.security.captcha;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;

@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_SESSION)
@Service
public class CaptchaService
{
	@Autowired
	private HttpSession session;

	/**
	 * Creates a {@link Captcha} and stores it in the session.
	 *
	 * @param width  the width of the image
	 * @param height the height of the image
	 * @return {@link BufferedImage} containing the captcha image
	 */
	public BufferedImage createCaptcha(int width, int height)
	{
		Captcha captcha = new Captcha.Builder(width, height).addText()
															.addBackground(new GradiatedBackgroundProducer())
															.gimp()
															.addNoise()
															.addBorder()
															.build();
		session.setAttribute(Captcha.NAME, captcha);
		return captcha.getImage();
	}

	/**
	 * Validates a captcha answer. The same captcha can be validated multiple times.
	 *
	 * @param captchaAnswer the String to validate
	 * @return boolean indicating if the answer is correct
	 * @throws CaptchaException if no captcha found to validate
	 */
	public boolean validateCaptcha(String captchaAnswer) throws CaptchaException
	{
		Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
		if (captcha == null) throw new CaptchaException("no captcha to validate");
		return captcha.isCorrect(captchaAnswer);
	}

	public void removeCaptcha()
	{
		session.removeAttribute(Captcha.NAME);
	}
}
