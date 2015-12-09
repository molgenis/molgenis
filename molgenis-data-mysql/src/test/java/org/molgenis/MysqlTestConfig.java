package org.molgenis;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.mysql.embed.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
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
// @ComponentScan(
// { "org.molgenis.data.mysql", "org.molgenis.data.importer" })
@ComponentScan("org.molgenis.data.meta")
public class MysqlTestConfig
{

	@Bean(destroyMethod = "shutdown")
	public DataSource dataSource()
	{
		return new EmbeddedMysqlDatabaseBuilder().build();
	}

	@PostConstruct
	public void init()
	{
		dataService().setMeta(metaDataService());
		metaDataService().setDefaultBackend(mysqlRepositoryCollection());

		// Login
		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("admin", "admin", "ROLE_SYSTEM"));
	}

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public DataServiceImpl dataService()
	{
		return new DataServiceImpl();
	}

	@Bean
	public EntityManager entityResolver()
	{
		return new EntityManagerImpl(dataService());
	}

	@Bean
	public MySqlEntityFactory mySqlEntityFactory()
	{
		return new MySqlEntityFactory(entityResolver(), dataService());
	}

	@Bean
	public AsyncJdbcTemplate asyncJdbcTemplate()
	{
		return new AsyncJdbcTemplate(new JdbcTemplate(dataSource()));
	}

	@Bean
	@Scope("prototype")
	public MysqlRepository mysqlRepository()
	{
		return new MysqlRepository(dataService(), mySqlEntityFactory(), dataSource(), asyncJdbcTemplate());
	}

	@Bean
	public PermissionSystemService permissionSystemService()
	{
		return new PermissionSystemService(dataService());
	}

	@Bean
	public MetaDataService metaDataService()
	{
		return new MetaDataServiceImpl(dataService());
	}

	@Bean
	public MysqlRepositoryCollection mysqlRepositoryCollection()
	{
		MysqlRepositoryCollection mysqlRepositoryCollection = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return mysqlRepository();
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};

		return mysqlRepositoryCollection;
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisPluginRegistryImpl();
	}
}
