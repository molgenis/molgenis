package org.molgenis.security.login;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.login.MolgenisLoginControllerTest.Config;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.ResourceFingerprintRegistry;
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

@WebAppConfiguration
@ContextConfiguration(classes =
{ Config.class, GsonConfig.class })
public class MolgenisLoginControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MolgenisLoginController molgenisLoginController;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(molgenisLoginController)
				.setMessageConverters(gsonHttpMessageConverter).build();
	}

	@Test
	public void getLoginPage() throws Exception
	{
		this.mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("view-login"));
	}

	@Test
	public void getLoginErrorPage() throws Exception
	{
		this.mockMvc.perform(get("/login").param("error", "")).andExpect(status().isOk())
				.andExpect(view().name("view-login")).andExpect(model().attributeExists("errorMessage"));
	}

	@Configuration
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public MolgenisLoginController molgenisLoginController()
		{
			return new MolgenisLoginController(resourceFingerprintRegistry(), appSettings());
		}

		@Bean
		public ResourceFingerprintRegistry resourceFingerprintRegistry()
		{
			return mock(ResourceFingerprintRegistry.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}
	}
}
