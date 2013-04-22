package org.molgenis.compute.db;

import java.util.List;

import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.molgenis.compute.db.executor.ComputeExecutorTask;
import org.molgenis.compute.db.util.ComputeMolgenisSettings;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import app.DatabaseConfig;

@Configuration
@EnableWebMvc
@ComponentScan("org.molgenis")
@Import(DatabaseConfig.class)
public class WebAppConfig extends WebMvcConfigurerAdapter
{
	@Bean(destroyMethod = "close")
	public Database unathorizedDatabase() throws DatabaseException
	{
		return new app.JpaDatabase();
	}

	@Bean
	public ComputeExecutorTask computeExecutorTask() throws DatabaseException
	{
		return new ComputeExecutorTask(computeExecutor(), taskScheduler());
	}

	@Bean
	public ComputeExecutor computeExecutor() throws DatabaseException
	{
		return new ComputeExecutorPilotDB(unathorizedDatabase());
	}

	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler()
	{
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(2);

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

	/**
	 * Enable spring freemarker viewresolver. All freemarker template names
	 * should end with '.ftl'
	 */
	@Bean
	public ViewResolver viewRespolver()
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