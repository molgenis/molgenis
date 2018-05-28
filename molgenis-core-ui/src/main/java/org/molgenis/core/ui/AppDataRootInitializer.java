package org.molgenis.core.ui;

import org.molgenis.util.AppDataRootProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

/**
 * Configures and validates the application data root path:
 * 1) Use java system property 'molgenis.home'
 * 2) Use environment variable 'molgenis.home'
 * 3) Use .molgenis folder in user home directory
 */
public class AppDataRootInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(AppDataRootInitializer.class);

	private static final String MOLGENIS_HOME = "molgenis.home";

	private AppDataRootInitializer()
	{
	}

	public static void init() throws IOException
	{
		Path appDataRoot = getAppDataRoot();
		if (Files.notExists(appDataRoot))
		{
			LOG.info("Creating application data root directory: {}", appDataRoot.toString());
			Files.createDirectory(appDataRoot);
		}
		else
		{
			if (!Files.isDirectory(appDataRoot))
			{
				throw new IOException(
						format("Application data root path '%s' is not a directory", appDataRoot.toString()));
			}
			if (!Files.isWritable(appDataRoot))
			{
				throw new IOException(
						format("Application data root directory '%s' is not writable", appDataRoot.toString()));
			}
		}
		LOG.info("Application data directory: {}", appDataRoot.toString());
		AppDataRootProvider.setAppDataRoot(appDataRoot);
	}

	private static Path getAppDataRoot()
	{
		LOG.debug("Trying to retrieve app data root from Java system property '{}'", MOLGENIS_HOME);
		String appDataRootPathname = System.getProperty(MOLGENIS_HOME);
		if (appDataRootPathname == null)
		{
			LOG.debug("Trying to retrieve app data root from environment variable '{}'", MOLGENIS_HOME);
			appDataRootPathname = System.getenv(MOLGENIS_HOME);
			if (appDataRootPathname == null)
			{
				LOG.debug("Trying to retrieve app data root using user home directory");
				String userHome = System.getProperty("user.home");
				if (!userHome.endsWith(File.separator))
				{
					userHome = userHome + File.separator;
				}
				appDataRootPathname = userHome + ".molgenis";
			}
		}
		return Paths.get(appDataRootPathname);
	}
}
