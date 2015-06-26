package org.molgenis.ui;

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

import org.molgenis.data.AutoValueRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.IndexedAutoValueRepositoryDecorator;
import org.molgenis.data.IndexedCrudRepositorySecurityDecorator;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositorySecurityDecorator;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.transaction.TransactionLogIndexedRepositoryDecorator;
import org.molgenis.data.transaction.TransactionLogRepositoryDecorator;
import org.molgenis.data.transaction.TransactionLogService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.IndexedRepositoryValidationDecorator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.data.version.MolgenisUpgradeService;
import org.molgenis.file.FileStore;
import org.molgenis.framework.db.WebAppDatabasePopulator;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.messageconverter.CsvHttpMessageConverter;
import org.molgenis.security.CorsInterceptor;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.freemarker.HasPermissionDirective;
import org.molgenis.security.freemarker.NotHasPermissionDirective;
import org.molgenis.security.owned.OwnedEntityMetaData;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.ui.menu.MenuMolgenisUi;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.ui.security.MolgenisUiPermissionDecorator;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.IndexedRepositoryExceptionTranslatorDecorator;
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

import freemarker.template.TemplateException;

public abstract class MolgenisWebAppConfig extends WebMvcConfigurerAdapter
{
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private MolgenisSettings molgenisSettings;

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

	@Autowired
	public DataSource dataSource;

	@Autowired
	public TransactionLogService transactionLogService;

	@Autowired
	public IdGenerator idGenerator;

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
		boolean prettyPrinting = environment != null && environment.equals("development");
		converters.add(new GsonHttpMessageConverter(prettyPrinting));
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
		return new MolgenisInterceptor(resourceFingerprintRegistry(), molgenisSettings, environment);
	}

	@Bean
	public MolgenisPluginInterceptor molgenisPluginInterceptor()
	{
		return new MolgenisPluginInterceptor(molgenisUi(), molgenisSettings);
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
		FreeMarkerConfigurer result = new FreeMarkerConfigurer();
		result.setPreferFileSystemAccess(false);
		result.setTemplateLoaderPath("classpath:/templates/");
		result.setDefaultEncoding("UTF-8");

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
		return new MenuReaderServiceImpl(molgenisSettings);
	}

	@Bean
	public MenuManagerService menuManagerService()
	{
		return new MenuManagerServiceImpl(menuReaderService(), molgenisSettings, molgenisPluginRegistry());
	}

	@Bean
	public MolgenisUi molgenisUi()
	{
		MolgenisUi molgenisUi = new MenuMolgenisUi(molgenisSettings, menuReaderService());
		return new MolgenisUiPermissionDecorator(molgenisUi, molgenisPermissionService, molgenisSettings);
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

	protected abstract void addReposToReindex(DataServiceImpl localDataService);

	protected void reindex()
	{
		// Create local dataservice and metadataservice
		DataServiceImpl localDataService = new DataServiceImpl();
		MetaDataService metaDataService = new MetaDataServiceImpl(localDataService);
		localDataService.setMeta(metaDataService);

		addReposToReindex(localDataService);

		SearchService localSearchService = embeddedElasticSearchServiceFactory.create(localDataService,
				new EntityToSourceConverter());

		List<EntityMetaData> metas = DependencyResolver.resolve(Sets.newHashSet(localDataService.getMeta()
				.getEntityMetaDatas()));

		// Sort repos to the same sequence as the resolves metas
		List<Repository> repos = Lists.newArrayList(localDataService);
		repos.sort((r1, r2) -> Integer.compare(metas.indexOf(r1.getEntityMetaData()),
				metas.indexOf(r2.getEntityMetaData())));

		repos.forEach(repo -> {
			localSearchService.rebuildIndex(repo, repo.getEntityMetaData());
		});
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
		if (!indexExists() || didUpgrade)
		{
			LOG.info("Reindexing repositories....");
			reindex();
			LOG.info("Reindexing done.");
		}
		else
		{
			reindex();
			LOG.info("Index found. No need to reindex.");
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
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				// 1. security decorator
				// 2. autoid decorator
				// 3. validation decorator
				// 4. IndexedRepositoryExceptionTranslatorDecorator
				if (repository instanceof IndexedRepository)
				{
					IndexedRepository indexedRepos = (IndexedRepository) repository;
					if (EntityUtils.doesExtend(repository.getEntityMetaData(), OwnedEntityMetaData.ENTITY_NAME))
					{
						indexedRepos = new OwnedEntityRepositoryDecorator(indexedRepos);
					}

					return new IndexedCrudRepositorySecurityDecorator(new IndexedAutoValueRepositoryDecorator(
							new IndexedRepositoryValidationDecorator(dataService(),
									new IndexedRepositoryExceptionTranslatorDecorator(
											new TransactionLogIndexedRepositoryDecorator(indexedRepos,
													transactionLogService)), new EntityAttributesValidator()),
							idGenerator), molgenisSettings);
				}

				return new RepositorySecurityDecorator(new AutoValueRepositoryDecorator(
						new RepositoryValidationDecorator(dataService(), new TransactionLogRepositoryDecorator(
								repository, transactionLogService), new EntityAttributesValidator()), idGenerator));
			}
		};
	}

	/**
	 * Adds the upgrade steps to the {@link MolgenisUpgradeService}.
	 */
	public abstract void addUpgrades();
}
