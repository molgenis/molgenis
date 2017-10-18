package org.molgenis.security.core.service.impl;

import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.testng.Assert.assertEquals;

public class UserDetailsServiceImplTest
{
	private UserDetailsServiceImpl userDetailsService;

	@Mock
	private UserService userService;

	@Mock
	private GroupService groupService;
	private final User user = User.builder()
								  .username("user")
								  .password("password")
								  .email("user@example.com")
								  .twoFactorAuthentication(false)
								  .active(true)
								  .changePassword(false)
								  .id("defgh")
								  .superuser(false)
								  .build();
	private final User admin = User.builder()
								   .username("admin")
								   .password("password")
								   .superuser(true)
								   .email("admin@example.com")
								   .twoFactorAuthentication(false)
								   .active(true)
								   .changePassword(false)
								   .id("abcde")
								   .build();

	@BeforeMethod
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);

		when(userService.findByUsername("admin")).thenReturn(admin);
		when(userService.findByUsername("user")).thenReturn(user);

		userDetailsService = new UserDetailsServiceImpl(userService, groupService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisUserDetailsService()
	{
		new UserDetailsServiceImpl(null, null);
	}

	@Test
	public void loadUserByUsername_SuperUser()
	{
		UserDetails user = userDetailsService.loadUserByUsername("admin");

		Set<String> authorities = user.getAuthorities()
									  .stream()
									  .map(GrantedAuthority::getAuthority)
									  .collect(Collectors.toSet());
		assertEquals(authorities, singleton(AUTHORITY_SU));
	}

	@Test
	public void loadUserByUsername_NonSuperUser()
	{
		Role editorRole = Role.create("zzzzz", "ABCDE Editor");
		Role parentGroupRole1 = Role.create("qqqqq", "Role 1");
		Role parentGroupRole2 = Role.create("rrrrr", "Role 2");

		Group parentGroup = Group.builder()
								 .id("aaaaa")
								 .label("ABCDE")
								 .roles(ImmutableList.of(parentGroupRole1, parentGroupRole2))
								 .build();
		Group editorGroup = Group.builder()
								 .id("bbbbb")
								 .label("ABCDE Editor")
								 .parent(parentGroup)
								 .roles(emptyList())
								 .build();
		Group otherGroup = Group.builder()
								.id("ccccc")
								.label("OTHER Viewer")
								.roles(ImmutableList.of(editorRole))
								.build();

		GroupMembership editorGroupMembership = GroupMembership.builder()
															   .id("ddddd")
															   .user(user)
															   .group(editorGroup)
															   .start(Instant.now())
															   .build();

		GroupMembership otherGroupMembership = GroupMembership.builder()
															  .id("eeeee")
															  .user(user)
															  .group(otherGroup)
															  .start(Instant.now())
															  .end(Instant.now())
															  .build();
		when(groupService.getGroupMemberships(user)).thenReturn(
				ImmutableList.of(editorGroupMembership, otherGroupMembership));

		UserDetails user = userDetailsService.loadUserByUsername("user");
		assertEquals(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
				ImmutableList.of(parentGroupRole1.getId(), parentGroupRole2.getId()));
	}
}
