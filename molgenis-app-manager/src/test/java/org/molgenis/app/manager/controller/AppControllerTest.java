package org.molgenis.app.manager.controller;

import org.mockito.Mock;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static java.util.Locale.ENGLISH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;

@Configuration
@EnableWebMvc
public class AppControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private AppManagerService appManagerService;

	@Mock
	private AppSettings appSettings;

	@Mock
	private LocaleResolver localeResolver;

	@Mock
	private MenuReaderService menuReaderService;

	private AppResponse appResponse;

	@BeforeClass
	public void beforeClass()
	{
		TestAllPropertiesMessageSource messageSource = new TestAllPropertiesMessageSource(new MessageFormatFactory());
		messageSource.addMolgenisNamespaces("app-manager");
		MessageSourceHolder.setMessageSource(messageSource);
	}

	@AfterClass
	public void afterClass()
	{
		MessageSourceHolder.setMessageSource(null);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(AppController.ID + "/uri/")).thenReturn("/test/path");
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
		ClassLoader classLoader = getClass().getClassLoader();
		File testFile = new File(classLoader.getResource("test-resources/js/test.js").getFile());
		String absoluteTestFileResourcePath = testFile.getPath().replace("js/test.js", "");;
		when(app.getResourceFolder()).thenReturn(absoluteTestFileResourcePath);

		appResponse = AppResponse.create(app);
		when(appManagerService.getAppByUri("uri")).thenReturn(appResponse);

		AppController controller = new AppController(appManagerService, appSettings, menuReaderService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
								 .setControllerAdvice(new GlobalControllerExceptionHandler(),
										 new FallbackExceptionHandler(), new SpringExceptionHandler())
								 .setLocaleResolver(localeResolver)
								 .build();
	}

	@Test
	public void testServeApp() throws Exception
	{
		mockMvc.perform(get(AppController.URI + "/uri/"))
			   .andExpect(status().isOk())
			   .andExpect(model().attribute("app", appResponse))
			   .andExpect(model().attribute("baseUrl", "/test/path"))
			   .andExpect(view().name("view-app"));
	}

	@Test
	public void testServeAppRedirectToApp() throws Exception
	{
		mockMvc.perform(get(AppController.URI + "/uri"))
			   .andExpect(status().is3xxRedirection());
	}

	@Test
	public void testServeAppInactiveApp() throws Exception
	{
		App app = mock(App.class);
		when(app.getId()).thenReturn("id");
		when(app.getUri()).thenReturn("uri");
		when(app.getLabel()).thenReturn("label");
		when(app.getDescription()).thenReturn("description");
		when(app.getAppVersion()).thenReturn("v1.0.0");
		when(app.includeMenuAndFooter()).thenReturn(true);
		when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
		when(app.getAppConfig()).thenReturn("{'config': 'test'}");
		when(app.getResourceFolder()).thenReturn("foo/bar");

		when(app.isActive()).thenReturn(false);

		AppResponse appResponse = AppResponse.create(app);
		when(appManagerService.getAppByUri("uri")).thenReturn(appResponse);
		String expectedMessage = "";
		try
		{
			mockMvc.perform(get(AppController.URI + "/uri/")).andExpect(status().is4xxClientError());
		}
		catch (Exception e)
		{
			expectedMessage = e.getCause().getMessage();
		}
		finally
		{
			assertEquals(expectedMessage, "uri:uri");
		}
	}

	@Test
	public void testServeResource() throws Exception
	{
		mockMvc.perform(get(AppController.URI + "/uri/js/test.js"))
			   .andExpect(status().isOk())
			   .andReturn()
			   .getResponse();

	}
}
