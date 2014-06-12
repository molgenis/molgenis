package org.molgenis.col7a1;

import org.molgenis.omx.controller.BackgroundController;
import org.molgenis.omx.controller.ContactController;
import org.molgenis.omx.controller.ContentControllersTest.Config;
import org.molgenis.omx.controller.HomeController;
import org.molgenis.omx.controller.NewsController;
import org.molgenis.omx.controller.ReferencesController;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class PatientsViewControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MysqlViewService mysqlViewService;

	@Autowired
	private PatientsViewController patientsViewController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeMethod()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(patientsViewController)
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


	// @Test
	// public void init() throws Exception
	// {
	// this.initEditGetMethodTest(mockMvc, PatientsViewController.URI, PatientsViewController.ID);
	// }

	// @Test
	// public void initEditGetReferences() throws Exception
	// {
	// this.initEditGetMethodTest(mockMvcReferences, ReferencesController.URI, ReferencesController.ID);
	// }

	// public void initMethodTest(MockMvc mockMvc, String uri, String uniqueReference) throws Exception
	// {
	// when(this.staticContentService.getContent(any(String.class))).thenReturn("staticcontent");
	// mockMvc.perform(MockMvcRequestBuilders.get(uri)).andExpect(MockMvcResultMatchers.status().isOk())
	// .andExpect(view().name("view-staticcontent")).andExpect(model().attributeExists("content"))
	// .andExpect(model().attributeExists("isCurrentUserCanEdit"));
	// }
	//
	// public void initEditGetMethodTest(MockMvc mockMvc, String uri, String uniqueReference) throws Exception
	// {
	// when(this.staticContentService.getContent(any(String.class))).thenReturn("staticcontent");
	// when(this.staticContentService.isCurrentUserCanEdit()).thenReturn(true);
	//
	// mockMvc.perform(MockMvcRequestBuilders.get(uri + "/edit")).andExpect(MockMvcResultMatchers.status().isOk())
	// .andExpect(view().name("view-staticcontent-edit")).andExpect(model().attributeExists("content"));
	// }
	//
	// @Test
	// public void initNotExistingURI() throws Exception
	// {
	// mockMvcHome.perform(MockMvcRequestBuilders.get(HomeController.URI + "/NotExistingURI")).andExpect(
	// MockMvcResultMatchers.status().isNotFound());
	// }
	//
	// @Configuration
	// public static class Config
	// {
	// @Bean
	// public HomeController homeController()
	// {
	// return new HomeController();
	// }
	//
	// @Bean
	// public NewsController newsController()
	// {
	// return new NewsController();
	// }
	//
	// @Bean
	// public BackgroundController backgroundController()
	// {
	// return new BackgroundController();
	// }
	//
	// @Bean
	// public ContactController ContactController()
	// {
	// return new ContactController();
	// }
	//
	// @Bean
	// public ReferencesController referencesController()
	// {
	// return new ReferencesController();
	// }
	//
	// @Bean
	// public StaticContentService staticContentService()
	// {
	// return mock(StaticContentService.class);
	// }
	//
	// @Bean
	// public FileStore fileStore()
	// {
	// return mock(FileStore.class);
	// }
	//
	// @Bean
	// public MolgenisSettings molgenisSettings()
	// {
	// return mock(MolgenisSettings.class);
	// }
	// }
}
