package org.molgenis.data.elasticsearch;

import org.molgenis.data.CrudRepositoryCollection;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.logback.LoggingEventMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Register the Elasticsearch repositories with the DataService
 */
@Component
public class ElasticsearchRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final CrudRepositoryCollection repositoryCollection;

	@Autowired
	public ElasticsearchRepositoryRegistrator(DataService dataService,
			@Qualifier("ElasticsearchRepositoryCollection") CrudRepositoryCollection repositoryCollection)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (repositoryCollection == null) throw new IllegalArgumentException(
				"ElasticsearchRepositoryCollection is missing");
		this.dataService = dataService;
		this.repositoryCollection = repositoryCollection;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		dataService.getMeta().addEntityMeta(LoggingEventMetaData.INSTANCE);
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}
