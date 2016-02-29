package org.molgenis;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.JobExecutionUpdaterImpl;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class DatabaseConfig implements TransactionManagementConfigurer
{
	@Value("${db_driver:com.mysql.jdbc.Driver}")
	private String dbDriverClass;
	@Value("${db_uri:@null}")
	private String dbJdbcUri;
	@Value("${db_user:@null}")
	private String dbUser;
	@Value("${db_password:@null}")
	private String dbPassword;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Bean
	public DataSource dataSource()
	{
		if(dbDriverClass == null) throw new IllegalArgumentException("db_driver is null");
		if(dbJdbcUri == null) throw new IllegalArgumentException("db_uri is null");
		if(dbUser == null) throw new IllegalArgumentException("please configure the db_user property in your molgenis-server.properties");
		if(dbPassword == null) throw new IllegalArgumentException("please configure the db_password property in your molgenis-server.properties");
		
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try
		{
			dataSource.setDriverClass(dbDriverClass);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException(e);
		}
		dataSource
				.setJdbcUrl(dbJdbcUri);
		dataSource.setUser(dbUser);
		dataSource.setPassword(dbPassword);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxPoolSize(150);
		dataSource.setTestConnectionOnCheckin(true);
		dataSource.setIdleConnectionTestPeriod(120);
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new MolgenisTransactionManager(idGenerator, dataSource());
	}

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager()
	{
		return transactionManager();
	}

	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl();
	}
}
