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
	private static final String INTEGRATION_DATABASE = "molgenis_integration_test";

	private static Connection getConnection() throws IOException, SQLException
	{
		Properties properties = new Properties();
		File file = ResourceUtils.getFile(PostgreSqlDatabase.class, "/postgresql/molgenis.properties");
		properties.load(new FileInputStream(file));

		String db_uri = properties.getProperty("db_uri");
		int slashIndex = db_uri.lastIndexOf('/');

		// remove the, not yet created, database name from the connection url
		String adminDbUri = db_uri.substring(0, slashIndex + 1);
		return DriverManager.getConnection(adminDbUri, "postgres", "umcg2016");
	}

	static void dropDatabase() throws IOException, SQLException
	{
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		statement.executeUpdate("DROP DATABASE IF exists \"" + INTEGRATION_DATABASE + "\"");
		conn.close();
	}

	static void dropAndCreateDatabase()
	{
		try
		{
			Connection conn = getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("DROP DATABASE IF EXISTS \"" + INTEGRATION_DATABASE + "\"");
			statement.executeUpdate("CREATE DATABASE \"" + INTEGRATION_DATABASE + "\"");

			conn.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
