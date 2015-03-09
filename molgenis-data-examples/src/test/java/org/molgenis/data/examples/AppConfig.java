package org.molgenis.data.examples;

import java.util.Collections;
import java.util.UUID;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Samples configuration
 */
@Configuration
public class AppConfig
{

	@Bean
	public UserMetaData userMetaData()
	{
		return UserMetaData.INSTANCE;
	}

	@Bean
	public ElasticsearchRepositoryCollection elasticsearchRepositoryCollection()
	{
		return new ElasticsearchRepositoryCollection(searchService(), dataService());
	}

	@Bean
	public MyRepositoryCollection myRepositoryCollection()
	{
		return new MyRepositoryCollection();
	}

	@Bean(destroyMethod = "close")
	public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory()
	{
		String dataPath = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString().replace("-", "");
		return new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", dataPath));
	}

	@Bean
	public SearchService searchService()
	{
		return embeddedElasticSearchServiceFactory().create(dataService(), new EntityToSourceConverter());
	}

	@Bean
	public DataService dataService()
	{
		return new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory());
	}

	@Bean
	public MetaDataService metaDataService()
	{
		DataServiceImpl dataService = (DataServiceImpl) dataService();
		MetaDataService metaDataService = new MetaDataServiceImpl(dataService);
		dataService.setMetaDataService(metaDataService);

		metaDataService.setDefaultBackend(elasticsearchRepositoryCollection());

		return metaDataService;
	}

}
