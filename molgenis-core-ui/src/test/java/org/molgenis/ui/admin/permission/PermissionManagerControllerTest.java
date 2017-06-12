package org.molgenis.ui.admin.permission;

import org.molgenis.auth.GroupAuthorityFactory;
import org.molgenis.auth.Group;
import org.molgenis.auth.User;
import org.molgenis.auth.UserAuthorityFactory;
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

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//TODO add additional test
@WebAppConfiguration
@ContextConfiguration(classes = { Config.class, GsonConfig.class })
public class PermissionManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public UserAuthorityFactory userAuthorityFactory()
		{
			return mock(UserAuthorityFactory.class);
		}

		@Bean
		public GroupAuthorityFactory groupAuthorityFactory()
		{
			return mock(GroupAuthorityFactory.class);
		}

		@Bean
		public PermissionManagerController permissionManagerController()
		{
			return new PermissionManagerController(permissionManagerService(), userAuthorityFactory(),
					groupAuthorityFactory());
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
	private User user1, user2;
	private Group group1, group2;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(permissionManagerController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();

		reset(permissionManagerService);
		user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
		when(user1.isSuperuser()).thenReturn(true);
		user2 = when(mock(User.class).getId()).thenReturn("2").getMock();
		when(user2.isSuperuser()).thenReturn(false);
		when(permissionManagerService.getUsers()).thenReturn(Arrays.<User>asList(user1, user2));
		when(permissionManagerService.getGroups()).thenReturn(Arrays.<Group>asList(group1, group2));

	}

	@Test(expectedExceptions = NullPointerException.class)
	public void PermissionManagerController()
	{
		new PermissionManagerController(null, null, null);
	}

	@Test
	public void init() throws Exception
	{
		this.mockMvc.perform(get(MolgenisPluginController.PLUGIN_URI_PREFIX + "/permissionmanager"))
					.andExpect(status().isOk())
					.andExpect(view().name("view-permissionmanager"))
					.andExpect(model().attribute("users", Arrays.asList(user2)))
					.andExpect(model().attribute("groups", Arrays.asList(group1, group2)));
	}
}
