package org.molgenis.data.migrate.version;

import org.molgenis.util.UncheckedSqlException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.Objects.requireNonNull;

@Service
public class MolgenisVersionService
{
	/**
	 * package-private for testability
	 */
	static final int VERSION = 31;

	private final DataSource dataSource;

	public MolgenisVersionService(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	@PostConstruct
	public void init()
	{
		if (!versionTableExist())
		{
			try (Connection connection = dataSource.getConnection())
			{
				createVersionTable(connection);
				createVersionTableRow(connection);
			}
			catch (SQLException e)
			{
				throw new UncheckedSqlException(e);
			}
		}
	}

	private boolean versionTableExist()
	{
		try
		{
			return (boolean) JdbcUtils.extractDatabaseMetaData(dataSource, dbmd ->
			{
				ResultSet tables = dbmd.getTables(null, null, "Version", new String[] { "TABLE" });
				return tables.first();
			});
		}
		catch (MetaDataAccessException e)
		{
			return false;
		}
	}

	private void createVersionTable(Connection connection) throws SQLException
	{
		try (Statement statement = connection.createStatement())
		{
			String createVersionTableSql = "CREATE TABLE \"Version\" (\"id\" integer PRIMARY KEY)";
			statement.execute(createVersionTableSql);
		}
	}

	private void createVersionTableRow(Connection connection) throws SQLException
	{
		try (Statement statement = connection.createStatement())
		{
			String insertVersionRow = "INSERT INTO \"Version\" (\"id\") VALUES(" + MolgenisVersionService.VERSION + ")";
			statement.execute(insertVersionRow);
		}
	}

	int getAppVersion()
	{
		return VERSION;
	}

	int getSchemaVersion()
	{
		int version;
		try (Connection connection = dataSource.getConnection())
		{
			try (Statement statement = connection.createStatement())
			{
				String selectVersionSql = "SELECT \"id\" FROM \"Version\"";
				try (ResultSet resultSet = statement.executeQuery(selectVersionSql))
				{
					if (resultSet.next())
					{
						version = resultSet.getInt("id");
					}
					else
					{
						throw new SQLException("Expected non-empty result set");
					}
				}
			}
		}
		catch (SQLException e)
		{
			throw new UncheckedSqlException(e);
		}
		return version;
	}

	void setSchemaVersion(int version)
	{
		try (Connection connection = dataSource.getConnection())
		{
			try (Statement statement = connection.createStatement())
			{
				String updateVersionSql = "UPDATE \"Version\" SET \"id\"=" + version;
				statement.execute(updateVersionSql);
			}
		}
		catch (SQLException e)
		{
			throw new UncheckedSqlException(e);
		}
	}
}
