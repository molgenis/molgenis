package org.molgenis.security.account;

import com.google.gson.Gson;
import org.molgenis.security.account.AccountControllerTest.Config;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.MolgenisUserException;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { Config.class })
public class AccountControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AccountController authenticationController;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private AuthenticationSettings authenticationSettings;

	private MockMvc mockMvc;

	private RegisterRequest registerRequest;

	@BeforeMethod
	public void setUp() throws CaptchaException
	{
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
								 .setMessageConverters(new FormHttpMessageConverter(),
										 new GsonHttpMessageConverter(new Gson()))
								 .build();

		registerRequest = new RegisterRequest();
		registerRequest.setUsername("admin");
		registerRequest.setPassword("adminpw-invalid");
		registerRequest.setConfirmPassword("adminpw-invalid");
		registerRequest.setEmail("admin@molgenis.org");
		registerRequest.setLastname("min");
		registerRequest.setFirstname("ad");

		reset(authenticationSettings);
		reset(captchaService);
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(true);
		reset(accountService); // mocks in the config class are not resetted after each test
	}

	@Test
	public void getLoginForm() throws Exception
	{
		this.mockMvc.perform(get("/account/login")).andExpect(status().isOk()).andExpect(view().name("login-modal"));
	}

	@Test
	public void getPasswordResetForm() throws Exception
	{
		this.mockMvc.perform(get("/account/password/reset"))
					.andExpect(status().isOk())
					.andExpect(view().name("resetpassword-modal"));
	}

	@Test
	public void getRegisterForm() throws Exception
	{
		this.mockMvc.perform(get("/account/register"))
					.andExpect(status().isOk())
					.andExpect(view().name("register-modal"))
					.andExpect(model().attributeExists("countries"));
	}

	@Test
	public void activateUser() throws Exception
	{
		this.mockMvc.perform(get("/account/activate/123")).andExpect(view().name("forward:/"));
		verify(accountService).activateUser("123");
	}

	@Test
	public void activateUserMolgenisUserException() throws Exception
	{
		doThrow(new MolgenisUserException("message")).when(accountService).activateUser("123");
		this.mockMvc.perform(get("/account/activate/123"))
					.andExpect(view().name("forward:/"))
					.andExpect(model().attribute("warningMessage", "message"));
		verify(accountService).activateUser("123");
	}

	@Test
	public void registerUser_activationModeUserProxy() throws Exception
	{
		when(authenticationSettings.getSignUp()).thenReturn(true);
		when(authenticationSettings.getSignUpModeration()).thenReturn(false);

		this.mockMvc.perform(post("/account/register").header("X-Forwarded-Host", "website.com")
													  .param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk())
					.andExpect(content().string(
							"{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
		verify(accountService).register(registerRequest, "http://website.com/account/activate");
	}

	@Test
	public void registerUser_activationModeUserProxyWithScheme() throws Exception
	{
		when(authenticationSettings.getSignUp()).thenReturn(true);
		when(authenticationSettings.getSignUpModeration()).thenReturn(false);

		this.mockMvc.perform(post("/account/register").header("X-Forwarded-Proto", "https")
													  .header("X-Forwarded-Host", "website.com")
													  .param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk())
					.andExpect(content().string(
							"{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
		verify(accountService).register(registerRequest, "https://website.com/account/activate");
	}

	@Test
	public void registerUser_activationModeUser() throws Exception
	{
		when(authenticationSettings.getSignUp()).thenReturn(true);
		when(authenticationSettings.getSignUpModeration()).thenReturn(false);

		this.mockMvc.perform(post("/account/register").param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk())
					.andExpect(content().string(
							"{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
		verify(captchaService).validateCaptcha("validCaptcha");
		verify(accountService).register(registerRequest, "http://localhost/account/activate");
	}

	@Test
	public void registerUser_activationModeAdmin() throws Exception
	{
		when(authenticationSettings.getSignUp()).thenReturn(true);
		when(authenticationSettings.getSignUpModeration()).thenReturn(true);

		this.mockMvc.perform(post("/account/register").param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk())
					.andExpect(content().string(
							"{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_ADMIN + "\"}"));
		verify(captchaService).validateCaptcha("validCaptcha");
		verify(accountService).register(registerRequest, "http://localhost/account/activate");
	}

	@Test
	public void registerUser_invalidRegisterRequest() throws Exception
	{
		// when(accountService.isSelfRegistrationEnabled()).thenReturn(true);
		this.mockMvc.perform(post("/account/register").param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isBadRequest());
		verifyZeroInteractions(captchaService);
		verifyZeroInteractions(accountService);
	}

	@Test
	public void registerUser_passwordNotEqualsConfirmPassword() throws Exception
	{
		when(authenticationSettings.getSignUp()).thenReturn(true);
		this.mockMvc.perform(post("/account/register").param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid-typo")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isBadRequest());
		verifyZeroInteractions(captchaService);
		verifyZeroInteractions(accountService);
	}

	@Test
	public void registerUser_invalidCaptcha() throws Exception
	{
		when(authenticationSettings.getSignUp()).thenReturn(true);
		this.mockMvc.perform(post("/account/register").param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("confirmPassword", "adminpw-invalid")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "invalidCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isBadRequest());
		verifyZeroInteractions(accountService);
	}

	@Test
	public void resetPassword() throws Exception
	{
		this.mockMvc.perform(post("/account/password/reset").param("email", "admin@molgenis.org")
															.contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isNoContent());
		verify(accountService).resetPassword("admin@molgenis.org");
	}

	@Test
	public void postPasswordChange() throws Exception
	{
		User user = when(mock(User.class).getUsername()).thenReturn("user").getMock();
		when(userAccountService.getCurrentUser()).thenReturn(user);

		this.mockMvc.perform(post("/account/password/change").param("password1", "abcdefg")
															 .param("password2", "abcdefg")
															 .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isOk());

		verify(accountService).changePassword("user", "abcdefg");
	}

	@Test
	public void postPasswordChangeNoMatch() throws Exception
	{
		this.mockMvc.perform(post("/account/password/change").param("password1", "abcdefg")
															 .param("password2", "different")
															 .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isBadRequest());

		verifyZeroInteractions(accountService);
	}

	@Test
	public void registerUser_invalidUserField() throws Exception
	{
		this.mockMvc.perform(post("/account/register").param("username", "admin")
													  .param("password", "adminpw-invalid")
													  .param("email", "admin@molgenis.org")
													  .param("lastname", "min")
													  .param("firstname", "ad")
													  .param("captcha", "validCaptcha")
													  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
					.andExpect(status().isBadRequest());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public AccountController accountController()
		{
			return new AccountController(userAccountService(), accountService(), captchaService(), redirectStrategy(),
					authenticationSettings());
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return mock(UserAccountService.class);
		}

		@Bean
		public AccountService accountService()
		{
			return mock(AccountService.class);
		}

		@Bean
		public CaptchaService captchaService()
		{
			return mock(CaptchaService.class);
		}

		@Bean
		public RedirectStrategy redirectStrategy()
		{
			return mock(RedirectStrategy.class);
		}

		@Bean
		public AuthenticationSettings authenticationSettings()
		{
			return mock(AuthenticationSettings.class);
		}

	}
}
