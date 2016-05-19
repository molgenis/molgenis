package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterators.spliteratorUnknownSize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Applies decorators to all {@link RepositoryCollection RepositoryCollections}.
 */
@Component
public class RepositoryCollectionDecoratorFactory
{
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	@Autowired
	public RepositoryCollectionDecoratorFactory(RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
	}

	public RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		return new RepositoryCollectionDecorator(repositoryCollection, repositoryDecoratorFactory);
	}

}
