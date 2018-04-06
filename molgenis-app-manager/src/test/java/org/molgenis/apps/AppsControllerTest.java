package org.molgenis.apps;

import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.apps.model.App;
import org.molgenis.apps.model.AppMetaData;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.text.ParseException;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.apps.model.AppMetaData.APP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class AppsControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private DataService dataService;

	@Mock
	private UserPermissionEvaluator permissionService;

	private MockMvc mockMvc;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	public AppsControllerTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		AppsController appsController = new AppsController(dataService, permissionService);
		mockMvc = MockMvcBuilders.standaloneSetup(appsController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void AppsController()
	{
		new AppsController(null, null);
	}

	@DataProvider(name = "testInitProvider")
	public static Iterator<Object[]> testInitProvider() throws ParseException
	{
		return asList(new Object[] { true }, new Object[] { false }).iterator();
	}

	@Test(dataProvider = "testInitProvider")
	public void testInit(boolean hasWriteAppPermission) throws Exception
	{
		App app0 = mock(App.class);
		when(app0.getId()).thenReturn("id0");
		when(app0.getName()).thenReturn("name0");
		when(app0.getDescription()).thenReturn("description0");
		when(app0.isActive()).thenReturn(true);
		when(app0.getIconHref()).thenReturn("/icon0.png");
		App app1 = mock(App.class);
		when(app1.getId()).thenReturn("id1");
		when(app1.getName()).thenReturn("name1");
		when(app1.isActive()).thenReturn(false);

		@SuppressWarnings("unchecked")
		Query<App> query = mock(Query.class);
		when(dataService.query(APP, App.class)).thenReturn(query);
		Sort sort = mock(Sort.class);
		when(query.sort()).thenReturn(sort);
		when(query.findAll()).thenReturn(Stream.of(app0, app1));
		when(permissionService.hasPermission(new EntityTypeIdentity(APP), EntityTypePermission.WRITE)).thenReturn(
				hasWriteAppPermission);

		AppInfoDto appInfoDto0 = AppInfoDto.builder()
										   .setId("id0")
										   .setName("name0")
										   .setDescription("description0")
										   .setActive(true)
										   .setIconHref(new URI("/icon0.png"))
										   .build();
		AppInfoDto appInfoDto1 = AppInfoDto.builder().setId("id1").setName("name1").setActive(false).build();

		ResultActions resultActions = mockMvc.perform(get(AppsController.URI))
											 .andExpect(status().isOk())
											 .andExpect(view().name("view-apps"))
											 .andExpect(model().attribute("appEntityTypeId", APP));
		verify(sort).on(AppMetaData.NAME);
		if (hasWriteAppPermission)
		{
			resultActions.andExpect(model().attribute("apps", asList(appInfoDto0, appInfoDto1)));
		}
		else
		{
			resultActions.andExpect(model().attribute("apps", singletonList(appInfoDto0)));
		}
	}

	@Test
	public void testViewAppWithFreeMarkerTemplate() throws Exception
	{
		App app = mock(App.class);
		when(app.getId()).thenReturn("id");
		when(app.getUseFreemarkerTemplate()).thenReturn(true);
		when(app.getName()).thenReturn("name");
		when(app.isActive()).thenReturn(true);
		FreemarkerTemplate htmlTemplate = mock(FreemarkerTemplate.class);
		when(htmlTemplate.getNameWithoutExtension()).thenReturn("html");
		when(app.getHtmlTemplate()).thenReturn(htmlTemplate);
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		AppInfoDto expectedAppInfo = AppInfoDto.builder().setId("id").setName("name").setActive(true).build();
		mockMvc.perform(get(AppsController.URI + "/id"))
			   .andExpect(status().isOk())
			   .andExpect(view().name("html"))
			   .andExpect(model().attribute("app", expectedAppInfo));
	}

	@Test
	public void testViewAppWithIndexInAppResultsInRedirect() throws Exception
	{
		App app = mock(App.class);
		when(app.getUseFreemarkerTemplate()).thenReturn(false);
		when(app.getId()).thenReturn("id");
		when(app.getName()).thenReturn("name");
		when(app.isActive()).thenReturn(true);
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		mockMvc.perform(get(AppsController.URI + "/id")).andExpect(status().is3xxRedirection());
	}

	@Test
	public void testViewAppUnknownApp() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(true).getMock();
		mockMvc.perform(get(AppsController.URI + "/id"))
			   .andExpect(status().isBadRequest())
			   .andExpect(view().name("forward:/plugin/apps"))
			   .andExpect(model().attributeExists("errorMessage"));
	}

	@Test
	public void testViewAppDeactivatedApp() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(false).getMock();
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		mockMvc.perform(get(AppsController.URI + "/id"))
			   .andExpect(status().isBadRequest())
			   .andExpect(view().name("forward:/plugin/apps"))
			   .andExpect(model().attributeExists("errorMessage"));
	}

	@Test
	public void testActivateApp() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(false).getMock();
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		mockMvc.perform(post(AppsController.URI + "/id/activate")).andExpect(status().isOk());
		verify(app).setActive(true);
		verify(dataService).update(APP, app);
	}

	@Test
	public void testActivateAppUnknownApp() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(false).getMock();
		mockMvc.perform(post(AppsController.URI + "/id/activate"))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().string("{\"errors\":[{\"message\":\"Unknown app 'id'\"}]}"));
	}

	@Test
	public void testActivateAppAlreadyActivated() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(true).getMock();
		when(app.getName()).thenReturn("name");
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		mockMvc.perform(post(AppsController.URI + "/id/activate"))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().string("{\"errors\":[{\"message\":\"App 'name' already activated\"}]}"));
	}

	@Test
	public void testDeactivateApp() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(true).getMock();
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		mockMvc.perform(post(AppsController.URI + "/id/deactivate")).andExpect(status().isOk());
		verify(app).setActive(false);
		verify(dataService).update(APP, app);
	}

	@Test
	public void testDeactivateAppUnknownApp() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(true).getMock();
		mockMvc.perform(post(AppsController.URI + "/id/deactivate"))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().string("{\"errors\":[{\"message\":\"Unknown app 'id'\"}]}"));
	}

	@Test
	public void testDeActivateAppAlreadyDeactivated() throws Exception
	{
		App app = when(mock(App.class).isActive()).thenReturn(false).getMock();
		when(app.getName()).thenReturn("name");
		when(dataService.findOneById(APP, "id", App.class)).thenReturn(app);
		mockMvc.perform(post(AppsController.URI + "/id/deactivate"))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().string("{\"errors\":[{\"message\":\"App 'name' already deactivated\"}]}"));
	}
}