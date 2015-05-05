package org.molgenis.data.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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

	public static File propertiesFile;

	private static String getServerProperty(String key)
	{
		return getMolgenisServerProperties().getProperty(key);
	}

	public static String getDatabaseName()
	{
		String prop = getServerProperty(DB_KEY);
		if (prop == null)
		{
			LOG.error("No {} property found in molgenis-server.properties.", DB_KEY);
		}

		return prop.substring(prop.lastIndexOf('/') + 1);
	}

	public static String getVersion()
	{
		return getServerProperty(VERSION_KEY);
	}

	public static Properties getMolgenisServerProperties()
	{
		try (InputStream in = new FileInputStream(getMolgenisServerPropertiesFile()))
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
		if (propertiesFile == null)
		{
			// get molgenis home directory
			String molgenisHomeDir = System.getProperty("molgenis.home");
			if (molgenisHomeDir == null)
			{
				throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
			}
			propertiesFile = new File(molgenisHomeDir, "molgenis-server.properties");
		}

		return propertiesFile;
	}
}
