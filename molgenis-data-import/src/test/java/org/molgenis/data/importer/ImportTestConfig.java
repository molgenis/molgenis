package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.semanticsearch.config.SemanticSearchConfig;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.service.impl.SemanticSearchServiceHelper;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.mysql.embed.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.ui.settings.AppDbSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
// @ComponentScan(
// { "org.molgenis.data.mysql", "org.molgenis.data.importer" })
@ComponentScan("org.molgenis.data.meta")
@ContextConfiguration(classes = SemanticSearchConfig.class)
public class ImportTestConfig
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
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("admin", "admin", "ROLE_SYSTEM"));
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
	public EntityManager entityManager()
	{
		return new EntityManagerImpl(dataService());
	}

	@Bean
	public MySqlEntityFactory mySqlEntityFactory()
	{
		return new MySqlEntityFactory(entityManager(), dataService());
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
	public SemanticSearchService semanticSearchService()
	{
		return mock(SemanticSearchService.class);
	}

	@Bean
	public SemanticSearchServiceHelper semanticSearchServiceHelper()
	{
		return mock(SemanticSearchServiceHelper.class);
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisPluginRegistryImpl();
	}

	@Bean
	OntologyService ontologyService()
	{
		return mock(OntologyService.class);
	}

	@Bean
	OntologyRepository ontologyRepository()
	{
		return mock(OntologyRepository.class);
	}

	@Bean
	OntologyTermRepository ontologyTermRepository()
	{
		return mock(OntologyTermRepository.class);
	}

	@Bean
	IdGenerator idGenerator()
	{
		return mock(IdGenerator.class);
	}

	@Bean
	public LanguageService languageService()
	{
		return new LanguageService(dataService(), new AppDbSettings());
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		return new FreeMarkerConfigurer();
	}
}
