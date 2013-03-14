package org.molgenis.lifelines;

import java.util.List;

import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import app.DatabaseConfig;

@Configuration
@EnableWebMvc
@ComponentScan("org.molgenis")
@Import(
{ DatabaseConfig.class, EmbeddedElasticSearchConfig.class })
public class WebAppConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		converters.add(new GsonHttpMessageConverter());
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/css/**").addResourceLocations("/css/", "classpath:/css/");
		registry.addResourceHandler("/img/**").addResourceLocations("/img/", "classpath:/img/");
		registry.addResourceHandler("/js/**").addResourceLocations("/js/", "classpath:/js/");
	}

	@Controller
	@RequestMapping("/")
	public static class RootController
	{
		@RequestMapping(method = RequestMethod.GET)
		public String index()
		{
			return "forward:molgenis.do";
		}
	}
}
