package org.molgenis.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Can be used by legacy classes to get a reference to the application context
 *
 * @author erwin
 */
// Intended static write from instance
@SuppressWarnings("squid:S2696")
public class ApplicationContextProvider implements ApplicationContextAware {
  private static ApplicationContext ctx = null;

  public static ApplicationContext getApplicationContext() {
    return ctx;
  }

  @Override
  public void setApplicationContext(ApplicationContext ctx) {
    ApplicationContextProvider.ctx = ctx;
  }
}
