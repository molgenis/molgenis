package org.molgenis.core.ui.admin.usermanager;

import org.molgenis.core.ui.admin.usermanager.UserManagerServiceImplTest.Config;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { Config.class })
public class UserManagerServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class Config extends WebSecurityConfigurerAdapter
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public UserManagerService userManagerService()
		{
			return new UserManagerServiceImpl(dataService(), molgenisGroupMemberFactory());
		}

		@Override
		protected org.springframework.security.core.userdetails.UserDetailsService userDetailsService()
		{
			return mock(UserDetailsService.class);
		}

		@Bean
		@Override
		public org.springframework.security.core.userdetails.UserDetailsService userDetailsServiceBean()
				throws Exception
		{
			return userDetailsService();
		}

		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception
		{
			return super.authenticationManagerBean();
		}

		@Autowired
		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception
		{
			auth.inMemoryAuthentication().withUser("user").password("password").authorities("ROLE_USER");
		}

		@Bean
		GroupMemberFactory molgenisGroupMemberFactory()
		{
			return mock(GroupMemberFactory.class);
		}
	}

	@Autowired
	private GroupMemberFactory groupMemberFactory;

	@Autowired
	private UserManagerService userManagerService;

	@Autowired
	private DataService dataService;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void userManagerServiceImpl()
	{
		new UserManagerServiceImpl(null, groupMemberFactory);
	}

	@Test
	public void getAllMolgenisUsersSu()
	{
		String molgenisUserId0 = "id0";
		String molgenisUserName0 = "user0";
		User user0 = when(mock(User.class).getId()).thenReturn(molgenisUserId0).getMock();
		when(user0.getIdValue()).thenReturn(molgenisUserId0);
		when(user0.getUsername()).thenReturn(molgenisUserName0);
		String molgenisUserId1 = "id1";
		String molgenisUserName1 = "user1";
		User user1 = when(mock(User.class).getId()).thenReturn(molgenisUserId1).getMock();
		when(user1.getIdValue()).thenReturn(molgenisUserId1);
		when(user1.getUsername()).thenReturn(molgenisUserName1);
		when(dataService.findOneById(UserMetaData.USER, molgenisUserId0, User.class)).thenReturn(user0);
		when(dataService.findOneById(UserMetaData.USER, molgenisUserId1, User.class)).thenReturn(user1);
		when(dataService.findAll(UserMetaData.USER, User.class)).thenReturn(Stream.of(user0, user1));
		GroupMember groupMember0 = mock(GroupMember.class);
		Role role0 = mock(Role.class);
		when(groupMember0.getGroup()).thenReturn(role0);
		GroupMember groupMember1 = mock(GroupMember.class);
		Role role1 = mock(Role.class);
		when(groupMember1.getGroup()).thenReturn(role1);
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user0),
				GroupMember.class)).thenReturn(Stream.of(groupMember0));
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1),
				GroupMember.class)).thenReturn(Stream.of(groupMember1));
		this.setSecurityContextSuperUser();
		assertEquals(userManagerService.getAllUsers(), Arrays.asList(new UserViewData(user0, singletonList(role0)),
				new UserViewData(user1, singletonList(role1))));
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getAllMolgenisUsersNonSu()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getAllUsers();
	}

	@Test
	public void getAllMolgenisGroupsSu()
	{
		Role role0 = mock(Role.class);
		Role role1 = mock(Role.class);
		when(dataService.findAll(RoleMetadata.ROLE, Role.class)).thenReturn(Stream.of(role0, role1));
		this.setSecurityContextSuperUser();
		assertEquals(userManagerService.getAllGroups(), Arrays.asList(role0, role1));
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getAllMolgenisGroups_Non_SU()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getAllGroups();
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getGroupsWhereUserIsMemberNonUs()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getGroupsWhereUserIsMember("1");
	}

	@Test
	public void getGroupsWhereUserIsMemberSu()
	{
		this.setSecurityContextSuperUser();

		User user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
		Role role20 = mock(Role.class);
		Role role21 = mock(Role.class);

		final GroupMember groupMemberOne = mock(GroupMember.class);
		groupMemberOne.setGroup(role20);
		groupMemberOne.setUser(user1);

		final GroupMember groupMemberTwo = mock(GroupMember.class);
		groupMemberTwo.setGroup(role21);
		groupMemberTwo.setUser(user1);

		when(dataService.findOneById(UserMetaData.USER, "1", User.class)).thenReturn(user1);
		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1),
				GroupMember.class)).thenReturn(Stream.of(groupMemberOne, groupMemberTwo));
		List<Role> roles = this.userManagerService.getGroupsWhereUserIsMember("1");

		assertEquals(roles.size(), 2);
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getUsersMemberInGroupNonSu()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getUsersMemberInGroup("22");
	}

	@Test
	public void getUsersMemberInGroup()
	{
		reset(dataService);

		this.setSecurityContextSuperUser();

		User user1 = mock(User.class);
		when(user1.getId()).thenReturn("1");
		when(user1.getUsername()).thenReturn("Jonathan");

		Role role22 = when(mock(Role.class).getId()).thenReturn("22").getMock();

		GroupMember groupMember = mock(GroupMember.class);
		when(groupMember.getUser()).thenReturn(user1);
		when(groupMember.getGroup()).thenReturn(role22);

		when(dataService.findOneById(UserMetaData.USER, "1", User.class)).thenReturn(user1);
		when(dataService.findOneById(RoleMetadata.ROLE, "22", Role.class)).thenReturn(role22);

		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1),
				GroupMember.class)).thenAnswer(invocation -> Stream.of(groupMember));

		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.GROUP, role22),
				GroupMember.class)).thenAnswer(invocation -> Stream.of(groupMember));

		List<UserViewData> users = this.userManagerService.getUsersMemberInGroup("22");
		assertEquals(users.size(), 1);
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getGroupsWhereUserIsNotMemberNonUs()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getGroupsWhereUserIsNotMember("1");
	}

	@Test
	public void getGroupsWhereUserIsNotMemberSu()
	{
		setSecurityContextSuperUser();

		User user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
		Role role22 = when(mock(Role.class).getId()).thenReturn("22").getMock();
		Role role33 = when(mock(Role.class).getId()).thenReturn("33").getMock();
		Role role44 = when(mock(Role.class).getId()).thenReturn("44").getMock();

		GroupMember groupMember = mock(GroupMember.class);
		when(groupMember.getUser()).thenReturn(user1);
		when(groupMember.getGroup()).thenReturn(role22);

		when(dataService.findOneById(UserMetaData.USER, "1", User.class)).thenReturn(user1);

		when(dataService.findAll(GroupMemberMetaData.USER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1), GroupMember.class)).thenReturn(
				Stream.of(groupMember));
		when(dataService.findAll(RoleMetadata.ROLE, Role.class)).thenReturn(Stream.of(role22, role33, role44));

		groupMember = mock(GroupMember.class);
		when(groupMember.getGroup()).thenReturn(role22);

		List<GroupMember> groupMemberships = new ArrayList<>();
		groupMemberships.add(groupMember);

		when(dataService.findAll(GROUP_MEMBER, new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1),
				GroupMember.class)).thenReturn(groupMemberships.stream());

		List<Role> roles = this.userManagerService.getGroupsWhereUserIsNotMember("1");
		assertEquals(roles.size(), 2);
	}

	@Test
	public void addUserToGroupSu() throws NumberFormatException
	{
		when(groupMemberFactory.create()).thenReturn(mock(GroupMember.class));
		setSecurityContextSuperUser();
		this.userManagerService.addUserToGroup("22", "1");
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void addUserToGroupNonSu() throws NumberFormatException
	{
		setSecurityContextNonSuperUserWrite();
		this.userManagerService.addUserToGroup("22", "1");
	}

	@Test
	public void removeUserFromGroupSu() throws NumberFormatException
	{
		setSecurityContextSuperUser();

		User user1 = when(mock(User.class).getId()).thenReturn("1").getMock();
		Role role22 = when(mock(Role.class).getId()).thenReturn("22").getMock();
		GroupMember groupMember = mock(GroupMember.class);
		when(groupMember.getUser()).thenReturn(user1);
		when(groupMember.getGroup()).thenReturn(role22);

		when(dataService.findOneById(UserMetaData.USER, "1", User.class)).thenReturn(user1);
		when(dataService.findOneById(RoleMetadata.ROLE, "22", Role.class)).thenReturn(role22);

		Query<GroupMember> q = new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user1)
														   .and().eq(GroupMemberMetaData.GROUP, role22);

		when(dataService.findAll(GROUP_MEMBER, q, GroupMember.class)).thenReturn(Stream.of(groupMember));

		this.userManagerService.removeUserFromGroup("22", "1");
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void removeUserFromGroupNonSu() throws NumberFormatException
	{
		setSecurityContextNonSuperUserWrite();
		this.userManagerService.removeUserFromGroup("22", "1");
	}

	private void setSecurityContextSuperUser()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays.asList(
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		SecurityContextHolder.getContext()
							 .setAuthentication(new UsernamePasswordAuthenticationToken(null, null, authorities));
	}

	private void setSecurityContextNonSuperUserWrite()
	{
		SecurityContextHolder.getContext()
							 .setAuthentication(new UsernamePasswordAuthenticationToken(null, null, emptyList()));
	}
}
