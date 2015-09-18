package org.molgenis.security.account;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.mockito.ArgumentCaptor;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.account.AccountControllerTest.Config;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.HandleRequestDelegationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes =
{ Config.class, GsonConfig.class })
public class AccountControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AccountController authenticationController;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
				.setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter).build();

		reset(captchaService);
		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(true);
		when(captchaService.consumeCaptcha("validCaptcha")).thenReturn(true);
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
		this.mockMvc.perform(get("/account/password/reset")).andExpect(status().isOk())
				.andExpect(view().name("resetpassword-modal"));
	}

	@Test
	public void getRegisterForm() throws Exception
	{
		this.mockMvc.perform(get("/account/register")).andExpect(status().isOk())
				.andExpect(view().name("register-modal")).andExpect(model().attributeExists("countries"));
	}

	@Test
	public void activateUser() throws Exception
	{
		this.mockMvc.perform(get("/account/activate/123")).andExpect(view().name("forward:/"));
		verify(accountService).activateUser("123");
	}

	@Test
	public void registerUser_activationModeUserProxy() throws Exception
	{
		when(accountService.getActivationMode()).thenReturn(ActivationMode.USER);
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);

		this.mockMvc
				.perform(post("/account/register").header("X-Forwarded-Host", "website.com").param("username", "admin")
						.param("password", "adminpw-invalid").param("confirmPassword", "adminpw-invalid")
						.param("email", "admin@molgenis.org").param("lastname", "min").param("firstname", "ad")
						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk()).andExpect(content()
						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
		ArgumentCaptor<MolgenisUser> molgenisUserCaptor = ArgumentCaptor.forClass(MolgenisUser.class);
		ArgumentCaptor<String> baseActivationUriCaptor = ArgumentCaptor.forClass(String.class);
		verify(accountService).createUser(molgenisUserCaptor.capture(), baseActivationUriCaptor.capture());
		assertEquals(baseActivationUriCaptor.getValue(), "http://website.com/account/activate");
	}

	@Test
	public void registerUser_activationModeUserProxyWithScheme() throws Exception
	{
		when(accountService.getActivationMode()).thenReturn(ActivationMode.USER);
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);

		this.mockMvc
				.perform(post("/account/register").header("X-Forwarded-Proto", "https")
						.header("X-Forwarded-Host", "website.com").param("username", "admin")
						.param("password", "adminpw-invalid").param("confirmPassword", "adminpw-invalid")
						.param("email", "admin@molgenis.org").param("lastname", "min").param("firstname", "ad")
						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk()).andExpect(content()
						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
		ArgumentCaptor<MolgenisUser> molgenisUserCaptor = ArgumentCaptor.forClass(MolgenisUser.class);
		ArgumentCaptor<String> baseActivationUriCaptor = ArgumentCaptor.forClass(String.class);
		verify(accountService).createUser(molgenisUserCaptor.capture(), baseActivationUriCaptor.capture());
		assertEquals(baseActivationUriCaptor.getValue(), "https://website.com/account/activate");
	}

	@Test
	public void registerUser_activationModeUser() throws Exception
	{
		when(accountService.getActivationMode()).thenReturn(ActivationMode.USER);
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);

		this.mockMvc
				.perform(post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
						.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk()).andExpect(content()
						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
		verify(captchaService).consumeCaptcha("validCaptcha");
	}

	@Test
	public void registerUser_activationModeAdmin() throws Exception
	{
		when(accountService.getActivationMode()).thenReturn(ActivationMode.ADMIN);
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);

		this.mockMvc
				.perform(post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
						.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk()).andExpect(content()
						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_ADMIN + "\"}"));
		verify(captchaService).consumeCaptcha("validCaptcha");
	}

	@Test
	public void registerUser_invalidRegisterRequest() throws Exception
	{
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);
		this.mockMvc
				.perform(post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid").param("lastname", "min").param("firstname", "ad")
						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isBadRequest());
		verify(captchaService, times(0)).consumeCaptcha("validCaptcha");
	}

	@Test
	public void registerUser_passwordNotEqualsConfirmPassword() throws Exception
	{
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);
		this.mockMvc.perform(post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
				.param("confirmPassword", "adminpw-invalid-typo").param("email", "admin@molgenis.org")
				.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
		verify(captchaService, times(0)).consumeCaptcha("validCaptcha");
	}

	@Test
	public void registerUser_invalidCaptcha() throws Exception
	{
		when(accountService.isSelfRegistrationEnabled()).thenReturn(true);

		this.mockMvc.perform(post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
				.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
				.param("lastname", "min").param("firstname", "ad").param("captcha", "invalidCaptcha")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
	}

	@Test
	public void resetPassword() throws Exception
	{
		this.mockMvc.perform(post("/account/password/reset").param("email", "admin@molgenis.org")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
		verify(accountService).resetPassword("admin@molgenis.org");
	}

	// @Test
	// public void registerUser_invalidUserField() throws Exception
	// {
	// this.mockMvc.perform(
	// post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
	// .param("email", "admin@molgenis.org").param("lastname", "min").param("firstname", "ad")
	// .param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
	// .andExpect(status().isNoContent());
	// }

	@Configuration
	public static class Config
	{
		@Bean
		public AccountController accountController()
		{
			return new AccountController();
		}

		@Bean
		public AccountService accountService()
		{
			return mock(AccountService.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
		}

		@Bean
		public DataService dataService()
		{
			DataService dataService = mock(DataService.class);
			MolgenisUser molgenisUser = mock(MolgenisUser.class);
			when(dataService.findAll(MolgenisUser.ENTITY_NAME,
					new QueryImpl().eq(MolgenisUser.EMAIL, "admin@molgenis.org")))
							.thenReturn(Collections.<Entity> singletonList(molgenisUser));

			return dataService;
		}

		@Bean
		public CaptchaService captchaService()
		{
			return mock(CaptchaService.class);
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public RedirectStrategy redirectStrategy()
		{
			return mock(RedirectStrategy.class);
		}
	}
}
