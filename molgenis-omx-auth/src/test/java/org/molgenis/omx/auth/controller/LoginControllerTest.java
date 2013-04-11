package org.molgenis.omx.auth.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.controller.LoginControllerTest.LoginControllerConfig;
import org.molgenis.util.HandleRequestDelegationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = LoginControllerConfig.class)
public class LoginControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private LoginController loginController;
	@Autowired
	private Database database;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");
		mockMvc = MockMvcBuilders.standaloneSetup(loginController).setMessageConverters(new FormHttpMessageConverter())
				.build();
		Login login = mock(Login.class);
		when(login.login(database, "admin", "adminpw")).thenReturn(true);
		when(database.getLogin()).thenReturn(login);
	}

	@Test
	public void doLogin() throws Exception
	{
		this.mockMvc.perform(
				post("/login").param("username", "admin").param("password", "adminpw")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
	}

	@Test
	public void doLogin_unauthorized() throws Exception
	{
		this.mockMvc.perform(
				post("/login").param("username", "admin").param("password", "adminpw-invalid")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isUnauthorized());
	}

	@Test
	public void doLogout() throws Exception
	{
		this.mockMvc.perform(post("/logout")).andExpect(status().isNoContent());
	}

	@Configuration
	public static class LoginControllerConfig extends WebMvcConfigurerAdapter
	{
		@Bean
		public LoginController loginController()
		{
			return new LoginController();
		}

		@Bean
		public Database database()
		{
			return mock(Database.class);
		}
	}
}
