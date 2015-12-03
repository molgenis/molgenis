package org.molgenis.data.examples;

import java.util.Collections;
import java.util.UUID;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Samples configuration
 */
@Configuration
public class AppConfig
{
	@Autowired
	public SourceToEntityConverter sourceToEntityConverter;

	@Autowired
	public EntityToSourceConverter entityToSourceConverter;

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
	public ElasticsearchEntityFactory elasticsearchEntityFactory()
	{
		return new ElasticsearchEntityFactory(entityManager(), sourceToEntityConverter, entityToSourceConverter);
	}

	@Bean
	public SearchService searchService()
	{
		return embeddedElasticSearchServiceFactory().create(dataService(), elasticsearchEntityFactory());
	}

	@Bean
	public DataService dataService()
	{
		return new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory());
	}

	@Bean
	public EntityManager entityManager()
	{
		return new EntityManagerImpl(dataService());
	}

	@Bean
	public MetaDataService metaDataService()
	{
		DataServiceImpl dataService = (DataServiceImpl) dataService();
		MetaDataService metaDataService = new MetaDataServiceImpl(dataService);
		dataService.setMeta(metaDataService);

		metaDataService.setDefaultBackend(elasticsearchRepositoryCollection());

		return metaDataService;
	}

}
