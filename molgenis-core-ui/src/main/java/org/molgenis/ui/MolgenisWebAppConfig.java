package org.molgenis.ui;

import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_CSS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_FONTS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_IMG;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_JS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedCrudRepository;
import org.molgenis.data.IndexedCrudRepositorySecurityDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.meta.IndexingWritableMetaDataServiceDecorator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.meta.WritableMetaDataServiceDecorator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.IndexedRepositoryValidationDecorator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
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
import org.molgenis.system.core.RuntimeProperty;
import org.molgenis.ui.freemarker.FormLinkDirective;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.ui.menu.MenuMolgenisUi;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.ui.security.MolgenisUiPermissionDecorator;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.FileStore;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.ResourceFingerprintRegistry;
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

import com.google.common.collect.Maps;

import freemarker.template.TemplateException;

public abstract class MolgenisWebAppConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private WebAppDatabasePopulatorService webAppDatabasePopulatorService;

	// temporary workaround for module dependencies
	@Autowired
	private DataService dataService;
	@Autowired
	private SearchService elasticSearchService;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		final int aYear = 31536000;
		registry.addResourceHandler(PATTERN_CSS).addResourceLocations("/css/", "classpath:/css/").setCachePeriod(aYear);
		registry.addResourceHandler(PATTERN_IMG).addResourceLocations("/img/", "classpath:/img/").setCachePeriod(aYear);
		registry.addResourceHandler(PATTERN_JS).addResourceLocations("/js/", "classpath:/js/").setCachePeriod(aYear);
		registry.addResourceHandler(PATTERN_FONTS).addResourceLocations("/fonts/", "classpath:/fonts/")
				.setCachePeriod(aYear);
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/").setCachePeriod(3600);
		registry.addResourceHandler("/html/**").addResourceLocations("/html/", "classpath:/html/").setCachePeriod(3600);
	}

	@Value("${molgenis.build.profile}")
	private String molgenisBuildProfile;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		boolean prettyPrinting = molgenisBuildProfile != null && molgenisBuildProfile.equals("dev");
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
		return new MolgenisInterceptor(resourceFingerprintRegistry(), molgenisSettings);
	}

	@Bean
	public MolgenisPluginInterceptor molgenisPluginInterceptor()
	{
		return new MolgenisPluginInterceptor(molgenisUi());
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
		freemarkerVariables.put("formLink", new FormLinkDirective());
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

	// temporary workaround for module dependencies

	@Bean
	WritableMetaDataServiceDecorator writableMetaDataServiceDecorator()
	{
		return new WritableMetaDataServiceDecorator()
		{
			@Override
			public WritableMetaDataService decorate(WritableMetaDataService metaDataRepositories)
			{
				return new IndexingWritableMetaDataServiceDecorator(metaDataRepositories, dataService,
						elasticSearchService);
			}
		};
	}

	@Bean
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				// do not index an indexed repository
				if (repository instanceof IndexedCrudRepository)
				{
					// 1. security decorator
					// 2. validation decorator
					// 3. indexed repository
					return new IndexedCrudRepositorySecurityDecorator(new IndexedRepositoryValidationDecorator(
							dataService, (IndexedCrudRepository) repository, new EntityAttributesValidator()),
							molgenisSettings);
				}
				else
				{
					EntityMetaData entityMetaData = repository.getEntityMetaData();

					if (RuntimeProperty.ENTITY_NAME.equalsIgnoreCase(entityMetaData.getName()))
					{
						// Do not index RuntimeProperty, because lucene term has a max of 32766 bytes and the content of
						// a RuntimeProperty can exceed this (for example the menu structure in JSON and the static
						// plugin contents are stored in a RuntimeProperty)
						return new CrudRepositorySecurityDecorator(new RepositoryValidationDecorator(dataService,
								(CrudRepository) repository, new EntityAttributesValidator()));
					}

					// create indexing meta data if meta data does not exist
					if (!elasticSearchService.hasMapping(entityMetaData))
					{
						try
						{
							elasticSearchService.createMappings(entityMetaData);
						}
						catch (IOException e)
						{
							throw new MolgenisDataException(e);
						}
					}

					// 1. security decorator
					// 2. validation decorator
					// 3. indexing decorator
					// 4. repository
					IndexedCrudRepository indexedRepo = new ElasticsearchRepositoryDecorator(repository,
							elasticSearchService);
					if (AttributeMetaDataMetaData.ENTITY_NAME.equals(entityMetaData.getName())
							|| EntityMetaDataMetaData.ENTITY_NAME.equals(entityMetaData.getName())
							|| PackageMetaData.ENTITY_NAME.equals(entityMetaData.getName()))
					{
						// TODO: help! how to prevent all sorts of nasty security warnings and String -> Xref entity
						// conversion hiccups upon construction of the application context?
						return indexedRepo;
					}

					return new IndexedCrudRepositorySecurityDecorator(new IndexedRepositoryValidationDecorator(
							dataService, indexedRepo, new EntityAttributesValidator()), molgenisSettings);
				}
			}
		};
	}
}
