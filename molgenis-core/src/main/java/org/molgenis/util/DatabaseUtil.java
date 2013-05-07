package org.molgenis.util;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.springframework.context.ApplicationContext;

public class DatabaseUtil
{
	private static final Logger logger = Logger.getLogger(DatabaseUtil.class);

	/**
	 * Create new database (requires an ApplicationContext) Important: User is responsible for closing the Database
	 * instance
	 * 
	 * @return
	 */
	public static Database createDatabase()
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null) throw new RuntimeException("missing required application context");
		return applicationContext.getBean("unauthorizedPrototypeDatabase", Database.class);
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
