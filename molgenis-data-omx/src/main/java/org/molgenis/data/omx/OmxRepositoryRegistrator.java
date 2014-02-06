package org.molgenis.data.omx;

import org.molgenis.data.DataService;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.SearchService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * Add startup add an OmxRepository to the DataService for all existing DataSets
 */
public class OmxRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final SearchService searchService;

	public OmxRepositoryRegistrator(DataService dataService, SearchService searchService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (searchService == null) throw new IllegalArgumentException("searchService is null");
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		for (DataSet dataSet : dataService.findAll(DataSet.ENTITY_NAME, DataSet.class))
		{
			OmxRepository repo = new OmxRepository(dataService, searchService, dataSet.getIdentifier());
			dataService.addRepository(repo);
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}
}