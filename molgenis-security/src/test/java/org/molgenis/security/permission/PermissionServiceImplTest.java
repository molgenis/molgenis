package org.molgenis.security.permission;

import org.mockito.Mock;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
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

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnEntityTypeTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionEvaluator.hasPermission(authentication, "entityType0", "entityType",
				EntityTypePermission.READ)).thenReturn(true);
		assertTrue(permissionService.hasPermission(EntityTypeIdentity.TYPE, "entityType0", EntityTypePermission.READ));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnEntityTypeFalse()
	{
		assertFalse(permissionService.hasPermission(EntityTypeIdentity.TYPE, "entityType0", EntityTypePermission.READ));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnEntityTypeSuperuser()
	{
		assertTrue(permissionService.hasPermission(EntityTypeIdentity.TYPE, "entityType0", EntityTypePermission.WRITE));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnEntityTypeSystemUser()
	{
		assertTrue(permissionService.hasPermission(EntityTypeIdentity.TYPE, "entityType0", EntityTypePermission.WRITE));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionEvaluator.hasPermission(authentication, "plugin1", "plugin", PluginPermission.READ)).thenReturn(
				true);
		assertTrue(permissionService.hasPermission(PluginIdentity.TYPE, "plugin1", PluginPermission.READ));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginFalse()
	{
		assertFalse(permissionService.hasPermission(PluginIdentity.TYPE, "plugin1", PluginPermission.READ));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnPluginSuperuser()
	{
		assertTrue(permissionService.hasPermission(PluginIdentity.TYPE, "plugin1", PluginPermission.WRITE));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnPluginSystemUser()
	{
		assertTrue(permissionService.hasPermission(PluginIdentity.TYPE, "plugin1", PluginPermission.WRITE));
	}

	static class Config
	{
	}
}
