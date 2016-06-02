package org.molgenis.ui.admin.permission;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//import org.molgenis.ui.admin.permission.PermissionManagerControllerTest.Config;

////TODO add additional test
//@WebAppConfiguration
//@ContextConfiguration(classes =
//{ Config.class, GsonConfig.class })
public class PermissionManagerControllerTest extends AbstractTestNGSpringContextTests
{
	//	@Configuration
	//	public static class Config extends WebMvcConfigurerAdapter
	//	{
	//		@Bean
	//		public PermissionManagerController permissionManagerController()
	//		{
	//			return new PermissionManagerController(permissionManagerService(), , );
	//		}
	//
	//		@Bean
	//		public MolgenisPluginRegistry molgenisPluginRegistry()
	//		{
	//			return new MolgenisPluginRegistryImpl();
	//		}
	//
	//		@Bean
	//		public PermissionManagerService permissionManagerService()
	//		{
	//			return mock(PermissionManagerService.class);
	//		}
	//
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//	}
	//
	//	@Autowired
	//	private PermissionManagerController permissionManagerController;
	//
	//	@Autowired
	//	private PermissionManagerService permissionManagerService;
	//
	//	@Autowired
	//	private GsonHttpMessageConverter gsonHttpMessageConverter;
	//
	//	private MockMvc mockMvc;
	//	private MolgenisUser user1, user2;
	//	private MolgenisGroup group1, group2;
	//
	//	@BeforeMethod
	//	public void setUp()
	//	{
	//		mockMvc = MockMvcBuilders.standaloneSetup(permissionManagerController)
	//				.setMessageConverters(gsonHttpMessageConverter).build();
	//
	//		reset(permissionManagerService);
	//		user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
	//		when(user1.isSuperuser()).thenReturn(true);
	//		user2 = when(mock(MolgenisUser.class).getId()).thenReturn("2").getMock();
	//		when(user2.isSuperuser()).thenReturn(false);
	//		when(permissionManagerService.getUsers()).thenReturn(Arrays.<MolgenisUser> asList(user1, user2));
	//		when(permissionManagerService.getGroups()).thenReturn(Arrays.<MolgenisGroup> asList(group1, group2));
	//
	//	}
	//
	//	@Test(expectedExceptions = IllegalArgumentException.class)
	//	public void PermissionManagerController()
	//	{
	//		new PermissionManagerController(null, , );
	//	}
	//
	//	@Test
	//	public void init() throws Exception
	//	{
	//		this.mockMvc.perform(get(MolgenisPluginController.PLUGIN_URI_PREFIX + "/permissionmanager"))
	//				.andExpect(status().isOk()).andExpect(view().name("view-permissionmanager"))
	//				.andExpect(model().attribute("users", Arrays.asList(user2)))
	//				.andExpect(model().attribute("groups", Arrays.asList(group1, group2)));
	//	}
}
