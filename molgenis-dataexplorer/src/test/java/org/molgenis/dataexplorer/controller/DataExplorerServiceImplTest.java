package org.molgenis.dataexplorer.controller;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.dataexplorer.EntityTypeReportPermission;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataExplorerServiceImplTest extends AbstractMockitoTest {
  @Mock private DataExplorerSettings dataExplorerSettings;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private DataExplorerServiceImpl dataExplorerServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    dataExplorerServiceImpl =
        new DataExplorerServiceImpl(dataExplorerSettings, userPermissionEvaluator);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testDataExplorerServiceImpl() {
    new DataExplorerServiceImpl(null, null);
  }

  @Test
  public void testGetModulesData() {
    EntityType entityType = getMockEntityType();
    when(dataExplorerSettings.getModData()).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypePermission.READ_DATA))
        .thenReturn(true);
    assertEquals(dataExplorerServiceImpl.getModules(entityType), singletonList(Module.DATA));
  }

  @Test
  public void testGetModulesAggregation() {
    EntityType entityType = getMockEntityType();
    when(dataExplorerSettings.getModAggregates()).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypePermission.AGGREGATE_DATA))
        .thenReturn(true);
    assertEquals(dataExplorerServiceImpl.getModules(entityType), singletonList(Module.AGGREGATION));
  }

  @Test
  public void testGetModulesReport() {
    EntityType entityType = getMockEntityType();
    when(dataExplorerSettings.getModReports()).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypeReportPermission.VIEW_REPORT))
        .thenReturn(true);
    when(dataExplorerSettings.getEntityReport("MyEntityType")).thenReturn("MyEntityReport");
    assertEquals(dataExplorerServiceImpl.getModules(entityType), singletonList(Module.REPORT));
  }

  private EntityType getMockEntityType() {
    return when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
  }
}
