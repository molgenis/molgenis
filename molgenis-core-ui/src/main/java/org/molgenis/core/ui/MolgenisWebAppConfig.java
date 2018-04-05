package org.molgenis.core.ui;

import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.molgenis.core.ui.converter.RdfConverter;
import org.molgenis.core.ui.freemarker.LimitMethod;
import org.molgenis.core.ui.freemarker.MolgenisFreemarkerObjectWrapper;
import org.molgenis.core.ui.menu.MenuMolgenisUi;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.core.ui.menu.MenuReaderServiceImpl;
import org.molgenis.core.ui.messageconverter.CsvHttpMessageConverter;
import org.molgenis.core.ui.security.MolgenisUiPermissionDecorator;
import org.molgenis.core.ui.style.StyleService;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.data.DataService;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.i18n.PropertiesMessageSource;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.freemarker.HasPermissionDirective;
import org.molgenis.security.freemarker.NotHasPermissionDirective;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.settings.AppSettings;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.web.PluginController;
import org.molgenis.web.PluginInterceptor;
import org.molgenis.web.Ui;
import org.molgenis.web.i18n.MolgenisLocaleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static freemarker.template.Configuration.VERSION_2_3_23;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.*;
import static org.molgenis.core.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;
import static org.molgenis.security.UriConstants.PATH_SEGMENT_APPS;

@Import({ PlatformConfig.class, RdfConverter.class })
public abstract class MolgenisWebAppConfig implements WebMvcConfigurer
{
	private static final String MOLGENIS_HOME = "molgenis.home";

	@Autowired
	private DataService dataService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private AuthenticationSettings authenticationSettings;

	@Autowired
	private UserPermissionEvaluator permissionService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private RdfConverter rdfConverter;

	@Autowired
	private StyleService styleService;

	@Autowired
	private MessageSource messageSource;

	@Override
	public void addCorsMappings(CorsRegistry registry)
	{
		registry.addMapping("/api/**").allowedMethods("*");
	}

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
		registry.addResourceHandler(PATTERN_SWAGGER)
				.addResourceLocations("/swagger/", "classpath:/swagger/")
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
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers)
	{
		argumentResolvers.add(new TokenExtractor());
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer)
	{
		// Fix for https://github.com/molgenis/molgenis/issues/5431
		configurer.setUseRegisteredSuffixPatternMatch(true);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer)
	{
		// Fix for https://github.com/molgenis/molgenis/issues/6575
		configurer.favorPathExtension(false);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		String pluginInterceptPattern = PluginController.PLUGIN_URI_PREFIX + "**";
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
	public ThemeFingerprintRegistry themeFingerprintRegistry()
	{
		return new ThemeFingerprintRegistry(styleService);
	}

	@Bean
	public MolgenisInterceptor molgenisInterceptor()
	{
		return new MolgenisInterceptor(resourceFingerprintRegistry(), themeFingerprintRegistry(), appSettings,
				authenticationSettings, environment, messageSource);
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
	public PluginInterceptor molgenisPluginInterceptor()
	{
		return new PluginInterceptor(molgenisUi(), permissionService);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[] {
				new FileSystemResource(System.getProperty(MOLGENIS_HOME) + "/molgenis-server.properties"),
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
		String molgenisHomeDir = System.getProperty(MOLGENIS_HOME);
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException(
					String.format("missing required java system property '%s'", MOLGENIS_HOME));
		}
		if (!molgenisHomeDir.endsWith(File.separator)) molgenisHomeDir = molgenisHomeDir + File.separator;

		// create molgenis store directory in molgenis data directory if not exists
		String molgenisFileStoreDirStr = molgenisHomeDir + "data" + File.separator + "filestore";
		File molgenisDataDir = new File(molgenisFileStoreDirStr);
		if (!molgenisDataDir.exists() && !molgenisDataDir.mkdirs())
		{
			throw new RuntimeException("failed to create directory: " + molgenisFileStoreDirStr);
		}

		return new FileStore(molgenisFileStoreDirStr);
	}

	/**
	 * Bean that allows referencing Spring managed beans from Java code which is not managed by Spring
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
	 */
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
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
		freemarkerVariables.put("hasPermission", new HasPermissionDirective(permissionService));
		freemarkerVariables.put("notHasPermission", new NotHasPermissionDirective(permissionService));
		addFreemarkerVariables(freemarkerVariables);

		result.setFreemarkerVariables(freemarkerVariables);

		return result;
	}

	// Override in subclass if you need more freemarker variables
	@SuppressWarnings({ "unused", "WeakerAccess" })
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
	public Ui molgenisUi()
	{
		Ui molgenisUi = new MenuMolgenisUi(menuReaderService());
		return new MolgenisUiPermissionDecorator(molgenisUi, permissionService);
	}

	@Bean
	public LocaleResolver localeResolver()
	{
		return new MolgenisLocaleResolver(dataService, () -> new Locale(appSettings.getLanguageCode()));
	}

	@PostConstruct
	public void validateMolgenisServerProperties()
	{
		// validate properties defined in molgenis-server.properties
		String path = System.getProperty(MOLGENIS_HOME) + File.separator + "molgenis-server.properties";
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
