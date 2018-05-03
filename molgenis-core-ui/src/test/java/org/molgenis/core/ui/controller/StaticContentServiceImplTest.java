package org.molgenis.core.ui.controller;

import org.mockito.Mock;
import org.molgenis.core.ui.settings.StaticContent;
import org.molgenis.core.ui.settings.StaticContentFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
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
	public void isCurrentUserCanEditAnonymousFalse()
	{
		doReturn(false).when(permissionService)
					   .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
		assertFalse(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditStaticContentPresentEditTrue()
	{
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.UPDATE_DATA);
		assertTrue(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditStaticContentNotPresentEditTrue()
	{
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.ADD_DATA);
		assertTrue(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditStaticContentPresentEditFalse()
	{
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		assertFalse(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test
	public void isCurrentUserCanEditStaticContentNotPresentEditFalse()
	{
		assertFalse(staticContentService.isCurrentUserCanEdit("home"));
	}

	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:UPDATE_DATA entityTypeId:sys_StaticContent")
	public void checkPermissionsThrowsException()
	{
		this.staticContentService.checkPermissions("home");
	}

	@Test
	public void checkPermissionsNoException()
	{
		doReturn(true).when(permissionService)
					  .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.UPDATE_DATA);
		staticContentService.checkPermissions("home");
	}

	@Test
	public void submitContentNoContentNoCreatePermissions()
	{
		doReturn(staticContent).when(staticContentFactory).create("home");
		doThrow(new EntityTypePermissionDeniedException(EntityTypePermission.ADD_DATA, STATIC_CONTENT)).when(
				dataService).add(STATIC_CONTENT,
																										 staticContent);
		assertFalse(staticContentService.submitContent("home", "<p>Updated Content!</p>"));
	}

	@Test
	public void submitContentExistingContentNoUpdatePermissions()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		doThrow(new EntityTypePermissionDeniedException(EntityTypePermission.UPDATE_DATA, STATIC_CONTENT)).when(
				dataService).update(STATIC_CONTENT,
																											staticContent);
		assertFalse(staticContentService.submitContent("home", "<p>Updated Content!</p>"));
	}

	@Test
	public void submitContentExisting()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(staticContent);
		assertTrue(this.staticContentService.submitContent("home", "<p>Updated Content!</p>"));

		verify(staticContent).setContent("<p>Updated Content!</p>");
		verify(dataService).update(STATIC_CONTENT, staticContent);
	}

	@Test
	public void submitContentNew()
	{
		when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(null);
		when(staticContentFactory.create("home")).thenReturn(staticContent);

		assertTrue(this.staticContentService.submitContent("home", "<p>New Content!</p>"));

		verify(staticContent).setContent("<p>New Content!</p>");
		verify(dataService).add(STATIC_CONTENT, staticContent);
	}
}