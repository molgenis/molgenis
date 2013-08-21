package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.controller.HomeControllerTest.Config;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class HomeControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private HomeController homeController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(homeController).setMessageConverters(new GsonHttpMessageConverter())
				.build();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void HomeController()
	{
		new HomeController(null);
	}

	@Test
	public void init() throws Exception
	{
		mockMvc.perform(get(HomeController.URI)).andExpect(status().isOk());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public HomeController homeController()
		{
			return new HomeController(molgenisSettings());
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}
	}
}
