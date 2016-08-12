package org.molgenis.security.user;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.*;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MolgenisUserDetailsServiceTest
{
	private MolgenisUserDetailsService userDetailsService;

	@BeforeMethod
	public void setUp()
	{
		DataService dataService = mock(DataService.class);
		MolgenisUser adminUser = when(mock(MolgenisUser.class).isSuperuser()).thenReturn(Boolean.TRUE).getMock();
		when(adminUser.getUsername()).thenReturn("admin");
		when(adminUser.getPassword()).thenReturn("password");
		MolgenisUser userUser = when(mock(MolgenisUser.class).isSuperuser()).thenReturn(Boolean.FALSE).getMock();
		when(userUser.getUsername()).thenReturn("user");
		when(userUser.getPassword()).thenReturn("password");
		Query<MolgenisUser> qAdmin = new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.USERNAME, "admin");
		when(dataService.findOne(MOLGENIS_USER, qAdmin, MolgenisUser.class)).thenReturn(adminUser);
		Query<MolgenisUser> qUser = new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.USERNAME, "user");
		when(dataService.findOne(MOLGENIS_USER, qUser, MolgenisUser.class)).thenReturn(userUser);
		GrantedAuthoritiesMapper authoritiesMapper = new GrantedAuthoritiesMapper()
		{
			@Override
			public Collection<? extends GrantedAuthority> mapAuthorities(
					Collection<? extends GrantedAuthority> authorities)
			{
				return authorities;
			}
		};
		when(dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.MOLGENIS_USER, userUser), UserAuthority.class))
				.thenAnswer(new Answer<Stream<UserAuthority>>()
				{
					@Override
					public Stream<UserAuthority> answer(InvocationOnMock invocation) throws Throwable
					{
						return Stream.empty();
					}
				});
		when(dataService.findAll(USER_AUTHORITY,
				new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.MOLGENIS_USER, adminUser), UserAuthority.class))
				.thenAnswer(new Answer<Stream<UserAuthority>>()
				{
					@Override
					public Stream<UserAuthority> answer(InvocationOnMock invocation) throws Throwable
					{
						return Stream.empty();
					}
				});
		when(dataService.findAll(MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, userUser),
				MolgenisGroupMember.class)).thenAnswer(new Answer<Stream<MolgenisGroupMember>>()
		{
			@Override
			public Stream<MolgenisGroupMember> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		when(dataService.findAll(MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, adminUser),
				MolgenisGroupMember.class)).thenAnswer(new Answer<Stream<MolgenisGroupMember>>()
		{
			@Override
			public Stream<MolgenisGroupMember> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		userDetailsService = new MolgenisUserDetailsService(dataService, authoritiesMapper);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisUserDetailsService()
	{
		new MolgenisUserDetailsService(null, null);
	}

	@Test
	public void loadUserByUsername_SuperUser()
	{
		UserDetails user = userDetailsService.loadUserByUsername("admin");
		Set<String> authorities = Sets
				.newHashSet(Collections2.transform(user.getAuthorities(), new Function<GrantedAuthority, String>()
				{
					@Override
					public String apply(GrantedAuthority authority)
					{
						return authority.getAuthority();
					}
				}));
		assertTrue(authorities.contains(SecurityUtils.AUTHORITY_SU));
		assertEquals(authorities.size(), 1);
	}

	@Test
	public void loadUserByUsername_NonSuperUser()
	{
		UserDetails user = userDetailsService.loadUserByUsername("user");
		Set<String> authorities = Sets
				.newHashSet(Collections2.transform(user.getAuthorities(), new Function<GrantedAuthority, String>()
				{
					@Override
					public String apply(GrantedAuthority authority)
					{
						return authority.getAuthority();
					}
				}));
		assertEquals(authorities.size(), 0);
	}
}
