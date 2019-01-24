package org.molgenis.dataexplorer.decorator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.dataexplorer.EntityTypeReportPermission.MANAGE_REPORT;
import static org.molgenis.dataexplorer.EntityTypeReportPermission.VIEW_REPORT;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.data.decorator.PermissionChecker;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.dataexplorer.EntityTypeReportPermission;
import org.molgenis.security.core.UserPermissionEvaluator;

public class EntityReportPermissionChecker implements PermissionChecker<FreemarkerTemplate> {
  private static final Pattern ENTITY_REPORT_PATTERN =
      Pattern.compile("view-entityreport-specific-(.+)\\.ftl");

  private final UserPermissionEvaluator permissionEvaluator;

  EntityReportPermissionChecker(UserPermissionEvaluator permissionEvaluator) {
    this.permissionEvaluator = requireNonNull(permissionEvaluator);
  }

  @Override
  public boolean isAddAllowed(FreemarkerTemplate freemarkerTemplate) {
    return hasEntityTypeReportPermission(freemarkerTemplate, MANAGE_REPORT);
  }

  @Override
  public boolean isCountAllowed(FreemarkerTemplate freemarkerTemplate) {
    return isReadAllowed(freemarkerTemplate);
  }

  @Override
  public boolean isReadAllowed(FreemarkerTemplate freemarkerTemplate) {
    return hasEntityTypeReportPermission(freemarkerTemplate, VIEW_REPORT);
  }

  @Override
  public boolean isUpdateAllowed(FreemarkerTemplate freemarkerTemplate) {
    return hasEntityTypeReportPermission(freemarkerTemplate, MANAGE_REPORT);
  }

  @Override
  public boolean isDeleteAllowed(FreemarkerTemplate freemarkerTemplate) {
    return hasEntityTypeReportPermission(freemarkerTemplate, MANAGE_REPORT);
  }

  /** @return true or false if the given Freemarker template is a report template, true otherwise */
  private boolean hasEntityTypeReportPermission(
      FreemarkerTemplate freemarkerTemplate, EntityTypeReportPermission permission) {
    return getEntityTypeId(freemarkerTemplate.getName())
        .map(EntityTypeIdentity::new)
        .map(id -> permissionEvaluator.hasPermission(id, permission))
        .orElse(true);
  }

  private static Optional<String> getEntityTypeId(String templateName) {
    Matcher m = ENTITY_REPORT_PATTERN.matcher(templateName);
    if (m.matches()) {
      return Optional.of(m.group(1));
    }
    return Optional.empty();
  }
}
