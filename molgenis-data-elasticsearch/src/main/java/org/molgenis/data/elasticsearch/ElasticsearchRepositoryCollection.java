package org.molgenis.data.elasticsearch;

import static autovalue.shaded.com.google.common.common.collect.Sets.immutableEnumSet;
import static org.molgenis.data.RepositoryCollectionCapability.UPDATABLE;
import static org.molgenis.data.RepositoryCollectionCapability.WRITABLE;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.AbstractRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection extends AbstractRepositoryCollection
{
	public static final String NAME = "ElasticSearch";
	private final SearchService searchService;
	private final DataService dataService;
	private final Map<String, AbstractElasticsearchRepository> repositories = Maps.newLinkedHashMap();

	@Autowired
	public ElasticsearchRepositoryCollection(SearchService searchService, DataService dataService)
	{
		this.searchService = searchService;
		this.dataService = dataService;
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		ElasticsearchRepository repo = new ElasticsearchRepository(entityMeta, searchService);
		if (!searchService.hasMapping(entityMeta))
		{
			searchService.createMappings(entityMeta);
		}
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return immutableEnumSet(EnumSet.of(WRITABLE, UPDATABLE));
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMetaData)
	{
		return getRepository(entityMetaData.getName());
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return Iterators.transform(repositories.values().iterator(), input -> input);
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		// remove the repo
		AbstractElasticsearchRepository r = repositories.get(entityMeta);
		if (r != null)
		{
			searchService.delete(entityMeta.getName());
			repositories.remove(entityMeta.getName());
		}
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		EntityMetaData entityMetaData;
		try
		{
			entityMetaData = (EntityMetaData) dataService.getEntityMetaData(entityName);
		}
		catch (ClassCastException ex)
		{
			throw new RuntimeException("Cannot cast EntityMetaData to EntityMetaData " + ex);
		}
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		entityMetaData.addAttribute(attribute);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		throw new UnsupportedOperationException(); // FIXME
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityName);
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		EntityMetaData EntityMetaData = new EntityMetaDataImpl(dataService.getMeta().getEntityMetaData(entityName));
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null) throw new UnknownAttributeException(String.format("Unknown attribute '%s' of entity '%s'",
				attributeName, entityName));

		EntityMetaData.removeAttribute(attr);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public boolean hasRepository(String name)
	{
		return name != null && Iterables.contains(getEntityNames(), name);
	}

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return hasRepository(entityMeta.getName());
	}
}
