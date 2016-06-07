package org.molgenis.integrationtest.data;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.file.FileMetaMetaData;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.google.common.io.Files;

@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan({ "org.molgenis.data.meta", "org.molgenis.data.reindex", "org.molgenis.data.elasticsearch.index",
		"org.molgenis.data.jobs", "org.molgenis.data.elasticsearch.reindex", "org.molgenis.auth" })
@Import({ DatabaseConfig.class, EmbeddedElasticSearchConfig.class, GsonConfig.class, ElasticsearchEntityFactory.class,
		ElasticsearchRepositoryCollection.class, RunAsSystemBeanPostProcessor.class, FileMetaMetaData.class,
		OwnedEntityMetaData.class, MolgenisUserServiceImpl.class, RhinoConfig.class, UuidGenerator.class,
		ExpressionValidator.class, LanguageService.class })
public abstract class AbstractDataApiTestConfig
{
	@Autowired
	protected SearchService searchService;

	@Autowired
	public ExpressionValidator expressionValidator;

	@Autowired
	public ReindexActionRegisterService reindexActionRegisterService;

	@Autowired
	public DataSource dataSource;

	@Autowired
	public IdGenerator idGenerator;

	@Autowired
	public DataService dataService;

	@Autowired
	public MetaDataService metaDataService;

	@Autowired
	public ReindexActionJobMetaData reindexActionJobMetaData;

	@Autowired
	public ReindexActionMetaData reindexActionMetaData;

	protected AbstractDataApiTestConfig()
	{
		System.setProperty("molgenis.home", Files.createTempDir().getAbsolutePath());
		setUp();
	}

	@PostConstruct
	public void init()
	{
		//		dataService.setMeta(metaDataService()); // FIXME
		//		metaDataService.setDefaultBackend(getBackend()); // FIXME
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
		return new DataServiceImpl();
	}

	@Bean
	public EntityManager entityManager()
	{
		return new EntityManagerImpl(dataService());
	}

	@Bean
	public PermissionSystemService permissionSystemService()
	{
		return new PermissionSystemService(dataService(), null); // FIXME
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
			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
			{
				return new MolgenisRepositoryDecoratorFactory(entityManager(), entityAttributesValidator(), idGenerator,
						appSettings(), dataService(), expressionValidator, repositoryDecoratorRegistry(), null, null,
						null, reindexActionRegisterService, searchService)
						.createDecoratedRepository(repository); // FIXME
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

	@Bean
	public SecuritySupportService securitySupportService()
	{
		return new SecuritySupportService();
	}

	@Value("${mail.host:smtp.gmail.com}")
	private String mailHost;
	@Value("${mail.port:587}")
	private Integer mailPort;
	@Value("${mail.protocol:smtp}")
	private String mailProtocol;
	@Value("${mail.username}")
	private String mailUsername; // specify in molgenis-server.properties
	@Value("${mail.password}")
	private String mailPassword; // specify in molgenis-server.properties
	@Value("${mail.java.auth:true}")
	private String mailJavaAuth;
	@Value("${mail.java.starttls.enable:true}")
	private String mailJavaStartTlsEnable;
	@Value("${mail.java.quitwait:false}")
	private String mailJavaQuitWait;

	@Bean
	public JavaMailSender mailSender()
	{
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailHost);
		mailSender.setPort(mailPort);
		mailSender.setProtocol(mailProtocol);
		mailSender.setUsername(mailUsername); // specify in molgenis-server.properties
		mailSender.setPassword(mailPassword); // specify in molgenis-server.properties
		Properties javaMailProperties = new Properties();
		javaMailProperties.setProperty("mail.smtp.auth", mailJavaAuth);
		javaMailProperties.setProperty("mail.smtp.starttls.enable", mailJavaStartTlsEnable);
		javaMailProperties.setProperty("mail.smtp.quitwait", mailJavaQuitWait);
		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}

}
