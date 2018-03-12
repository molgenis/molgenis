package org.molgenis.data.platform;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionDecorator;
import org.molgenis.data.RepositoryCollectionDecoratorFactory;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryCollectionDecorator;
import org.molgenis.data.security.RepositoryCollectionSecurityDecoratorFactory;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class RepositoryCollectionDecoratorFactoryImpl implements RepositoryCollectionDecoratorFactory
{
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final IndexActionRegisterService indexActionRegisterService;
	private final RepositoryCollectionSecurityDecoratorFactory repositoryCollectionSecurityDecoratorFactory;

	public RepositoryCollectionDecoratorFactoryImpl(RepositoryDecoratorFactory repositoryDecoratorFactory,
			IndexActionRegisterService indexActionRegisterService,
			RepositoryCollectionSecurityDecoratorFactory repositoryCollectionSecurityDecoratorFactory)
	{
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
		this.repositoryCollectionSecurityDecoratorFactory = requireNonNull(
				repositoryCollectionSecurityDecoratorFactory);
	}

	@Override
	public RepositoryCollection createDecoratedRepositoryCollection(RepositoryCollection repositoryCollection)
	{
		RepositoryCollection repoCollectionDecorator;
		repoCollectionDecorator = repositoryCollectionSecurityDecoratorFactory.createDecoratedRepositoryCollection(
				repositoryCollection);
		repoCollectionDecorator = new RepositoryCollectionDecorator(repoCollectionDecorator,
				repositoryDecoratorFactory);
		repoCollectionDecorator = new IndexActionRepositoryCollectionDecorator(repoCollectionDecorator,
				indexActionRegisterService);
		return repoCollectionDecorator;
	}

}
