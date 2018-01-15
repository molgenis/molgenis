package org.molgenis.data.migrate.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Get the Molgenis version from molgenis-server.properties or, in absence there, from a {@link DataSource}.
 * <p>
 * The current Molgenis version is found in the server properties under key <code>molgenis.version</code>.
 * <p>
 * <p>
 * If this key is not present, we're either looking at a molgenis 1.4.3 or a new install should be run. We'll check the
 * datasource for the presence of a mysql entities table. If no <code>MolgenisUser</code> table exists in the
 * datasource's database, a new install is assumed, so the version will be set to the current version and no upgrade
 * will take place.
 * </p>
 * <p>
 * <p>
 * This is done so we can upgrade the database. If we store it in the database we must access the database to get it but
 * we must upgrade before we can access the database...
 * </p>
 */
@Service
public class MolgenisVersionService
{
	public static final int CURRENT_VERSION = 31;

	private static final Logger LOG = LoggerFactory.getLogger(MolgenisVersionService.class);

	public MolgenisVersionService(DataSource dataSource)
	{
		if (MigrationUtils.getVersion() == null)
		{
			LOG.warn("No {} property found in molgenis-server.properties.", MigrationUtils.VERSION_KEY);
			if (isPopulatedDatabase(dataSource))
			{
				LOG.info("Database is populated. Setting molgenis-server.properties to 0. (Molgenis 1.4.3)");
				updateToVersion(0);
			}
			else
			{
				LOG.info("Database is empty. Setting molgenis-server.properties to current version. (Clean install)");
				updateToCurrentVersion();
			}
		}
	}

	/**
	 * Checks if this is a populated database.
	 */
	private boolean isPopulatedDatabase(DataSource dataSource)
	{
		if (dataSource == null)
		{
			LOG.warn("No datasource found");
			return false;
		}
		try
		{
			return (boolean) JdbcUtils.extractDatabaseMetaData(dataSource, dbmd ->
			{
				ResultSet tables = dbmd.getTables(null, null, "MolgenisUser", new String[] { "TABLE" });
				boolean resultRow = tables.first();
				LOG.info("Table MolgenisUser {}found.", resultRow ? "" : "not ");
				return resultRow;
			});
		}
		catch (MetaDataAccessException e)
		{
			return false;
		}
	}

	/**
	 * Get the molgenis version from the molgenis server properties.
	 */
	public int getMolgenisVersionFromServerProperties()
	{
		return Integer.parseInt(MigrationUtils.getVersion());
	}

	public void updateToCurrentVersion()
	{
		updateToVersion(CURRENT_VERSION);
	}

	public void updateToVersion(int version)
	{
		Properties properties = MigrationUtils.getMolgenisServerProperties();
		properties.setProperty(MigrationUtils.VERSION_KEY, Integer.toString(version));

		try (OutputStream out = new FileOutputStream(MigrationUtils.getMolgenisServerPropertiesFile()))
		{
			properties.store(out, "Molgenis server properties");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
