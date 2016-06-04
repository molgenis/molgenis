package org.molgenis.ui.controller;

import org.mockito.Mock;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.user.UserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.ui.controller.FeedbackControllerTest.Config;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { Config.class, GsonConfig.class })
public class FeedbackControllerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private FeedbackController feedbackController;

	@Autowired
	private UserService userService;

	@Autowired
	private Supplier<MailSender> mailSenderSupplier;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private AppSettings appSettings;

	@Mock
	private MailSender mailSender;

	@Autowired
	private UserFactory userFactory;

	private MockMvc mockMvcFeedback;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
	}

	@BeforeMethod
	public void beforeMethod() throws CaptchaException
	{
		reset(mailSenderSupplier, appSettings, userService, mailSender, captchaService);
		when(mailSenderSupplier.get()).thenReturn(mailSender);
		when(appSettings.getTitle()).thenReturn("app123");
		mockMvcFeedback = MockMvcBuilders.standaloneSetup(feedbackController)
				.setMessageConverters(gsonHttpMessageConverter).build();
		Authentication authentication = new TestingAuthenticationToken("userName", null);
		authentication.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(true);
	}

	@Test
	public void initFeedbackAnonymous() throws Exception
	{
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("anonymous", null));

		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		verify(userService, never()).getUser("anonymous");

		mockMvcFeedback.perform(get(FeedbackController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-feedback")).andExpect(model().attribute("adminEmails", adminEmails))
				.andExpect(model().attributeDoesNotExist("userName"))
				.andExpect(model().attributeDoesNotExist("userEmail"));

	}

	@Test
	public void initFeedbackLoggedIn() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		User user = userFactory.create();
		user.setFirstName("First");
		user.setLastName("Last");
		user.setEmail("user@blah.org");
		when(userService.getUser("userName")).thenReturn(user);
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(get(FeedbackController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-feedback")).andExpect(model().attribute("adminEmails", adminEmails))
				.andExpect(model().attribute("userName", "First Last"))
				.andExpect(model().attribute("userEmail", "user@blah.org"));
	}

	@Test
	public void initFeedbackLoggedInDetailsUnknown() throws Exception
	{
		User user = userFactory.create();
		when(userService.getUser("userName")).thenReturn(user);
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(get(FeedbackController.URI)).andExpect(status().isOk())
				.andExpect(view().name("view-feedback")).andExpect(model().attribute("adminEmails", adminEmails))
				.andExpect(model().attributeDoesNotExist("userName"))
				.andExpect(model().attributeDoesNotExist("userEmail"));
	}

	@Test
	public void submit() throws Exception
	{
		List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
		when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
				.param("subject", "Feedback form").param("email", "user@domain.com")
				.param("feedback", "Feedback.\nLine two.").param("captcha", "validCaptcha")).andExpect(status().isOk())
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
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
				.param("subject", "Feedback form").param("email", "user@domain.com").param("feedback", "")
				.param("captcha", "validCaptcha")).andExpect(status().is4xxClientError());
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
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
				.param("subject", "Feedback form").param("email", "user@domain.com")
				.param("feedback", "Feedback.\nLine two.").param("captcha", "validCaptcha")).andExpect(status().isOk())
				.andExpect(view().name("view-feedback"))
				.andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false)))).andExpect(
				model().attribute("feedbackForm", hasProperty("errorMessage",
						equalTo("Unfortunately, we were unable to send the mail containing "
								+ "your feedback. Please contact the administrator."))));
		verify(captchaService, times(1)).validateCaptcha("validCaptcha");
	}

	@Test
	public void submitInvalidCaptcha() throws Exception
	{
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(false);
		mockMvcFeedback.perform(MockMvcRequestBuilders.post(FeedbackController.URI).param("name", "First Last")
				.param("subject", "Feedback form").param("email", "user@domain.com")
				.param("feedback", "Feedback.\nLine two.").param("captcha", "invalidCaptcha"))
				.andExpect(status().isOk()).andExpect(view().name("view-feedback"))
				.andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
				.andExpect(model().attribute("feedbackForm", hasProperty("errorMessage", equalTo("Invalid captcha."))));
	}

	@ComponentScan({ "org.molgenis.auth" })
	@Configuration
	public static class Config
	{
		public Config()
		{
			initMocks(this);
		}

		@Mock
		private Supplier<MailSender> mailSenderSupplier;

		@Bean
		public FeedbackController feedbackController()
		{
			return new FeedbackController(molgenisUserService(), appSettings(), captchaService(), mailSenderSupplier);
		}

		@Bean
		public UserService molgenisUserService()
		{
			return mock(UserService.class);
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
		public Supplier<MailSender> mailSenderSupplier()
		{
			return mailSenderSupplier;
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
