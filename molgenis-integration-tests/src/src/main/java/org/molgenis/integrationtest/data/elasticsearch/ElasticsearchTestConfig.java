package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.integrationtest.data.AbstractDataApiTestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchTestConfig extends AbstractDataApiTestConfig
{
	@Override
	protected ManageableRepositoryCollection getBackend()
	{
		return elasticsearchRepositoryCollection();
	}

	@Bean
	public ElasticsearchRepositoryCollection elasticsearchRepositoryCollection()
	{
		return new ElasticsearchRepositoryCollection(searchService, dataService());
	}

}
