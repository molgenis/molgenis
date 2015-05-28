package org.molgenis.data.rest.v2;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class RestConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addConverter(new AttributeFilterConverter());
	}
}
