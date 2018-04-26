package org.molgenis.core.ui.controller;

import org.mockito.Mock;
import org.molgenis.core.ui.settings.StaticContent;
import org.molgenis.core.ui.settings.StaticContentFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.core.ui.settings.StaticContentMeta.STATIC_CONTENT;
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
	private UserPermissionEvaluator permissionService;

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
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);
		assertTrue(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditNoPluginPermissionFalse()
	{
		assertFalse(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditNoEntityTypePermissionFalse()
	{
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);
		assertFalse(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No write permission on static content entity type.")
	public void checkPermissionsThrowsException()
	{
		this.staticContentService.checkPermissions("home");
	}

	@Test
	public void checkPermissionsNoException()
	{
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);
		staticContentService.checkPermissions("home");
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No write permission on static content entity type.")
	public void submitContentNoPluginPermissions()
	{
		this.staticContentService.submitContent("home", "<p>Updated Content!</p>");
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "No write permission on static content entity type.")
	public void submitContentNoStaticContentPermissions()
	{
		//		doReturn(true).when(permissionService).hasPermission(new PluginIdentity("home"), PluginPermission.WRITE);
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);
		this.staticContentService.submitContent("home", "<p>Updated Content!</p>");
	}

	@Test
	public void submitContentExisting()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);

		assertTrue(this.staticContentService.submitContent("home", "<p>Updated Content!</p>"));

		verify(staticContent).setContent("<p>Updated Content!</p>");
		verify(dataService).update(STATIC_CONTENT, staticContent);
	}

	@Test
	public void submitContentNew()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(null);
		when(staticContentFactory.create("home")).thenReturn(staticContent);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);

		assertTrue(this.staticContentService.submitContent("home", "<p>New Content!</p>"));

		verify(staticContent).setContent("<p>New Content!</p>");
		verify(dataService).add(STATIC_CONTENT, staticContent);
	}
}