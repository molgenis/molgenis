package org.molgenis.omx.auth.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.AccountService;
import org.molgenis.omx.auth.service.CaptchaService;
import org.molgenis.util.HandleRequestDelegationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class AccountControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AccountController authenticationController;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private Database database;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
				.setMessageConverters(new FormHttpMessageConverter()).build();
		Login login = mock(Login.class);
		when(login.login(database, "admin", "adminpw")).thenReturn(true);
		when(database.getLogin()).thenReturn(login);
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
		this.mockMvc.perform(get("/account/password/reset")).andExpect(status().isOk())
				.andExpect(view().name("resetpassword-modal"));
	}

	@Test
	public void getRegisterForm() throws Exception
	{
		this.mockMvc.perform(get("/account/register")).andExpect(status().isOk())
				.andExpect(view().name("register-modal"))
				.andExpect(model().attributeExists("institutes", "personroles", "countries"));
	}

	@Test
	public void loginUser() throws Exception
	{
		this.mockMvc.perform(
				post("/account/login").param("username", "admin").param("password", "adminpw")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
	}

	@Test
	public void loginUser_unauthorized() throws Exception
	{
		this.mockMvc.perform(
				post("/account/login").param("username", "admin").param("password", "adminpw-invalid")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isUnauthorized());
	}

	@Test
	public void activateUser() throws Exception
	{
		this.mockMvc.perform(get("/account/activate/123")).andExpect(view().name("redirect:http://localhost"));
		verify(accountService).activateUser("123");
	}

	@Test
	public void logoutUser() throws Exception
	{
		this.mockMvc.perform(get("/account/logout")).andExpect(view().name("redirect:http://localhost"));
	}

	@Test
	public void registerUser() throws Exception
	{
		this.mockMvc.perform(
				post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
						.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
	}

	@Test
	public void registerUser_invalidRegisterRequest() throws Exception
	{
		this.mockMvc.perform(
				post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid").param("lastname", "min").param("firstname", "ad")
						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void registerUser_passwordNotEqualsConfirmPassword() throws Exception
	{
		this.mockMvc.perform(
				post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid-typo").param("email", "admin@molgenis.org")
						.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
	}

	@Test
	public void registerUser_invalidCaptcha() throws Exception
	{
		this.mockMvc.perform(
				post("/account/register").param("username", "admin").param("password", "adminpw-invalid")
						.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
						.param("lastname", "min").param("firstname", "ad").param("captcha", "invalidCaptcha")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
	}

	@Test
	public void resetPassword() throws Exception
	{
		this.mockMvc.perform(
				post("/account/password/reset").param("email", "admin@molgenis.org").contentType(
						MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
		verify(accountService).resetPassword(any(MolgenisUser.class));
	}

	@Test
	public void resetPassword_invalidUser() throws Exception
	{
		this.mockMvc.perform(
				post("/account/password/reset").param("email", "invalidUser@molgenis.org").contentType(
						MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
		verifyZeroInteractions(accountService);
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
		public Database database() throws DatabaseException
		{
			Database database = mock(Database.class);
			MolgenisUser molgenisUser = mock(MolgenisUser.class);
			when(
					database.find(MolgenisUser.class, new QueryRule(MolgenisUser.EMAIL, Operator.EQUALS,
							"admin@molgenis.org"))).thenReturn(Collections.<MolgenisUser> singletonList(molgenisUser));
			return database;
		}

		@Bean
		public Database unauthorizedDatabase()
		{
			return mock(Database.class);
		}

		@Bean
		public CaptchaService captchaService()
		{
			return mock(CaptchaService.class);
		}
	}
}
