package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.EntityMetaData;

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
		// Do nothing
		// FIXME reindex from source documents (see https://github.com/molgenis/molgenis/issues/3309)
		// Do not throw UnsupportedOperationException here because reindexing of repos at startup will fail
	}
}