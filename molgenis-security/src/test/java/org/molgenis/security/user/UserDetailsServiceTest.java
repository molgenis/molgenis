package org.molgenis.security.user;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.*;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class UserDetailsServiceTest extends AbstractMockitoTest
{
	@Mock
	private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	@Mock
	private DataService dataService;

	private UserDetailsService userDetailsService;

	@BeforeMethod
	public void setUp()
	{
		userDetailsService = new UserDetailsService(dataService, grantedAuthoritiesMapper);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testUserDetailsService()
	{
		new UserDetailsService(null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadUserByUsernameSuperuser()
	{
		String username = "user";
		User user = mock(User.class);
		when(user.getUsername()).thenReturn(username);
		when(user.getPassword()).thenReturn("pw");
		when(user.isActive()).thenReturn(true);
		when(user.isSuperuser()).thenReturn(true);

		Query<User> userQuery = mock(Query.class);
		when(userQuery.findOne()).thenReturn(user);
		Query<User> baseUserQuery = mock(Query.class);
		when(baseUserQuery.eq(UserMetaData.USERNAME, username)).thenReturn(userQuery);
		doReturn(baseUserQuery).when(dataService).query(UserMetaData.USER, User.class);

		Group group = mock(Group.class);
		when(group.getId()).thenReturn("groupId");
		GroupMember groupMember = mock(GroupMember.class);
		when(groupMember.getGroup()).thenReturn(group);

		Query<GroupMember> groupMemberQuery = mock(Query.class);
		when(groupMemberQuery.findAll()).thenReturn(Stream.of(groupMember));
		Query<GroupMember> baseGroupMemberQuery = mock(Query.class);
		when(baseGroupMemberQuery.eq(GroupMemberMetaData.USER, user)).thenReturn(groupMemberQuery);
		doReturn(baseGroupMemberQuery).when(dataService).query(GroupMemberMetaData.GROUP_MEMBER, GroupMember.class);

		Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
		userAuthorities.add(new SimpleGrantedAuthority("ROLE_SU"));
		userAuthorities.add(new SimpleGrantedAuthority("ROLE_groupId"));
		Collection<GrantedAuthority> mappedAuthorities = singletonList(new SimpleGrantedAuthority("ROLE_MAPPED"));
		when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities)).thenReturn((Collection) mappedAuthorities);

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		assertEquals(userDetails.getUsername(), username);
		assertEquals(userDetails.getAuthorities(), mappedAuthorities);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadUserByUsernameNonSuperuser()
	{
		String username = "user";
		User user = mock(User.class);
		when(user.getUsername()).thenReturn(username);
		when(user.getPassword()).thenReturn("pw");
		when(user.isActive()).thenReturn(true);

		Query<User> userQuery = mock(Query.class);
		when(userQuery.findOne()).thenReturn(user);
		Query<User> baseUserQuery = mock(Query.class);
		when(baseUserQuery.eq(UserMetaData.USERNAME, username)).thenReturn(userQuery);
		doReturn(baseUserQuery).when(dataService).query(UserMetaData.USER, User.class);

		Group group = mock(Group.class);
		when(group.getId()).thenReturn("groupId");
		GroupMember groupMember = mock(GroupMember.class);
		when(groupMember.getGroup()).thenReturn(group);

		Query<GroupMember> groupMemberQuery = mock(Query.class);
		when(groupMemberQuery.findAll()).thenReturn(Stream.of(groupMember));
		Query<GroupMember> baseGroupMemberQuery = mock(Query.class);
		when(baseGroupMemberQuery.eq(GroupMemberMetaData.USER, user)).thenReturn(groupMemberQuery);
		doReturn(baseGroupMemberQuery).when(dataService).query(GroupMemberMetaData.GROUP_MEMBER, GroupMember.class);

		Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
		userAuthorities.add(new SimpleGrantedAuthority("ROLE_groupId"));
		Collection<GrantedAuthority> mappedAuthorities = singletonList(new SimpleGrantedAuthority("ROLE_MAPPED"));
		when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities)).thenReturn((Collection) mappedAuthorities);

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		assertEquals(userDetails.getUsername(), username);
		assertEquals(userDetails.getAuthorities(), mappedAuthorities);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadUserByUsernameAnonymous()
	{
		String username = "anonymous";
		User user = mock(User.class);
		when(user.getUsername()).thenReturn(username);
		when(user.getPassword()).thenReturn("pw");
		when(user.isActive()).thenReturn(true);

		Query<User> userQuery = mock(Query.class);
		when(userQuery.findOne()).thenReturn(user);
		Query<User> baseUserQuery = mock(Query.class);
		when(baseUserQuery.eq(UserMetaData.USERNAME, username)).thenReturn(userQuery);
		doReturn(baseUserQuery).when(dataService).query(UserMetaData.USER, User.class);

		Group group = mock(Group.class);
		when(group.getId()).thenReturn("groupId");
		GroupMember groupMember = mock(GroupMember.class);
		when(groupMember.getGroup()).thenReturn(group);

		Query<GroupMember> groupMemberQuery = mock(Query.class);
		when(groupMemberQuery.findAll()).thenReturn(Stream.of(groupMember));
		Query<GroupMember> baseGroupMemberQuery = mock(Query.class);
		when(baseGroupMemberQuery.eq(GroupMemberMetaData.USER, user)).thenReturn(groupMemberQuery);
		doReturn(baseGroupMemberQuery).when(dataService).query(GroupMemberMetaData.GROUP_MEMBER, GroupMember.class);

		Set<GrantedAuthority> userAuthorities = new LinkedHashSet<>();
		userAuthorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
		userAuthorities.add(new SimpleGrantedAuthority("ROLE_groupId"));
		Collection<GrantedAuthority> mappedAuthorities = singletonList(new SimpleGrantedAuthority("ROLE_MAPPED"));
		when(grantedAuthoritiesMapper.mapAuthorities(userAuthorities)).thenReturn((Collection) mappedAuthorities);

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		assertEquals(userDetails.getUsername(), username);
		assertEquals(userDetails.getAuthorities(), mappedAuthorities);
	}

	@Test(expectedExceptions = UsernameNotFoundException.class, expectedExceptionsMessageRegExp = "unknown user 'unknownUser'")
	public void testLoadUserByUsernameUnknownUser()
	{
		String username = "unknownUser";

		Query<User> userQuery = mock(Query.class);
		when(userQuery.findOne()).thenReturn(null);
		Query<User> baseUserQuery = mock(Query.class);
		when(baseUserQuery.eq(UserMetaData.USERNAME, username)).thenReturn(userQuery);
		doReturn(baseUserQuery).when(dataService).query(UserMetaData.USER, User.class);

		userDetailsService.loadUserByUsername(username);
	}
}
