package org.molgenis.app.manager.decorator;

import static java.util.Objects.requireNonNull;

import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Component;

@Component
public class AppsRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<App, AppMetadata> {
  private final AppManagerService appManagerService;

  public AppsRepositoryDecoratorFactory(
      AppMetadata appMetadata, AppManagerService appManagerService) {
    super(appMetadata);
    this.appManagerService = requireNonNull(appManagerService);
  }

  @Override
  public Repository<App> createDecoratedRepository(Repository<App> repository) {
    return new AppRepositoryDecorator(repository, appManagerService);
  }
}
