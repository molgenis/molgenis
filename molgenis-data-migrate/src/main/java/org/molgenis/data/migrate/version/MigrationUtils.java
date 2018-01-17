package org.molgenis.data.migrate.version;

import java.io.*;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Some utilities for the migration steps that need to access molgenis-server.properties
 */
public class MigrationUtils
{
	public static final String VERSION_KEY = "molgenis.version";
	public static final String DB_KEY = "db_uri";
	public static final String MOLGENIS_HOME_KEY = "molgenis.home";

	static File propertiesFile;

	private static String getServerProperty(String key)
	{
		return getMolgenisServerProperties().getProperty(key);
	}

	public static String getDatabaseName()
	{
		String prop = getServerProperty(DB_KEY);
		if (prop == null)
		{
			return null;
		}

		return prop.substring(prop.lastIndexOf('/') + 1);
	}

	public static String getVersion()
	{
		return getServerProperty(VERSION_KEY);
	}

	public static Properties getMolgenisServerProperties()
	{
		try (InputStreamReader in = new InputStreamReader(new FileInputStream(getMolgenisServerPropertiesFile()),
				UTF_8))
		{
			Properties p = new Properties();
			p.load(in);

			return p;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public static File getMolgenisServerPropertiesFile()
	{
		if (propertiesFile == null || !propertiesFile.exists())
		{
			// get molgenis home directory
			String molgenisHomeDir = System.getProperty(MOLGENIS_HOME_KEY);
			if (molgenisHomeDir == null)
			{
				throw new IllegalArgumentException(
						String.format("missing required java system property '%s'", MOLGENIS_HOME_KEY));
			}
			propertiesFile = new File(molgenisHomeDir, "molgenis-server.properties");
		}

		return propertiesFile;
	}
}
