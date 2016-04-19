package org.molgenis.integrationtest.data;

import com.google.common.io.Files;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.transaction.TransactionConfig;
import org.molgenis.data.transaction.TransactionLogService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.file.FileMetaMetaData;
import org.molgenis.js.RhinoConfig;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.runas.RunAsSystemBeanPostProcessor;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(
{ "org.molgenis.data.meta", "org.molgenis.data.elasticsearch.index", "org.molgenis.auth" })
@Import(
{TransactionConfig.class,
		RunAsSystemBeanPostProcessor.class, FileMetaMetaData.class,
		OwnedEntityMetaData.class, RhinoConfig.class, ExpressionValidator.class, LanguageService.class,
		DatabaseConfig.class, UuidGenerator.class
})
public abstract class AbstractDataApiTestConfig
{
	@Autowired
	private TransactionLogService transactionLogService;

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
		dataService().setMeta(metaDataService());
		metaDataService().setDefaultBackend(getBackend());
	}

	protected abstract void setUp();
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
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				return new MolgenisRepositoryDecoratorFactory(entityManager(), transactionLogService,
						entityAttributesValidator(), idGenerator, appSettings(), dataService(), expressionValidator,
						repositoryDecoratorRegistry()).createDecoratedRepository(repository);
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
