package org.molgenis.app;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.molgenis.app.controller.HomeController;
import org.molgenis.bootstrap.populate.SystemEntityRegistry;
import org.molgenis.core.ui.settings.StaticContent;
import org.molgenis.core.ui.settings.StaticContentFactory;
import org.molgenis.data.Entity;
import org.springframework.stereotype.Component;

/** Registry of application system entities to be added to an empty database. */
@Component
public class WebAppSystemEntityRegistry implements SystemEntityRegistry {

  private final StaticContentFactory staticContentFactory;

  WebAppSystemEntityRegistry(StaticContentFactory staticContentFactory) {
    this.staticContentFactory = requireNonNull(staticContentFactory);
  }

  @Override
  public Collection<Entity> getEntities() {
    List<Entity> entityList = new ArrayList<>();

    StaticContent staticContentHome = staticContentFactory.create(HomeController.ID);
    staticContentHome.setContent(
        "<div class=\"jumbotron jumbotron-fluid\">\n"
            + "  <div class=\"container\">\n"
            + "    <h1 class=\"display-4\">Flexible software for scientific data</h1>\n"
            + "    <p class=\"lead\">Process, manage, query, annotate, integrate, analyse, share</p>\n"
            + "    <a class=\"btn btn-primary btn-lg\" href=\"https://molgenis.gitbooks.io/molgenis/content/\" "
            + "role=\"button\" target=\"_blank'\">Learn more</a>\n"
            + "  </div>\n"
            + "</div>");
    entityList.add(staticContentHome);

    return entityList;
  }
}
