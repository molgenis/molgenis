package org.molgenis.omx;

import java.util.List;

import org.molgenis.dataexplorer.config.DataExplorerConfig;
import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.search.SearchSecurityConfig;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
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
@Import(
{ DatabaseConfig.class, OmxConfig.class, EmbeddedElasticSearchConfig.class, DataExplorerConfig.class,
		SearchSecurityConfig.class })
public class WebAppConfig extends WebMvcConfigurerAdapter
{
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

	/**
	 * Used by system for example indexing. Should be replaced by a system user?
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Bean
	public Database unauthorizedDatabase() throws DatabaseException
	{
		return new app.JpaDatabase();
	}

	/**
	 * Redirects '/' to the Home plugin
	 * 
	 */
	@Controller
	@RequestMapping("/")
	public static class RootController
	{
		@RequestMapping(method =
		{ RequestMethod.GET, RequestMethod.POST })
		public String index()
		{
			return "redirect:molgenis.do?__target=main&select=Home";
		}
	}
}