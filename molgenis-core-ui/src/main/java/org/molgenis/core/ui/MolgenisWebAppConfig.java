package org.molgenis.core.ui;

import static freemarker.template.Configuration.VERSION_2_3_23;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_CSS;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_FONTS;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_IMG;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_JS;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_SWAGGER;
import static org.molgenis.core.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;
import static org.molgenis.security.UriConstants.PATH_SEGMENT_APPS;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.molgenis.core.ui.converter.RdfConverter;
import org.molgenis.core.ui.freemarker.MolgenisFreemarkerObjectWrapper;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
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
import org.molgenis.util.AppDataRootProvider;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.web.PluginController;
import org.molgenis.web.PluginInterceptor;
import org.molgenis.web.converter.CsvHttpMessageConverter;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.MenuReaderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@Import({PlatformConfig.class, RdfConverter.class})
public abstract class MolgenisWebAppConfig implements WebMvcConfigurer {
  @Autowired private AppSettings appSettings;

  @Autowired private AuthenticationSettings authenticationSettings;

  @Autowired private UserPermissionEvaluator permissionService;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @Autowired private Gson gson;

  @Autowired private RdfConverter rdfConverter;

  @Autowired ThemeFingerprintRegistry themeFingerprintRegistry;

  @Autowired private MessageSource messageSource;

