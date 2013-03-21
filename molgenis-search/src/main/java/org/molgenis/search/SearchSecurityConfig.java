package org.molgenis.search;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * Adds security support to the SearchController if you import it in your
 * application context
 * 
 * @author erwin
 * 
 */
@Configuration
public class SearchSecurityConfig
{
	@Bean
	public MappedInterceptor searchMappedInterceptor()
	{
		return new MappedInterceptor(new String[]
		{ SearchController.URI }, new SearchSecurityHandlerInterceptor());
	}
}
