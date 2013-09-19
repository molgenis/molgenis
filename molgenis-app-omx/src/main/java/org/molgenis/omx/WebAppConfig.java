package org.molgenis.omx;

import java.io.IOException;
import java.nio.file.FileStore;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.molgenis.DatabaseConfig;

@Configuration
@EnableWebMvc
@EnableAsync
@ComponentScan("org.molgenis")
@Import(
{ DatabaseConfig.class, OmxConfig.class, EmbeddedElasticSearchConfig.class, DataExplorerConfig.class,
		SearchSecurityConfig.class })
public class WebAppConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	@Qualifier("database")
	private Database database;

	@Autowired
	@Qualifier("unauthorizedDatabase")
	private Database unauthorizedDatabase;

	@Autowired
	private Login login;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/css/**").addResourceLocations("/css/", "classpath:/css/");
		registry.addResourceHandler("/img/**").addResourceLocations("/img/", "classpath:/img/");
		registry.addResourceHandler("/js/**").addResourceLocations("/js/", "classpath:/js/");
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/");
		registry.addResourceHandler("/html/**").addResourceLocations("/html/", "classpath:/html/");
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		converters.add(new GsonHttpMessageConverter());
		converters.add(new BufferedImageHttpMessageConverter());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		String pluginInterceptPattern = MolgenisPlugin.PLUGIN_URI_PREFIX + "**";
		registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
	}

	@Bean
	public MolgenisPluginInterceptor molgenisPluginInterceptor()
	{
		return new MolgenisPluginInterceptor(login, molgenisPermissionService(), molgenisUi());
	}

	@Bean
	public ApplicationListener<?> databasePopulator()
	{
		return new WebAppDatabasePopulator();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new FileSystemResource[]
		{ new FileSystemResource(System.getProperty("user.home") + "/molgenis-server.properties") };
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
		AsyncJavaMailSender mailSender = new AsyncJavaMailSender();
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
		return new FileStore(System.getProperty("user.home"));
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
	 */
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		FreeMarkerConfigurer result = new FreeMarkerConfigurer();
		result.setPreferFileSystemAccess(false);
		result.setTemplateLoaderPath("classpath:/templates/");

		return result;
	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		return new StandardServletMultipartResolver();
	}

	@Bean
	public MolgenisUi molgenisUi()
	{
		try
		{
			return new XmlMolgenisUi(new XmlMolgenisUiLoader(), molgenisSettings, molgenisPermissionService());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Bean
	public MolgenisPermissionService molgenisPermissionService()
	{
		return new OmxPermissionService(unauthorizedDatabase, login);
	}

	@Bean
	public CatalogManagerService catalogManagerService()
	{
		return new OmxCatalogManagerService(database);
	}

	@Bean
	public StudyManagerService studyDefinitionManagerService()
	{
		return new OmxStudyManagerService(database);
	}
}