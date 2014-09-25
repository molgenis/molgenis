package org.molgenis;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.mysql.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.data.mysql.MysqlAttributeMetaDataRepository;
import org.molgenis.data.mysql.MysqlEntityMetaDataRepository;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
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
	public MysqlEntityMetaDataRepository entityMetaDataRepository()
	{
		return new MysqlEntityMetaDataRepository(dataSource());
	}

	@Bean
	public MysqlAttributeMetaDataRepository attributeMetaDataRepository()
	{
		return new MysqlAttributeMetaDataRepository(dataSource());
	}

	@Bean
	public PermissionSystemService permissionSystemService()
	{
		return new PermissionSystemService(dataService());
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		return new MysqlRepositoryCollection(dataSource(), dataService(), entityMetaDataRepository(),
				attributeMetaDataRepository())
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

	// temporary workaround for module dependencies
	@Bean
	public AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory()
	{
		return new AttributeMetaDataRepositoryDecoratorFactory()
		{
			@Override
			public AttributeMetaDataRepository createDecoratedRepository(AttributeMetaDataRepository repository)
			{
				return repository;
			}
		};
	}

	// temporary workaround for module dependencies
	@Bean
	public EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory()
	{
		return new EntityMetaDataRepositoryDecoratorFactory()
		{
			@Override
			public EntityMetaDataRepository createDecoratedRepository(EntityMetaDataRepository repository)
			{
				return repository;
			}
		};
	}
}
