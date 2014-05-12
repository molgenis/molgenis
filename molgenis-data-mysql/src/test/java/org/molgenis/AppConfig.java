package org.molgenis;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement
@ComponentScan("org.molgenis.data")
public class AppConfig
{
	private final String dbDriverClass = "com.mysql.jdbc.Driver";
	private final String dbJdbcUri = "jdbc:mysql://localhost:3306/omx?rewriteBatchedStatements=true";
	private final String dbUser = "molgenis";
	private final String dbPassword = "molgenis";

	@Bean
	public DataSource dataSource()
	{
		if (dbDriverClass == null) throw new IllegalArgumentException("db_driver is null");
		if (dbJdbcUri == null) throw new IllegalArgumentException("db_uri is null");
		if (dbUser == null) throw new IllegalArgumentException(
				"please configure the db_user property in your molgenis-server.properties");
		if (dbPassword == null) throw new IllegalArgumentException(
				"please configure the db_password property in your molgenis-server.properties");

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try
		{
			dataSource.setDriverClass(dbDriverClass);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException(e);
		}
		dataSource.setJdbcUrl(dbJdbcUri);
		dataSource.setUser(dbUser);
		dataSource.setPassword(dbPassword);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxPoolSize(200);
		dataSource.setTestConnectionOnCheckin(true);
		dataSource.setIdleConnectionTestPeriod(120);
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new DataSourceTransactionManager(dataSource());
	}
}
