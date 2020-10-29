package org.molgenis.app;

import javax.servlet.ServletContext;
import org.molgenis.core.ui.MolgenisWebAppInitializer;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.WebApplicationInitializer;

public class WebAppInitializer extends MolgenisWebAppInitializer
    implements WebApplicationInitializer {
  @Override
  public void onStartup(ServletContext servletContext) {
    servletContext.addListener(new WebAppContextCleanupListener());
    servletContext.addListener(HttpSessionEventPublisher.class);
    super.onStartup(servletContext, WebAppConfig.class, 150);
  }
}
