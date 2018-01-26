package org.molgenis.security.user;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class UserDetailsServiceTest
{
	private UserDetailsService userDetailsService;

	@BeforeMethod
	public void setUp()
	{
		DataService dataService = mock(DataService.class);
		User adminUser = when(mock(User.class).isSuperuser()).thenReturn(Boolean.TRUE).getMock();
		when(adminUser.getUsername()).thenReturn("admin");
		when(adminUser.getPassword()).thenReturn("password");
		User userUser = when(mock(User.class).isSuperuser()).thenReturn(Boolean.FALSE).getMock();
		when(userUser.getUsername()).thenReturn("user");
		when(userUser.getPassword()).thenReturn("password");
		Query<User> qAdmin = new QueryImpl<User>().eq(UserMetaData.USERNAME, "admin");
		when(dataService.findOne(USER, qAdmin, User.class)).thenReturn(adminUser);
		Query<User> qUser = new QueryImpl<User>().eq(UserMetaData.USERNAME, "user");
		when(dataService.findOne(USER, qUser, User.class)).thenReturn(userUser);
		GrantedAuthoritiesMapper authoritiesMapper = authorities -> authorities;
		when(dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, userUser),
				UserAuthority.class)).thenAnswer(invocation -> Stream.empty());
		when(dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, adminUser),
				UserAuthority.class)).thenAnswer(invocation -> Stream.empty());
		when(dataService.findAll(GroupMemberMetaData.GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, userUser), GroupMember.class)).thenAnswer(
				invocation -> Stream.empty());
		when(dataService.findAll(GroupMemberMetaData.GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, adminUser), GroupMember.class)).thenAnswer(
				invocation -> Stream.empty());
		userDetailsService = new UserDetailsService(dataService, authoritiesMapper);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisUserDetailsService()
	{
		new UserDetailsService(null, null);
	}

	@Test
	public void loadUserByUsername_SuperUser()
	{
		UserDetails user = userDetailsService.loadUserByUsername("admin");
		Set<String> authorities = Sets.newHashSet(Collections2.transform(user.getAuthorities(),
				(Function<GrantedAuthority, String>) GrantedAuthority::getAuthority));
		assertTrue(authorities.contains(SecurityUtils.AUTHORITY_SU));
		assertEquals(authorities.size(), 1);
	}

	@Test
	public void loadUserByUsername_NonSuperUser()
	{
		UserDetails user = userDetailsService.loadUserByUsername("user");
		Set<String> authorities = Sets.newHashSet(Collections2.transform(user.getAuthorities(),
				(Function<GrantedAuthority, String>) GrantedAuthority::getAuthority));
		assertEquals(authorities.size(), 0);
	}
}
