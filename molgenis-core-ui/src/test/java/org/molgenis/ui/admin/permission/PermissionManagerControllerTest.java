package org.molgenis.ui.admin.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.security.permission.PermissionManagerService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.admin.permission.PermissionManagerControllerTest.Config;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

//TODO add additional test
@WebAppConfiguration
@ContextConfiguration(classes =
{ Config.class, GsonConfig.class })
public class PermissionManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public PermissionManagerController permissionManagerController()
		{
			return new PermissionManagerController(permissionManagerService());
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return new MolgenisPluginRegistryImpl();
		}

		@Bean
		public PermissionManagerService permissionManagerService()
		{
			return mock(PermissionManagerService.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}

	@Autowired
	private PermissionManagerController permissionManagerController;

	@Autowired
	private PermissionManagerService permissionManagerService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private MockMvc mockMvc;
	private MolgenisUser user1, user2;
	private MolgenisGroup group1, group2;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(permissionManagerController)
				.setMessageConverters(gsonHttpMessageConverter).build();

		reset(permissionManagerService);
		user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
		when(user1.isSuperuser()).thenReturn(true);
		user2 = when(mock(MolgenisUser.class).getId()).thenReturn("2").getMock();
		when(user2.isSuperuser()).thenReturn(false);
		when(permissionManagerService.getUsers()).thenReturn(Arrays.<MolgenisUser> asList(user1, user2));
		when(permissionManagerService.getGroups()).thenReturn(Arrays.<MolgenisGroup> asList(group1, group2));

	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void PermissionManagerController()
	{
		new PermissionManagerController(null);
	}

	@Test
	public void init() throws Exception
	{
		this.mockMvc.perform(get(MolgenisPluginController.PLUGIN_URI_PREFIX + "/permissionmanager"))
				.andExpect(status().isOk()).andExpect(view().name("view-permissionmanager"))
				.andExpect(model().attribute("users", Arrays.asList(user2)))
				.andExpect(model().attribute("groups", Arrays.asList(group1, group2)));
	}
}
