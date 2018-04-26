package org.molgenis.data.postgresql;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.molgenis.data.config.DataSourceConfig;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.postgresql.transaction.PostgreSqlTransactionManager;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistry;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Import(DataSourceConfig.class)
public class DatabaseConfig implements TransactionManagementConfigurer
{
	/**
	 * Max pool size must be <= the maximum number of connections of configured in the DBMS (e.g. PostgreSQL).
	 * The magic number is based on PostgreSQL default max connections = 100 minus 5 connections for admin tools
	 * communicating with the DBMS.
	 */
	private static final int MAX_POOL_SIZE = 95;

	@Value("${db_driver:org.postgresql.Driver}")
	private String dbDriverClass;
	@Value("${db_uri:@null}")
	private String dbJdbcUri;
	@Value("${db_user:@null}")
	private String dbUser;
	@Value("${db_password:@null}")
	private String dbPassword;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry;

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
		dataSource.setInitialPoolSize(5);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxPoolSize(MAX_POOL_SIZE);
		dataSource.setTestConnectionOnCheckin(true);
		dataSource.setIdleConnectionTestPeriod(120);

		Properties properties = dataSource.getProperties();
		properties.setProperty("reWriteBatchedInserts", "true");
		properties.setProperty("autosave", "CONSERVATIVE");
		dataSource.setProperties(properties);

		return dataSource;
	}

	@Bean
	public TransactionManager transactionManager()
	{
		return new PostgreSqlTransactionManager(idGenerator, dataSource(), transactionExceptionTranslatorRegistry);
	}

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager()
	{
		return transactionManager();
	}
}