  @Autowired private UserPermissionEvaluator userPermissionEvaluator;

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setDefaultTimeout(60L * 1000L);
    configurer.setTaskExecutor(asyncTaskExecutor());
  }

  @Bean
  public AsyncTaskExecutor asyncTaskExecutor() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(5);
    threadPoolTaskExecutor.setMaxPoolSize(10);
    threadPoolTaskExecutor.setQueueCapacity(25);
    threadPoolTaskExecutor.initialize();
    return new DelegatingSecurityContextAsyncTaskExecutor(threadPoolTaskExecutor);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    int cachePeriod;
    if (environment.equals("development")) {
      cachePeriod = 0;
    } else {
      cachePeriod = 31536000; // a year
    }
    registry
        .addResourceHandler(PATTERN_CSS)
        .addResourceLocations("/css/", "classpath:/css/")
        .setCachePeriod(cachePeriod);
    registry
        .addResourceHandler(PATTERN_IMG)
        .addResourceLocations("/img/", "classpath:/img/")
        .setCachePeriod(cachePeriod);
    registry
        .addResourceHandler(PATTERN_JS)
        .addResourceLocations("/js/", "classpath:/js/")
        .setCachePeriod(cachePeriod);
    registry
        .addResourceHandler(PATTERN_FONTS)
        .addResourceLocations("/fonts/", "classpath:/fonts/")
        .setCachePeriod(cachePeriod);
    registry
        .addResourceHandler(PATTERN_SWAGGER)
        .addResourceLocations("/swagger/", "classpath:/swagger/")
        .setCachePeriod(cachePeriod);
    registry
        .addResourceHandler("/generated-doc/**")
        .addResourceLocations("/generated-doc/")
        .setCachePeriod(3600);
    registry
        .addResourceHandler("/html/**")
        .addResourceLocations("/html/", "classpath:/html/")
        .setCachePeriod(3600);

    // Add resource handler for apps
    FileStore fileStore = fileStore();
    registry
        .addResourceHandler("/" + PATH_SEGMENT_APPS + "/**")
        .addResourceLocations(
            "file:///" + fileStore.getStorageDir() + '/' + FILE_STORE_PLUGIN_APPS_PATH + '/');
    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
        .setCachePeriod(3600)
        .resourceChain(true);
    // see https://github.com/spring-projects/spring-boot/issues/4403 for why the resourceChain
    // needs to be explicitly added.
  }

  @Value("${environment:production}")
  private String environment;

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(gsonHttpMessageConverter);
    converters.add(new BufferedImageHttpMessageConverter());
    converters.add(new CsvHttpMessageConverter());
    converters.add(new ResourceHttpMessageConverter());
    converters.add(new StringHttpMessageConverter());
    converters.add(rdfConverter);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new TokenExtractor());
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setUseSuffixPatternMatch(false);
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    // Fix for https://github.com/molgenis/molgenis/issues/6575
    configurer.favorPathExtension(false);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    String pluginInterceptPattern = PluginController.PLUGIN_URI_PREFIX + "**";
    registry.addInterceptor(molgenisInterceptor());
    registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToDateTimeConverter());
    registry.addConverter(new StringToDateConverter());
  }

  @Bean
  public ResourceFingerprintRegistry resourceFingerprintRegistry() {
    return new ResourceFingerprintRegistry();
  }

  @Bean
  public MolgenisInterceptor molgenisInterceptor() {
    return new MolgenisInterceptor(
        resourceFingerprintRegistry(),
        themeFingerprintRegistry,
        appSettings,
        authenticationSettings,
        environment,
        messageSource,
        gson);
  }

  @Bean
  public PropertiesMessageSource formMessageSource() {
    return new PropertiesMessageSource("form");
  }

  @Bean
  public PropertiesMessageSource feedbackMessageSource() {
    return new PropertiesMessageSource("feedback");
  }

  @Bean
  public PropertiesMessageSource dataexplorerMessageSource() {
    return new PropertiesMessageSource("dataexplorer");
  }

  @Bean
  public PropertiesMessageSource uiFormMessageSource() {
    return new PropertiesMessageSource("ui-form");
  }

  @Bean
  public PluginInterceptor molgenisPluginInterceptor() {
    return new PluginInterceptor(menuReaderService(), permissionService);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer properties() throws IOException {
    AppDataRootInitializer.init();

    PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
    Resource[] resources =
        new Resource[] {
          new FileSystemResource(
              AppDataRootProvider.getAppDataRoot().toString()
                  + File.separator
                  + "molgenis-server.properties"),
          new ClassPathResource("/molgenis.properties")
        };
    pspc.setLocations(resources);
    pspc.setFileEncoding("UTF-8");
    pspc.setIgnoreUnresolvablePlaceholders(true);
    pspc.setIgnoreResourceNotFound(true);
    pspc.setNullValue("@null");
    return pspc;
  }

  @Bean
  public FileStore fileStore() {
    // get molgenis home directory
    Path appDataRoot = AppDataRootProvider.getAppDataRoot();

    // create molgenis store directory in molgenis data directory if not exists
    String molgenisFileStoreDirStr =
        Paths.get(appDataRoot.toString(), "data", "filestore").toString();
    File molgenisDataDir = new File(molgenisFileStoreDirStr);
    if (!molgenisDataDir.exists() && !molgenisDataDir.mkdirs()) {
      throw new UncheckedIOException(
          new IOException("failed to create directory: " + molgenisFileStoreDirStr));
    }

    return new FileStore(molgenisFileStoreDirStr);
  }

  /** Enable spring freemarker viewresolver. All freemarker template names should end with '.ftl' */
  @Bean
  public ViewResolver viewResolver() {
    FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
    resolver.setCache(true);
    resolver.setSuffix(".ftl");
    resolver.setContentType("text/html;charset=UTF-8");
    return resolver;
  }

  /**
   * Configure freemarker. All freemarker templates should be on the classpath in a package called
   * 'freemarker'
   */
  @Bean
  public FreeMarkerConfigurer freeMarkerConfigurer() {
    FreeMarkerConfigurer result =
        new FreeMarkerConfigurer() {
          @Override
          protected void postProcessConfiguration(Configuration config) {
            config.setObjectWrapper(new MolgenisFreemarkerObjectWrapper(VERSION_2_3_23));
          }

          @Override
          protected void postProcessTemplateLoaders(List<TemplateLoader> templateLoaders) {
            templateLoaders.add(new ClassTemplateLoader(FreeMarkerConfigurer.class, ""));
          }
        };
    result.setPreferFileSystemAccess(false);
    result.setTemplateLoaderPath("classpath:/templates/");
    result.setDefaultEncoding("UTF-8");
    Properties freemarkerSettings = new Properties();
    freemarkerSettings.setProperty(Configuration.LOCALIZED_LOOKUP_KEY, Boolean.FALSE.toString());
    freemarkerSettings.setProperty(Configuration.NUMBER_FORMAT_KEY, "computer");
    result.setFreemarkerSettings(freemarkerSettings);
    Map<String, Object> freemarkerVariables = Maps.newHashMap();
    freemarkerVariables.put("hasPermission", new HasPermissionDirective(permissionService));
    freemarkerVariables.put("notHasPermission", new NotHasPermissionDirective(permissionService));
    addFreemarkerVariables(freemarkerVariables);

    result.setFreemarkerVariables(freemarkerVariables);

    return result;
  }

  // Override in subclass if you need more freemarker variables
  @SuppressWarnings({"unused", "WeakerAccess"})
  protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables) {}

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }

  @Bean
  public MenuReaderService menuReaderService() {
    return new MenuReaderServiceImpl(appSettings, gson, userPermissionEvaluator);
  }

  /**
   * Bean that allows referencing Spring managed beans from Java code which is not managed by Spring
   */
  @Bean
  public ApplicationContextProvider applicationContextProvider() {
    return new ApplicationContextProvider();
  }

  /** Introduced due to https://jira.spring.io/browse/SPR-16668 */
  @Bean
  public ForwardedHeaderTransformer forwardedHeaderTransformer() {
    return new ForwardedHeaderTransformer();
  }
}
