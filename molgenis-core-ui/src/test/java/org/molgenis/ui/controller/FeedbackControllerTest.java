package org.molgenis.ui.controller;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.core.service.UserService;
import org.molgenis.ui.controller.FeedbackControllerTest.Config;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { Config.class, GsonConfig.class })
@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class FeedbackControllerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private FeedbackController feedbackController;

	@Autowired
	private UserService userService;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private MailSender mailSender;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private AppSettings appSettings;

	private MockMvc mockMvcFeedback;

	@BeforeMethod
	public void beforeMethod() throws CaptchaException
	{
		reset(mailSender, appSettings, userService, captchaService);
		when(appSettings.getTitle()).thenReturn("app123");
		mockMvcFeedback = MockMvcBuilders.standaloneSetup(feedbackController)
										 .setMessageConverters(gsonHttpMessageConverter)
										 .build();
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(true);
	}

	@Test
	@WithAnonymousUser
	public void initFeedbackAnonymous() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		verify(userService, never()).findByUsername("anonymous");

		mockMvcFeedback.perform(get(FeedbackController.URI))
					   .andExpect(status().isOk())
					   .andExpect(view().name("view-feedback"))
					   .andExpect(model().attribute("adminEmails", adminEmails))
					   .andExpect(model().attributeDoesNotExist("userName"))
					   .andExpect(model().attributeDoesNotExist("userEmail"));

	}

	@Test
	@WithMockUser
	public void initFeedbackLoggedIn() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		User user = mock(User.class);
		when(user.getFormattedName()).thenReturn("First Last");
		when(user.getEmail()).thenReturn("user@blah.org");
		when(userAccountService.getCurrentUser()).thenReturn(user);
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(get(FeedbackController.URI))
					   .andExpect(status().isOk())
					   .andExpect(view().name("view-feedback"))
					   .andExpect(model().attribute("adminEmails", adminEmails))
					   .andExpect(model().attribute("userName", "First Last"))
					   .andExpect(model().attribute("userEmail", "user@blah.org"));
	}

	@Test
	public void initFeedbackLoggedInDetailsUnknown() throws Exception
	{
		when(userAccountService.getCurrentUserIfPresent()).thenReturn(Optional.empty());
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(get(FeedbackController.URI))
					   .andExpect(status().isOk())
					   .andExpect(view().name("view-feedback"))
					   .andExpect(model().attribute("adminEmails", adminEmails))
					   .andExpect(model().attributeDoesNotExist("userName"))
					   .andExpect(model().attributeDoesNotExist("userEmail"));
	}

	@Test
	public void submit() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI)
													  .param("name", "First Last")
													  .param("subject", "Feedback form")
													  .param("email", "user@domain.com")
													  .param("feedback", "Feedback.\nLine two.")
													  .param("captcha", "validCaptcha"))
					   .andExpect(status().isOk())
					   .andExpect(view().name("view-feedback"))
					   .andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(true))));

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("molgenis@molgenis.org");
		expected.setCc("user@domain.com");
		expected.setReplyTo("user@domain.com");
		expected.setSubject("[feedback-app123] Feedback form");
		expected.setText("Feedback from First Last (user@domain.com):\n\n" + "Feedback.\nLine two.");
		verify(mailSender, times(1)).send(expected);
		verify(captchaService, times(1)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitFeedbackNotSpecified() throws Exception
	{
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI)
													  .param("name", "First Last")
													  .param("subject", "Feedback form")
													  .param("email", "user@domain.com")
													  .param("feedback", "")
													  .param("captcha", "validCaptcha"))
					   .andExpect(status().is4xxClientError());
		verify(captchaService, times(0)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitErrorWhileSendingMail() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo("molgenis@molgenis.org");
		expected.setCc("user@domain.com");
		expected.setReplyTo("user@domain.com");
		expected.setSubject("[feedback-app123] Feedback form");
		expected.setText("Feedback from First Last (user@domain.com):\n\n" + "Feedback.\nLine two.");
		doThrow(new MailSendException("ERRORRR!")).when(mailSender).send(expected);
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI)
													  .param("name", "First Last")
													  .param("subject", "Feedback form")
													  .param("email", "user@domain.com")
													  .param("feedback", "Feedback.\nLine two.")
													  .param("captcha", "validCaptcha"))
					   .andExpect(status().isOk())
					   .andExpect(view().name("view-feedback"))
					   .andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
					   .andExpect(model().attribute("feedbackForm", hasProperty("errorMessage",
							   equalTo("Unfortunately, we were unable to send the mail containing "
									   + "your feedback. Please contact the administrator."))));
		verify(captchaService, times(1)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitInvalidCaptcha() throws Exception
	{
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(false);
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI)
													  .param("name", "First Last")
													  .param("subject", "Feedback form")
													  .param("email", "user@domain.com")
													  .param("feedback", "Feedback.\nLine two.")
													  .param("captcha", "invalidCaptcha"))
					   .andExpect(status().isOk())
					   .andExpect(view().name("view-feedback"))
					   .andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
					   .andExpect(model().attribute("feedbackForm",
							   hasProperty("errorMessage", equalTo("Invalid captcha."))));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public FeedbackController feedbackController()
		{
			return new FeedbackController(userService(), userAccountService(), appSettings(), captchaService(),
					mailSender());
		}

		@Bean
		public UserService userService()
		{
			return mock(UserService.class);
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return mock(UserAccountService.class);
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
		public MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		public StaticContentService staticContentService()
		{
			return mock(StaticContentService.class);
		}
	}
}
