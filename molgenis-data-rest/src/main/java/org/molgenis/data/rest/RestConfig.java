package org.molgenis.data.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * Spring configuration class for the molgenis-data-rest package
 * 
 * @author erwin
 * 
 */
@Configuration
public class RestConfig
{
	/**
	 * Prevents non authenticated access to the rest api
	 * 
	 * @return MappedInterceptor
	 */
	@Bean
	public MappedInterceptor checkAuthenticatedInterceptor()
	{
		return new MappedInterceptor(new String[]
		{ RestController.BASE_URI + "/**" }, new RestApiSecurityHandlerInterceptor());
	}
}
