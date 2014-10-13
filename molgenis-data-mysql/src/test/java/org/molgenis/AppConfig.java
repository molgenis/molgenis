package org.molgenis;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.meta.MetaDataRepositories;
import org.molgenis.data.meta.MetaDataRepositoriesDecorator;
import org.molgenis.data.mysql.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.mysql.meta.MysqlMetaDataRepositories;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private MysqlMetaDataRepositories mysqlMetaDataRepositories;

	@Bean(destroyMethod = "shutdown")
	public DataSource dataSource()
	{
		return new EmbeddedMysqlDatabaseBuilder().build();
	}

	@PostConstruct
	public void login()
	{
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("admin", "admin", "ROLE_SYSTEM"));
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
		return new MysqlRepository(dataSource());
	}

	@Bean
	public PermissionSystemService permissionSystemService()
	{
		return new PermissionSystemService(dataService());
	}

	@Bean
	public MetaDataRepositories metaDataRepositories()
	{
		mysqlMetaDataRepositories = new MysqlMetaDataRepositories(dataSource());
		return metaDataRepositoriesDecorator().decorate(mysqlMetaDataRepositories);
	}

	@Bean
	/**
	 * non-decorating decorator, to be overrided if you wish to decorate the MetaDataRepositories
	 */
	MetaDataRepositoriesDecorator metaDataRepositoriesDecorator()
	{
		return new MetaDataRepositoriesDecorator()
		{
			@Override
			public MetaDataRepositories decorate(MetaDataRepositories metaDataRepositories)
			{
				return metaDataRepositories;
			}
		};
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection(dataSource(),
				dataService(), metaDataRepositories(), repositoryDecoratorFactory())

		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				MysqlRepository repo = mysqlRepository();
				repo.setRepositoryCollection(this);
				return repo;
			}
		};

		mysqlMetaDataRepositories.setRepositoryCollection(mysqlRepositoryCollection);

		return mysqlRepositoryCollection;
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisPluginRegistryImpl();
	}

	// temporary workaround for module dependencies
	@Bean
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				return repository;
			}
		};
	}

}
