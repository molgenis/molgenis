package org.molgenis.data;

/**
 * Applies decorators to all {@link RepositoryCollection RepositoryCollections}.
 */
public interface RepositoryCollectionDecoratorFactory
{
	RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection);
}
