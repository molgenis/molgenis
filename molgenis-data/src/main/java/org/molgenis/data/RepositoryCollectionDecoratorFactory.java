package org.molgenis.data;

import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryCollectionDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Applies decorators to all {@link RepositoryCollection RepositoryCollections}.
 */
@Component
public class RepositoryCollectionDecoratorFactory
{
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final IndexActionRegisterService indexActionRegisterService;

	@Autowired
	public RepositoryCollectionDecoratorFactory(RepositoryDecoratorFactory repositoryDecoratorFactory,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
	}

	RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		RepositoryCollection repoCollectionDecorator = new RepositoryCollectionDecorator(repositoryCollection,
				repositoryDecoratorFactory);
		repoCollectionDecorator = new IndexActionRepositoryCollectionDecorator(repoCollectionDecorator,
				indexActionRegisterService);
		return repoCollectionDecorator;
	}

}
