package org.molgenis.integrationtest.platform;

import org.molgenis.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Drops and creates the integration database
 */
class PostgreSqlDatabase
{
	public static String getPostgreSqlDatabaseUri() throws IOException
	{
		Properties properties = new Properties();
		File file = ResourceUtils.getFile(PostgreSqlDatabase.class, "/conf/elasticsearch/molgenis.properties");
		properties.load(new FileInputStream(file));
		return properties.getProperty("db_uri_admin");
	}

	private static Connection getConnection() throws IOException, SQLException
	{
		Properties properties = new Properties();
		File file = ResourceUtils.getFile(PostgreSqlDatabase.class, "/conf/elasticsearch/molgenis.properties");
		properties.load(new FileInputStream(file));

		String dbUriAdmin = getPostgreSqlDatabaseUri();
		String dbUser = properties.getProperty("db_user");
		String dbPassword = properties.getProperty("db_password");
		return DriverManager.getConnection(dbUriAdmin, dbUser, dbPassword);
	}

	static void dropDatabase(String databaseName) throws IOException, SQLException
	{
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		statement.executeUpdate("DROP DATABASE IF exists \"" + databaseName + "\"");
		conn.close();
	}

	static void dropAndCreateDatabase(String databaseName)
	{
		try
		{
			Connection conn = getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("DROP DATABASE IF EXISTS \"" + databaseName + "\"");
			statement.executeUpdate("CREATE DATABASE \"" + databaseName + "\"");

			conn.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
