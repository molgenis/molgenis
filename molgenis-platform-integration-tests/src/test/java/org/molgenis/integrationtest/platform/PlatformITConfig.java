package org.molgenis.integrationtest.platform;

import com.google.common.io.Files;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.postgresql.PostgreSqlEntityFactory;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.file.FileMetaMetaData;
import org.molgenis.integrationtest.data.TestAppSettings;
import org.molgenis.js.RhinoConfig;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.MolgenisUserServiceImpl;
import org.molgenis.ui.MolgenisRepositoryDecoratorFactory;
import org.molgenis.ui.RepositoryDecoratorRegistry;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import java.io.IOException;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan({ "org.molgenis.data.meta", "org.molgenis.data.reindex", "org.molgenis.data.elasticsearch.index",
		"org.molgenis.data.jobs", "org.molgenis.data.elasticsearch.reindex", "org.molgenis.auth",
		"org.molgenis.integrationtest.data.harness" })
@Import({ DatabaseConfig.class, EmbeddedElasticSearchConfig.class, GsonConfig.class, ElasticsearchEntityFactory.class,
		ElasticsearchRepositoryCollection.class, PostgreSqlConfiguration.class, RunAsSystemBeanPostProcessor.class,
		FileMetaMetaData.class, OwnedEntityMetaData.class, MolgenisUserServiceImpl.class, RhinoConfig.class,
		UuidGenerator.class, ExpressionValidator.class, LanguageService.class, PostgreSqlEntityFactory.class,
		PostgreSqlDatabase.class })
public class PlatformITConfig
{
	@Autowired
	private DataSource dataSource;
	@Autowired
	private SearchService searchService;
	@Autowired
	private ExpressionValidator expressionValidator;
	@Autowired
	private ReindexActionRegisterService reindexActionRegisterService;
	@Autowired
	private IdGenerator idGenerator;
	@Autowired
	private DataService dataService;
	@Autowired
	private MetaDataService metaDataService;
	@Autowired
	@Qualifier("PostgreSqlRepositoryCollection")
	ManageableRepositoryCollection backend;
	@Autowired
	private PostgreSqlDatabase postgresqlDatabase;

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
	public JavaMailSender mailSender()
	{
		return mock(JavaMailSender.class);
	}

	@PreDestroy
	public void cleanup() throws IOException, SQLException
	{
		((ComboPooledDataSource) dataSource).close();
		postgresqlDatabase.dropDatabase();
	}

	public PlatformITConfig()
	{
		System.setProperty("molgenis.home", Files.createTempDir().getAbsolutePath());
	}

	@PostConstruct
	public void init()
	{
		postgresqlDatabase.dropAndCreateDatabase();
		dataService.setMeta(metaDataService());
		metaDataService.setDefaultBackend(backend);
	}

	@Bean
	public MetaDataService metaDataService()
	{
		return new MetaDataServiceImpl(dataService());
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
	public RepositoryDecoratorRegistry repositoryDecoratorRegistry()
	{
		return new RepositoryDecoratorRegistry();
	}

	@Bean
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		return repository -> new MolgenisRepositoryDecoratorFactory(entityManager(), entityAttributesValidator(),
				idGenerator, appSettings(), dataService(), expressionValidator, repositoryDecoratorRegistry(),
				reindexActionRegisterService, searchService).createDecoratedRepository(repository);
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
}
