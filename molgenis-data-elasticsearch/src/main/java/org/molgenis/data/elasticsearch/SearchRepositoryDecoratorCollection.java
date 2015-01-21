package org.molgenis.data.elasticsearch;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;

public abstract class SearchRepositoryDecoratorCollection extends AbstractSearchRepositoryCollection
{

	public SearchRepositoryDecoratorCollection(SearchService searchService, DataService dataService, String name)
	{
		super(searchService, dataService, name);
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		Repository delegate = createRepository(entityMeta);
		ElasticsearchRepositoryDecorator repo = new ElasticsearchRepositoryDecorator(delegate, searchService);
		if (!searchService.hasMapping(entityMeta)) repo.create();
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

	protected abstract Repository createRepository(EntityMetaData entityMeta);

}
