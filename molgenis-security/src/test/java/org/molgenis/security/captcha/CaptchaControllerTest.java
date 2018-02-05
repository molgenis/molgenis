package org.molgenis.security.captcha;

import com.google.gson.Gson;
import org.molgenis.core.util.MolgenisGsonHttpMessageConverter;
import org.molgenis.security.captcha.CaptchaControllerTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration(classes = { Config.class })
public class CaptchaControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private CaptchaController captchaController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(captchaController)
								 .setMessageConverters(new BufferedImageHttpMessageConverter(),
										 new FormHttpMessageConverter(), new MolgenisGsonHttpMessageConverter(new Gson()))
								 .build();
	}

	@Test
	public void getCaptcha() throws Exception
	{
		this.mockMvc.perform(get("/captcha").accept(MediaType.IMAGE_JPEG))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.IMAGE_JPEG));
	}

	@Test
	public void validateCaptcha_valid() throws Exception
	{
		this.mockMvc.perform(
				post("/captcha").content("{\"captcha\":\"captcha_answer\"}").contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().string("true"));
	}

	@Test
	public void validateCaptcha_invalid() throws Exception
	{
		this.mockMvc.perform(
				post("/captcha").content("{\"captcha\":\"invalid_answer\"}").contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().string("false"));
	}

	@Test
	public void validateCaptchaFromForm_valid() throws Exception
	{
		this.mockMvc.perform(
				post("/captcha").param("captcha", "captcha_answer").contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk())
					.andExpect(content().string("true"));
	}

	@Test
	public void validateCaptchaFromForm_invalid() throws Exception
	{
		this.mockMvc.perform(
				post("/captcha").param("captcha", "invalid_answer").contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk())
					.andExpect(content().string("false"));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public CaptchaController captchaController()
		{
			return new CaptchaController();
		}

		@Bean
		public CaptchaService captchaService() throws CaptchaException
		{
			CaptchaService captchaService = mock(CaptchaService.class);
			when(captchaService.createCaptcha(220, 50)).thenReturn(
					new BufferedImage(220, 50, BufferedImage.TYPE_INT_RGB));
			when(captchaService.validateCaptcha("captcha_answer")).thenReturn(true);
			return captchaService;
		}
	}
}
