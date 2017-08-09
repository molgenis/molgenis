package org.molgenis.ui;

import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.PropertiesMessageSource;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.messageconverter.CsvHttpMessageConverter;
import org.molgenis.security.CorsInterceptor;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.freemarker.HasPermissionDirective;
import org.molgenis.security.freemarker.NotHasPermissionDirective;
import org.molgenis.ui.converter.RdfConverter;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.ui.freemarker.MolgenisFreemarkerObjectWrapper;
import org.molgenis.ui.menu.MenuMolgenisUi;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.ui.security.MolgenisUiPermissionDecorator;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static freemarker.template.Configuration.VERSION_2_3_23;
import static org.molgenis.framework.ui.ResourcePathPatterns.*;
import static org.molgenis.security.UriConstants.PATH_SEGMENT_APPS;
import static org.molgenis.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;

@Import({ PlatformConfig.class, RdfConverter.class })
public abstract class MolgenisWebAppConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	private AppSettings appSettings;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private RdfConverter rdfConverter;

	@Autowired
	private LanguageService languageService;

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
		registry.addResourceHandler(PATTERN_CSS)
				.addResourceLocations("/css/", "classpath:/css/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_IMG)
				.addResourceLocations("/img/", "classpath:/img/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_JS)
				.addResourceLocations("/js/", "classpath:/js/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler(PATTERN_FONTS)
				.addResourceLocations("/fonts/", "classpath:/fonts/")
				.setCachePeriod(cachePeriod);
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/").setCachePeriod(3600);
		registry.addResourceHandler("/html/**").addResourceLocations("/html/", "classpath:/html/").setCachePeriod(3600);

		// Add resource handler for apps
		FileStore fileStore = fileStore();
		registry.addResourceHandler("/" + PATH_SEGMENT_APPS + "/**")
				.addResourceLocations("file:///" + fileStore.getStorageDir() + '/' + FILE_STORE_PLUGIN_APPS_PATH + '/');
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/")
				.setCachePeriod(3600)
				.resourceChain(true);
		// see https://github.com/spring-projects/spring-boot/issues/4403 for why the resourceChain needs to be explicitly added.
	}

	@Value("${environment:production}")
	private String environment;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		converters.add(gsonHttpMessageConverter);
		converters.add(new BufferedImageHttpMessageConverter());
		converters.add(new CsvHttpMessageConverter());
		converters.add(new ResourceHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(rdfConverter);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer)
	{
		// Fix for https://github.com/molgenis/molgenis/issues/5431
		configurer.setUseRegisteredSuffixPatternMatch(true);
	}

	@Bean
	public MappedInterceptor mappedCorsInterceptor()
	{
		/*
		 * This way, the cors interceptor is added to the resource handlers as well, if the patterns overlap.
		 *
		 * See https://jira.spring.io/browse/SPR-10655
		 */
		return new MappedInterceptor(new String[] { "/api/**", "/fdp/**" }, corsInterceptor());
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
		registry.addConverter(new StringToDateTimeConverter());
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
	public PropertiesMessageSource formMessageSource()
	{
		return new PropertiesMessageSource("form");
	}

	@Bean
	public PropertiesMessageSource dataexplorerMessageSource()
	{
		return new PropertiesMessageSource("dataexplorer");
	}

	@Bean
	public MolgenisPluginInterceptor molgenisPluginInterceptor()
	{
		return new MolgenisPluginInterceptor(molgenisUi(), molgenisPermissionService);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[] {
				new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
				new ClassPathResource("/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
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
}
