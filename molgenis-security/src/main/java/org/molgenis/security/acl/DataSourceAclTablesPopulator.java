package org.molgenis.security.acl;

import org.molgenis.util.ResourceUtils;
import org.molgenis.util.UncheckedSqlException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@Component
public class DataSourceAclTablesPopulator
{
	private final DataSource dataSource;

	public DataSourceAclTablesPopulator(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	public void populate()
	{
		// resources based on PostgreSQL ACL schema in Spring ACL artifact JAR
		List<String> resourceNames = new ArrayList<>(4);
		resourceNames.add("/sql/create_table_acl_sid.sql");
		resourceNames.add("/sql/create_table_acl_class.sql");
		resourceNames.add("/sql/create_table_acl_object_identity.sql");
		resourceNames.add("/sql/create_table_acl_entry.sql");

		Connection connection = DataSourceUtils.getConnection(dataSource);
		try
		{
			resourceNames.forEach(resourceName -> createTable(connection, resourceName));
		}
		finally
		{
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	private void createTable(Connection connection, String resourceName)
	{
		try (Statement statement = connection.createStatement())
		{
			String sql = ResourceUtils.getString(getClass(), resourceName, UTF_8);
			statement.execute(sql);
		}
		catch (SQLException e)
		{
			throw new UncheckedSqlException(e);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
