package org.molgenis.omx.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.controller.BackgroundController;
import org.molgenis.omx.controller.ContactController;
import org.molgenis.omx.controller.HomeController;
import org.molgenis.omx.controller.NewsController;
import org.molgenis.omx.controller.ReferencesController;
import org.molgenis.omx.controller.ContentControllersTest.Config;
import org.molgenis.ui.controller.StaticContentService;
import org.molgenis.util.FileStore;
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

	private MockMvc mockMvcHome;
	private MockMvc mockMvcNews;
	private MockMvc mockMvcContact;
	private MockMvc mockMvcReferences;
	private MockMvc mockMvcBackground;

	@BeforeMethod
	public void beforeMethod()
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
	public void initHome() throws Exception
	{
		this.initMethodTest(mockMvcHome, HomeController.URI, HomeController.ID);
	}

	@Test
	public void initNews() throws Exception
	{
		this.initMethodTest(mockMvcNews, NewsController.URI, NewsController.ID);
	}

	@Test
	public void initBackground() throws Exception
	{
		this.initMethodTest(mockMvcBackground, BackgroundController.URI, BackgroundController.ID);
	}

	@Test
	public void initContact() throws Exception
	{
		this.initMethodTest(mockMvcContact, ContactController.URI, ContactController.ID);
	}

	@Test
	public void initReferences() throws Exception
	{
		this.initMethodTest(mockMvcReferences, ReferencesController.URI, ReferencesController.ID);
	}

	@Test
	public void initEditGetHome() throws Exception
	{
		this.initEditGetMethodTest(mockMvcHome, HomeController.URI, HomeController.ID);
	}

	@Test
	public void initEditGetNews() throws Exception
	{
		this.initEditGetMethodTest(mockMvcNews, NewsController.URI, NewsController.ID);
	}

	@Test
	public void initEditGetBackground() throws Exception
	{
		this.initEditGetMethodTest(mockMvcBackground, BackgroundController.URI, BackgroundController.ID);
	}

	@Test
	public void initEditGetContact() throws Exception
	{
		this.initEditGetMethodTest(mockMvcContact, ContactController.URI, ContactController.ID);
	}

	@Test
	public void initEditGetReferences() throws Exception
	{
		this.initEditGetMethodTest(mockMvcReferences, ReferencesController.URI, ReferencesController.ID);
	}

	public void initMethodTest(MockMvc mockMvc, String uri, String uniqueReference) throws Exception
	{
		when(this.staticContentService.getContent(any(String.class))).thenReturn("staticcontent");
		mockMvc.perform(MockMvcRequestBuilders.get(uri)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(view().name("view-staticcontent")).andExpect(model().attributeExists("content"))
				.andExpect(model().attributeExists("isCurrentUserCanEdit"));
	}

	public void initEditGetMethodTest(MockMvc mockMvc, String uri, String uniqueReference) throws Exception
	{
		when(this.staticContentService.getContent(any(String.class))).thenReturn("staticcontent");
		when(this.staticContentService.isCurrentUserCanEdit()).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get(uri + "/edit")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(view().name("view-staticcontent-edit")).andExpect(model().attributeExists("content"));
	}

	@Test
	public void initNotExistingURI() throws Exception
	{
		mockMvcHome.perform(MockMvcRequestBuilders.get(HomeController.URI + "/NotExistingURI")).andExpect(
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
		public StaticContentService staticContentService()
		{
			return mock(StaticContentService.class);
		}

		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}
	}
}
