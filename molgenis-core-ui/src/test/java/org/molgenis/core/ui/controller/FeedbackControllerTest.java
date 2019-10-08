package org.molgenis.core.ui.controller;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.core.ui.controller.FeedbackControllerTest.Config;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.config.UserTestConfig;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.captcha.ReCaptchaService;
import org.molgenis.settings.AppSettings;
import org.molgenis.util.i18n.TestAllPropertiesMessageSource;
import org.molgenis.util.i18n.format.MessageFormatFactory;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebAppConfiguration
@ContextConfiguration(classes = {Config.class, GsonConfig.class})
class FeedbackControllerTest extends AbstractMolgenisSpringTest {
  @Autowired private FeedbackController feedbackController;

  @Autowired private UserService userService;

  @Autowired private MailSender mailSender;

  @Autowired private ReCaptchaService reCaptchaService;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Autowired private AppSettings appSettings;

  @Autowired private UserFactory userFactory;

  private MockMvc mockMvcFeedback;
  private SecurityContext previousContext;

  @BeforeEach
  void beforeMethod() {
    reset(mailSender, appSettings, userService, reCaptchaService);
    when(appSettings.getTitle()).thenReturn("app123");
    mockMvcFeedback =
        MockMvcBuilders.standaloneSetup(feedbackController)
            .setMessageConverters(gsonHttpMessageConverter)
            .build();
    Authentication authentication = new TestingAuthenticationToken("userName", null);
    authentication.setAuthenticated(true);

    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);
  }

  @AfterEach
  void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void initFeedbackAnonymous() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("anonymous", null));

    List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
    when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
    verify(userService, never()).getUser("anonymous");

    mockMvcFeedback
        .perform(get(FeedbackController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name("view-feedback"))
        .andExpect(model().attribute("adminEmails", adminEmails))
        .andExpect(model().attributeDoesNotExist("userName"))
        .andExpect(model().attributeDoesNotExist("userEmail"));
  }

  @Test
  void initFeedbackLoggedIn() throws Exception {
    List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
    User user = userFactory.create();
    user.setFirstName("First");
    user.setLastName("Last");
    user.setEmail("user@blah.org");
    when(userService.getUser("userName")).thenReturn(user);
    when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
    mockMvcFeedback
        .perform(get(FeedbackController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name("view-feedback"))
        .andExpect(model().attribute("adminEmails", adminEmails))
        .andExpect(model().attribute("userName", "First Last"))
        .andExpect(model().attribute("userEmail", "user@blah.org"));
  }

  @Test
  void initFeedbackLoggedInDetailsUnknown() throws Exception {
    User user = userFactory.create();
    when(userService.getUser("userName")).thenReturn(user);
    List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
    when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
    mockMvcFeedback
        .perform(get(FeedbackController.URI))
        .andExpect(status().isOk())
        .andExpect(view().name("view-feedback"))
        .andExpect(model().attribute("adminEmails", adminEmails))
        .andExpect(model().attributeDoesNotExist("userName"))
        .andExpect(model().attributeDoesNotExist("userEmail"));
  }

  @Test
  void submit() throws Exception {
    List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
    when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);
    when(reCaptchaService.validate("validCaptcha")).thenReturn(true);
    mockMvcFeedback
        .perform(
            MockMvcRequestBuilders.post(FeedbackController.URI)
                .param("name", "First Last")
                .param("subject", "Feedback form")
                .param("email", "user@domain.com")
                .param("feedback", "Feedback.\nLine two.")
                .param("recaptcha", "validCaptcha"))
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
    verify(reCaptchaService, times(1)).validate("validCaptcha");
  }

  @Test
  void submitFeedbackNotSpecified() throws Exception {
    mockMvcFeedback
        .perform(
            MockMvcRequestBuilders.post(FeedbackController.URI)
                .param("name", "First Last")
                .param("subject", "Feedback form")
                .param("email", "user@domain.com")
                .param("feedback", "")
                .param("recaptcha", "validCaptcha"))
        .andExpect(status().is4xxClientError());
    verify(reCaptchaService, times(0)).validate("validCaptcha");
  }

  @Test
  void submitAuthenticationErrorWhileSendingMail() throws Exception {
    List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
    when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);
    when(reCaptchaService.validate("validCaptcha")).thenReturn(true);
    SimpleMailMessage expected = new SimpleMailMessage();
    expected.setTo("molgenis@molgenis.org");
    expected.setCc("user@domain.com");
    expected.setReplyTo("user@domain.com");
    expected.setSubject("[feedback-app123] Feedback form");
    expected.setText("Feedback from First Last (user@domain.com):\n\n" + "Feedback.\nLine two.");
    doThrow(new MailAuthenticationException("ERRORRR!")).when(mailSender).send(expected);
    mockMvcFeedback
        .perform(
            MockMvcRequestBuilders.post(FeedbackController.URI)
                .param("name", "First Last")
                .param("subject", "Feedback form")
                .param("email", "user@domain.com")
                .param("feedback", "Feedback.\nLine two.")
                .param("recaptcha", "validCaptcha"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-feedback"))
        .andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
        .andExpect(
            model()
                .attribute(
                    "feedbackForm",
                    hasProperty(
                        "errorMessage",
                        equalTo(
                            "Unfortunately, we were unable to send the mail containing "
                                + "your feedback. Please contact the administrator."))));
    verify(reCaptchaService, times(1)).validate("validCaptcha");
  }

  @Test
  void submitErrorWhileSendingMail() throws Exception {
    List<String> adminEmails = Collections.singletonList("molgenis@molgenis.org");
    when(userService.getSuEmailAddresses()).thenReturn(adminEmails);
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);
    when(reCaptchaService.validate("validCaptcha")).thenReturn(true);
    SimpleMailMessage expected = new SimpleMailMessage();
    expected.setTo("molgenis@molgenis.org");
    expected.setCc("user@domain.com");
    expected.setReplyTo("user@domain.com");
    expected.setSubject("[feedback-app123] Feedback form");
    expected.setText("Feedback from First Last (user@domain.com):\n\n" + "Feedback.\nLine two.");
    doThrow(new MailSendException("ERRORRR!")).when(mailSender).send(expected);
    mockMvcFeedback
        .perform(
            MockMvcRequestBuilders.post(FeedbackController.URI)
                .param("name", "First Last")
                .param("subject", "Feedback form")
                .param("email", "user@domain.com")
                .param("feedback", "Feedback.\nLine two.")
                .param("recaptcha", "validCaptcha"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-feedback"))
        .andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
        .andExpect(
            model()
                .attribute(
                    "feedbackForm",
                    hasProperty(
                        "errorMessage",
                        equalTo(
                            "Unfortunately, we were unable to send the mail containing "
                                + "your feedback. Please contact the administrator."))));
    verify(reCaptchaService, times(1)).validate("validCaptcha");
  }

  @Test
  void submitInvalidCaptcha() throws Exception {
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);
    when(reCaptchaService.validate("invalidCaptcha")).thenReturn(false);
    mockMvcFeedback
        .perform(
            MockMvcRequestBuilders.post(FeedbackController.URI)
                .param("name", "First Last")
                .param("subject", "Feedback form")
                .param("email", "user@domain.com")
                .param("feedback", "Feedback.\nLine two.")
                .param("recaptcha", "invalidCaptcha"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-feedback"))
        .andExpect(model().attribute("feedbackForm", hasProperty("submitted", equalTo(false))))
        .andExpect(
            model()
                .attribute(
                    "feedbackForm",
                    hasProperty(
                        "errorMessage",
                        equalTo("You are not human, go away robot. Stop spamming the humans."))));
  }

  @Configuration
  @Import(UserTestConfig.class)
  static class Config {
    @Bean
    FeedbackController feedbackController() {
      return new FeedbackController(
          molgenisUserService(), appSettings(), reCaptchaService(), mailSender(), messageSource());
    }

    @Bean
    UserService molgenisUserService() {
      return mock(UserService.class);
    }

    @Bean
    AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    ReCaptchaService reCaptchaService() {
      return mock(ReCaptchaService.class);
    }

    @Bean
    TestAllPropertiesMessageSource messageSource() {
      TestAllPropertiesMessageSource testAllPropertiesMessageSource =
          new TestAllPropertiesMessageSource(new MessageFormatFactory());
      testAllPropertiesMessageSource.addMolgenisNamespaces("feedback");
      return testAllPropertiesMessageSource;
    }

    @Bean
    MailSender mailSender() {
      return mock(MailSender.class);
    }

    @Bean
    StaticContentService staticContentService() {
      return mock(StaticContentService.class);
    }
  }
}
