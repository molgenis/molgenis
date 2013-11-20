package org.molgenis.security.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.permission.PermissionManagerServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class PermissionManagerServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public PermissionManagerServiceImpl pluginPermissionManagerServiceImpl()
		{
			return new PermissionManagerServiceImpl(dataService(), molgenisPluginRegistry(), grantedAuthoritiesMapper());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

		@Bean
		public GrantedAuthoritiesMapper grantedAuthoritiesMapper()
		{
			return mock(GrantedAuthoritiesMapper.class);
		}
	}

	@Autowired
	private PermissionManagerServiceImpl pluginPermissionManagerService;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPluginRegistry molgenisPluginRegistry;

	private GroupAuthority groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority, groupEntity2Authority;
	private UserAuthority userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws DatabaseException
	{
		reset(dataService);

		// MolgenisGroup group1 = when(mock(MolgenisGroup.class).getId()).thenReturn(1).getMock();
		// when(group1.getName()).thenReturn("group1");
		// Query<MolgenisGroup> queryGroup = mock(Query.class);
		// Query<MolgenisGroup> queryGroup1 = mock(Query.class);
		// when(queryGroup.eq(MolgenisGroup.ID, 1)).thenReturn(queryGroup1);
		// when(queryGroup.eq(MolgenisGroup.ID, -1)).thenReturn(queryGroup);
		// when(queryGroup1.find()).thenReturn(Arrays.<MolgenisGroup> asList(group1));
		// when(database.query(MolgenisGroup.class)).thenReturn(queryGroup);
		// when(database.find(MolgenisGroup.class)).thenReturn(Arrays.asList(group1));
		//
		// groupPlugin1Authority = mock(GroupAuthority.class);
		// when(groupPlugin1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin1");
		// when(groupPlugin1Authority.getMolgenisGroup()).thenReturn(group1);
		// groupPlugin2Authority = mock(GroupAuthority.class);
		// when(groupPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin2");
		// when(groupPlugin2Authority.getMolgenisGroup()).thenReturn(group1);
		// groupEntity1Authority = mock(GroupAuthority.class);
		// when(groupEntity1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity1");
		// when(groupEntity1Authority.getMolgenisGroup()).thenReturn(group1);
		// groupEntity2Authority = mock(GroupAuthority.class);
		// when(groupEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity2");
		// when(groupEntity2Authority.getMolgenisGroup()).thenReturn(group1);
		// when(
		// database.find(GroupAuthority.class,
		// new QueryRule(GroupAuthority.MOLGENISGROUP, IN, Arrays.asList(group1)))).thenReturn(
		// Arrays.<GroupAuthority> asList(groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority,
		// groupEntity2Authority));
		//
		// MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn(1).getMock();
		// MolgenisUser user2 = when(mock(MolgenisUser.class).getId()).thenReturn(2).getMock();
		// Query<MolgenisUser> queryUser = mock(Query.class);
		// Query<MolgenisUser> queryUser1 = mock(Query.class);
		// Query<MolgenisUser> queryUser2 = mock(Query.class);
		// when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUser1);
		// when(queryUser.eq(MolgenisUser.ID, 2)).thenReturn(queryUser2);
		// when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		// when(queryUser1.find()).thenReturn(Arrays.<MolgenisUser> asList(user1));
		// when(queryUser2.find()).thenReturn(Arrays.<MolgenisUser> asList(user2));
		// when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		//
		// MolgenisUser adminUser = when(mock(MolgenisUser.class).getId()).thenReturn(3).getMock();
		// when(adminUser.getSuperuser()).thenReturn(true);
		// when(database.find(MolgenisUser.class)).thenReturn(Arrays.asList(user1, user2, adminUser));
		//
		// userPlugin2Authority = mock(UserAuthority.class);
		// when(userPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin2");
		// when(userPlugin2Authority.getMolgenisUser()).thenReturn(user1);
		// userPlugin3Authority = mock(UserAuthority.class);
		// when(userPlugin3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin3");
		// when(userPlugin3Authority.getMolgenisUser()).thenReturn(user1);
		// userEntity2Authority = mock(UserAuthority.class);
		// when(userEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity2");
		// when(userEntity2Authority.getMolgenisUser()).thenReturn(user1);
		// userEntity3Authority = mock(UserAuthority.class);
		// when(userEntity3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity3");
		// when(userEntity3Authority.getMolgenisUser()).thenReturn(user1);
		//
		// when(database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER, EQUALS,
		// user1))).thenReturn(
		// Arrays.<UserAuthority> asList(userPlugin2Authority, userPlugin3Authority, userEntity2Authority,
		// userEntity3Authority));
		//
		// when(database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER, EQUALS,
		// user2))).thenReturn(
		// Arrays.<UserAuthority> asList(userPlugin2Authority, userPlugin3Authority, userEntity2Authority,
		// userEntity3Authority));
		//
		// MolgenisGroupMember molgenisGroupMember1 = mock(MolgenisGroupMember.class);
		// when(molgenisGroupMember1.getMolgenisGroup()).thenReturn(group1);
		// when(
		// database.find(MolgenisGroupMember.class, new QueryRule(MolgenisGroupMember.MOLGENISUSER,
		// Operator.EQUALS, user2))).thenReturn(Arrays.<MolgenisGroupMember> asList(molgenisGroupMember1));
		//
		// MolgenisPlugin plugin1 = when(mock(MolgenisPlugin.class).getId()).thenReturn("1").getMock();
		// when(plugin1.getName()).thenReturn("plugin1");
		// MolgenisPlugin plugin2 = when(mock(MolgenisPlugin.class).getId()).thenReturn("2").getMock();
		// when(plugin2.getName()).thenReturn("plugin2");
		// MolgenisPlugin plugin3 = when(mock(MolgenisPlugin.class).getId()).thenReturn("3").getMock();
		// when(plugin3.getName()).thenReturn("plugin3n");
		// when(molgenisPluginRegistry.getPlugins()).thenReturn(Arrays.<MolgenisPlugin> asList(plugin1, plugin2,
		// plugin3));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void PluginPermissionManagerServiceImpl()
	{
		new PermissionManagerServiceImpl(null, null, null);
	}

	@Test
	public void getUsers() throws DatabaseException
	{
		List<MolgenisUser> users = pluginPermissionManagerService.getUsers();
		assertEquals(users.get(0).getId(), Integer.valueOf(1));
		assertEquals(users.get(1).getId(), Integer.valueOf(2));
		assertEquals(users.get(2).getId(), Integer.valueOf(3));
		assertEquals(users.size(), 3);
	}

	@Test
	public void getGroups() throws DatabaseException
	{
		List<MolgenisGroup> groups = pluginPermissionManagerService.getGroups();
		assertEquals(groups.get(0).getId(), Integer.valueOf(1));
		assertEquals(groups.size(), 1);
	}

	@Test
	public void getPluginIds() throws DatabaseException
	{
		List<MolgenisPlugin> pluginIds = pluginPermissionManagerService.getPlugins();
		assertEquals(pluginIds.get(0).getId(), "1");
		assertEquals(pluginIds.get(1).getId(), "2");
		assertEquals(pluginIds.get(2).getId(), "3");
		assertEquals(pluginIds.size(), 3);
	}

	@Test
	public void getGroupEntityClassPermissions() throws DatabaseException
	{
		Permissions permissions = pluginPermissionManagerService.getGroupEntityClassPermissions(1);
		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		permission.setGroup("group1");
		assertEquals(groupPermissions.get("entity1"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("entity2"), Arrays.asList(permission));
		assertEquals(groupPermissions.size(), 2);
	}

	@Test
	public void getGroupPluginPermissions() throws DatabaseException
	{
		MolgenisGroup group1 = when(mock(MolgenisGroup.class).getId()).thenReturn(1).getMock();
		when(group1.getName()).thenReturn("group1");

		Permissions permissions = pluginPermissionManagerService.getGroupPluginPermissions(1);
		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		permission.setGroup("group1");
		assertEquals(groupPermissions.get("plugin1"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("plugin2"), Arrays.asList(permission));
		assertEquals(groupPermissions.size(), 2);
	}

	@Test
	public void getUserEntityClassPermissions_noGroup() throws DatabaseException
	{
		Permissions permissions = pluginPermissionManagerService.getUserEntityClassPermissions(1);
		Map<String, List<Permission>> userPermissions = permissions.getUserPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		assertEquals(userPermissions.get("entity2"), Arrays.asList(permission));
		assertEquals(userPermissions.get("entity3"), Arrays.asList(permission));
		assertEquals(userPermissions.size(), 2);
	}

	@Test
	public void getUserEntityClassPermissions_inGroup() throws DatabaseException
	{
		Permissions permissions = pluginPermissionManagerService.getUserEntityClassPermissions(2);

		Map<String, List<Permission>> userPermissions = permissions.getUserPermissions();
		Permission permission = new Permission();
		permission.setType("read");
		assertEquals(userPermissions.get("entity2"), Arrays.asList(permission));
		assertEquals(userPermissions.get("entity3"), Arrays.asList(permission));
		assertEquals(userPermissions.size(), 2);

		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();
		Permission groupPermission = new Permission();
		groupPermission.setType("read");
		groupPermission.setGroup("group1");
		assertEquals(groupPermissions.get("entity1"), Arrays.asList(groupPermission));
		assertEquals(groupPermissions.get("entity2"), Arrays.asList(groupPermission));
		assertEquals(groupPermissions.size(), 2);
	}

	@Test
	public void getUserPluginPermissions_noGroup() throws DatabaseException
	{
		Permissions permissions = pluginPermissionManagerService.getUserPluginPermissions(1);
		Map<String, List<Permission>> userPermissions = permissions.getUserPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		assertEquals(userPermissions.get("plugin2"), Arrays.asList(permission));
		assertEquals(userPermissions.get("plugin3"), Arrays.asList(permission));
		assertEquals(userPermissions.size(), 2);
	}

	@Test
	public void getUserPluginPermissions_inGroup() throws DatabaseException
	{
		Permissions permissions = pluginPermissionManagerService.getUserPluginPermissions(2);

		Map<String, List<Permission>> userPermissions = permissions.getUserPermissions();
		Permission permission = new Permission();
		permission.setType("read");
		assertEquals(userPermissions.get("plugin2"), Arrays.asList(permission));
		assertEquals(userPermissions.get("plugin3"), Arrays.asList(permission));
		assertEquals(userPermissions.size(), 2);

		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();
		Permission groupPermission = new Permission();
		groupPermission.setType("read");
		groupPermission.setGroup("group1");
		assertEquals(groupPermissions.get("plugin1"), Arrays.asList(groupPermission));
		assertEquals(groupPermissions.get("plugin2"), Arrays.asList(groupPermission));
		assertEquals(groupPermissions.size(), 2);
	}

	@Test
	public void replaceGroupEntityClassPermissions() throws DatabaseException
	{
		List<GroupAuthority> authorities = Arrays.asList(mock(GroupAuthority.class), mock(GroupAuthority.class));
		pluginPermissionManagerService.replaceGroupEntityClassPermissions(authorities, 1);
		verify(dataService).delete(GroupAuthority.ENTITY_NAME,
				Arrays.asList(groupEntity1Authority, groupEntity2Authority));
		verify(dataService).add(GroupAuthority.ENTITY_NAME, authorities);
	}

	@Test
	public void replaceGroupPluginPermissions() throws DatabaseException
	{
		List<GroupAuthority> authorities = Arrays.asList(mock(GroupAuthority.class), mock(GroupAuthority.class));
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, 1);
		verify(dataService).delete(GroupAuthority.ENTITY_NAME,
				Arrays.asList(groupPlugin1Authority, groupPlugin2Authority));
		verify(dataService).add(GroupAuthority.ENTITY_NAME, authorities);
	}

	@Test
	public void replaceUserEntityClassPermissions() throws DatabaseException
	{
		List<UserAuthority> authorities = Arrays.asList(mock(UserAuthority.class), mock(UserAuthority.class));
		pluginPermissionManagerService.replaceUserEntityClassPermissions(authorities, 1);
		verify(dataService)
				.delete(UserAuthority.ENTITY_NAME, Arrays.asList(userEntity2Authority, userEntity3Authority));
		verify(dataService).add(UserAuthority.ENTITY_NAME, authorities);
	}

	@Test
	public void replaceUserPluginPermissions() throws DatabaseException
	{
		List<UserAuthority> authorities = Arrays.asList(mock(UserAuthority.class), mock(UserAuthority.class));
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, 1);
		verify(dataService)
				.delete(UserAuthority.ENTITY_NAME, Arrays.asList(userPlugin2Authority, userPlugin3Authority));
		verify(dataService).add(UserAuthority.ENTITY_NAME, authorities);
	}
}
