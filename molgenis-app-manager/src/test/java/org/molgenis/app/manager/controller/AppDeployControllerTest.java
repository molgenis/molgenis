package org.molgenis.app.manager.controller;

import org.mockito.Mock;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.settings.AppSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Locale.ENGLISH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Configuration
@EnableWebMvc
public class AppDeployControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private AppDeployService appDeployService;

	@Mock
	private AppManagerService appManagerService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private LocaleResolver localeResolver;

	@Mock
	private MenuReaderService menuReaderService;

	private AppResponse appResponse;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(AppDeployController.ID + "/uri/")).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(appSettings.getLanguageCode()).thenReturn("en");
		when(localeResolver.resolveLocale(any())).thenReturn(ENGLISH);

		App app = mock(App.class);
		when(app.getId()).thenReturn("id");
		when(app.getUri()).thenReturn("uri");
		when(app.getLabel()).thenReturn("label");
		when(app.getDescription()).thenReturn("description");
		when(app.isActive()).thenReturn(true);
		when(app.getAppVersion()).thenReturn("v1.0.0");
		when(app.includeMenuAndFooter()).thenReturn(true);
		when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
		when(app.getAppConfig()).thenReturn("{'config': 'test'}");
		appResponse = AppResponse.create(app);
		when(appManagerService.getAppByUri("uri")).thenReturn(appResponse);

		AppDeployController controller = new AppDeployController(appDeployService, appManagerService, appSettings,
				menuReaderService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).setLocaleResolver(localeResolver).build();
	}

	@Test
	public void testDeployApp() throws Exception
	{
		mockMvc.perform(get(AppDeployController.URI + "/uri/"))
			   .andExpect(status().isOk())
			   .andExpect(model().attribute("app", appResponse))
			   .andExpect(model().attribute("baseUrl", "/test/path"))
			   .andExpect(view().name("view-app"));
	}

	@Test
	public void testLoadJavascriptResources() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get(AppDeployController.URI + "/uri/js/test.js"))
									 .andExpect(status().isOk())
									 .andReturn();
		verify(appDeployService).loadJavascriptResources("uri", "test.js", mvcResult.getResponse());
	}

	@Test
	public void testLoadCSSResources() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get(AppDeployController.URI + "/uri/css/test.css"))
									 .andExpect(status().isOk())
									 .andReturn();
		verify(appDeployService).loadCSSResources("uri", "test.css", mvcResult.getResponse());
	}

	@Test
	public void testLoadImageResources() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get(AppDeployController.URI + "/uri/img/test.png"))
									 .andExpect(status().isOk())
									 .andReturn();
		verify(appDeployService).loadImageResources("uri", "test.png", mvcResult.getResponse());
	}
}
