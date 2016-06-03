package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.ReindexActionRepositoryCollectionDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Applies decorators to all {@link RepositoryCollection RepositoryCollections}.
 */
@Component
public class RepositoryCollectionDecoratorFactory
{
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final ReindexActionRegisterService reindexActionRegisterService;

	@Autowired
	public RepositoryCollectionDecoratorFactory(RepositoryDecoratorFactory repositoryDecoratorFactory,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	public RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		RepositoryCollection repoCollectionDecorator = new RepositoryCollectionDecorator(repositoryCollection,
				repositoryDecoratorFactory);
		repoCollectionDecorator = new ReindexActionRepositoryCollectionDecorator(repoCollectionDecorator,
				reindexActionRegisterService);
		return repoCollectionDecorator;
	}

}
