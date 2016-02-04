package org.molgenis.data.view;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;


@Component("ViewRepositoryCollection")
public class ViewRepositoryCollection implements ManageableRepositoryCollection
{
	public static final String NAME = "VIEW";
	private final SearchService searchService;
	private final DataService dataService;
	private final Map<String, ViewRepository> repositories = Maps.newLinkedHashMap();

	@Autowired
	public ViewRepositoryCollection(SearchService searchService, DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		ViewRepository repo = new ViewRepository(entityMeta, dataService, searchService);
		if (!searchService.hasMapping(entityMeta)) repo.create();
		repositories.put(entityMeta.getName(), repo);
		return repo;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return Iterators.transform(repositories.values().iterator(), new Function<ViewRepository, Repository>()
		{
			@Override
			public Repository apply(ViewRepository input)
			{
				return input;
			}
		});
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		ViewRepository repo = repositories.get(entityName);
		if (repo != null)
		{
			repo.drop();
			repositories.remove(entityName);
		}
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		DefaultEntityMetaData entityMetaData;
		try
		{
			entityMetaData = (DefaultEntityMetaData) dataService.getEntityMetaData(entityName);
		}
		catch (ClassCastException ex)
		{
			throw new RuntimeException("Cannot cast EntityMetaData to DefaultEntityMetadata " + ex);
		}
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		entityMetaData.addAttributeMetaData(attribute);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityName);
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(dataService.getMeta()
				.getEntityMetaData(entityName));
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null) throw new UnknownAttributeException(String.format("Unknown attribute '%s' of entity '%s'",
				attributeName, entityName));

		defaultEntityMetaData.removeAttributeMetaData(attr);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		addAttribute(entityName, attribute);
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		Iterator<String> entityNames = getEntityNames().iterator();
		while (entityNames.hasNext())
		{
			if (entityNames.next().equals(name)) return true;
		}
		return false;
	}

}
