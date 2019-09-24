package org.molgenis.core.ui.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.core.ui.settings.StaticContentMetadata.STATIC_CONTENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.core.ui.settings.StaticContent;
import org.molgenis.core.ui.settings.StaticContentFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class StaticContentServiceImplTest extends AbstractMockitoTest {
  @Mock private StaticContentFactory staticContentFactory;
  @Mock private DataService dataService;
  @Mock private StaticContent staticContent;
  @Mock private UserPermissionEvaluator permissionService;

  private StaticContentServiceImpl staticContentService;

  @BeforeEach
  void beforeMethod() {
    staticContentService =
        new StaticContentServiceImpl(dataService, staticContentFactory, permissionService);
  }

  @Test
  void getContent() {
    when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class))
        .thenReturn(staticContent);
    when(staticContent.getContent()).thenReturn("<p>Welcome to Molgenis!</p>");
    assertEquals("<p>Welcome to Molgenis!</p>", staticContentService.getContent("home"));
  }

  @Test
  void isCurrentUserCanEditAnonymousFalse() {
    doReturn(false)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
    assertFalse(staticContentService.isCurrentUserCanEdit("home"));
  }

  @Test
  void isCurrentUserCanEditStaticContentPresentEditTrue() {
    doReturn(true)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
    when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class))
        .thenReturn(staticContent);
    doReturn(true)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.UPDATE_DATA);
    assertTrue(staticContentService.isCurrentUserCanEdit("home"));
  }

  @Test
  void isCurrentUserCanEditStaticContentNotPresentEditTrue() {
    doReturn(true)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
    doReturn(true)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.ADD_DATA);
    assertTrue(staticContentService.isCurrentUserCanEdit("home"));
  }

  @Test
  void isCurrentUserCanEditStaticContentPresentEditFalse() {
    doReturn(true)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.READ_DATA);
    when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class))
        .thenReturn(staticContent);
    assertFalse(staticContentService.isCurrentUserCanEdit("home"));
  }

  @Test
  void isCurrentUserCanEditStaticContentNotPresentEditFalse() {
    assertFalse(staticContentService.isCurrentUserCanEdit("home"));
  }

  @Test
  void checkPermissionsThrowsException() {
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> this.staticContentService.checkPermissions("home"));
    assertThat(exception.getMessage())
        .containsPattern("permission:UPDATE_DATA entityTypeId:sys_StaticContent");
  }

  @Test
  void checkPermissionsNoException() {
    doReturn(true)
        .when(permissionService)
        .hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.UPDATE_DATA);
    staticContentService.checkPermissions("home");
  }

  @Test
  void submitContentNoContentNoCreatePermissions() {
    doReturn(staticContent).when(staticContentFactory).create("home");
    doThrow(new EntityTypePermissionDeniedException(EntityTypePermission.ADD_DATA, STATIC_CONTENT))
        .when(dataService)
        .add(STATIC_CONTENT, staticContent);
    assertFalse(staticContentService.submitContent("home", "<p>Updated Content!</p>"));
  }

  @Test
  void submitContentExistingContentNoUpdatePermissions() {
    when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class))
        .thenReturn(staticContent);
    doThrow(
            new EntityTypePermissionDeniedException(
                EntityTypePermission.UPDATE_DATA, STATIC_CONTENT))
        .when(dataService)
        .update(STATIC_CONTENT, staticContent);
    assertFalse(staticContentService.submitContent("home", "<p>Updated Content!</p>"));
  }

  @Test
  void submitContentExisting() {
    when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class))
        .thenReturn(staticContent);
    assertTrue(this.staticContentService.submitContent("home", "<p>Updated Content!</p>"));

    verify(staticContent).setContent("<p>Updated Content!</p>");
    verify(dataService).update(STATIC_CONTENT, staticContent);
  }

  @Test
  void submitContentNew() {
    when(dataService.findOneById(STATIC_CONTENT, "home", StaticContent.class)).thenReturn(null);
    when(staticContentFactory.create("home")).thenReturn(staticContent);

    assertTrue(this.staticContentService.submitContent("home", "<p>New Content!</p>"));

    verify(staticContent).setContent("<p>New Content!</p>");
    verify(dataService).add(STATIC_CONTENT, staticContent);
  }
}
