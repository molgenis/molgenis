package org.molgenis.security.permission;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MolgenisPermissionControllerTest
{
	@Mock
	private MolgenisPermissionService molgenisPermissionService;
	@Mock
	private EntityAclService entityAclService;
	@Mock
	private LanguageService languageService;
	@Mock
	private DataService dataService;
	private MolgenisPermissionController molgenisPermissionController;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		molgenisPermissionController = new MolgenisPermissionController(molgenisPermissionService, entityAclService,
				languageService, dataService);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(molgenisPermissionService, entityAclService, languageService, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisPermissionController()
	{
		new MolgenisPermissionController(null, null, null, null);
	}

	@Test
	public void hasReadPermissionTrue()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasReadPermissionFalse()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasReadPermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionTrue()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.WRITE)).thenReturn(true);
		assertTrue(molgenisPermissionController.hasWritePermission(entityTypeId));
	}

	@Test
	public void hasWritePermissionFalse()
	{
		String entityTypeId = "entity";
		when(molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.WRITE)).thenReturn(false);
		assertFalse(molgenisPermissionController.hasWritePermission(entityTypeId));
	}
}
