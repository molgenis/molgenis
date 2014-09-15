package org.molgenis;

import static org.mockito.Mockito.mock;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
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
	public SearchService searchService()
	{
		return mock(SearchService.class);
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisPluginRegistryImpl();
	}
}
