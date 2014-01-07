package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.controller.CbmToOmxConverterControllerTest.Config;
import org.molgenis.util.FileStore;
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
public class CbmToOmxConverterControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private CbmToOmxConverterController cbmToOmxConverterController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(cbmToOmxConverterController).setMessageConverters(new GsonHttpMessageConverter())
				.build();
	}

	@Test
	public void init() throws Exception
	{
		mockMvc.perform(get(CbmToOmxConverterController.URI)).andExpect(status().isOk());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public CbmToOmxConverterController cbmToOmxConverterController()
		{
			return new CbmToOmxConverterController();
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}
		
		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
		}
	}
}
