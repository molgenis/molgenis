package org.molgenis.security.permission;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.auth.User;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PermissionSystemServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PermissionSystemServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Captor
	private ArgumentCaptor<UserAuthority> userAuthorityStreamCaptor;

	@Autowired
	private Config config;

	@Autowired
	private DataService dataService;

	@Autowired
	private UserAuthorityFactory userAuthorityFactory;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		initMocks(this);
		config.resetMocks();
		permissionSystemService = new PermissionSystemServiceImpl(dataService, userAuthorityFactory);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPermissionSystemService()
	{
		new PermissionSystemServiceImpl(null, null);
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
		when(dataService
				.findOne(UserMetaData.USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, "user"), User.class))
				.thenReturn(user);
		when(userAuthorityFactory.create()).thenAnswer(invocation -> mock(UserAuthority.class));

		permissionSystemService.giveUserEntityPermissions(Stream.of(entityType0, entityType1));

		String prefix = "ROLE_ENTITY";
		verify(dataService, times(2)).add(eq(USER_AUTHORITY), userAuthorityStreamCaptor.capture());
		List<UserAuthority> userAuthorities = userAuthorityStreamCaptor.getAllValues();
		assertEquals(userAuthorities.size(), 2);
		verify(userAuthorities.get(0)).setUser(user);
		verify(userAuthorities.get(0)).setRole(prefix + "_WRITEMETA_" + id0);
		verify(userAuthorities.get(1)).setUser(user);
		verify(userAuthorities.get(1)).setRole(prefix + "_WRITEMETA_" + id1);

		Set<String> newAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.map(GrantedAuthority::getAuthority).collect(toSet());

		assertEquals(newAuthorities,
				Sets.newHashSet("existingAuthority", prefix + "_COUNT_" + id0, prefix + "_READ_" + id0,
						prefix + "_WRITE_" + id0, prefix + "_WRITEMETA_" + id0, prefix + "_COUNT_" + id1,
						prefix + "_READ_" + id1, prefix + "_WRITE_" + id1, prefix + "_WRITEMETA_" + id1));
	}

	@Test
	@WithMockUser(username = "SYSTEM", authorities = { "ROLE_SYSTEM" })
	public void giveUserEntityPermissionsUserSystem()
	{
		giveUserEntityPermissionsUserSystemOrAdmin();
	}

	// This test tests buggy behavior because a superuser has authority ROLE_SU instead of ROLE_ADMIN
	@Test
	@WithMockUser(username = "user", authorities = { "ROLE_ADMIN" })
	public void giveUserEntityPermissionsUserAdmin()
	{
		giveUserEntityPermissionsUserSystemOrAdmin();
	}

	private void giveUserEntityPermissionsUserSystemOrAdmin()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entityTypeId").getMock();
		permissionSystemService.giveUserEntityPermissions(Stream.of(entityType));
		verifyZeroInteractions(dataService);
	}

	@Configuration
	public static class Config
	{
		@Mock
		private DataService dataService;

		@Mock
		private UserAuthorityFactory userAuthorityFactory;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		@Bean
		public UserAuthorityFactory userAuthorityFactory()
		{
			return userAuthorityFactory;
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return new PermissionSystemServiceImpl(dataService(), userAuthorityFactory());
		}

		void resetMocks()
		{
			reset(dataService, userAuthorityFactory);
		}
	}
}