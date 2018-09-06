package org.molgenis.core.ui;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.molgenis.util.AppDataRootProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures and validates the application data root path: 1) Use java system property
 * 'molgenis.home' 2) Use environment variable 'molgenis.home' 3) Use .molgenis folder in user home
 * directory
 */
public class AppDataRootInitializer {
  private static final Logger LOG = LoggerFactory.getLogger(AppDataRootInitializer.class);

  private static final String MOLGENIS_HOME = "molgenis.home";

  private AppDataRootInitializer() {}

  public static void init() throws IOException {
    Path appDataRoot = getAppDataRoot();
    if (!appDataRoot.toFile().exists()) {
      LOG.info("Creating application data root directory: {}", appDataRoot);
      Files.createDirectory(appDataRoot);
    } else {
      if (!appDataRoot.toFile().isDirectory()) {
        throw new IOException(
            format("Application data root path '%s' is not a directory", appDataRoot));
      }
      if (!Files.isWritable(appDataRoot)) {
        throw new IOException(
            format("Application data root directory '%s' is not writable", appDataRoot));
      }
    }
    LOG.info("Application data directory: {}", appDataRoot);
    AppDataRootProvider.setAppDataRoot(appDataRoot);
  }

  private static Path getAppDataRoot() {
    LOG.debug("Trying to retrieve app data root from Java system property '{}'", MOLGENIS_HOME);
    String appDataRootPathname = System.getProperty(MOLGENIS_HOME);
    if (appDataRootPathname == null) {
      LOG.debug("Trying to retrieve app data root from environment variable '{}'", MOLGENIS_HOME);
      appDataRootPathname = System.getenv(MOLGENIS_HOME);
      if (appDataRootPathname == null) {
        LOG.debug("Trying to retrieve app data root using user home directory");
        String userHome = System.getProperty("user.home");
        if (!userHome.endsWith(File.separator)) {
          userHome = userHome + File.separator;
        }
        appDataRootPathname = userHome + ".molgenis";
      }
    }
    return Paths.get(appDataRootPathname);
  }
}
