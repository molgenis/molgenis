package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;

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
	public Iterator<Repository> iterator()
	{
		return new Iterator<Repository>()
		{
			Iterator<Repository> it = delegate.iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository next()
			{
				return new ElasticsearchRepositoryDecorator(it.next(), searchService);
			}

		};
	}

	@Override
	public String getName()
	{
		return delegate.getName();
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		Repository repo = delegate.addEntityMeta(entityMeta);
		try
		{
			searchService.createMappings(entityMeta);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		return new ElasticsearchRepositoryDecorator(repo, searchService);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return delegate.getEntityNames();
	}

	@Override
	public Repository getRepository(String name)
	{
		return new ElasticsearchRepositoryDecorator(delegate.getRepository(name), searchService);
	}

	/**
	 * Get undelying not indexed repository
	 * 
	 * @param name
	 * @return
	 */
	public Repository getUnderlying(String name)
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

}
