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
	private final RepositoryCollectionDecoratorFactory repositoryCollectionDecoratorFactory;

	private Map<String, RepositoryCollection> repositoryCollectionMap;

	@Autowired
	public RepositoryCollectionRegistry(RepositoryCollectionDecoratorFactory repositoryCollectionDecoratorFactory)
	{
		this.repositoryCollectionDecoratorFactory = requireNonNull(repositoryCollectionDecoratorFactory);
		repositoryCollectionMap = new HashMap<>();
	}

	public Stream<RepositoryCollection> getRepositoryCollections()
	{
		return repositoryCollectionMap.values().stream()
				.map(repositoryCollectionDecoratorFactory::createDecoratedRepositoryCollection);
	}

	public void addRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		repositoryCollectionMap.put(repositoryCollection.getName(), repositoryCollection);
	}

	public RepositoryCollection getRepositoryCollection(String name)
	{
		RepositoryCollection repositoryCollection = repositoryCollectionMap.get(name);
		return repositoryCollection != null ? repositoryCollectionDecoratorFactory
				.createDecoratedRepositoryCollection(repositoryCollection) : null;
	}

	public boolean hasRepositoryCollection(String name)
	{
		return repositoryCollectionMap.containsKey(name);
	}
}
