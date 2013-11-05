package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.controller.ContentControllerTest.Config;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class ContentControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private ContentController contentController;

	@Autowired
	private Database unsecuredDatabase;
	
	@Autowired
	private MolgenisSettings molgenisSettings;
	
	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(contentController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getContentController()
	{
		new ContentController(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getContentControllerWithURL()
	{
		new ContentController(null, null);
	}

	@Test
	public void initHome() throws Exception
	{	
		this.succes("home");
	}
	
	@Test
	public void initReferences() throws Exception
	{
		this.succes("references");
	}
	
	@Test
	public void initNews() throws Exception
	{
		this.succes("news");
	}
	
	@Test
	public void initBackground() throws Exception
	{
		this.succes("background");
	}
	
	@Test
	public void initContact() throws Exception
	{
		this.succes("contact");
	}
	
	@Test
	public void initNotExistingURI() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(ContentController.URI + "/NotExistingURI")).andExpect(
				MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void initNotFound() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(ContentController.URI)).andExpect(
				MockMvcResultMatchers.status().isNotFound());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public ContentController contentController()
		{
			return new ContentController(molgenisSettings());
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}
		
		@Bean
		public Database unsecuredDatabase(){
			return mock(Database.class);
		}
	}
	
	private void succes(String uniqueReference) throws Exception{
		RuntimeProperty runtimeProperty = mock(RuntimeProperty.class);
		when(runtimeProperty.getValue()).thenReturn("<p>content</p>");
		
		when(unsecuredDatabase.find(RuntimeProperty.class, new QueryRule(RuntimeProperty.IDENTIFIER,
						Operator.EQUALS, RuntimeProperty.class.getSimpleName() + "_app." + uniqueReference))).thenReturn(
				Arrays.asList(runtimeProperty));
		
		when(molgenisSettings.getProperty(ContentController.PREFIX_KEY + uniqueReference , ContentController.DEFAULT_CONTENT))
			.thenReturn("<p>content</p>");
		
		mockMvc.perform(MockMvcRequestBuilders.get(ContentController.URI + "/" + uniqueReference)).andExpect(
				MockMvcResultMatchers.status().isOk());
	}
}
