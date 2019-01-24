package org.molgenis.dataexplorer.controller;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.dataexplorer.EntityTypeReportPermission;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Service;

@Service
public class DataExplorerServiceImpl implements DataExplorerService {
  private final DataExplorerSettings dataExplorerSettings;
  private final UserPermissionEvaluator userPermissionEvaluator;

  DataExplorerServiceImpl(
      DataExplorerSettings dataExplorerSettings, UserPermissionEvaluator userPermissionEvaluator) {
    this.dataExplorerSettings = requireNonNull(dataExplorerSettings);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @Override
  public List<Module> getModules(EntityType entityType) {
    List<Module> modules = new ArrayList<>();
    for (Module module : Module.values()) {
      boolean includeModule;
      switch (module) {
        case DATA:
          includeModule = includeDataModule(entityType);
          break;
        case AGGREGATION:
          includeModule = includeAggregationModule(entityType);
          break;
        case REPORT:
          includeModule = includeReportModule(entityType);
          break;
        default:
          throw new UnexpectedEnumException(module);
      }

      if (includeModule) {
        modules.add(module);
      }
    }
    return modules;
  }

  private boolean includeDataModule(EntityType entityType) {
    return dataExplorerSettings.getModData()
        && userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypePermission.READ_DATA);
  }

  private boolean includeAggregationModule(EntityType entityType) {
    return dataExplorerSettings.getModAggregates()
        && userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypePermission.AGGREGATE_DATA);
  }

  private boolean includeReportModule(EntityType entityType) {
    return dataExplorerSettings.getModReports()
        && userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(entityType), EntityTypeReportPermission.VIEW_REPORT)
        && dataExplorerSettings.getEntityReport(entityType.getId()) != null;
  }
}
