package org.molgenis.dataexplorer.decorator;

import static java.util.Objects.requireNonNull;

import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplateMetaData;
import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.PermissionCheckingDecorator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.stereotype.Component;

@Component
public class EntityReportDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<
        FreemarkerTemplate, FreemarkerTemplateMetaData> {
  private final UserPermissionEvaluator permissionEvaluator;

  public EntityReportDecoratorFactory(
      FreemarkerTemplateMetaData entityType, UserPermissionEvaluator permissionEvaluator) {
    super(entityType);
    this.permissionEvaluator = requireNonNull(permissionEvaluator);
  }

  @Override
  public Repository<FreemarkerTemplate> createDecoratedRepository(
      Repository<FreemarkerTemplate> repository) {
    return new PermissionCheckingDecorator<>(
        repository, new EntityReportPermissionChecker(permissionEvaluator));
  }
}
