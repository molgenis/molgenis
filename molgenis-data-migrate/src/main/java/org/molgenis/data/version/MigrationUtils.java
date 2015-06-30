package org.molgenis.data.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utilities for the migration steps that need to access molgenis-server.properties
 */
public class MigrationUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisVersionService.class);

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
				StandardCharsets.UTF_8))
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
				throw new IllegalArgumentException(String.format("missing required java system property '%s'",
						MOLGENIS_HOME_KEY));
			}
			propertiesFile = new File(molgenisHomeDir, "molgenis-server.properties");
		}

		return propertiesFile;
	}
}
