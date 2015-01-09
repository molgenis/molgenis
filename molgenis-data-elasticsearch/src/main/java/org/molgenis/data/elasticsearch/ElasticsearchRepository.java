package org.molgenis.data.elasticsearch;

import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.springframework.transaction.annotation.Transactional;

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
	public void rebuildIndex()
	{
		throw new UnsupportedOperationException(); // FIXME reindex from source
													// documents
	}

	@Override
	public void create()
	{
		// FIXME
	}

	@Override
	public void drop()
	{
		// FIXME
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		elasticSearchService.index(entity, getEntityMetaData(), ElasticSearchService.IndexingMode.UPDATE);
		// FIXME: use metadataService when ElasticSearch uses this service instead of managing its own metadata
		try
		{
			elasticSearchService.createMappings(getEntityMetaData(), true, true, true, true);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not update the ElasticSeach mapping");
		}
		elasticSearchService.refresh();
	}
}