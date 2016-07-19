package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Registry containing all {@link RepositoryCollection RepositoryCollections}.
 */
@Component
public class RepositoryCollectionRegistry
{
	private final RepositoryCollectionDecoratorFactory repoCollectionDecoratorFactory;

	private Map<String, RepositoryCollection> repoCollectionMap;
	private RepositoryCollection defaultRepoCollection;

	@Autowired
	public RepositoryCollectionRegistry(RepositoryCollectionDecoratorFactory repoCollectionDecoratorFactory)
	{
		this.repoCollectionDecoratorFactory = requireNonNull(repoCollectionDecoratorFactory);
		repoCollectionMap = new HashMap<>();
	}

	public Stream<RepositoryCollection> getRepositoryCollections()
	{
		return repoCollectionMap.values().stream()
				.map(repoCollectionDecoratorFactory::createDecoratedRepositoryCollection);
	}

	public void addRepositoryCollection(RepositoryCollection repoCollection)
	{
		repoCollectionMap.put(repoCollection.getName(), repoCollection);
	}

	public RepositoryCollection getRepositoryCollection(String name)
	{
		RepositoryCollection repoCollection = repoCollectionMap.get(name);
		return repoCollection != null ? repoCollectionDecoratorFactory
				.createDecoratedRepositoryCollection(repoCollection) : null;
	}

	public boolean hasRepositoryCollection(String name)
	{
		return repoCollectionMap.containsKey(name);
	}

	public RepositoryCollection getDefaultRepoCollection()
	{
		return repoCollectionDecoratorFactory.createDecoratedRepositoryCollection(defaultRepoCollection);
	}

	public void setDefaultRepoCollection(RepositoryCollection defaultRepoCollection)
	{
		this.defaultRepoCollection = requireNonNull(defaultRepoCollection);
	}
}
