package org.molgenis.data.postgresql;

import org.molgenis.data.AbstractRepositoryCollectionDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;

import static java.util.Objects.requireNonNull;

/**
 * Repository collection decorator that updates {@link EntityTypeRegistry} when entity types are create, updated or deleted.
 */
public class PostgreSqlRepositoryCollectionDecorator extends AbstractRepositoryCollectionDecorator
{
	private final EntityTypeRegistry entityTypeRegistry;

	PostgreSqlRepositoryCollectionDecorator(RepositoryCollection delegateRepositoryCollection, EntityTypeRegistry entityTypeRegistry)
	{
		super(delegateRepositoryCollection);
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		Repository<Entity> repo = delegate().createRepository(entityType);
		entityTypeRegistry.registerEntityType(entityType);
		return repo;
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		delegate().deleteRepository(entityType);
		entityTypeRegistry.unregisterEntityType(entityType);
	}

	@Override
	public void updateRepository(EntityType entityType, EntityType updatedEntityType)
	{
		delegate().updateRepository(entityType, updatedEntityType);
		entityTypeRegistry.registerEntityType(updatedEntityType);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		entityTypeRegistry.addAttribute(entityType, attribute);
		delegate().addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		entityTypeRegistry.updateAttribute(entityType, attr, updatedAttr);
		delegate().updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		entityTypeRegistry.deleteAttribute(entityType, attr);
		delegate().deleteAttribute(entityType, attr);
	}
}
