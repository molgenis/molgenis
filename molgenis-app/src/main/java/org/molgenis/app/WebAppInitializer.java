package org.molgenis.app;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.molgenis.core.ui.MolgenisWebAppInitializer;
import org.springframework.web.WebApplicationInitializer;

public class WebAppInitializer extends MolgenisWebAppInitializer
    implements WebApplicationInitializer {
  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext, WebAppConfig.class, 150);
  }
}
