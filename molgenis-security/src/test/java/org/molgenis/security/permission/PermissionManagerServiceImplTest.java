package org.molgenis.security.permission;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionManagerServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { Config.class })
public class PermissionManagerServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public PermissionManagerServiceImpl pluginPermissionManagerServiceImpl()
		{
			return new PermissionManagerServiceImpl(dataService(), mutableAclService());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public GrantedAuthoritiesMapper grantedAuthoritiesMapper()
		{
			return mock(GrantedAuthoritiesMapper.class);
		}

		@Bean
		public MutableAclService mutableAclService()
		{
			return mock(MutableAclService.class);
		}
	}

	@Autowired
	private PermissionManagerService pluginPermissionManagerService;

	@Autowired
	private DataService dataService;

	private GroupAuthority groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority, groupEntity2Authority;
	private UserAuthority userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority;
	private User user1, user2, user3;
	private Group group1;
	private Plugin plugin1, plugin2, plugin3;

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);

		String group1Id = "1";
		String user1Id = "1", user2Id = "2", user3Id = "3";
		user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
		user2 = when(mock(User.class).getId()).thenReturn("2").getMock();
		user3 = when(mock(User.class).getId()).thenReturn("3").getMock();
		when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(user1, user2, user3));

		group1 = when(mock(Group.class).getId()).thenReturn(group1Id).getMock();
		when(group1.getName()).thenReturn("group1");

		GroupMember groupMember1 = mock(GroupMember.class);
		when(groupMember1.getGroup()).thenReturn(group1);

		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user2),
				GroupMember.class)).thenReturn(Stream.of(groupMember1));

		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1),
				GroupMember.class)).thenReturn(Stream.of(groupMember1));

		groupPlugin1Authority = mock(GroupAuthority.class);
		when(groupPlugin1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin1");
		when(groupPlugin1Authority.getGroup()).thenReturn(group1);
		groupPlugin2Authority = mock(GroupAuthority.class);
		when(groupPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin2");
		when(groupPlugin2Authority.getGroup()).thenReturn(group1);
		groupEntity1Authority = mock(GroupAuthority.class);
		when(groupEntity1Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity1");
		when(groupEntity1Authority.getGroup()).thenReturn(group1);
		groupEntity2Authority = mock(GroupAuthority.class);
		when(groupEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity2");
		when(groupEntity2Authority.getGroup()).thenReturn(group1);

		userPlugin2Authority = mock(UserAuthority.class);
		when(userPlugin2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin2");
		when(userPlugin2Authority.getUser()).thenReturn(user1);
		userPlugin3Authority = mock(UserAuthority.class);
		when(userPlugin3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "plugin3");
		when(userPlugin3Authority.getUser()).thenReturn(user1);
		userEntity2Authority = mock(UserAuthority.class);
		when(userEntity2Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity2");
		when(userEntity2Authority.getUser()).thenReturn(user1);
		userEntity3Authority = mock(UserAuthority.class);
		when(userEntity3Authority.getRole()).thenReturn(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "entity3");
		when(userEntity3Authority.getUser()).thenReturn(user1);

		plugin1 = when(mock(Plugin.class).getId()).thenReturn("1").getMock();
		when(plugin1.getId()).thenReturn("plugin1");
		plugin2 = when(mock(Plugin.class).getId()).thenReturn("2").getMock();
		when(plugin1.getId()).thenReturn("plugin2");
		plugin3 = when(mock(Plugin.class).getId()).thenReturn("3").getMock();
		when(plugin1.getId()).thenReturn("plugin3");

		when(dataService.findAll(GROUP, Group.class)).thenReturn(Stream.of(group1));
		when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(user1));
		when(dataService.findAll(PLUGIN, Plugin.class)).thenReturn(Stream.of(plugin1, plugin2, plugin3));

		when(dataService.findOneById(GROUP, group1Id, Group.class)).thenReturn(group1);
		when(dataService.findOneById(USER, user1Id, User.class)).thenReturn(user1);
		when(dataService.findOneById(USER, user2Id, User.class)).thenReturn(user2);
		when(dataService.findOneById(USER, user3Id, User.class)).thenReturn(user3);

		when(dataService.findAll(GROUP_AUTHORITY,
				new QueryImpl<GroupAuthority>().in(GroupAuthorityMetaData.GROUP, Arrays.<Entity>asList(group1)),
				GroupAuthority.class)).thenReturn(
				Stream.of(groupPlugin1Authority, groupPlugin2Authority, groupEntity1Authority, groupEntity2Authority));

		when(dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().in(UserAuthorityMetaData.USER, Arrays.<Entity>asList(user1)),
				UserAuthority.class)).thenReturn(
				Stream.of(userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority));

		when(dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user1),
				UserAuthority.class)).thenReturn(
				Stream.of(userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority));
		when(dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user2),
				UserAuthority.class)).thenReturn(
				Stream.of(userPlugin2Authority, userPlugin3Authority, userEntity2Authority, userEntity3Authority));

		when(dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA)).thenReturn(Stream.empty());
		when(dataService.findAll(eq(EntityTypeMetadata.ENTITY_TYPE_META_DATA), any(),
				eq(new Fetch().field(EntityTypeMetadata.ID).field(EntityTypeMetadata.PACKAGE)),
				eq(EntityType.class))).thenReturn(Stream.empty());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void PluginPermissionManagerServiceImpl()
	{
		new PermissionManagerServiceImpl(null, null);
	}

	@Test
	public void getUsers()
	{
		when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(user1, user2, user3));
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
}
