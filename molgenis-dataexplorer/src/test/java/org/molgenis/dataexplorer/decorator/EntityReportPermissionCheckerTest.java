package org.molgenis.dataexplorer.decorator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.dataexplorer.EntityTypeReportPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class EntityReportPermissionCheckerTest extends AbstractMockitoTest {
  @Mock private UserPermissionEvaluator permissionEvaluator;

  private EntityReportPermissionChecker entityReportPermissionChecker;

  @BeforeEach
  void setUpBeforeMethod() {
    entityReportPermissionChecker = new EntityReportPermissionChecker(permissionEvaluator);
  }

  @Test
  void testEntityReportPermissionChecker() {
    assertThrows(NullPointerException.class, () -> new EntityReportPermissionChecker(null));
  }

  @Test
  void testIsAddAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.MANAGE_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isAddAllowed(freemarkerTemplate));
  }

  @Test
  void testIsAddAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isAddAllowed(freemarkerTemplate));
  }

  @Test
  void testIsAddNotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isAddAllowed(freemarkerTemplate));
  }

  @Test
  void testIsCountAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.VIEW_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isCountAllowed(freemarkerTemplate));
  }

  @Test
  void testIsCountAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isCountAllowed(freemarkerTemplate));
  }

  @Test
  void testIsCountAllowedNotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isCountAllowed(freemarkerTemplate));
  }

  @Test
  void testIsReadAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.VIEW_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isReadAllowed(freemarkerTemplate));
  }

  @Test
  void testIsReadAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isReadAllowed(freemarkerTemplate));
  }

  @Test
  void testIsReadAllowedotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isReadAllowed(freemarkerTemplate));
  }

  @Test
  void testIsUpdateAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.MANAGE_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isUpdateAllowed(freemarkerTemplate));
  }

  @Test
  void testIsUpdateAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isUpdateAllowed(freemarkerTemplate));
  }

  @Test
  void testIsUpdateAllowedotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isUpdateAllowed(freemarkerTemplate));
  }

  @Test
  void testIsDeleteAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.MANAGE_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isDeleteAllowed(freemarkerTemplate));
  }

  @Test
  void testIsDeleteAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isDeleteAllowed(freemarkerTemplate));
  }

  @Test
  void testIsDeleteAllowedotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isDeleteAllowed(freemarkerTemplate));
  }
}
