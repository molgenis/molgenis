package org.molgenis.security.permission;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.security.acl.*;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MolgenisPermissionControllerTest
{
	@Mock
	private PermissionService molgenisPermissionService;
	@Mock
	private EntityAclService entityAclService;
	@Mock
	private LanguageService languageService;
	@Mock
	private DataService dataService;
	@Mock
	private EntityAclManager entityAclManager;
	private MolgenisPermissionController molgenisPermissionController;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		molgenisPermissionController = new MolgenisPermissionController(molgenisPermissionService, entityAclService,
				entityAclManager, languageService, dataService);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(molgenisPermissionService, entityAclService, entityAclManager, languageService, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisPermissionController()
	{
		new MolgenisPermissionController(null, null, null, null, null);
	}

	@Test
	public void hasReadPermissionTrue()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntityType(entityTypeId, Permission.READ)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasReadPermissionFalse()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntityType(entityTypeId, Permission.READ)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionTrue()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITE)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasWritePermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionFalse()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntityType(entityTypeId, Permission.WRITE)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasWritePermission(entityTypeId));
	}

	@Test
	public void testSave()
	{
		EntityIdentity homePluginId = EntityIdentity.create("sys_Plugin", "home");
		SecurityId owner = SecurityId.createForUsername("henk");
		SecurityId role = SecurityId.createForAuthority("ROLE_abcde");
		ImmutableSet<Permission> permissions = ImmutableSet.of(Permission.WRITE, Permission.READ);
		EntityAce entityAce = EntityAce.create(permissions, role, true);
		EntityAcl entityAcl = EntityAcl.create(homePluginId, owner, null, singletonList(entityAce));
		molgenisPermissionController.save(entityAcl);
	}
}
