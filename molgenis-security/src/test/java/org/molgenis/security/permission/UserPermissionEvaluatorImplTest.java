package org.molgenis.security.permission;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.molgenis.security.core.PermissionSet.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { UserPermissionEvaluatorImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class UserPermissionEvaluatorImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private PermissionEvaluator permissionEvaluator;
	@Mock
	private PermissionRegistry permissionRegistry;

	private UserPermissionEvaluatorImpl userPermissionEvaluator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		userPermissionEvaluator = new UserPermissionEvaluatorImpl(permissionEvaluator, permissionRegistry);
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionSetOnEntityTypeTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionRegistry.getPermissions(READ_DATA)).thenReturn(ImmutableSet.of(READ));
		when(permissionEvaluator.hasPermission(authentication, "entityType0", "entityType", READ)).thenReturn(true);
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.READ_DATA));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnEntityTypeTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionRegistry.getPermissions(READ_DATA)).thenReturn(ImmutableSet.of(READ, WRITE, WRITEMETA));
		CumulativePermission permissionToCheck = new CumulativePermission().set(READ).set(WRITE).set(WRITEMETA);
		when(permissionEvaluator.hasPermission(authentication, "entityType0", "entityType",
				permissionToCheck)).thenReturn(true);
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"), READ_DATA));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionSetOnEntityTypeFalse()
	{
		assertFalse(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.READ_DATA));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnEntityTypeFalse()
	{
		assertFalse(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"), READ_DATA));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionSetOnEntityTypeSuperuser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.UPDATE_DATA));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnEntityTypeSuperuser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.UPDATE_METADATA));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionSetOnEntityTypeSystemUser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.UPDATE_DATA));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnEntityTypeSystemUser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"),
				EntityTypePermission.ADD_DATA));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionSetOnPluginTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionRegistry.getPermissions(VIEW_PLUGIN)).thenReturn(ImmutableSet.of(READ));
		when(permissionEvaluator.hasPermission(authentication, "plugin1", "plugin", READ)).thenReturn(
				true);
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), PluginPermission.VIEW_PLUGIN));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginTrue()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		when(permissionRegistry.getPermissions(VIEW_PLUGIN)).thenReturn(ImmutableSet.of(READ));
		when(permissionEvaluator.hasPermission(authentication, "plugin1", "plugin",
				new CumulativePermission().set(READ))).thenReturn(true);
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionSetOnPluginFalse()
	{
		assertFalse(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), PluginPermission.VIEW_PLUGIN));
	}

	@WithMockUser(username = "USER")
	@Test
	public void hasPermissionOnPluginFalse()
	{
		assertFalse(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionSetOnPluginSuperuser()
	{
		assertTrue(
				userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), EntityTypePermission.READ_DATA));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void hasPermissionOnPluginSuperuser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionSetOnPluginSystemUser()
	{
		assertTrue(
				userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), EntityTypePermission.READ_DATA));
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void hasPermissionOnPluginSystemUser()
	{
		assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
	}

	static class Config
	{
	}
}
