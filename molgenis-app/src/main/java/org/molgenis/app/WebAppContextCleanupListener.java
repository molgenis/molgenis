package org.molgenis.app;

import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebAppContextCleanupListener implements ServletContextListener {
  private static final Logger LOG = LoggerFactory.getLogger(WebAppContextCleanupListener.class);

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    try {
      org.postgresql.Driver.deregister();
    } catch (SQLException e) {
      LOG.error("deregistering database driver failed", e);
    }
  }
}
