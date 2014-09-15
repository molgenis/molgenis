package org.molgenis.data.elasticsearch;

import org.molgenis.data.EntityMetaData;
import org.molgenis.elasticsearch.ElasticSearchService;

public class ElasticsearchRepository extends AbstractElasticsearchRepository
{
	private final EntityMetaData entityMetaData;

	public ElasticsearchRepository(EntityMetaData entityMetaData, ElasticSearchService elasticSearchService)
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
