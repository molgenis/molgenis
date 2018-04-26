package org.molgenis.app.manager.service;

import com.google.gson.Gson;
import org.mockito.Mock;
import org.molgenis.app.manager.exception.AppForIDDoesNotExistException;
import org.molgenis.app.manager.exception.AppForURIDoesNotExistException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppFactory;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.impl.AppManagerServiceImpl;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class AppManagerServiceTest
{
	private static final String APP_META_NAME = "sys_App";
	private static final String APP_META_URI = "uri";
	private static final String PLUGIN_META_NAME = "sys_Plugin";

	private AppManagerService appManagerService;

	@Mock
	private AppFactory appFactory;

	@Mock
	private DataService dataService;

	@Mock
	private FileStore fileStore;

	@Mock
	private MenuReaderService menuReaderService;

	@Mock
	private PluginFactory pluginFactory;

	private Gson gson;

	private App app;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

		app = mock(App.class);
		when(app.getId()).thenReturn("id");
		when(app.getUri()).thenReturn("uri");
		when(app.getLabel()).thenReturn("label");
		when(app.getDescription()).thenReturn("description");
		when(app.isActive()).thenReturn(true);
		when(app.getAppVersion()).thenReturn("v1.0.0");
		when(app.includeMenuAndFooter()).thenReturn(true);
		when(app.getTemplateContent()).thenReturn("<h1>Test</h1>");
		when(app.getAppConfig()).thenReturn("{'config': 'test'}");
		when(app.getResourceFolder()).thenReturn("folder");

		gson = new Gson();
		appManagerService = new AppManagerServiceImpl(appFactory, dataService, fileStore, gson, menuReaderService,
				pluginFactory);
	}

	@Test
	public void testGetApps()
	{
		AppResponse appResponse = AppResponse.create(app);

		when(dataService.findAll(APP_META_NAME, App.class)).thenReturn(newArrayList(app).stream());
		List<AppResponse> actual = appManagerService.getApps();
		List<AppResponse> expected = newArrayList(appResponse);

		assertEquals(actual, expected);
	}

	@Test
	public void testGetAppByUri()
	{
		Query<App> query = QueryImpl.EQ(APP_META_URI, "test");
		when(dataService.findOne(APP_META_NAME, query, App.class)).thenReturn(app);
		AppResponse actual = appManagerService.getAppByUri("test");
		AppResponse expected = AppResponse.create(app);

		assertEquals(actual, expected);
	}

	@Test
	public void testActivateApp()
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(app);
		app.setActive(true);

		Plugin plugin = mock(Plugin.class);
		when(pluginFactory.create("app/uri/")).thenReturn(plugin);
		plugin.setLabel("label");
		plugin.setDescription("description");

		appManagerService.activateApp("test");
		verify(dataService).update(APP_META_NAME, app);
		verify(dataService).add("sys_Plugin", plugin);
	}

	@Test
	public void testDeactivateApp()
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(app);
		app.setActive(false);

		Menu menu = mock(Menu.class);
		when(menuReaderService.getMenu()).thenReturn(menu);

		appManagerService.deactivateApp("test");
		verify(dataService).update(APP_META_NAME, app);
		verify(dataService).deleteById(PLUGIN_META_NAME, "app/uri/");
		verify(menu).deleteMenuItem("app/uri/");
	}

	@Test
	public void testDeleteApp() throws IOException
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(app);

		appManagerService.deleteApp("test");
		verify(dataService).deleteById(APP_META_NAME, "test");
	}

	@Test
	public void testAppUriDoesNotExist()
	{
		Query<App> query = QueryImpl.EQ(APP_META_URI, "test");
		when(dataService.findOne(APP_META_NAME, query, App.class)).thenReturn(null);
		try
		{
			appManagerService.getAppByUri("test");
			fail();
		}
		catch (AppForURIDoesNotExistException actual)
		{
			assertEquals(actual.getUri(), "test");
		}
	}

	@Test
	public void testAppIdDoesNotExist()
	{
		when(dataService.findOneById(APP_META_NAME, "test", App.class)).thenReturn(null);
		try
		{
			appManagerService.activateApp("test");
			fail();
		}
		catch (AppForIDDoesNotExistException actual)
		{
			assertEquals(actual.getId(), "test");
		}
	}
}
