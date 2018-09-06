package org.molgenis.util;

import java.nio.file.Path;

/** Contains the application data root path, typically ~/.molgenis/. */
public class AppDataRootProvider {
  private static Path appDataRoot;

  private AppDataRootProvider() {}

  /** @return application data root path */
  public static Path getAppDataRoot() {
    if (appDataRoot == null) {
      throw new IllegalStateException("Missing required application data root");
    }
    return appDataRoot;
  }

  public static void setAppDataRoot(Path appDataRoot) {
    AppDataRootProvider.appDataRoot = appDataRoot;
  }
}
