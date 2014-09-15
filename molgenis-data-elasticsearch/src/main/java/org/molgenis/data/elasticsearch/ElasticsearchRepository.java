package org.molgenis.data.elasticsearch;

import org.molgenis.data.EntityMetaData;

public class ElasticsearchRepository extends AbstractElasticsearchRepository
{
	private final EntityMetaData entityMetaData;

	public ElasticsearchRepository(EntityMetaData entityMetaData, SearchService elasticSearchService)
	{
		super(elasticSearchService);
		if (entityMetaData == null) throw new IllegalArgumentException("entityMetaData is null");
		this.entityMetaData = entityMetaData;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}
}
