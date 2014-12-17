package org.molgenis.data.elasticsearch.meta;

import java.io.IOException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta data repository for attributes that wraps an existing repository
 */
public class IndexingWritableMetaDataServiceDecorator implements WritableMetaDataService
{
	private final WritableMetaDataService delegate;
	private final DataService dataService;
	private final SearchService elasticSearchService;

	public IndexingWritableMetaDataServiceDecorator(WritableMetaDataService delegate, DataService dataService,
			SearchService elasticSearchService)
	{
		if (delegate == null) throw new IllegalArgumentException("metaDataRepositories is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.delegate = delegate;
		this.dataService = dataService;
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	@Transactional
	public void addAttributeMetaData(String entityName, AttributeMetaData attribute)
	{
		delegate.addAttributeMetaData(entityName, attribute);

		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		EditableEntityMetaData updatedEntityMetaData = new DefaultEntityMetaData(entityMetaData);
		updatedEntityMetaData.addAttributeMetaData(attribute);

		updateMappings(updatedEntityMetaData);
	}

	@Override
	@Transactional
	public void removeAttributeMetaData(String entityName, String attributeName)
	{
		delegate.removeAttributeMetaData(entityName, attributeName);

		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		EditableEntityMetaData updatedEntityMetaData = new DefaultEntityMetaData(entityMetaData);
		updatedEntityMetaData.removeAttributeMetaData(attributeName);

		updateMappings(updatedEntityMetaData);
	}

	private void updateMappings(EntityMetaData entityMetaData)
	{
		try
		{
			elasticSearchService.createMappings(entityMetaData);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	@Transactional
	public void addEntityMetaData(EntityMetaData entityMetaData)
	{
		delegate.addEntityMetaData(entityMetaData);

		updateMappings(entityMetaData);
	}

	@Override
	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		return delegate.getEntityMetaDatas();
	}

	@Override
	public void removeEntityMetaData(String name)
	{
		delegate.removeEntityMetaData(name);
		elasticSearchService.delete(name);
	}

	@Override
	public Iterable<Package> getRootPackages()
	{
		return delegate.getRootPackages();
	}

	@Override
	public Package getPackage(String name)
	{
		return delegate.getPackage(name);
	}

	@Override
	public void addPackage(Package p)
	{
		delegate.addPackage(p);
	}

	@Override
	public EntityMetaData getEntityMetaData(String name)
	{
		return delegate.getEntityMetaData(name);
	}

	@Override
	public void refreshCaches()
	{
		delegate.refreshCaches();
	}

}
