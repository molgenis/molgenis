package org.molgenis.security.permission;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.GroupAuthorityMetaData;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisGroupMemberMetaData;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.core.utils.SecurityUtils;
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
	private MolgenisUser user1, user2, user3;
	private MolgenisGroup group1;
	private MolgenisPlugin plugin1, plugin2, plugin3;

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);

		String group1Id = "1";
		String user1Id = "1", user2Id = "2", user3Id = "3";
		user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
		user2 = when(mock(MolgenisUser.class).getId()).thenReturn("2").getMock();
		user3 = when(mock(MolgenisUser.class).getId()).thenReturn("3").getMock();
		when(dataService.findAll(MOLGENIS_USER, MolgenisUser.class)).thenReturn(
				Stream.of(user1, user2, user3));

		group1 = when(mock(MolgenisGroup.class).getId()).thenReturn(group1Id).getMock();
		when(group1.getName()).thenReturn("group1");

		MolgenisGroupMember molgenisGroupMember1 = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember1.getMolgenisGroup()).thenReturn(group1);

		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user2),
				MolgenisGroupMember.class))
				.thenReturn(Stream.of(molgenisGroupMember1));

		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user1),
				MolgenisGroupMember.class))
				.thenReturn(Stream.of(molgenisGroupMember1));

		groupPlugin1Authority = mock(GroupAuthority.class);
		when(groupPlugin1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin1");
		when(groupPlugin1Authority.getMolgenisGroup()).thenReturn(group1);
		groupPlugin2Authority = mock(GroupAuthority.class);
		when(groupPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin2");
		when(groupPlugin2Authority.getMolgenisGroup()).thenReturn(group1);
		groupEntity1Authority = mock(GroupAuthority.class);
		when(groupEntity1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity1");
		when(groupEntity1Authority.getMolgenisGroup()).thenReturn(group1);
		groupEntity2Authority = mock(GroupAuthority.class);
		when(groupEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity2");
		when(groupEntity2Authority.getMolgenisGroup()).thenReturn(group1);

		userPlugin2Authority = mock(UserAuthority.class);
		when(userPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin2");
		when(userPlugin2Authority.getMolgenisUser()).thenReturn(user1);
		userPlugin3Authority = mock(UserAuthority.class);
		when(userPlugin3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin3");
		when(userPlugin3Authority.getMolgenisUser()).thenReturn(user1);
		userEntity2Authority = mock(UserAuthority.class);
		when(userEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity2");
		when(userEntity2Authority.getMolgenisUser()).thenReturn(user1);
		userEntity3Authority = mock(UserAuthority.class);
		when(userEntity3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity3");
		when(userEntity3Authority.getMolgenisUser()).thenReturn(user1);

		when(dataService.findAll(MOLGENIS_GROUP, MolgenisGroup.class)).thenReturn(Stream.of(group1));

		when(dataService.findAll(MOLGENIS_USER, MolgenisUser.class)).thenReturn(Stream.of(user1));

		when(dataService.findOneById(MOLGENIS_GROUP, group1Id, MolgenisGroup.class)).thenReturn(group1);
		when(dataService.findOneById(MOLGENIS_USER, user1Id, MolgenisUser.class)).thenReturn(user1);
		when(dataService.findOneById(MOLGENIS_USER, user2Id, MolgenisUser.class)).thenReturn(user2);
		when(dataService.findOneById(MOLGENIS_USER, user3Id, MolgenisUser.class)).thenReturn(user3);

		when(dataService.findAll(GROUP_AUTHORITY, new QueryImpl<GroupAuthority>()
						.in(GroupAuthorityMetaData.MOLGENIS_GROUP, Arrays.<Entity>asList(group1)),
						GroupAuthority.class)).thenReturn(
				Stream.of(groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority, groupEntity2Authority));

		when(dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().in(UserAuthorityMetaData.MOLGENIS_USER, Arrays.<Entity>asList(user1)),
						UserAuthority.class)).thenReturn(
				Stream.of(userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority));

		when(dataService
				.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.MOLGENIS_USER, user1),
						UserAuthority.class)).thenReturn(
				Stream.of(userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority));
		when(dataService
				.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.MOLGENIS_USER, user2),
						UserAuthority.class)).thenReturn(
				Stream.of(userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority));

		plugin1 = when(mock(MolgenisPlugin.class).getId()).thenReturn("1").getMock();
		when(plugin1.getName()).thenReturn("plugin1");
		plugin2 = when(mock(MolgenisPlugin.class).getId()).thenReturn("2").getMock();
		when(plugin2.getName()).thenReturn("plugin2");
		plugin3 = when(mock(MolgenisPlugin.class).getId()).thenReturn("3").getMock();
		when(plugin3.getName()).thenReturn("plugin3n");
		when(molgenisPluginRegistry.iterator()).thenReturn(
				Arrays.<MolgenisPlugin> asList(plugin1, plugin2, plugin3).iterator());

		when(dataService.getEntityNames()).thenReturn(Stream.empty());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void PluginPermissionManagerServiceImpl()
	{
		new PermissionManagerServiceImpl(null, null, null);
	}

	@Test
	public void getUsers()
	{
		when(dataService.findAll(MOLGENIS_USER, MolgenisUser.class)).thenReturn(
				Stream.of(user1, user2, user3));
		assertEquals(pluginPermissionManagerService.getUsers(), Arrays.asList(user1, user2, user3));
	}

	@Test
	public void getGroups()
	{
		assertEquals(pluginPermissionManagerService.getGroups(), Arrays.asList(group1));
	}

	@Test
	public void getPlugins()
	{
		assertEquals(pluginPermissionManagerService.getPlugins(), Arrays.asList(plugin1, plugin2, plugin3));
	}

	@Test
	public void getGroupEntityClassPermissions()
	{
		Permissions permissions = pluginPermissionManagerService.getGroupEntityClassPermissions("1");
		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		permission.setGroup("group1");
		assertEquals(groupPermissions.get("entity1"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("entity2"), Arrays.asList(permission));
		assertEquals(groupPermissions.size(), 2);
	}

	@Test
	public void getGroupPluginPermissions()
	{
		MolgenisGroup group1 = when(mock(MolgenisGroup.class).getId()).thenReturn("1").getMock();
		when(group1.getName()).thenReturn("group1");

		Permissions permissions = pluginPermissionManagerService.getGroupPluginPermissions("1");
		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		permission.setGroup("group1");
		assertEquals(groupPermissions.get("plugin1"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("plugin2"), Arrays.asList(permission));
		assertEquals(groupPermissions.size(), 2);
	}

	@Test
	public void getUserEntityClassPermissions_noGroup()
	{
		Permissions permissions = pluginPermissionManagerService.getUserEntityClassPermissions("1");
		Map<String, List<Permission>> userPermissions = permissions.getUserPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		assertEquals(userPermissions.get("entity2"), Arrays.asList(permission));
		assertEquals(userPermissions.get("entity3"), Arrays.asList(permission));
		assertEquals(userPermissions.size(), 2);
	}

	@Test
	public void getUserEntityClassPermissions_inGroup()
	{
		Permissions permissions = pluginPermissionManagerService.getUserEntityClassPermissions("2");

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
	public void getUserPluginPermissions_noGroup()
	{
		Permissions permissions = pluginPermissionManagerService.getUserPluginPermissions("1");
		Map<String, List<Permission>> userPermissions = permissions.getUserPermissions();

		Permission permission = new Permission();
		permission.setType("read");
		assertEquals(userPermissions.get("plugin2"), Arrays.asList(permission));
		assertEquals(userPermissions.get("plugin3"), Arrays.asList(permission));
		assertEquals(userPermissions.size(), 2);
	}

	@Test
	public void getUserPluginPermissions_inGroup()
	{
		Permissions permissions = pluginPermissionManagerService.getUserPluginPermissions("2");

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

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void replaceGroupEntityClassPermissions()
	{
		List<GroupAuthority> authorities = Arrays.asList(mock(GroupAuthority.class), mock(GroupAuthority.class));
		pluginPermissionManagerService.replaceGroupEntityClassPermissions(authorities, "1");

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(GROUP_AUTHORITY), captor.capture());
		assertEquals(captor.getValue().collect(toList()), Arrays.asList(groupEntity1Authority, groupEntity2Authority));

		ArgumentCaptor<Stream<Entity>> captor2 = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(GROUP_AUTHORITY), captor2.capture());
		assertEquals(captor2.getValue().collect(toList()), authorities);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void replaceGroupPluginPermissions()
	{
		List<GroupAuthority> authorities = Arrays.asList(mock(GroupAuthority.class), mock(GroupAuthority.class));
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, "1");

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(GROUP_AUTHORITY), captor.capture());
		assertEquals(captor.getValue().collect(toList()), Arrays.asList(groupPlugin1Authority, groupPlugin2Authority));

		ArgumentCaptor<Stream<Entity>> captor2 = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(GROUP_AUTHORITY), captor.capture());
		assertEquals(captor.getValue().collect(toList()), authorities);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void replaceUserEntityClassPermissions()
	{
		List<UserAuthority> authorities = Arrays.asList(mock(UserAuthority.class), mock(UserAuthority.class));
		pluginPermissionManagerService.replaceUserEntityClassPermissions(authorities, "1");

		ArgumentCaptor<Stream<UserAuthority>> captor1 = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(USER_AUTHORITY), captor1.capture());
		assertEquals(captor1.getValue().collect(toList()), Arrays.asList(userEntity2Authority, userEntity3Authority));

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(USER_AUTHORITY), captor.capture());
		assertEquals(captor.getValue().collect(toList()), authorities);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void replaceUserPluginPermissions()
	{
		List<UserAuthority> authorities = Arrays.asList(mock(UserAuthority.class), mock(UserAuthority.class));
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, "1");

		ArgumentCaptor<Stream<UserAuthority>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).delete(eq(USER_AUTHORITY), captor.capture());

		ArgumentCaptor<Stream<Entity>> captor1 = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(USER_AUTHORITY), captor1.capture());

		assertEquals(captor.getValue().collect(toList()), Arrays.asList(userPlugin2Authority, userPlugin3Authority));
		assertEquals(captor1.getValue().collect(toList()), authorities);
	}
}
