package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.EntityMetaData;

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
		elasticSearchService.rebuildIndex(this);
	}
}