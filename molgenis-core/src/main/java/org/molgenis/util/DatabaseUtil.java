package org.molgenis.util;

import org.molgenis.framework.db.Database;

/**
 * Util class for non-spring managed classes to get a reference to a database
 * instance
 * 
 * @author erwin
 * 
 */
public class DatabaseUtil
{
	public static Database getDatabase()
	{
		return ApplicationContextProvider.getApplicationContext().getBean("database", Database.class);
	}
}
