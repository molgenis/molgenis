package org.molgenis.data.elasticsearch;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection extends AbstractSearchRepositoryCollection
{
	public static final String NAME = "ElasticSearch";

	@Autowired
	public ElasticsearchRepositoryCollection(SearchService searchService, DataService dataService)
	{
		super(searchService, dataService, NAME);
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		ElasticsearchRepository repo = new ElasticsearchRepository(entityMeta, searchService);
		if (!searchService.hasMapping(entityMeta)) repo.create();
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

}
