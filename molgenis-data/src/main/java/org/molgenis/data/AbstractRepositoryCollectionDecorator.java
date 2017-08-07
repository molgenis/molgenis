package org.molgenis.data;

import com.google.common.collect.ForwardingObject;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Abstract superclass for {@link RepositoryCollection} decorators that delegates everything to the
 * decorated repository collection.
 */
public abstract class AbstractRepositoryCollectionDecorator extends ForwardingObject implements RepositoryCollection
{
	private final RepositoryCollection delegateRepositoryCollection;

	public AbstractRepositoryCollectionDecorator(RepositoryCollection delegateRepositoryCollection)
	{
		this.delegateRepositoryCollection = requireNonNull(delegateRepositoryCollection);
	}

	@Override
	protected RepositoryCollection delegate()
	{
		return delegateRepositoryCollection;
	}

	@Override
	public String getName()
	{
		return delegate().getName();
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return delegate().getCapabilities();
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		return delegate().createRepository(entityType);
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return delegate().getEntityTypeIds();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return delegate().getRepository(name);
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		return delegate().getRepository(entityType);
	}

	@Override
	public boolean hasRepository(String name)
	{
		return delegate().hasRepository(name);
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return delegate().hasRepository(entityType);
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		delegate().deleteRepository(entityType);
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		delegate().updateRepository(entityType, updatedEntityType);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		delegate().addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		delegate().updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		delegate().deleteAttribute(entityType, attr);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return delegate().iterator();
	}
}
