package org.molgenis.data.elasticsearch;

import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

/**
 * Adds indexing functionality to a RepositoryCollection
 */
public class IndexedRepositoryCollectionDecorator implements RepositoryCollection
{
	private final SearchService searchService;
	private RepositoryCollection delegate;

	public IndexedRepositoryCollectionDecorator(SearchService searchService, RepositoryCollection delegate)
	{
		this.searchService = searchService;
		this.delegate = delegate;
	}

	protected IndexedRepositoryCollectionDecorator(SearchService searchService)
	{
		this(searchService, null);
	}

	protected void setDelegate(RepositoryCollection delegate)
	{
		this.delegate = delegate;
	}

	protected RepositoryCollection getDelegate()
	{
		return delegate;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return new Iterator<Repository<Entity>>()
		{
			Iterator<Repository<Entity>> it = delegate.iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository<Entity> next()
			{
				return new IndexedRepositoryDecorator(it.next(), searchService);
			}

		};
	}

	@Override
	public String getName()
	{
		return delegate.getName();
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return delegate.getCapabilities();
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		Repository<Entity> repo = delegate.createRepository(entityMeta);
		searchService.createMappings(entityMeta);

		return new IndexedRepositoryDecorator(repo, searchService);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return delegate.getEntityNames();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return new IndexedRepositoryDecorator(delegate.getRepository(name), searchService);
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMetaData)
	{
		return getRepository(entityMetaData.getName());
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(EntityMetaData entityMeta, Class<E> clazz)
	{
		throw new UnsupportedOperationException(); // FIXME implement
	}

	/**
	 * Get undelying not indexed repository
	 *
	 * @param name
	 * @return
	 */
	public Repository<Entity> getUnderlying(String name)
	{
		return delegate.getRepository(name);
	}

	protected SearchService getSearchService()
	{
		return searchService;
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

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return hasRepository(entityMeta.getName());
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		throw new UnsupportedOperationException(); // FIXME implement
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		throw new UnsupportedOperationException(); // FIXME
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		throw new UnsupportedOperationException(); // FIXME implement
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		throw new UnsupportedOperationException(); // FIXME implement
	}
}
