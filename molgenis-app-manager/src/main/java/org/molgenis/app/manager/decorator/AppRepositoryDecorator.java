package org.molgenis.app.manager.decorator;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;

public class AppRepositoryDecorator extends AbstractRepositoryDecorator<App> {

  private final Repository<App> delegateRepository;
  private AppManagerService appManagerService;

  public AppRepositoryDecorator(
      Repository<App> delegateRepository, AppManagerService appManagerService) {
    super(delegateRepository);
    this.appManagerService = requireNonNull(appManagerService);
    this.delegateRepository = requireNonNull(delegateRepository);
  }

  @Override
  public void delete(App entity) {
    deleteById(entity.getId());
  }

  @Override
  public void deleteById(Object id) {
    appManagerService.deleteApp((String) id);
    delegate().deleteById(id);
  }

  @Override
  public void deleteAll() {
    findAll(new QueryImpl<>()).forEach(app -> deleteById(app.getId()));
  }

  @Override
  protected Repository<App> delegate() {
    return delegateRepository;
  }

  @Override
  public void update(App app) {
    App current = findOneById(app.getId());
    if (current != null && (current.isActive() != app.isActive())) {
      if (app.isActive()) {
        appManagerService.activateApp(app);
      } else {
        appManagerService.deactivateApp(app);
      }
    }
    delegate().update(app);
  }

  @Override
  public void update(Stream<App> apps) {
    apps.forEach(this::update);
  }
}
