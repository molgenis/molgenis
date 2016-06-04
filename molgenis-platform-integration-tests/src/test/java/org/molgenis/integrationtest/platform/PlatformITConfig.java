package org.molgenis.integrationtest.platform;

import com.google.common.io.Files;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.io.FileUtils;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.data.populate.UuidGenerator;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.integrationtest.data.TestAppSettings;
import org.molgenis.mail.MailConfig;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.MolgenisPermissionServiceImpl;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonConfig;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;
import static org.molgenis.integrationtest.platform.PostgreSqlDatabase.dropAndCreateDatabase;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
/*
 FIXME Ideally, we'd like to scan all of org.molgenis.data or even org.molgenis, but there's some unwanted dependencies
 in org.molgenis.data and subpackages from included modules
  */
@ComponentScan({ "org.molgenis.data.aggregation", "org.molgenis.data.meta", "org.molgenis.data.index",
		"org.molgenis.data.jobs", "org.molgenis.js", "org.molgenis.data.elasticsearch", "org.molgenis.auth",
		"org.molgenis.test.data", "org.molgenis.data.platform", "org.molgenis.data.meta.model",
		"org.molgenis.data.system.model", "org.molgenis.data.cache", "org.molgenis.data.i18n",
		"org.molgenis.data.postgresql", "org.molgenis.file.model", "org.molgenis.security.owned",
		"org.molgenis.security.user", "org.molgenis.data.validation", "org.molgenis.data.transaction" })
@Import({ DatabaseConfig.class, EmbeddedElasticSearchConfig.class, GsonConfig.class, PostgreSqlConfiguration.class,
		RunAsSystemBeanPostProcessor.class, UuidGenerator.class, ExpressionValidator.class, PlatformConfig.class,
		org.molgenis.data.RepositoryCollectionRegistry.class,
		org.molgenis.data.RepositoryCollectionDecoratorFactory.class,
		org.molgenis.data.RepositoryCollectionBootstrapper.class, org.molgenis.data.EntityFactoryRegistrar.class,
		MailConfig.class })
public class PlatformITConfig implements ApplicationListener<ContextRefreshedEvent>
{
	static
	{
		dropAndCreateDatabase();
	}

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

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[] { new ClassPathResource("/postgresql/molgenis.properties") };
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

	@PreDestroy
	public void cleanup() throws IOException, SQLException
	{
		((ComboPooledDataSource) dataSource).close();
		PostgreSqlDatabase.dropDatabase();

		try
		{
			// Delete molgenis home folder
			FileUtils.deleteDirectory(new File(System.getProperty("molgenis.home")));
		}
		catch (IOException e)
		{
			LOG.error("Error removing molgenis home directory", e);
		}
	}

	public PlatformITConfig()
	{
		System.setProperty("molgenis.home", Files.createTempDir().getAbsolutePath());
	}

	@Bean
	public AppSettings appSettings()
	{
		return new TestAppSettings();
	}

	@Bean
	public ConversionService conversionService()
	{
		return new DefaultConversionService();
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

	@Bean
	public MolgenisPermissionService molgenisPermissionService()
	{
		return new MolgenisPermissionServiceImpl();
	}

	// FIXME The bootstrapping of the data platform should be delegated to a specific bootstrapper so that updates
	// are reflected in the test
	@Autowired
	MolgenisTransactionManager molgenisTransactionManager;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(molgenisTransactionManager);
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
					LOG.debug("Bootstrapped registries");

					LOG.trace("Bootstrapping system entity types ...");
					systemEntityTypeBootstrapper.bootstrap(event);
					LOG.debug("Bootstrapped system entity types");
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
