package org.molgenis.dataexplorer.decorator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.dataexplorer.EntityTypeReportPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityReportPermissionCheckerTest extends AbstractMockitoTest {
  @Mock private UserPermissionEvaluator permissionEvaluator;

  private EntityReportPermissionChecker entityReportPermissionChecker;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityReportPermissionChecker = new EntityReportPermissionChecker(permissionEvaluator);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testEntityReportPermissionChecker() {
    new EntityReportPermissionChecker(null);
  }

  @Test
  public void testIsAddAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.MANAGE_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isAddAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsAddAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isAddAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsAddNotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isAddAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsCountAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.VIEW_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isCountAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsCountAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isCountAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsCountAllowedNotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isCountAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsReadAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.VIEW_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isReadAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsReadAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isReadAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsReadAllowedotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isReadAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsUpdateAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.MANAGE_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isUpdateAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsUpdateAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isUpdateAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsUpdateAllowedotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isUpdateAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsDeleteAllowedTrue() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    when(permissionEvaluator.hasPermission(
            new EntityTypeIdentity("MyEntityType"), EntityTypeReportPermission.MANAGE_REPORT))
        .thenReturn(true);
    assertTrue(entityReportPermissionChecker.isDeleteAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsDeleteAllowedFalse() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("view-entityreport-specific-MyEntityType.ftl");
    assertFalse(entityReportPermissionChecker.isDeleteAllowed(freemarkerTemplate));
  }

  @Test
  public void testIsDeleteAllowedotAnEntityReport() {
    FreemarkerTemplate freemarkerTemplate = mock(FreemarkerTemplate.class);
    when(freemarkerTemplate.getName()).thenReturn("not-an-entity-report.ftl");
    assertTrue(entityReportPermissionChecker.isDeleteAllowed(freemarkerTemplate));
  }
}
