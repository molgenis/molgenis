package org.molgenis.security.permission;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.auth.User;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.user.UserService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PermissionSystemServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PermissionSystemServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Captor
	private ArgumentCaptor<Stream<UserAuthority>> userAuthorityStreamCaptor;

	@Autowired
	private Config config;

	@Autowired
	private UserService userService;

	@Autowired
	private UserAuthorityFactory userAuthorityFactory;

	@Autowired
	private RoleHierarchy roleHierarchy;

	@Autowired
	private DataService dataService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		config.resetMocks();
		permissionSystemService = new PermissionSystemServiceImpl(userService, userAuthorityFactory, roleHierarchy,
				dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPermissionSystemService()
	{
		new PermissionSystemServiceImpl(null, null, null, null);
	}

	@Test
	@WithMockUser(username = "user", authorities = { "existingAuthority" })
	public void giveUserEntityPermissions()
	{
		String id0 = "entityTypeId0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(id0).getMock();
		String id1 = "entityTypeId1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(id1).getMock();

		User user = mock(User.class);
		when(userService.getUser("user")).thenReturn(user);
		when(userAuthorityFactory.create()).thenAnswer(invocation -> mock(UserAuthority.class));

		Collection<? extends GrantedAuthority> authorities = asList(
				new SimpleGrantedAuthority("ROLE_ENTITY_WRITEMETA_" + id0),
				new SimpleGrantedAuthority("ROLE_ENTITY_WRITEMETA_" + id1));
		when(roleHierarchy.getReachableGrantedAuthorities(authorities)).thenAnswer(
				invocation -> asList(new SimpleGrantedAuthority("newAuthority0"),
						new SimpleGrantedAuthority("newAuthority1")));

		permissionSystemService.giveUserWriteMetaPermissions(asList(entityType0, entityType1));

		String prefix = "ROLE_ENTITY";
		verify(dataService).add(eq(USER_AUTHORITY), userAuthorityStreamCaptor.capture());
		List<UserAuthority> userAuthorities = userAuthorityStreamCaptor.getValue().collect(toList());
		assertEquals(userAuthorities.size(), 2);
		verify(userAuthorities.get(0)).setUser(user);
		verify(userAuthorities.get(0)).setRole(prefix + "_WRITEMETA_" + id0);
		verify(userAuthorities.get(1)).setUser(user);
		verify(userAuthorities.get(1)).setRole(prefix + "_WRITEMETA_" + id1);

		Set<String> newAuthorities = SecurityContextHolder.getContext()
														  .getAuthentication()
														  .getAuthorities()
														  .stream()
														  .map(GrantedAuthority::getAuthority)
														  .collect(toSet());
		assertEquals(newAuthorities, Sets.newHashSet("existingAuthority", "newAuthority0", "newAuthority1"));
	}

	@Test
	@WithMockUser(username = "SYSTEM", authorities = { "ROLE_SYSTEM" })
	public void giveUserEntityPermissionsUserSystem()
	{
		giveUserEntityPermissionsUserSystemOrAdmin();
	}

	@Test
	@WithMockUser(username = "user", authorities = { "ROLE_SU" })
	public void giveUserEntityPermissionsUserAdmin()
	{
		giveUserEntityPermissionsUserSystemOrAdmin();
	}

	private void giveUserEntityPermissionsUserSystemOrAdmin()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
		permissionSystemService.giveUserWriteMetaPermissions(Collections.singleton(entityType));
		verifyZeroInteractions(dataService);
	}

	@Configuration
	public static class Config
	{
		@Mock
		private UserService userService;

		@Mock
		private UserAuthorityFactory userAuthorityFactory;

		@Mock
		private RoleHierarchy roleHierarchy;

		@Mock
		private DataService dataService;

		public Config()
		{
			MockitoAnnotations.initMocks(this);
		}

		@Bean
		public UserService userService()
		{
			return userService;
		}

		@Bean
		public UserAuthorityFactory userAuthorityFactory()
		{
			return userAuthorityFactory;
		}

		@Bean
		public RoleHierarchy roleHierarchy()
		{
			return roleHierarchy;
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return new PermissionSystemServiceImpl(userService(), userAuthorityFactory(), roleHierarchy(),
					dataService());
		}

		void resetMocks()
		{
			reset(userService, userAuthorityFactory, roleHierarchy, dataService);
		}
	}
}