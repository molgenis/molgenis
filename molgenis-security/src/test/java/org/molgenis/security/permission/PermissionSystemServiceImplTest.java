package org.molgenis.security.permission;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.service.UserService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = { PermissionSystemServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PermissionSystemServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Autowired
	private Config config;

	@Autowired
	private UserService userService;

	@Autowired
	private DataService dataService;

	@Autowired
	private PrincipalSecurityContextRegistry principalSecurityContextRegistry;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		config.resetMocks();
		permissionSystemService = new PermissionSystemServiceImpl();
	}

	@Test
	@WithMockUser(username = "user", authorities = { "existingAuthority" })
	public void giveUserEntityPermissions()
	{
		String id0 = "entityTypeId0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(id0).getMock();
		String id1 = "entityTypeId1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(id1).getMock();

		permissionSystemService.giveUserWriteMetaPermissions(asList(entityType0, entityType1));
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
		private RoleHierarchy roleHierarchy;

		@Mock
		private DataService dataService;

		@Mock
		private PrincipalSecurityContextRegistry principalSecurityContextRegistry;

		@Mock
		private AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater;

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
		public PrincipalSecurityContextRegistry principalSecurityContextRegistry()
		{
			return principalSecurityContextRegistry;
		}

		@Bean
		public AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater()
		{
			return authenticationAuthoritiesUpdater;
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return new PermissionSystemServiceImpl();
		}

		void resetMocks()
		{
			reset(userService, roleHierarchy, dataService, principalSecurityContextRegistry,
					authenticationAuthoritiesUpdater);
		}
	}
}