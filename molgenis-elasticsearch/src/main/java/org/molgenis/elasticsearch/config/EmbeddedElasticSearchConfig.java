package org.molgenis.elasticsearch.config;

import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.search.SearchService;
import org.molgenis.search.SearchServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring config for embedded elastic search server. Use this in your own app by
 * importing this in your spring config:
 * <code> @Import(EmbeddedElasticSearchConfig.class)</code>
 * 
 * @author erwin
 * 
 */
@Configuration
public class EmbeddedElasticSearchConfig
{
	@Bean(destroyMethod = "close")
	public SearchServiceFactory searchServiceFactory()
	{
		return new EmbeddedElasticSearchServiceFactory();
	}

	@Bean
	@Scope("request")
	public SearchService searchService()
	{
		return searchServiceFactory().create();
	}
}
