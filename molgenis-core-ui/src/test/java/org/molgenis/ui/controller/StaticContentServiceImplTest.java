package org.molgenis.ui.controller;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.exception.PluginPermissionDeniedException;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.ui.settings.StaticContent;
import org.molgenis.ui.settings.StaticContentFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.ui.settings.StaticContentMeta.STATIC_CONTENT;
import static org.testng.Assert.*;

public class StaticContentServiceImplTest extends AbstractMockitoTest
{
	@Mock
	private StaticContentFactory staticContentFactory;
	@Mock
	private DataService dataService;
	@Mock
	private StaticContent staticContent;
	@Mock
	private PermissionService permissionService;

	private StaticContentServiceImpl staticContentService;

	@BeforeMethod
	public void beforeMethod()
	{
		staticContentService = new StaticContentServiceImpl(dataService, staticContentFactory, permissionService);
	}

	@Test
	public void getContent()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		when(staticContent.getContent()).thenReturn("<p>Welcome to Molgenis!</p>");
		assertEquals(staticContentService.getContent("home"), "<p>Welcome to Molgenis!</p>");
	}

	@Test
	public void isCurrentUserCanEditTrue()
	{
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(true);
		assertTrue(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditFalse()
	{
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(false);
		assertFalse(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test(expectedExceptions = PluginPermissionDeniedException.class, expectedExceptionsMessageRegExp = "id:home permission:WRITE")
	public void checkPermissionsThrowsException()
	{
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(false);
		this.staticContentService.checkPermissions("home");
	}

	@Test
	public void checkPermissionsNoException()
	{
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(true);
		staticContentService.checkPermissions("home");
	}

	@Test(expectedExceptions = PluginPermissionDeniedException.class, expectedExceptionsMessageRegExp = "id:home permission:WRITE")
	public void submitContentNoPermissions()
	{
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(false);
		this.staticContentService.submitContent("home", "<p>Updated Content!</p>");
	}

	@Test
	public void submitContentExisting()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(true);

		assertTrue(this.staticContentService.submitContent("home", "<p>Updated Content!</p>"));

		verify(staticContent).setContent("<p>Updated Content!</p>");
		verify(dataService).update(STATIC_CONTENT, staticContent);
	}

	@Test
	public void submitContentNew()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(null);
		when(staticContentFactory.create("home")).thenReturn(staticContent);
		when(permissionService.hasPermissionOnPlugin("home", Permission.WRITE)).thenReturn(true);

		assertTrue(this.staticContentService.submitContent("home", "<p>New Content!</p>"));

		verify(staticContent).setContent("<p>New Content!</p>");
		verify(dataService).add(STATIC_CONTENT, staticContent);
	}
}