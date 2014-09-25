package org.molgenis.data.elasticsearch.meta;

import java.io.IOException;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta data repository for entities that wraps an existing repository
 */
public class ElasticsearchEntityMetaDataRepository implements EntityMetaDataRepository
{
	private final EntityMetaDataRepository entityMetaDataRepository;
	private final SearchService elasticSearchService;

	public ElasticsearchEntityMetaDataRepository(EntityMetaDataRepository entityMetaDataRepository,
			SearchService elasticSearchService)
	{
		if (entityMetaDataRepository == null) throw new IllegalArgumentException("entityMetaDataRepository is null");
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		return entityMetaDataRepository.getEntityMetaDatas();
	}

	@Override
	public EntityMetaData getEntityMetaData(String name)
	{
		return entityMetaDataRepository.getEntityMetaData(name);
	}

	@Override
	@Transactional
	public void addEntityMetaData(EntityMetaData entityMetaData)
	{
		entityMetaDataRepository.addEntityMetaData(entityMetaData);

		try
		{
			elasticSearchService.createMappings(entityMetaData);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}
}
