package org.molgenis.lifelines;

import java.util.List;

import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
@EnableWebMvc
@ComponentScan("org.molgenis")
@Import(EmbeddedElasticSearchConfig.class)
public class WebAppConfig extends WebMvcConfigurerAdapter
{
	private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).setPrettyPrinting().disableHtmlEscaping()
				.create();

		converters.add(new GsonHttpMessageConverter(gson));
	}
}
