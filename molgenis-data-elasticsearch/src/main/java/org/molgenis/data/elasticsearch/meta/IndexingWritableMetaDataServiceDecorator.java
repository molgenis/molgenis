package org.molgenis.data.elasticsearch.meta;

import java.io.IOException;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.MetaDataService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta data repository for attributes that wraps an existing repository
 */
public class IndexingWritableMetaDataServiceDecorator implements MetaDataService
{
	private final MetaDataService delegate;
	private final SearchService elasticSearchService;

	public IndexingWritableMetaDataServiceDecorator(MetaDataService delegate, SearchService elasticSearchService)
	{
		if (delegate == null) throw new IllegalArgumentException("metaDataRepositories is null");
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.delegate = delegate;
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	@Transactional
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		delegate.addAttribute(entityName, attribute);
		updateMappings(entityName);
	}

	@Override
	@Transactional
	public void deleteAttribute(String entityName, String attributeName)
	{
		delegate.deleteAttribute(entityName, attributeName);
		updateMappings(entityName);
	}

	private void updateMappings(String entityName)
	{
		try
		{
			elasticSearchService.createMappings(delegate.getEntityMetaData(entityName));
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	@Transactional
	public Repository addEntityMeta(EntityMetaData entityMetaData)
	{
		Repository repo = delegate.addEntityMeta(entityMetaData);

		try
		{
			elasticSearchService.createMappings(entityMetaData);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}

		return repo;
	}

	@Override
	public Iterable<EntityMetaData> getEntityMetaDatas()
	{
		return delegate.getEntityMetaDatas();
	}

	@Override
	public void deleteEntityMeta(String name)
	{
		delegate.deleteEntityMeta(name);
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

	@Override
	public List<AttributeMetaData> updateEntityMeta(EntityMetaData entityMeta)
	{
		List<AttributeMetaData> attributes = delegate.updateEntityMeta(entityMeta);
		updateMappings(entityMeta.getName());

		return attributes;
	}

	@Override
	public void setDefaultBackend(ManageableRepositoryCollection backend)
	{
		delegate.setDefaultBackend(backend);
	}

	@Override
	public List<AttributeMetaData> updateSync(EntityMetaData sourceEntityMetaData)
	{
		return delegate.updateSync(sourceEntityMetaData);
	}

	@Override
	public ManageableRepositoryCollection getDefaultBackend()
	{
		return delegate.getDefaultBackend();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		delegate.onApplicationEvent(event);
	}

	@Override
	public int getOrder()
	{
		return delegate.getOrder();
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		delegate.addAttributeSync(entityName, attribute);
	}

}
