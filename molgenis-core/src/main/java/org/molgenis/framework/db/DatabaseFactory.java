package org.molgenis.framework.db;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.util.DatabaseUtil;

/**
 * @deprecated
 * 
 *             This class is deprecated, please use DatabaseUtil.getDatabase().
 */
@Deprecated
public class DatabaseFactory
{
	private static final Logger logger = Logger.getLogger(DatabaseFactory.class);

	private static ThreadLocal<Database> databaseThreadLocal;

	@Deprecated
	public static void create(Database database) throws Exception
	{
		// lazy initialization
		if (databaseThreadLocal == null) databaseThreadLocal = new ThreadLocal<Database>();

		if (databaseThreadLocal.get() != null)
		{
			throw new RuntimeException("Database already created use get() to get a reference to it.");
		}

		databaseThreadLocal.set(database);
	}

	public static Database get()
	{
		// delegate to DatabaseUtil unless database was created using this factory class
		return databaseThreadLocal != null ? databaseThreadLocal.get() : DatabaseUtil.getDatabase();
	}

	@Deprecated
	public static void destroy()
	{
		// destroy this database only if this factory class was used to create database
		if (databaseThreadLocal != null)
		{
			try
			{
				Database database = databaseThreadLocal.get();
				if (database != null) database.close();
			}
			catch (IOException e)
			{
				logger.error("Exception closing database", e);
			}

			databaseThreadLocal.remove();
		}
	}
}
