package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

/**
 * Applies {@link Repository} decorators to all {@link RepositoryCollection} repositories.
 */
public class RepositoryCollectionDecorator extends AbstractRepositoryCollectionDecorator
{
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	public RepositoryCollectionDecorator(RepositoryCollection delegateRepositoryCollection,
			RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		super(delegateRepositoryCollection);
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return StreamSupport.stream(spliteratorUnknownSize(delegate().iterator(), ORDERED), false)
							.map(repositoryDecoratorFactory::createDecoratedRepository)
							.iterator();
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		return repositoryDecoratorFactory.createDecoratedRepository(delegate().createRepository(entityType));
	}

	@Override
	public Repository<Entity> getRepository(String id)
	{
		Repository<Entity> repository = delegate().getRepository(id);
		return repository != null ? repositoryDecoratorFactory.createDecoratedRepository(repository) : null;
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		Repository<Entity> repository = delegate().getRepository(entityType);
		return repository != null ? repositoryDecoratorFactory.createDecoratedRepository(repository) : null;
	}
}
