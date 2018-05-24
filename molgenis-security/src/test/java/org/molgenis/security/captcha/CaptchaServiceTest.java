package org.molgenis.security.captcha;

import cn.apiclub.captcha.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@ContextConfiguration
public class CaptchaServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public CaptchaService captchaService()
		{
			return new CaptchaService();
		}

		@Bean
		public HttpSession httpSession()
		{
			return mock(HttpSession.class);
		}
	}

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private HttpSession httpSession;

	@BeforeMethod
	public void beforeMethod()
	{
		reset(httpSession);
	}

	@Test
	public void createCaptcha()
	{
		BufferedImage captchaImg = captchaService.createCaptcha(100, 75);
		assertEquals(captchaImg.getWidth(), 100);
		assertEquals(captchaImg.getHeight(), 75);
		verify(httpSession).setAttribute(eq(Captcha.NAME), any(Captcha.class));
	}

	@Test
	public void validateCaptcha() throws CaptchaException
	{
		Captcha captcha = new Captcha.Builder(100, 75).build();
		when(httpSession.getAttribute(Captcha.NAME)).thenReturn(captcha);
		assertTrue(captchaService.validateCaptcha(captcha.getAnswer()));
		assertFalse(captchaService.validateCaptcha("invalid_answer"));
		assertTrue(captchaService.validateCaptcha(captcha.getAnswer()));
		verify(httpSession, times(0)).removeAttribute(Captcha.NAME);
		verify(httpSession, times(0)).setAttribute(eq(Captcha.NAME), any(Object.class));
	}

	@Test(expectedExceptions = CaptchaException.class)
	public void validateCaptcha_noCaptcha() throws CaptchaException
	{
		when(httpSession.getAttribute(Captcha.NAME)).thenReturn(null);
		assertFalse(captchaService.validateCaptcha("invalid_answer"));
	}

}
