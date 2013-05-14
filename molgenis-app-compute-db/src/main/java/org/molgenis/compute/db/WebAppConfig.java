package org.molgenis.compute.db;

import java.util.List;

import org.molgenis.compute.db.controller.PilotDashboardController;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.pilot.ScriptBuilder;
import org.molgenis.compute.db.util.ComputeMolgenisSettings;
import org.molgenis.compute.db.util.SecurityHandlerInterceptor;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import app.DatabaseConfig;
import app.ui.PilotDashboardPluginPlugin;

@Configuration
@EnableWebMvc
@ComponentScan(
{ "org.molgenis.service", "org.molgenis.controller", "org.molgenis.compute.db.controller",
		"org.molgenis.compute.db.service" })
@Import(DatabaseConfig.class)
public class WebAppConfig extends WebMvcConfigurerAdapter
{
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new FileSystemResource[]
		{ new FileSystemResource(System.getProperty("user.home") + "/molgenis-server.properties") };
		pspc.setLocations(resources);
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		return pspc;
	}

	@Value("${api.user.name:api}")
	private String apiUserName; // specify in molgenis-server.properties

	@Value("${api.user.password:api}")
	private String apiUserPassword; // specify in molgenis-server.properties

	@Bean
	public ScriptBuilder scriptBuilder()
	{
		return new ScriptBuilder(apiUserName, apiUserPassword);
	}

	@Bean
	public Scheduler scheduler()
	{
		return new Scheduler(taskScheduler());
	}

	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler()
	{
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10);

		return scheduler;
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer)
	{
		configurer.enable("front-controller");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/css/**").addResourceLocations("/css/", "classpath:/css/");
		registry.addResourceHandler("/img/**").addResourceLocations("/img/", "classpath:/img/");
		registry.addResourceHandler("/js/**").addResourceLocations("/js/", "classpath:/js/");
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/");
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		converters.add(new GsonHttpMessageConverter());
	}

	@Bean
	public ApplicationListener<?> databasePopulator()
	{
		return new WebAppDatabasePopulator();
	}

	/**
	 * Bean that allows referencing Spring managed beans from Java code which is
	 * not managed by Spring
	 * 
	 * @return
	 */
	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	@Bean
	public MolgenisSettings molgenisSettings()
	{
		return new ComputeMolgenisSettings();
	}

	@Bean
	public MappedInterceptor pilotDashboardMappedInterceptor()
	{
		return new MappedInterceptor(new String[]
		{ PilotDashboardController.URI }, pilotDashboardSecurityHandlerInterceptor());
	}

	@Bean
	public SecurityHandlerInterceptor pilotDashboardSecurityHandlerInterceptor()
	{
		return new SecurityHandlerInterceptor(PilotDashboardPluginPlugin.class.getName());
	}

	/**
	 * Enable spring freemarker viewresolver. All freemarker template names
	 * should end with '.ftl'
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
	 * Configure freemarker. All freemarker templates should be on the classpath
	 * in a package called 'freemarker'
	 */
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		FreeMarkerConfigurer result = new FreeMarkerConfigurer();
		result.setPreferFileSystemAccess(false);
		result.setTemplateLoaderPath("classpath:/templates/");

		return result;
	}

	@Controller
	@RequestMapping("/")
	public static class RootController
	{
		@RequestMapping(method =
		{ RequestMethod.GET, RequestMethod.POST })
		public String index()
		{
			return "forward:molgenis.do";
		}
	}
}