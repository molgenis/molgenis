package org.molgenis.data.migrate.version;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;
import org.molgenis.util.AppDataRootProvider;

/** Some utilities for the migration steps that need to access molgenis-server.properties */
public class MigrationUtils {
  public static final String VERSION_KEY = "molgenis.version";
  public static final String DB_KEY = "db_uri";

  static File propertiesFile;

  private MigrationUtils() {}

  private static String getServerProperty(String key) {
    return getMolgenisServerProperties().getProperty(key);
  }

  public static String getDatabaseName() {
    String prop = getServerProperty(DB_KEY);
    if (prop == null) {
      return null;
    }

    return prop.substring(prop.lastIndexOf('/') + 1);
  }

  public static Properties getMolgenisServerProperties() {
    try (InputStreamReader in =
        new InputStreamReader(new FileInputStream(getMolgenisServerPropertiesFile()), UTF_8)) {
      Properties p = new Properties();
      p.load(in);

      return p;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static File getMolgenisServerPropertiesFile() {
    if (propertiesFile == null || !propertiesFile.exists()) {
      // get molgenis home directory
      Path appDataRootPath = AppDataRootProvider.getAppDataRoot();
      propertiesFile = new File(appDataRootPath.toString(), "molgenis-server.properties");
    }

    return propertiesFile;
  }
}
