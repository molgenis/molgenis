package org.molgenis.security.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;
import static org.molgenis.framework.db.QueryRule.Operator.IN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.permission.PermissionManagerServiceImpl;
import org.molgenis.security.permission.PermissionManagerServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
			return new PermissionManagerServiceImpl(database(), molgenisPluginRegistry());
		}

		@Bean
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}
	}

	@Autowired
	private PermissionManagerServiceImpl pluginPermissionManagerService;

	@Autowired
	private Database database;

	@Autowired
	private MolgenisPluginRegistry molgenisPluginRegistry;

	private GroupAuthority groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority, groupEntity2Authority;
	private UserAuthority userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws DatabaseException
	{
		reset(database);

		// meta data
		Model metaData = mock(Model.class);
		Vector<Entity> entities = new Vector<Entity>();
		Entity entity1 = when(mock(Entity.class).getName()).thenReturn("entity1").getMock();
		Entity entity2 = when(mock(Entity.class).getName()).thenReturn("entity2").getMock();
		entities.add(entity1);
		entities.add(entity2);
		when(metaData.getEntities(false, false)).thenReturn(entities);
		when(database.getMetaData()).thenReturn(metaData);

		MolgenisGroup group1 = when(mock(MolgenisGroup.class).getId()).thenReturn(1).getMock();
		Query<MolgenisGroup> queryGroup = mock(Query.class);
		Query<MolgenisGroup> queryGroup1 = mock(Query.class);
		when(queryGroup.eq(MolgenisGroup.ID, 1)).thenReturn(queryGroup1);
		when(queryGroup.eq(MolgenisGroup.ID, -1)).thenReturn(queryGroup);
		when(queryGroup1.find()).thenReturn(Arrays.<MolgenisGroup> asList(group1));
		when(database.query(MolgenisGroup.class)).thenReturn(queryGroup);
		when(database.find(MolgenisGroup.class)).thenReturn(Arrays.asList(group1));

		groupPlugin1Authority = mock(GroupAuthority.class);
		when(groupPlugin1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin1");
		groupPlugin2Authority = mock(GroupAuthority.class);
		when(groupPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin2");
		groupEntity1Authority = mock(GroupAuthority.class);
		when(groupEntity1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity1");
		groupEntity2Authority = mock(GroupAuthority.class);
		when(groupEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity2");
		when(
				database.find(GroupAuthority.class,
						new QueryRule(GroupAuthority.MOLGENISGROUP, IN, Arrays.asList(group1)))).thenReturn(
				Arrays.<GroupAuthority> asList(groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority,
						groupEntity2Authority));

		MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn(1).getMock();
		MolgenisUser user2 = when(mock(MolgenisUser.class).getId()).thenReturn(2).getMock();
		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUser1 = mock(Query.class);
		Query<MolgenisUser> queryUser2 = mock(Query.class);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUser1);
		when(queryUser.eq(MolgenisUser.ID, 2)).thenReturn(queryUser2);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUser1.find()).thenReturn(Arrays.<MolgenisUser> asList(user1));
		when(queryUser2.find()).thenReturn(Arrays.<MolgenisUser> asList(user2));
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);

		MolgenisUser adminUser = when(mock(MolgenisUser.class).getId()).thenReturn(3).getMock();
		when(adminUser.getSuperuser()).thenReturn(true);
		when(database.find(MolgenisUser.class)).thenReturn(Arrays.asList(user1, user2, adminUser));

		userPlugin2Authority = mock(UserAuthority.class);
		when(userPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin2");
		userPlugin3Authority = mock(UserAuthority.class);
		when(userPlugin3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin3");
		userEntity2Authority = mock(UserAuthority.class);
		when(userEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity2");
		userEntity3Authority = mock(UserAuthority.class);
		when(userEntity3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity3");

		when(database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER, EQUALS, user1))).thenReturn(
				Arrays.<UserAuthority> asList(userPlugin2Authority, userPlugin3Authority, userEntity2Authority,
						userEntity3Authority));

		when(database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER, EQUALS, user2))).thenReturn(
				Arrays.<UserAuthority> asList(userPlugin2Authority, userPlugin3Authority, userEntity2Authority,
						userEntity3Authority));

		MolgenisGroupMember molgenisGroupMember1 = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember1.getMolgenisGroup()).thenReturn(group1);
		when(
				database.find(MolgenisGroupMember.class, new QueryRule(MolgenisGroupMember.MOLGENISUSER,
						Operator.EQUALS, user2))).thenReturn(Arrays.<MolgenisGroupMember> asList(molgenisGroupMember1));

		MolgenisPlugin plugin1 = when(mock(MolgenisPlugin.class).getId()).thenReturn("1").getMock();
		MolgenisPlugin plugin2 = when(mock(MolgenisPlugin.class).getId()).thenReturn("2").getMock();
		MolgenisPlugin plugin3 = when(mock(MolgenisPlugin.class).getId()).thenReturn("3").getMock();
		when(molgenisPluginRegistry.getPlugins()).thenReturn(Arrays.<MolgenisPlugin> asList(plugin1, plugin2, plugin3));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void PluginPermissionManagerServiceImpl()
	{
		new PermissionManagerServiceImpl(null, null);
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
	public void getEntityClassIds() throws DatabaseException
	{
		assertEquals(pluginPermissionManagerService.getEntityClassIds(), Arrays.asList("entity1", "entity2"));
	}

	@Test
	public void getGroupEntityClassPermissions() throws DatabaseException
	{
		List<GroupAuthority> permissions = pluginPermissionManagerService.getGroupEntityClassPermissions(1);
		assertEquals(permissions.get(0).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity1");
		assertEquals(permissions.get(1).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity2");
		assertEquals(permissions.size(), 2);
	}

	@Test
	public void getGroupPluginPermissions() throws DatabaseException
	{
		List<GroupAuthority> permissions = pluginPermissionManagerService.getGroupPluginPermissions(1);
		assertEquals(permissions.get(0).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin1");
		assertEquals(permissions.get(1).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin2");
		assertEquals(permissions.size(), 2);
	}

	@Test
	public void getUserEntityClassPermissions_noGroup() throws DatabaseException
	{
		List<? extends Authority> permissions = pluginPermissionManagerService.getUserEntityClassPermissions(1);
		assertEquals(permissions.get(0).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity2");
		assertEquals(permissions.get(1).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity3");
		assertEquals(permissions.size(), 2);
	}

	@Test
	public void getUserEntityClassPermissions_inGroup() throws DatabaseException
	{
		List<? extends Authority> permissions = pluginPermissionManagerService.getUserEntityClassPermissions(2);
		assertEquals(permissions.get(0).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity2");
		assertTrue(permissions.get(0) instanceof UserAuthority);
		assertEquals(permissions.get(1).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity3");
		assertTrue(permissions.get(1) instanceof UserAuthority);
		assertEquals(permissions.get(2).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity1");
		assertTrue(permissions.get(2) instanceof GroupAuthority);
		assertEquals(permissions.get(3).getRole(), SecurityUtils.AUTHORITY_ENTITY_PREFIX + "entity2");
		assertTrue(permissions.get(3) instanceof GroupAuthority);
		assertEquals(permissions.size(), 4);
	}

	@Test
	public void getUserPluginPermissions_noGroup() throws DatabaseException
	{
		List<? extends Authority> permissions = pluginPermissionManagerService.getUserPluginPermissions(1);
		assertEquals(permissions.get(0).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin2");
		assertEquals(permissions.get(1).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin3");
		assertEquals(permissions.size(), 2);
	}

	@Test
	public void getUserPluginPermissions_inGroup() throws DatabaseException
	{
		List<? extends Authority> permissions = pluginPermissionManagerService.getUserPluginPermissions(2);
		assertEquals(permissions.get(0).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin2");
		assertTrue(permissions.get(0) instanceof UserAuthority);
		assertEquals(permissions.get(1).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin3");
		assertTrue(permissions.get(1) instanceof UserAuthority);
		assertEquals(permissions.get(2).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin1");
		assertTrue(permissions.get(2) instanceof GroupAuthority);
		assertEquals(permissions.get(3).getRole(), SecurityUtils.AUTHORITY_PLUGIN_PREFIX + "plugin2");
		assertTrue(permissions.get(3) instanceof GroupAuthority);
		assertEquals(permissions.size(), 4);
	}

	@Test
	public void replaceGroupEntityClassPermissions() throws DatabaseException
	{
		List<GroupAuthority> authorities = Arrays.asList(mock(GroupAuthority.class), mock(GroupAuthority.class));
		pluginPermissionManagerService.replaceGroupEntityClassPermissions(authorities, 1);
		verify(database).remove(Arrays.asList(groupEntity1Authority, groupEntity2Authority));
		verify(database).add(authorities);
	}

	@Test
	public void replaceGroupPluginPermissions() throws DatabaseException
	{
		List<GroupAuthority> authorities = Arrays.asList(mock(GroupAuthority.class), mock(GroupAuthority.class));
		pluginPermissionManagerService.replaceGroupPluginPermissions(authorities, 1);
		verify(database).remove(Arrays.asList(groupPlugin1Authority, groupPlugin2Authority));
		verify(database).add(authorities);
	}

	@Test
	public void replaceUserEntityClassPermissions() throws DatabaseException
	{
		List<UserAuthority> authorities = Arrays.asList(mock(UserAuthority.class), mock(UserAuthority.class));
		pluginPermissionManagerService.replaceUserEntityClassPermissions(authorities, 1);
		verify(database).remove(Arrays.asList(userEntity2Authority, userEntity3Authority));
		verify(database).add(authorities);
	}

	@Test
	public void replaceUserPluginPermissions() throws DatabaseException
	{
		List<UserAuthority> authorities = Arrays.asList(mock(UserAuthority.class), mock(UserAuthority.class));
		pluginPermissionManagerService.replaceUserPluginPermissions(authorities, 1);
		verify(database).remove(Arrays.asList(userPlugin2Authority, userPlugin3Authority));
		verify(database).add(authorities);
	}
}
