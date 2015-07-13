package org.molgenis.data.elasticsearch;

import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.UPDATEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.util.Set;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;

import com.google.common.collect.Sets;

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

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(AGGREGATEABLE, QUERYABLE, WRITABLE, UPDATEABLE);
	}

	@Override
	public void rebuildIndex()
	{
		// Do nothing
		// FIXME reindex from source documents (see https://github.com/molgenis/molgenis/issues/3309)
		// Do not throw UnsupportedOperationException here because reindexing of repos at startup will fail
	}

}