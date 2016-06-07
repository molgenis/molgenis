package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

/**
 * Applies {@link Repository} decorators to all {@link RepositoryCollection} repositories.
 */
class RepositoryCollectionDecorator implements RepositoryCollection
{
	private final RepositoryCollection decoratedRepositoryCollection;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	RepositoryCollectionDecorator(RepositoryCollection decoratedRepositoryCollection,
			RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.decoratedRepositoryCollection = requireNonNull(decoratedRepositoryCollection);
		this.repositoryDecoratorFactory = requireNonNull(repositoryDecoratorFactory);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return StreamSupport.stream(spliteratorUnknownSize(decoratedRepositoryCollection.iterator(), ORDERED), false)
				.map(repositoryDecoratorFactory::createDecoratedRepository).iterator();
	}

	@Override
	public String getName()
	{
		return decoratedRepositoryCollection.getName();
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return decoratedRepositoryCollection.getCapabilities();
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		return repositoryDecoratorFactory
				.createDecoratedRepository(decoratedRepositoryCollection.createRepository(entityMeta));
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return decoratedRepositoryCollection.getEntityNames();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		Repository<Entity> repository = decoratedRepositoryCollection.getRepository(name);
		return repository != null ? repositoryDecoratorFactory.createDecoratedRepository(repository) : null;
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMeta)
	{
		Repository<Entity> repository = decoratedRepositoryCollection.getRepository(entityMeta);
		return repository != null ? repositoryDecoratorFactory.createDecoratedRepository(repository) : null;
	}

	@Override
	public boolean hasRepository(String name)
	{
		return decoratedRepositoryCollection.hasRepository(name);
	}

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return decoratedRepositoryCollection.hasRepository(entityMeta);
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		decoratedRepositoryCollection.deleteRepository(entityMeta);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		decoratedRepositoryCollection.addAttribute(entityName, attribute);
	}

	@Override
	public void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr)
	{
		decoratedRepositoryCollection.updateAttribute(entityMetaData, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		decoratedRepositoryCollection.deleteAttribute(entityName, attributeName);
	}
}
