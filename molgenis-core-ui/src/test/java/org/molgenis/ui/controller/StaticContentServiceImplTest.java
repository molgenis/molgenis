package org.molgenis.ui.controller;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.ui.settings.StaticContent;
import org.molgenis.ui.settings.StaticContentFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.molgenis.ui.settings.StaticContentMeta.STATIC_CONTENT;
import static org.testng.Assert.*;

public class StaticContentServiceImplTest
{
	private final String pluginId = "home";
	@Mock
	private DataService dataService;
	@Mock
	private PermissionService permissionService;
	@Mock
	private StaticContent staticContent;
	@Mock
	private StaticContentFactory staticContentFactory;

	@InjectMocks
	private StaticContentServiceImpl staticContentService;

	MockitoSession mockitoSession;

	@BeforeMethod
	public void beforeMethod()
	{
		staticContentService = null;
		mockitoSession = Mockito.mockitoSession().strictness(Strictness.STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void getContent()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		when(staticContent.getContent()).thenReturn("<p>Welcome to Molgenis!</p>").getMock();

		assertEquals(staticContentService.getContent(pluginId), "<p>Welcome to Molgenis!</p>");
	}

	@Test
	public void isCurrentUserCanEdit_HasWritePermission()
	{
		when(permissionService.hasPermissionOnPlugin(pluginId, Permission.WRITE)).thenReturn(true);
		assertTrue(staticContentService.isCurrentUserCanEdit(pluginId));
	}

	@Test
	public void isCurrentUserCanEdit_NoWritePermission()
	{
		when(permissionService.hasPermissionOnPlugin(pluginId, Permission.WRITE)).thenReturn(false);
		assertFalse(staticContentService.isCurrentUserCanEdit(pluginId));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No write permissions on static content page")
	public void checkPermissions_withoutPermissions()
	{
		when(permissionService.hasPermissionOnPlugin(pluginId, Permission.WRITE)).thenReturn(false);
		staticContentService.checkPermissions(pluginId);
	}

	@Test
	public void checkPermissions_withPermissions()
	{
		when(permissionService.hasPermissionOnPlugin(pluginId, Permission.WRITE)).thenReturn(true);
		staticContentService.checkPermissions(pluginId);
	}

	@Test
	public void submitContent()
	{
		when(permissionService.hasPermissionOnPlugin(pluginId, Permission.WRITE)).thenReturn(true);
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);

		assertTrue(this.staticContentService.submitContent(pluginId, "<p>Welcome to Molgenis!</p>"));
	}
}