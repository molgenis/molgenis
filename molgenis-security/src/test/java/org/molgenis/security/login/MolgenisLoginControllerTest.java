package org.molgenis.security.login;

import org.molgenis.security.login.MolgenisLoginControllerTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { Config.class })
public class MolgenisLoginControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MolgenisLoginController molgenisLoginController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(molgenisLoginController).build();
	}

	@Test
	public void getLoginPage() throws Exception
	{
		this.mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("view-login"));
	}

	@Test
	public void getLoginErrorPageSessionExpired() throws Exception
	{
		this.mockMvc.perform(get("/login").param("expired", ""))
					.andExpect(status().isOk())
					.andExpect(view().name("view-login"))
					.andExpect(model().attribute("errorMessage", "Your login session has expired."));
	}

	@Test
	public void getLoginErrorPage() throws Exception
	{
		this.mockMvc.perform(get("/login").param("error", ""))
					.andExpect(status().isOk())
					.andExpect(view().name("view-login"))
					.andExpect(model().attributeExists("errorMessage"));
	}

	@Configuration
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public MolgenisLoginController molgenisLoginController()
		{
			return new MolgenisLoginController();
		}
	}
}
