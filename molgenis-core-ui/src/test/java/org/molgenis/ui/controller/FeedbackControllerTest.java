package org.molgenis.ui.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.ui.controller.FeedbackControllerTest.Config;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class FeedbackControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private FeedbackController feedbackController;

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private AppSettings appSettings;

	private MockMvc mockMvcFeedback;

	private Authentication authentication;

	@BeforeMethod
	public void beforeMethod() throws CaptchaException
	{
		reset(javaMailSender, appSettings, molgenisUserService);
		when(appSettings.getTitle()).thenReturn("app123");
		mockMvcFeedback = MockMvcBuilders.standaloneSetup(feedbackController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
		authentication = new TestingAuthenticationToken("userName", null);
		authentication.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		reset(captchaService);
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(true);
	}

	@Test
	public void initFeedbackAnonymous() throws Exception
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("anonymous", null));

		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(molgenisUserService.getSuEmailAddresses()).thenReturn(adminEmails);
		verify(molgenisUserService, never()).getUser("anonymous");

		mockMvcFeedback.perform(get(FeedbackController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-feedback")).andExpect(model().attribute("adminEmails", adminEmails))
				.andExpect(model().attributeDoesNotExist("userName"))
				.andExpect(model().attributeDoesNotExist("userEmail"));

	}

	@Test
	public void initFeedbackLoggedIn() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		MolgenisUser user = new MolgenisUser();
		user.setFirstName("First");
		user.setLastName("Last");
		user.setEmail("user@blah.org");
		when(molgenisUserService.getUser("userName")).thenReturn(user);
		when(molgenisUserService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(get(FeedbackController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-feedback")).andExpect(model().attribute("adminEmails", adminEmails))
				.andExpect(model().attribute("userName", "First Last"))
				.andExpect(model().attribute("userEmail", "user@blah.org"));
	}

	@Test
	public void initFeedbackLoggedInDetailsUnknown() throws Exception
	{
		MolgenisUser user = new MolgenisUser();
		when(molgenisUserService.getUser("userName")).thenReturn(user);
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(molgenisUserService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(get(FeedbackController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-feedback")).andExpect(model().attribute("adminEmails", adminEmails))
				.andExpect(model().attributeDoesNotExist("userName"))
				.andExpect(model().attributeDoesNotExist("userEmail"));
	}

	@Test
	public void submit() throws Exception
	{
		MimeMessage message = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(message);
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(molgenisUserService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback
				.perform(
						MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
								.param("subject", "Feedback form").param("email", "user@domain.com")
								.param("feedback", "Feedback.\nLine two.").param("captcha", "validCaptcha"))
				.andExpect(status().isOk()).andExpect(view().name("view-feedback"))
				.andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(true))));
		verify(message, times(1)).setRecipients(RecipientType.TO, new InternetAddress[]
		{ new InternetAddress("molgenis@molgenis.org") });
		verify(message, times(1)).setRecipient(RecipientType.CC, new InternetAddress("user@domain.com"));
		verify(message, times(1)).setReplyTo(new InternetAddress[]
		{ new InternetAddress("user@domain.com") });
		verify(message, times(1)).setSubject("[feedback-app123] Feedback form");
		verify(message, times(1)).setText("Feedback from First Last (user@domain.com):\n\n" + "Feedback.\nLine two.");
		verify(javaMailSender, times(1)).send(message);
		verify(captchaService, times(1)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitFeedbackNotSpecified() throws Exception
	{
		mockMvcFeedback.perform(
				MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
						.param("subject", "Feedback form").param("email", "user@domain.com").param("feedback", "")
						.param("captcha", "validCaptcha")).andExpect(status().is4xxClientError());
		verify(captchaService, times(0)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitErrorWhileSendingMail() throws Exception
	{
		MimeMessage message = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(message);
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(molgenisUserService.getSuEmailAddresses()).thenReturn(adminEmails);
		doThrow(new MailSendException("ERRORRR!")).when(javaMailSender).send(message);
		mockMvcFeedback
				.perform(
						MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
								.param("subject", "Feedback form").param("email", "user@domain.com")
								.param("feedback", "Feedback.\nLine two.").param("captcha", "validCaptcha"))
				.andExpect(status().isOk())
				.andExpect(view().name("view-feedback"))
				.andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
				.andExpect(
						model().attribute(
								"feedbackForm",
								hasProperty("errorMessage",
										equalTo("Unfortunately, we were unable to send the mail containing "
												+ "your feedback. Please contact the administrator."))));
		verify(captchaService, times(1)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitInvalidCaptcha() throws Exception
	{
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(false);
		mockMvcFeedback
				.perform(
						MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
								.param("subject", "Feedback form").param("email", "user@domain.com")
								.param("feedback", "Feedback.\nLine two.").param("captcha", "invalidCaptcha"))
				.andExpect(status().isOk()).andExpect(view().name("view-feedback"))
				.andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
				.andExpect(model().attribute("feedbackForm", hasProperty("errorMessage", equalTo("Invalid captcha."))));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public FeedbackController feedbackController()
		{
			return new FeedbackController(molgenisUserService(), appSettings(), captchaService(), mailSender());
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public CaptchaService captchaService()
		{
			return mock(CaptchaService.class);
		}

		@Bean
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public StaticContentService staticContentService()
		{
			return mock(StaticContentService.class);
		}
	}
}
