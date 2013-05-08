package org.molgenis.util;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;

public class DatabaseUtil
{
	private static final Logger logger = Logger.getLogger(DatabaseUtil.class);

	public static Database getDatabase()
	{
		return WebAppUtil.getDatabase();
	}

	/**
	 * Create new database (requires an ApplicationContext) Important: User is responsible for closing the Database
	 * instance
	 * 
	 * @return
	 */
	public static Database createDatabase()
	{
		return WebAppUtil.getUnauthorizedPrototypeDatabase();
	}

	/**
	 * Close a database unconditionally
	 * 
	 * @param database
	 */
	public static void closeQuietly(Database database)
	{
		try
		{
			if (database != null) database.close();
		}
		catch (IOException e)
		{
			logger.error(e);
		}
	}
}
