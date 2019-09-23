package org.molgenis.dataexplorer.controller;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.dataexplorer.controller.Module.AGGREGATION;
import static org.molgenis.dataexplorer.controller.Module.DATA;
import static org.molgenis.dataexplorer.controller.Module.REPORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.dataexplorer.EntityTypeReportPermission;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class DataExplorerServiceImplTest extends AbstractMockitoTest {
  @Mock private DataExplorerSettings dataExplorerSettings;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private DataExplorerServiceImpl dataExplorerServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    dataExplorerServiceImpl =
        new DataExplorerServiceImpl(dataExplorerSettings, userPermissionEvaluator);
  }

  @Test
  void testDataExplorerServiceImpl() {
    assertThrows(NullPointerException.class, () -> new DataExplorerServiceImpl(null, null));
  }

  @Test
  void testGetModulesData() {
    EntityType entityType = getMockEntityType();
    when(dataExplorerSettings.getModData()).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypePermission.READ_DATA))
        .thenReturn(true);
    assertEquals(singletonList(DATA), dataExplorerServiceImpl.getModules(entityType));
  }

  @Test
  void testGetModulesAggregation() {
    EntityType entityType = getMockEntityType();
    when(dataExplorerSettings.getModAggregates()).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypePermission.AGGREGATE_DATA))
        .thenReturn(true);
    assertEquals(singletonList(AGGREGATION), dataExplorerServiceImpl.getModules(entityType));
  }

  @Test
  void testGetModulesReport() {
    EntityType entityType = getMockEntityType();
    when(dataExplorerSettings.getModReports()).thenReturn(true);
    when(userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypeReportPermission.VIEW_REPORT))
        .thenReturn(true);
    when(dataExplorerSettings.getEntityReport("MyEntityType")).thenReturn("MyEntityReport");
    assertEquals(singletonList(REPORT), dataExplorerServiceImpl.getModules(entityType));
  }

  private EntityType getMockEntityType() {
    return when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
  }
}
