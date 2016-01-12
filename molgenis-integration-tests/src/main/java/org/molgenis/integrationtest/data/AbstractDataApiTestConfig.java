package org.molgenis.integrationtest.data;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.molgenis.data.AutoValueRepositoryDecorator;
import org.molgenis.data.ComputedEntityValuesDecorator;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.EntityReferenceResolverDecorator;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositorySecurityDecorator;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.data.transaction.TransactionConfig;
import org.molgenis.data.transaction.TransactionLogRepositoryDecorator;
import org.molgenis.data.transaction.TransactionLogService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.mysql.embed.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.ui.EntityListenerRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.MySqlRepositoryExceptionTranslatorDecorator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.io.Files;

@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(
{ "org.molgenis.data.meta", "org.molgenis.data.elasticsearch.index" })
@Import(
{ EmbeddedElasticSearchConfig.class, ElasticsearchEntityFactory.class, TransactionConfig.class,
		ElasticsearchRepositoryCollection.class, RunAsSystemBeanPostProcessor.class })
public abstract class AbstractDataApiTestConfig
{
	@Autowired
	protected SearchService searchService;

	@Autowired
	private TransactionLogService transactionLogService;

	protected AbstractDataApiTestConfig()
	{
		System.setProperty("molgenis.home", Files.createTempDir().getAbsolutePath());
	}

	@PostConstruct
	public void init()
	{
		dataService().setMeta(metaDataService());
		metaDataService().setDefaultBackend(getBackend());
	}

	protected abstract ManageableRepositoryCollection getBackend();

	@Bean
	public MetaDataService metaDataService()
	{
		return new MetaDataServiceImpl(dataService());
	}

	@Bean
	public LanguageService languageService()
	{
		return new LanguageService(dataService(), appSettings());
	}

	@Bean
	public IdGenerator idGenerator()
	{
		return new UuidGenerator();
	}

	@Bean
	public MolgenisTransactionManager transactionManager()
	{
		return new MolgenisTransactionManager(idGenerator());
	}

	@Bean
	public DataServiceImpl dataService()
	{
		return new DataServiceImpl(repositoryDecoratorFactory());
	}

	@Bean
	public EntityManager entityManager()
	{
		return new EntityManagerImpl(dataService());
	}

	@Bean
	public PermissionSystemService permissionSystemService()
	{
		return new PermissionSystemService(dataService());
	}

	@Bean
	public AppSettings appSettings()
	{
		return new TestAppSettings();
	}

	@Bean
	public EntityAttributesValidator entityAttributesValidator()
	{
		return new EntityAttributesValidator();
	}

	@Bean
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		// Moving this inner class to a separate class results in a FatalBeanException on application startup
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				Repository decoratedRepository = repository;

				// 9. Owned decorator
				if (EntityUtils.doesExtend(decoratedRepository.getEntityMetaData(), OwnedEntityMetaData.ENTITY_NAME))
				{
					decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
				}

				// 8. Entity reference resolver decorator
				decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager());

				// 7. Computed entity values decorator
				decoratedRepository = new ComputedEntityValuesDecorator(decoratedRepository);

				// 6. Entity listener
				decoratedRepository = new EntityListenerRepositoryDecorator(decoratedRepository);

				// 5. Transaction log decorator
				decoratedRepository = new TransactionLogRepositoryDecorator(decoratedRepository, transactionLogService);

				// 4. SQL exception translation decorator
				String backend = decoratedRepository.getEntityMetaData().getBackend();
				if (MysqlRepositoryCollection.NAME.equals(backend))
				{
					decoratedRepository = new MySqlRepositoryExceptionTranslatorDecorator(decoratedRepository);
				}

				// 3. validation decorator
				decoratedRepository = new RepositoryValidationDecorator(dataService(), decoratedRepository,
						entityAttributesValidator());

				// 2. auto value decorator
				decoratedRepository = new AutoValueRepositoryDecorator(decoratedRepository, idGenerator());

				// 1. security decorator
				decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, appSettings());

				return decoratedRepository;
			}
		};
	}

	@Bean(destroyMethod = "shutdown")
	public DataSource dataSource()
	{
		return new EmbeddedMysqlDatabaseBuilder().build();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[]
		{ new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
				new ClassPathResource("/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}

	// ************************************************************************
	// JPA config for MolgenisTransactionManager, remove after JPA is removed
	// ************************************************************************
	@Bean
	public JpaDialect jpaDialect()
	{
		return new EclipseLinkJpaDialect();
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter()
	{
		EclipseLinkJpaVendorAdapter eclipseLinkJpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
		return eclipseLinkJpaVendorAdapter;
	}

	@Bean
	public FactoryBean<EntityManagerFactory> localEntityManagerFactoryBean()
	{
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setPersistenceUnitName("test");
		entityManagerFactoryBean.setDataSource(dataSource());
		entityManagerFactoryBean.setJpaDialect(jpaDialect());
		entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
		entityManagerFactoryBean.setJpaPropertyMap(Collections.singletonMap("eclipselink.weaving", "false"));
		return entityManagerFactoryBean;
	}

	public PlatformTransactionManager annotationDrivenTransactionManager()
	{
		return transactionManager();
	}
}
