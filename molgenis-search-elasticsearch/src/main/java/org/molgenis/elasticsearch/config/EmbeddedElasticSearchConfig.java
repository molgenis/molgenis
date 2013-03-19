package org.molgenis.elasticsearch.config;

import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.search.SearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory()
	{
		return new EmbeddedElasticSearchServiceFactory();
	}

	@Bean
	public SearchService searchService()
	{
		return embeddedElasticSearchServiceFactory().create();
	}
}
