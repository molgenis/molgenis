package org.molgenis.ui;

import static freemarker.template.Configuration.VERSION_2_3_23;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_CSS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_FONTS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_IMG;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_JS;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.MySqlEntityFactory;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.transaction.TransactionLogService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.file.FileStore;
import org.molgenis.framework.MolgenisUpgradeService;
import org.molgenis.framework.db.WebAppDatabasePopulator;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.messageconverter.CsvHttpMessageConverter;
import org.molgenis.security.CorsInterceptor;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.freemarker.HasPermissionDirective;
import org.molgenis.security.freemarker.NotHasPermissionDirective;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.ui.menu.MenuMolgenisUi;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.ui.security.MolgenisUiPermissionDecorator;
import org.molgenis.ui.settings.AppDbSettings;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public abstract class MolgenisWebAppConfig extends WebMvcConfigurerAdapter
{
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private AppDbSettings appDbSettings;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private WebAppDatabasePopulatorService webAppDatabasePopulatorService;

	@Autowired
	public SearchService searchService;

	@Autowired
	public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	public MolgenisUpgradeService upgradeService;

	// used by classes that extend from this class
	@Autowired
	public DataSource dataSource;

	@Autowired
	public TransactionLogService transactionLogService;

	@Autowired
	public IdGenerator idGenerator;

	@Autowired
	public GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	public EntityAttributesValidator entityAttributesValidator;

	@Autowired
	public ExpressionValidator expressionValidator;

	@Autowired
	public LanguageService languageService;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		int cachePeriod;
		if (environment.equals("development"))
		{
			cachePeriod = 0;
		}
		else
		{
			cachePeriod = 31536000; // a year
		}
		registry.addResourceHandler(PATTERN_CSS).addResourceLocations("/css/", "classpath:/css/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_IMG).addResourceLocations("/img/", "classpath:/img/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_JS).addResourceLocations("/js/", "classpath:/js/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_FONTS).addResourceLocations("/fonts/", "classpath:/fonts/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/").setCachePeriod(3600);
		registry.addResourceHandler("/html/**").addResourceLocations("/html/", "classpath:/html/").setCachePeriod(3600);
	}

	@Value("${environment:production}")
	private String environment;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		converters.add(gsonHttpMessageConverter);
		converters.add(new BufferedImageHttpMessageConverter());
		converters.add(new CsvHttpMessageConverter());
	}

	@Bean
	public MappedInterceptor mappedCorsInterceptor()
	{
		/*
		 * This way, the cors interceptor is added to the resource handlers as well, if the patterns overlap.
		 * 
		 * See https://jira.spring.io/browse/SPR-10655
		 */
		String corsInterceptPattern = "/api/**";
		return new MappedInterceptor(new String[]
		{ corsInterceptPattern }, corsInterceptor());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		String pluginInterceptPattern = MolgenisPluginController.PLUGIN_URI_PREFIX + "**";
		registry.addInterceptor(molgenisInterceptor());
		registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
	}

	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addConverter(new DateToStringConverter());
		registry.addConverter(new StringToDateConverter());
	}

	@Bean
	public ResourceFingerprintRegistry resourceFingerprintRegistry()
	{
		return new ResourceFingerprintRegistry();
	}

	@Bean
	public MolgenisInterceptor molgenisInterceptor()
	{
		return new MolgenisInterceptor(resourceFingerprintRegistry(), appSettings, languageService, environment);
	}

	@Bean
	public MolgenisPluginInterceptor molgenisPluginInterceptor()
	{
		return new MolgenisPluginInterceptor(molgenisUi(), molgenisPermissionService);
	}

	@Bean
	public ApplicationListener<?> databasePopulator()
	{
		return new WebAppDatabasePopulator(webAppDatabasePopulatorService);
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

	@Bean
	public FileStore fileStore()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}
		if (!molgenisHomeDir.endsWith(File.separator)) molgenisHomeDir = molgenisHomeDir + File.separator;

		// create molgenis store directory in molgenis data directory if not exists
		String molgenisFileStoreDirStr = molgenisHomeDir + "data" + File.separator + "filestore";
		File molgenisDataDir = new File(molgenisFileStoreDirStr);
		if (!molgenisDataDir.exists())
		{
			if (!molgenisDataDir.mkdirs())
			{
				throw new RuntimeException("failed to create directory: " + molgenisFileStoreDirStr);
			}
		}

		return new FileStore(molgenisFileStoreDirStr);
	}

	/**
	 * Bean that allows referencing Spring managed beans from Java code which is not managed by Spring
	 * 
	 * @return
	 */
	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	/**
	 * Enable spring freemarker viewresolver. All freemarker template names should end with '.ftl'
	 */
	@Bean
	public ViewResolver viewResolver()
	{
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setCache(true);
		resolver.setSuffix(".ftl");
		resolver.setContentType("text/html;charset=UTF-8");
		return resolver;
	}

	/**
	 * Configure freemarker. All freemarker templates should be on the classpath in a package called 'freemarker'
	 * 
	 * @throws TemplateException
	 * @throws IOException
	 */
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
	{
		FreeMarkerConfigurer result = new FreeMarkerConfigurer()
		{
			@Override
			protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException
			{
				config.setObjectWrapper(new MolgenisFreemarkerObjectWrapper(VERSION_2_3_23));
			}
		};
		result.setPreferFileSystemAccess(false);
		result.setTemplateLoaderPath("classpath:/templates/");
		result.setDefaultEncoding("UTF-8");
		Properties freemarkerSettings = new Properties();
		freemarkerSettings.setProperty(Configuration.LOCALIZED_LOOKUP_KEY, Boolean.FALSE.toString());
		result.setFreemarkerSettings(freemarkerSettings);
		Map<String, Object> freemarkerVariables = Maps.newHashMap();
		freemarkerVariables.put("limit", new LimitMethod());
		freemarkerVariables.put("hasPermission", new HasPermissionDirective(molgenisPermissionService));
		freemarkerVariables.put("notHasPermission", new NotHasPermissionDirective(molgenisPermissionService));
		addFreemarkerVariables(freemarkerVariables);

		result.setFreemarkerVariables(freemarkerVariables);

		return result;
	}

	// Override in subclass if you need more freemarker variables
	protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables)
	{

	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		return new StandardServletMultipartResolver();
	}

	@Bean
	public MenuReaderService menuReaderService()
	{
		return new MenuReaderServiceImpl(appSettings);
	}

	@Bean
	public MenuManagerService menuManagerService()
	{
		return new MenuManagerServiceImpl(menuReaderService(), appSettings, molgenisPluginRegistry());
	}

	@Bean
	public MolgenisUi molgenisUi()
	{
		MolgenisUi molgenisUi = new MenuMolgenisUi(menuReaderService());
		return new MolgenisUiPermissionDecorator(molgenisUi, molgenisPermissionService);
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisPluginRegistryImpl();
	}

	@Bean
	public CorsInterceptor corsInterceptor()
	{
		return new CorsInterceptor();
	}

	protected abstract ManageableRepositoryCollection getBackend();

	protected abstract void addReposToReindex(DataServiceImpl localDataService,
			MySqlEntityFactory localMySqlEntityFactory);

	protected void reindex()
	{
		// Create local dataservice and metadataservice
		DataServiceImpl localDataService = new DataServiceImpl();
		EntityManager localEntityManager = new EntityManagerImpl(localDataService);
		MySqlEntityFactory localMySqlEntityFactory = new MySqlEntityFactory(localEntityManager, localDataService);

		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(localDataService);
		metaDataService.setLanguageService(new LanguageService(localDataService, appDbSettings));
		localDataService.setMeta(metaDataService);

		addReposToReindex(localDataService, localMySqlEntityFactory);

		SourceToEntityConverter sourceToEntityConverter = new SourceToEntityConverter(localDataService,
				localEntityManager);
		EntityToSourceConverter entityToSourceConverter = new EntityToSourceConverter();
		SearchService localSearchService = embeddedElasticSearchServiceFactory.create(localDataService,
				new ElasticsearchEntityFactory(localEntityManager, sourceToEntityConverter, entityToSourceConverter));

		List<EntityMetaData> metas = DependencyResolver.resolve(Sets.newHashSet(localDataService.getMeta()
				.getEntityMetaDatas()));

		// Sort repos to the same sequence as the resolves metas
		List<Repository> repos = Lists.newArrayList(localDataService);
		repos.sort((r1, r2) -> Integer.compare(metas.indexOf(r1.getEntityMetaData()),
				metas.indexOf(r2.getEntityMetaData())));

		repos.forEach(repo -> {
			localSearchService.rebuildIndex(repo, repo.getEntityMetaData());
		});

		localSearchService.optimizeIndex();
	}

	@PostConstruct
	public void validateMolgenisServerProperties()
	{
		// validate properties defined in molgenis-server.properties
		String path = System.getProperty("molgenis.home") + File.separator + "molgenis-server.properties";
		if (environment == null)
		{
			throw new RuntimeException("Missing required property 'environment' in " + path
					+ ", allowed values are [development, production].");
		}
		else if (!environment.equals("development") && !environment.equals("production"))
		{
			throw new RuntimeException("Invalid value '" + environment + "' for property 'environment' in " + path
					+ ", allowed values are [development, production].");
		}
	}

	@PostConstruct
	public void initRepositories()
	{
		dataService().setMeta(metaDataService());

		addUpgrades();
		boolean didUpgrade = upgradeService.upgrade();
		if (didUpgrade)
		{
			LOG.info("Reindexing repositories due to MOLGENIS upgrade...");
			reindex();
			LOG.info("Reindexing done.");
		}
		else if (!indexExists())
		{
			LOG.info("Reindexing repositories due to missing Elasticsearch index...");
			reindex();
			LOG.info("Reindexing done.");
		}
		else
		{
			LOG.debug("Elasticsearch index exists, no need to reindex.");
		}
		runAsSystem(() -> metaDataService().setDefaultBackend(getBackend()));
	}

	private boolean indexExists()
	{
		return searchService.hasMapping(EntityMetaDataMetaData.INSTANCE);
	}

	@Bean
	public DataService dataService()
	{
		return new DataServiceImpl(repositoryDecoratorFactory());
	}

	@Bean
	public MetaDataService metaDataService()
	{
		return new MetaDataServiceImpl((DataServiceImpl) dataService());
	}

	@Bean
	public EntityManager entityManager()
	{
		return new EntityManagerImpl(dataService());
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
				return new MolgenisRepositoryDecoratorFactory(entityManager(), transactionLogService,
						entityAttributesValidator, idGenerator, appSettings, dataService(), expressionValidator)
						.createDecoratedRepository(repository);
			}
		};
	}

	/**
	 * Adds the upgrade steps to the {@link MolgenisUpgradeService}.
	 */
	public abstract void addUpgrades();
}
