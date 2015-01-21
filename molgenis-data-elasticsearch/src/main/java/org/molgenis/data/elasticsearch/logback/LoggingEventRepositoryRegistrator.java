package org.molgenis.data.elasticsearch.logback;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Register the Elasticsearch repositories with the DataService
 */
@Component
public class LoggingEventRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

	@Autowired
	public LoggingEventRepositoryRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
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
