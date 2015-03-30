package org.molgenis.data.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import org.springframework.stereotype.Service;

/**
 * Get the MetaData version the database is built with and the current MetaData version
 * 
 * The version is stored in the molgenis-server.properties with key 'meta.data.version'. If this key is not present a
 * new install is assumed, so it will be set to the current version and no upgrade will take place.
 * 
 * This is done so we can upgrade the database. If we store it in the database we must access the database to get it but
 * we must upgrade before we can access the database...
 */
@Service
public class MetaDataVersionService
{
	public static final int CURRENT_META_DATA_VERSION = 4;
	private static final String META_DATE_VERSION_KEY = "meta.data.version";

	public MetaDataVersionService()
	{
		if (getMolgenisServerProperties().getProperty(META_DATE_VERSION_KEY) == null)
		{
			updateToCurrentVersion();
		}
	}

	/**
	 * Get the molgenis meta data version where the database is generated with.
	 */
	public int getDatabaseMetaDataVersion()
	{
		return Integer.parseInt(getMolgenisServerProperties().getProperty(META_DATE_VERSION_KEY));
	}

	public void updateToCurrentVersion()
	{
		updateToVersion(CURRENT_META_DATA_VERSION);
	}

	public void updateToVersion(int version)
	{
		Properties properties = getMolgenisServerProperties();
		properties.setProperty(META_DATE_VERSION_KEY, Integer.toString(version));

		try (OutputStream out = new FileOutputStream(getMolgenisServerPropertiesFile()))
		{
			properties.store(out, "Molgenis server properties");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public Properties getMolgenisServerProperties()
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

	private File getMolgenisServerPropertiesFile()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}

		return new File(molgenisHomeDir, "molgenis-server.properties");
	}
}
