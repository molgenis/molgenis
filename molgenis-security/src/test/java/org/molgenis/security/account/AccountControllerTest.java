package org.molgenis.security.account;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserMetadata.EMAIL;
import static org.molgenis.data.security.auth.UserMetadata.USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.account.AccountControllerTest.Config;
import org.molgenis.security.captcha.ReCaptchaService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@WebAppConfiguration
@ContextConfiguration(classes = {Config.class})
class AccountControllerTest extends AbstractMockitoSpringContextTests {
  @Autowired private AccountController authenticationController;

  @Autowired private AccountService accountService;

  @Autowired private ReCaptchaService reCaptchaService;

  @Autowired private AuthenticationSettings authenticationSettings;

  @Autowired private AppSettings appSettings;

  @Autowired private PasswordResetter passwordResetter;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
    freeMarkerViewResolver.setSuffix(".ftl");
    mockMvc =
        MockMvcBuilders.standaloneSetup(authenticationController)
            .setMessageConverters(
                new FormHttpMessageConverter(), new GsonHttpMessageConverter(new Gson()))
            .build();

    reset(authenticationSettings);
    reset(reCaptchaService);
    reset(appSettings);
    when(reCaptchaService.validate("validCaptcha")).thenReturn(true);
    reset(accountService); // mocks in the config class are not resetted after each test
  }

  @Test
  void getLoginForm() throws Exception {
    this.mockMvc
        .perform(get("/account/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("login-modal"));
  }

  @Test
  void getPasswordResetForm() throws Exception {
    this.mockMvc
        .perform(get("/account/password/reset"))
        .andExpect(status().isOk())
        .andExpect(view().name("resetpassword-modal"));
  }

  @Test
  void getRegisterForm() throws Exception {
    this.mockMvc
        .perform(get("/account/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("register-modal"))
        .andExpect(model().attributeExists("countries"));
  }

  @Test
  void getActivateView() throws Exception {
    this.mockMvc
        .perform(get("/account/activate"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-activate"));
  }

  @Test
  void getActivateSuccessView() throws Exception {
    this.mockMvc
        .perform(get("/account/activate-success"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-activate-success"));
  }

  @Test
  void activateUser() throws Exception {
    this.mockMvc
        .perform(
            post("/account/activate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(
                    EntityUtils.toString(
                        new UrlEncodedFormEntity(
                            Arrays.asList(new BasicNameValuePair("activationCode", "123"))))))
        .andExpect(view().name("redirect:/account/activate-success"));
    verify(accountService).activateUser("123");
  }

  @Test
  void activateUserMolgenisUserException() throws Exception {
    doThrow(new MolgenisUserException("message")).when(accountService).activateUser("123");
    this.mockMvc
        .perform(
            post("/account/activate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(
                    EntityUtils.toString(
                        new UrlEncodedFormEntity(
                            Arrays.asList(new BasicNameValuePair("activationCode", "123"))))))
        .andExpect(view().name("redirect:/account/activate"))
        .andExpect(model().attribute("errorMessage", "message"));
    verify(accountService).activateUser("123");
  }

  @Test
  void registerUser_activationModeUser() throws Exception {
    when(authenticationSettings.getSignUp()).thenReturn(true);
    when(authenticationSettings.getSignUpModeration()).thenReturn(false);
    when(reCaptchaService.validate("validCaptcha")).thenReturn(true);
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);

    this.mockMvc
        .perform(
            post("/account/register")
                .param("username", "admin")
                .param("password", "adminpw-invalid")
                .param("confirmPassword", "adminpw-invalid")
                .param("email", "admin@molgenis.org")
                .param("lastname", "min")
                .param("firstname", "ad")
                .param("recaptcha", "validCaptcha")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    "{\"message\":\""
                        + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER
                        + "\"}"));
    verify(reCaptchaService).validate("validCaptcha");
  }

  @Test
  void registerUser_activationModeAdmin() throws Exception {
    when(authenticationSettings.getSignUp()).thenReturn(true);
    when(authenticationSettings.getSignUpModeration()).thenReturn(true);
    when(reCaptchaService.validate("validCaptcha")).thenReturn(true);
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);

    this.mockMvc
        .perform(
            post("/account/register")
                .param("username", "admin")
                .param("password", "adminpw-invalid")
                .param("confirmPassword", "adminpw-invalid")
                .param("email", "admin@molgenis.org")
                .param("lastname", "min")
                .param("firstname", "ad")
                .param("recaptcha", "validCaptcha")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    "{\"message\":\""
                        + AccountController.REGISTRATION_SUCCESS_MESSAGE_ADMIN
                        + "\"}"));
    verify(reCaptchaService).validate("validCaptcha");
  }

  @Test
  void registerUser_passwordNotEqualsConfirmPassword() throws Exception {
    when(authenticationSettings.getSignUp()).thenReturn(true);
    this.mockMvc
        .perform(
            post("/account/register")
                .param("username", "admin")
                .param("password", "adminpw-invalid")
                .param("confirmPassword", "adminpw-invalid-typo")
                .param("email", "admin@molgenis.org")
                .param("lastname", "min")
                .param("firstname", "ad")
                .param("recaptcha", "validCaptcha")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest());
    verify(reCaptchaService, times(0)).validate("validCaptcha");
  }

  @Test
  void registerUser_invalidCaptcha() throws Exception {
    when(authenticationSettings.getSignUp()).thenReturn(true);
    when(appSettings.getRecaptchaIsEnabled()).thenReturn(true);
    this.mockMvc
        .perform(
            post("/account/register")
                .param("username", "admin")
                .param("password", "adminpw-invalid")
                .param("confirmPassword", "adminpw-invalid")
                .param("email", "admin@molgenis.org")
                .param("lastname", "min")
                .param("firstname", "ad")
                .param("recaptch", "invalidCaptcha")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest());
  }

  @Test
  void resetPassword() throws Exception {
    this.mockMvc
        .perform(
            post("/account/password/reset")
                .param("email", "admin@molgenis.org")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isNoContent());
    verify(passwordResetter).resetPassword("admin@molgenis.org");
  }

  @Test
  void registerUser_invalidUserField() throws Exception {
    when(authenticationSettings.getSignUp()).thenReturn(true);
    this.mockMvc
        .perform(
            post("/account/register")
                .param("username", "admin")
                .param("password", "adminpw-invalid")
                .param("email", "admin@molgenis.org")
                .param("lastname", "min")
                .param("firstname", "ad")
                .param("recaptch", "validCaptcha")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest());
  }

  @Configuration
  static class Config {
    @Bean
    AccountController accountController() {
      return new AccountController(
          accountService(),
          reCaptchaV3Service(),
          authenticationSettings(),
          molgenisUserFactory(),
          appSettings(),
          passwordResetter());
    }

    @Bean
    AccountService accountService() {
      return mock(AccountService.class);
    }

    @Bean
    ReCaptchaService reCaptchaV3Service() {
      return mock(ReCaptchaService.class);
    }

    @Bean
    AuthenticationSettings authenticationSettings() {
      return mock(AuthenticationSettings.class);
    }

    @SuppressWarnings("unchecked")
    @Bean
    DataService dataService() {
      DataService dataService = mock(DataService.class);
      User user = mock(User.class);
      when(dataService.findAll(USER, new QueryImpl().eq(EMAIL, "admin@molgenis.org")))
          .thenReturn(Stream.<Entity>of(user));

      return dataService;
    }

    @Bean
    UserService molgenisUserService() {
      return mock(UserService.class);
    }

    @Bean
    UserFactory molgenisUserFactory() {
      UserFactory userFactory = mock(UserFactory.class);
      when(userFactory.create()).thenAnswer(invocationOnMock -> mock(User.class));
      return userFactory;
    }

    @Bean
    AppSettings appSettings() {
      return mock(AppSettings.class);
    }

    @Bean
    PasswordResetter passwordResetter() {
      return mock(PasswordResetter.class);
    }
  }
}
