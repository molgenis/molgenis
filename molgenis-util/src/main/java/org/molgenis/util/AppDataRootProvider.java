package org.molgenis.util;

import java.nio.file.Path;

/**
 * Contains the application data root path, typically ~/.molgenis/.
 */
public class AppDataRootProvider
{
	private static Path APP_DATA_ROOT;

	private AppDataRootProvider()
	{
	}

	/**
	 * @return application data root path
	 */
	public static Path getAppDataRoot()
	{
		if (APP_DATA_ROOT == null)
		{
			throw new IllegalStateException("Missing required application data root");
		}
		return APP_DATA_ROOT;
	}

	public static void setAppDataRoot(Path appDataRoot)
	{
		APP_DATA_ROOT = appDataRoot;
	}
}
