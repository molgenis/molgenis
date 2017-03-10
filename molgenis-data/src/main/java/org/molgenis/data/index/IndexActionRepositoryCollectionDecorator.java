package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Decorator around a {@link Repository} that registers changes made to its data with the
 * {@link IndexActionRegisterServiceImpl}.
 */
public class IndexActionRepositoryCollectionDecorator implements RepositoryCollection
{
	private final RepositoryCollection decorated;
	private final IndexActionRegisterService indexActionRegisterService;

	public IndexActionRepositoryCollectionDecorator(RepositoryCollection decorated,
			IndexActionRegisterService indexActionRegisterService)
	{
		this.decorated = requireNonNull(decorated);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
	}

	@Override
	public String getName()
	{
		return this.decorated.getName();
	}

	public boolean hasRepository(String id)
	{
		return this.decorated.hasRepository(id);
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		this.indexActionRegisterService.register(entityType, null);
		this.decorated.deleteRepository(entityType);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		this.indexActionRegisterService.register(entityType, null);
		this.decorated.addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		this.indexActionRegisterService.register(entityType, null);
		this.decorated.updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		this.indexActionRegisterService.register(entityType, null);
		this.decorated.deleteAttribute(entityType, attr);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return this.decorated.iterator();
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		this.indexActionRegisterService.register(entityType, null);
		return this.decorated.createRepository(entityType);
	}

	@Override
	public Repository<Entity> updateRepository(EntityType entityType)
	{
		return this.decorated.updateRepository(entityType);
	}

	@Override
	public Iterable<String> getEntityIds()
	{
		return this.decorated.getEntityIds();
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public Repository<Entity> getRepository(EntityType entityType)
	{
		return decorated.getRepository(entityType);
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return decorated.hasRepository(entityType);
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return this.decorated.getRepository(name);
	}
}
