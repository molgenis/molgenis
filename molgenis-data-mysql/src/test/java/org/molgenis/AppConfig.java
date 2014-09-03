package org.molgenis;

import static org.mockito.Mockito.mock;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.mysql.AttributeMetaDataRepository;
import org.molgenis.data.mysql.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.data.mysql.EntityMetaDataRepository;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.elasticsearch.ElasticSearchService;
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
	public EntityMetaDataRepository entityMetaDataRepository()
	{
		return new EntityMetaDataRepository(dataSource());
	}

	@Bean
	public AttributeMetaDataRepository attributeMetaDataRepository()
	{
		return new AttributeMetaDataRepository(dataSource());
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
	public ElasticSearchService elasticsearchService()
	{
		return mock(ElasticSearchService.class);
	}
}
