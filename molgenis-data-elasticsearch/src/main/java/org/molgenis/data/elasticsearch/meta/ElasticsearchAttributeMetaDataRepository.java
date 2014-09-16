package org.molgenis.data.elasticsearch.meta;

import java.io.IOException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta data repository for attributes that wraps an existing repository
 */
public class ElasticsearchAttributeMetaDataRepository implements AttributeMetaDataRepository
{
	private final AttributeMetaDataRepository attributeMetaDataRepository;
	private final DataService dataService;
	private final SearchService elasticSearchService;

	public ElasticsearchAttributeMetaDataRepository(AttributeMetaDataRepository attributeMetaDataRepository,
			DataService dataService, SearchService elasticSearchService)
	{
		if (attributeMetaDataRepository == null) throw new IllegalArgumentException(
				"attributeMetaDataRepository is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.attributeMetaDataRepository = attributeMetaDataRepository;
		this.dataService = dataService;
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	public Iterable<AttributeMetaData> getEntityAttributeMetaData(String entityName)
	{
		return attributeMetaDataRepository.getEntityAttributeMetaData(entityName);
	}

	@Override
	@Transactional
	public void addAttributeMetaData(String entityName, AttributeMetaData attribute)
	{
		attributeMetaDataRepository.addAttributeMetaData(entityName, attribute);
		updateMappings(entityName);
	}

	@Override
	@Transactional
	public void removeAttributeMetaData(String entityName, String attributeName)
	{
		attributeMetaDataRepository.removeAttributeMetaData(entityName, attributeName);
		updateMappings(entityName);
	}

	private void updateMappings(String entityName)
	{
		try
		{
			elasticSearchService.createMappings(dataService.getEntityMetaData(entityName));
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}
}
