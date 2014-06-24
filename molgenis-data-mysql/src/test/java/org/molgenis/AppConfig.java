package org.molgenis;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.mysql.EmbeddedMysqlDatabaseBuilder;
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

/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan("org.molgenis.data")
public class AppConfig
{
	@Bean(destroyMethod = "shutdown")
	public DataSource dataSource()
	{
		return new EmbeddedMysqlDatabaseBuilder().build();
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
