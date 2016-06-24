package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;

public class ElasticsearchRepository extends AbstractElasticsearchRepository
{
	private final EntityMetaData entityMetaData;

	public ElasticsearchRepository(EntityMetaData entityMetaData, SearchService elasticSearchService)
	{
		super(elasticSearchService);
		this.entityMetaData = requireNonNull(entityMetaData);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public void rebuildIndex()
	{
		Iterable<Entity> entities = elasticSearchService.search(new QueryImpl(this), getEntityMetaData());
		elasticSearchService.rebuildIndex(entities, entityMetaData);
	}
}