package org.molgenis.data;

import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryCollectionDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class RepositoryCollectionDecoratorFactoryImpl implements RepositoryCollectionDecoratorFactory
{
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final IndexActionRegisterService indexActionRegisterService;

	@Autowired
	public RepositoryCollectionDecoratorFactoryImpl(RepositoryDecoratorFactory repositoryDecoratorFactory,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
	}

	@Override
	public RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		RepositoryCollection repoCollectionDecorator = new RepositoryCollectionDecorator(repositoryCollection,
				repositoryDecoratorFactory);
		repoCollectionDecorator = new IndexActionRepositoryCollectionDecorator(repoCollectionDecorator,
				indexActionRegisterService);
		return repoCollectionDecorator;
	}

}
