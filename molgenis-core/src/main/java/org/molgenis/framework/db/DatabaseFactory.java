package org.molgenis.framework.db;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @depricated
 * 
 *             This class is depricated, please use DatabaseUtil.getDatabase().
 */
public class DatabaseFactory
{
	private static Logger logger = Logger.getLogger(DatabaseFactory.class);
	private static ThreadLocal<Database> databaseThreadLocal = new ThreadLocal<Database>();

	public static void create(Database database) throws Exception
	{
		if (databaseThreadLocal.get() != null)
		{
			throw new RuntimeException("Database already created use get() to get a reference to it.");
		}

		databaseThreadLocal.set(database);
	}

	public static Database get()
	{
		return databaseThreadLocal.get();
	}

	public static void destroy()
	{
		try
		{
			get().close();
		}
		catch (IOException e)
		{
			logger.error("Exception closing database", e);
		}

		databaseThreadLocal.remove();
	}

}
