package org.molgenis.integrationtest.platform;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.molgenis.data.elasticsearch.client.ElasticsearchConfig;
import org.molgenis.data.jobs.JobConfig;
import org.molgenis.data.jobs.JobExecutionConfig;
import org.molgenis.data.jobs.JobFactoryRegistrar;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.semanticsearch.config.SemanticSearchConfig;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.integrationtest.data.TestAppSettings;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.security.MolgenisRoleHierarchy;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.MolgenisPermissionServiceImpl;
import org.molgenis.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Collection;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
/*
 FIXME Ideally, we'd like to scan all of org.molgenis.data or even org.molgenis, but there's some unwanted dependencies
 in org.molgenis.data and subpackages from included modules
  */
@ComponentScan({ "org.molgenis.data.aggregation", "org.molgenis.data.meta", "org.molgenis.data.index",
		"org.molgenis.js", "org.molgenis.data.elasticsearch", "org.molgenis.auth", "org.molgenis.data.platform",
		"org.molgenis.data.meta.model", "org.molgenis.data.meta.util", "org.molgenis.data.system.model",
		"org.molgenis.data.cache", "org.molgenis.data.i18n", "org.molgenis.data.postgresql", "org.molgenis.file.model",
		"org.molgenis.security.owned", "org.molgenis.security.user", "org.molgenis.data.validation",
		"org.molgenis.data.transaction", "org.molgenis.data.importer.emx", "org.molgenis.data.importer.config",
		"org.molgenis.data.excel", "org.molgenis.util", "org.molgenis.settings", "org.molgenis.data.settings" })
@Import({ TestHarnessConfig.class, EntityBaseTestConfig.class, DatabaseConfig.class, ElasticsearchConfig.class,
		PostgreSqlConfiguration.class, RunAsSystemBeanPostProcessor.class, IdGeneratorImpl.class,
		ExpressionValidator.class, PlatformConfig.class, OntologyTestConfig.class, JobConfig.class,
		org.molgenis.data.RepositoryCollectionRegistry.class,
		org.molgenis.data.RepositoryCollectionDecoratorFactoryImpl.class,
		org.molgenis.data.RepositoryCollectionBootstrapper.class, org.molgenis.data.EntityFactoryRegistrar.class,
		org.molgenis.data.importer.emx.EmxImportService.class, org.molgenis.data.importer.ImportServiceFactory.class,
		org.molgenis.data.FileRepositoryCollectionFactory.class, org.molgenis.data.excel.ExcelDataConfig.class,
		org.molgenis.security.permission.PermissionSystemServiceImpl.class,
		org.molgenis.data.importer.ImportServiceRegistrar.class, EntityTypeRegistryPopulator.class,
		MolgenisPermissionServiceImpl.class, MolgenisRoleHierarchy.class,
		SystemRepositoryDecoratorFactoryRegistrar.class, MolgenisPluginRegistryImpl.class, SemanticSearchConfig.class,
		OntologyConfig.class, JobExecutionConfig.class, JobFactoryRegistrar.class })
public class PlatformITConfig implements ApplicationListener<ContextRefreshedEvent>
{
	private final static Logger LOG = LoggerFactory.getLogger(PlatformITConfig.class);

	@Autowired
	private DataSource dataSource;
	@Autowired
	private RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	@Autowired
	private SystemEntityTypeRegistrar systemEntityTypeRegistrar;
	@Autowired
	private SystemPackageRegistrar systemPackageRegistrar;
	@Autowired
	private EntityFactoryRegistrar entityFactoryRegistrar;
	@Autowired
	private SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
	@Autowired
	private SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar;
	@Autowired
	private JobFactoryRegistrar jobFactoryRegistrar;

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[] { new ClassPathResource("/conf/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}

	@Bean
	public MailSender mailSender()
	{
		return mock(MailSender.class);
	}

	@Bean
	public UserDetailsService userDetailsService()
	{
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		UserDetails adminUserDetails = mock(UserDetails.class);
		Collection authorities = singleton(new SimpleGrantedAuthority(ROLE_SYSTEM));
		when(adminUserDetails.getAuthorities()).thenReturn(authorities);
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminUserDetails);
		return userDetailsService;
	}

	@Bean
	public AppSettings appSettings()
	{
		return new TestAppSettings();
	}

	@Bean
	public ConversionService conversionService()
	{
		DefaultConversionService defaultConversionService = new DefaultConversionService();
		defaultConversionService.addConverter(new StringToDateConverter());
		defaultConversionService.addConverter(new StringToDateTimeConverter());
		return defaultConversionService;
	}

	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new MolgenisPasswordEncoder(new BCryptPasswordEncoder());
	}

	// FIXME The bootstrapping of the data platform should be delegated to a specific bootstrapper so that updates
	// are reflected in the test
	@Autowired
	TransactionManager transactionManager;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager);
		transactionTemplate.execute((action) ->
		{
			try
			{
				RunAsSystemProxy.runAsSystem(() ->
				{
					LOG.info("Bootstrapping registries ...");
					LOG.trace("Registering repository collections ...");
					repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
					LOG.trace("Registered repository collections");

					LOG.trace("Registering system entity meta data ...");
					systemEntityTypeRegistrar.register(event);
					LOG.trace("Registered system entity meta data");

					LOG.trace("Registering system packages ...");
					systemPackageRegistrar.register(event);
					LOG.trace("Registered system packages");

					LOG.trace("Registering entity factories ...");
					entityFactoryRegistrar.register(event);
					LOG.trace("Registered entity factories");

					LOG.trace("Registering entity factories ...");
					systemRepositoryDecoratorFactoryRegistrar.register(event);
					LOG.trace("Registered entity factories");
					LOG.debug("Bootstrapped registries");

					LOG.trace("Bootstrapping system entity types ...");
					systemEntityTypeBootstrapper.bootstrap(event);
					LOG.debug("Bootstrapped system entity types");

					LOG.trace("Registering job factories ...");
					jobFactoryRegistrar.register(event);
					LOG.trace("Registered job factories");

					event.getApplicationContext().getBean(EntityTypeRegistryPopulator.class).populate();
				});
			}
			catch (Exception unexpected)
			{
				LOG.error("Error bootstrapping tests!", unexpected);
				throw new RuntimeException(unexpected);
			}
			return (Void) null;
		});
	}
}
