package org.molgenis.ui.form;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;
import org.molgenis.util.HandleRequestDelegationException;
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
@ContextConfiguration
public class MolgenisEntityFormPluginControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MolgenisEntityFormPluginController molgenisEntityFormPluginController;

	@Autowired
	private Database database;

	@Autowired
	private MolgenisPermissionService permissionService;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		mockMvc = MockMvcBuilders.standaloneSetup(molgenisEntityFormPluginController).build();
	}

	@Test
	public void listUnknownEntity() throws Exception
	{
		Model model = mock(Model.class);
		when(model.getEntity("Test")).thenReturn(null);
		when(database.getMetaData()).thenReturn(model);

		mockMvc.perform(get(MolgenisEntityFormPluginController.URI + ".Test")).andExpect(status().is(404));
	}

	@Test
	public void listUnknownUnauthorized() throws Exception
	{
		Model model = mock(Model.class);
		Entity entity = mock(Entity.class);
		when(model.getEntity("Test")).thenReturn(entity);
		when(database.getMetaData()).thenReturn(model);
		when(permissionService.hasPermissionOnEntity("Test", Permission.READ)).thenReturn(false);

		mockMvc.perform(get(MolgenisEntityFormPluginController.URI + ".Test")).andExpect(status().is(401));
	}

	@Test
	public void list() throws Exception
	{
		Model model = mock(Model.class);
		Entity entity = mock(Entity.class);
		when(model.getEntity("Test")).thenReturn(entity);
		when(database.getMetaData()).thenReturn(model);
		when(permissionService.hasPermissionOnEntity("Test", Permission.READ)).thenReturn(true);

		mockMvc.perform(get(MolgenisEntityFormPluginController.URI + ".Test")).andExpect(status().is(200))
				.andExpect(model().attributeExists("form"));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public MolgenisPermissionService permissionService()
		{
			return mock(MolgenisPermissionService.class);
		}

		@Bean
		public MolgenisEntityFormPluginController molgenisEntityFormPluginController()
		{
			return new MolgenisEntityFormPluginController();
		}
	}
}
