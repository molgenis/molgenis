package org.molgenis.integrationtest.data;

import com.google.common.io.Files;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.file.model.FileMetaMetaData;
import org.molgenis.js.RhinoConfig;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
import org.molgenis.security.owned.OwnedEntityMetaData;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.ui.MolgenisRepositoryDecoratorFactory;
import org.molgenis.ui.RepositoryDecoratorRegistry;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(
{ "org.molgenis.data.meta", "org.molgenis.data.elasticsearch.index", "org.molgenis.auth" })
@Import(
{ EmbeddedElasticSearchConfig.class, ElasticsearchEntityFactory.class,
		RunAsSystemBeanPostProcessor.class, FileMetaMetaData.class, OwnedEntityMetaData.class, RhinoConfig.class,
		DatabaseConfig.class, UuidGenerator.class, ExpressionValidator.class, LanguageService.class, ReindexActionRegisterService.class })
public abstract class AbstractDataApiTestConfig
{
	@Autowired
	protected SearchService searchService;

	@Autowired
	public ExpressionValidator expressionValidator;

	@Autowired
	public DataSource dataSource;

	@Autowired
	public IdGenerator idGenerator;

	protected AbstractDataApiTestConfig()
	{
		System.setProperty("molgenis.home", Files.createTempDir().getAbsolutePath());
		setUp();
	}

	@PostConstruct
	public void init()
	{
		SecuritySupport.login();
		dataService().setMetaDataService(metaDataService());
		metaDataService().setDefaultBackend(getBackend());
	}

	protected abstract void setUp();

	protected abstract RepositoryCollection getBackend();

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
	public MailSender mailSender()
	{
		return mock(MailSender.class);
	}

	@Autowired
	private ReindexActionRegisterService reindexActionRegisterService;

	@Bean
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
			{
				return new MolgenisRepositoryDecoratorFactory(entityManager(), entityAttributesValidator(),
						idGenerator, appSettings(), dataService(), expressionValidator, repositoryDecoratorRegistry(),
						reindexActionRegisterService, searchService).createDecoratedRepository(repository);
			}
		};
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		return new FreeMarkerConfigurer();
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
