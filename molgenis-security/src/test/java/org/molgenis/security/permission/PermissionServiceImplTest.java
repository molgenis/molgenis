package org.molgenis.security.permission;

import org.mockito.Mock;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.Permission;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { PermissionServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PermissionServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private PermissionEvaluator permissionEvaluator;

	private PermissionServiceImpl permissionService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionService = new PermissionServiceImpl(permissionEvaluator);
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_ENTITY_READ_entity1", "ROLE_ENTITY_WRITE_entity2",
			"ROLE_ENTITY_COUNT_entity3" })
	@Test
	public void hasPermissionOnEntity()
	{
		assertTrue(permissionService.hasPermissionOnEntityType("entity1", Permission.READ));
		assertFalse(permissionService.hasPermissionOnEntityType("entity1", Permission.WRITE));
		assertFalse(permissionService.hasPermissionOnEntityType("entity1", Permission.COUNT));
		assertFalse(permissionService.hasPermissionOnEntityType("entity2", Permission.READ));
		assertTrue(permissionService.hasPermissionOnEntityType("entity2", Permission.WRITE));
		assertFalse(permissionService.hasPermissionOnEntityType("entity2", Permission.COUNT));
		assertFalse(permissionService.hasPermissionOnEntityType("entity3", Permission.READ));
		assertFalse(permissionService.hasPermissionOnEntityType("entity3", Permission.WRITE));
		assertTrue(permissionService.hasPermissionOnEntityType("entity3", Permission.COUNT));
		assertFalse(permissionService.hasPermissionOnEntityType("entity-unknown", Permission.READ));
		assertFalse(permissionService.hasPermissionOnEntityType("entity-unknown", Permission.WRITE));
		assertFalse(permissionService.hasPermissionOnEntityType("entity-unknown", Permission.COUNT));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionEvaluator.hasPermission(authentication, "plugin1", "plugin", PluginPermission.READ)).thenReturn(
				true);
		assertTrue(permissionService.hasPermissionOnPlugin("plugin1", Permission.READ));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginFalse()
	{
		assertFalse(permissionService.hasPermissionOnPlugin("plugin1", Permission.READ));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnPluginSuperuser()
	{
		assertTrue(permissionService.hasPermissionOnPlugin("plugin1", Permission.WRITE));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnPluginSystemUser()
	{
		assertTrue(permissionService.hasPermissionOnPlugin("plugin1", Permission.WRITE));
	}

	static class Config
	{
	}
}
