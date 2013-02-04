package org.molgenis.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.molgenis.MolgenisOptions;

public class ConnectionUtil
{
	public static Connection createConnection(MolgenisOptions options)
	{
		try
		{
			Class.forName(options.db_driver.trim()).newInstance();
			Connection conn = DriverManager.getConnection(options.db_uri.trim(), options.db_user.trim(),
					options.db_password.trim());
			return conn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Connection createConnection(Properties p)
	{
		try
		{
			Class.forName(p.getProperty("db_driver").trim()).newInstance();
			Connection conn = DriverManager.getConnection(p.getProperty("db_uri").trim(), p.getProperty("db_user")
					.trim(), p.getProperty("db_password").trim());
			return conn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
