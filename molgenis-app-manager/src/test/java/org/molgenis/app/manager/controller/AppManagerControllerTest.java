package org.molgenis.app.manager.controller;

import com.google.gson.Gson;
import org.mockito.Mock;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.util.MolgenisGsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Configuration
@EnableWebMvc
public class AppManagerControllerTest
{
	private MockMvc mockMvc;

	@Mock
	private AppManagerService appManagerService;

	private AppResponse appResponse;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

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

		AppManagerController controller = new AppManagerController(appManagerService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
								 .setMessageConverters(new MolgenisGsonHttpMessageConverter(new Gson()))
								 .build();
	}

	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get(AppManagerController.URI))
			   .andExpect(status().is(200))
			   .andExpect(view().name("view-app-manager"));
	}

	@Test
	public void testGetApps() throws Exception
	{
		when(appManagerService.getApps()).thenReturn(newArrayList(appResponse));
		mockMvc.perform(get(AppManagerController.URI + "/apps"))
			   .andExpect(status().is(200))
			   .andExpect(content().string(
					   "[{\"id\":\"id\",\"uri\":\"uri\",\"label\":\"label\",\"description\":\"description\",\"isActive\":true,\"includeMenuAndFooter\":true,\"templateContent\":\"\\u003ch1\\u003eTest\\u003c/h1\\u003e\",\"version\":\"v1.0.0\",\"appConfig\":\"{\\u0027config\\u0027: \\u0027test\\u0027}\"}]"));
	}

	@Test
	public void testActivateApp() throws Exception
	{
		mockMvc.perform(get(AppManagerController.URI + "/activate/id")).andExpect(status().is(200));
		verify(appManagerService).activateApp("id");
	}

	@Test
	public void testDeactivateApp() throws Exception
	{
		mockMvc.perform(get(AppManagerController.URI + "/deactivate/id")).andExpect(status().is(200));
		verify(appManagerService).deactivateApp("id");
	}

	@Test
	public void testDeleteApp() throws Exception
	{
		mockMvc.perform(get(AppManagerController.URI + "/delete/id")).andExpect(status().is(200));
		verify(appManagerService).deleteApp("id");
	}

	@Test
	public void testUploadApp() throws Exception
	{
		String testFile = getClass().getClassLoader().getResource("app-example.zip").getFile();
		mockMvc.perform(multipart(AppManagerController.URI + "/upload").file("file", testFile.getBytes()))
			   .andExpect(status().is(200));
	}
}
