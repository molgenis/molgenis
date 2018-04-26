package org.molgenis.data.index;

import org.molgenis.data.AbstractRepositoryCollectionDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

/**
 * Decorator around a {@link Repository} that registers changes made to its data with the
 * {@link IndexActionRegisterServiceImpl}.
 */
public class IndexActionRepositoryCollectionDecorator extends AbstractRepositoryCollectionDecorator
{
	private final IndexActionRegisterService indexActionRegisterService;

	public IndexActionRepositoryCollectionDecorator(RepositoryCollection delegateRepositoryCollection,
			IndexActionRegisterService indexActionRegisterService)
	{
		super(delegateRepositoryCollection);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		this.indexActionRegisterService.register(entityType, null);
		delegate().deleteRepository(entityType);
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		this.indexActionRegisterService.register(entityType, null);
		delegate().updateRepository(entityType, updatedEntityType);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		this.indexActionRegisterService.register(entityType, null);
		delegate().addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		this.indexActionRegisterService.register(entityType, null);
		delegate().updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		this.indexActionRegisterService.register(entityType, null);
		delegate().deleteAttribute(entityType, attr);
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		this.indexActionRegisterService.register(entityType, null);
		return delegate().createRepository(entityType);
	}
}
