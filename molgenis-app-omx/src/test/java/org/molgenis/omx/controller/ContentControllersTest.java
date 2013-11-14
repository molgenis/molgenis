package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.controller.ContentControllersTest.Config;
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
public class ContentControllersTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private HomeController homeController;
	
	@Autowired
	private NewsController newsController;
	
	@Autowired
	private BackgroundController backgroundController;
	
	@Autowired
	private ContactController contactController;
	
	@Autowired
	private ReferencesController referencesController;

	@Autowired
	private StaticContentService staticContentService;

	@Autowired
	private Database unsecuredDatabase;
	
	@Autowired
	private MolgenisSettings molgenisSettings;
	
	private MockMvc mockMvcHome;
	private MockMvc mockMvcNews;
	private MockMvc mockMvcContact;
	private MockMvc mockMvcReferences;
	private MockMvc mockMvcBackground;

	@BeforeMethod
	public void setUp()
	{
		mockMvcHome = MockMvcBuilders.standaloneSetup(homeController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
		
		mockMvcNews = MockMvcBuilders.standaloneSetup(newsController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
		
		mockMvcContact = MockMvcBuilders.standaloneSetup(contactController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
		
		mockMvcBackground = MockMvcBuilders.standaloneSetup(backgroundController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
		
		mockMvcReferences = MockMvcBuilders.standaloneSetup(referencesController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
	}

	@Test
	public void getHomeController()
	{
		new HomeController();
	}
	
	@Test
	public void getNewsController()
	{
		new NewsController();
	}
	
	@Test
	public void getReferencesController()
	{
		new ReferencesController();
	}
	
	@Test
	public void getBackgroundController()
	{
		new BackgroundController();
	}
	
	@Test
	public void getContactController()
	{
		new ContactController();
	}
	
	@Test
	public void pageGetRequestHome() throws Exception {
		this.pageGetRequest(mockMvcHome, HomeController.URI, HomeController.ID);
	}
	
	@Test
	public void pageGetRequestNews() throws Exception {
		this.pageGetRequest(mockMvcNews, NewsController.URI, NewsController.ID);
	}
	
	@Test
	public void pageGetRequestBackground() throws Exception {
		this.pageGetRequest(mockMvcBackground, BackgroundController.URI, BackgroundController.ID);
	}
	
	@Test
	public void pageGetRequestContact() throws Exception {
		this.pageGetRequest(mockMvcContact, ContactController.URI, ContactController.ID);
	}
	
	@Test
	public void pageGetRequestReferences() throws Exception {
		this.pageGetRequest(mockMvcReferences, ReferencesController.URI, ReferencesController.ID);
	}

	public void pageGetRequest(MockMvc mockMvc, String uri, String uniqueReference) throws Exception
	{	
		RuntimeProperty runtimeProperty = mock(RuntimeProperty.class);
		when(runtimeProperty.getValue()).thenReturn("<p>content</p>");
		
		when(unsecuredDatabase.find(RuntimeProperty.class, new QueryRule(RuntimeProperty.IDENTIFIER,
						Operator.EQUALS, RuntimeProperty.class.getSimpleName() + "_app." + uniqueReference))).thenReturn(
				Arrays.asList(runtimeProperty));
		
		when(molgenisSettings.getProperty(StaticContentServiceImpl.PREFIX_KEY + uniqueReference , StaticContentServiceImpl.DEFAULT_CONTENT))
			.thenReturn("<p>content</p>");
		
		mockMvc.perform(MockMvcRequestBuilders.get(uri)).andExpect(
				MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void initNotExistingURI() throws Exception
	{
		mockMvcHome.perform(MockMvcRequestBuilders.get(org.molgenis.omx.controller.HomeController.URI + "/NotExistingURI")).andExpect(
				MockMvcResultMatchers.status().isNotFound());
	}
	

	@Configuration
	public static class Config
	{
		@Bean
		public HomeController homeController()
		{
			return new HomeController();
		}
		
		@Bean
		public NewsController newsController()
		{
			return new NewsController();
		}
		
		@Bean
		public BackgroundController backgroundController()
		{
			return new BackgroundController();
		}
		
		@Bean
		public ContactController ContactController()
		{
			return new ContactController();
		}
		
		@Bean
		public ReferencesController referencesController()
		{
			return new ReferencesController();
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
		
		@Bean
		public StaticContentService staticContentService(){
			return mock(StaticContentService.class);
		}
	}
}
