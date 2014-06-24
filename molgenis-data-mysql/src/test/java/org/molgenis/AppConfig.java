package org.molgenis;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
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

	@Bean
	public DataService dataService()
	{
		return new DataServiceImpl();
	}

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataSource(), new DefaultEntityValidator(dataService(),
				new EntityAttributesValidator()));
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		return new MysqlRepositoryCollection(dataSource(), dataService())
		{
			@Override
			protected MysqlRepository createMysqlRepsitory()
			{
				MysqlRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);

				return repo;
			}

		};
	}
}
