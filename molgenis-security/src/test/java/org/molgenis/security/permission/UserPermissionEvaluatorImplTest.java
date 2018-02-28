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

@ContextConfiguration(classes = { UserPermissionEvaluatorImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class UserPermissionEvaluatorImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private PermissionEvaluator permissionEvaluator;

	private UserPermissionEvaluatorImpl userPermissionEvaluator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		userPermissionEvaluator = new UserPermissionEvaluatorImpl(permissionEvaluator);
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnEntityTypeTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionEvaluator.hasPermission(authentication, "entityType0", "entityType",
				EntityTypePermission.READ)).thenReturn(true);
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.READ));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnEntityTypeFalse()
	{
		assertFalse(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.READ));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnEntityTypeSuperuser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.WRITE));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnEntityTypeSystemUser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.WRITE));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionEvaluator.hasPermission(authentication, "plugin1", "plugin", PluginPermission.READ)).thenReturn(
				true);
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginFalse()
	{
		assertFalse(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnPluginSuperuser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnPluginSystemUser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ));
	}

	static class Config
	{
	}
}
