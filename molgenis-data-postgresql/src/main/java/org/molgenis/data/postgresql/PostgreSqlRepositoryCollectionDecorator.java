package org.molgenis.data.postgresql;

import org.molgenis.data.AbstractRepositoryCollectionDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.util.AttributeCopier;
import org.molgenis.data.meta.util.EntityTypeCopier;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;

import static java.util.Objects.requireNonNull;

/**
 * Repository collection decorator that updates {@link EntityTypeRegistry} when entity types are create, updated or deleted.
 */
public class PostgreSqlRepositoryCollectionDecorator extends AbstractRepositoryCollectionDecorator
{
	private final EntityTypeRegistry entityTypeRegistry;
	private final EntityTypeCopier entityTypeCopier;
	private final AttributeCopier attributeCopier;

	PostgreSqlRepositoryCollectionDecorator(RepositoryCollection delegateRepositoryCollection, EntityTypeRegistry entityTypeRegistry,
			EntityTypeCopier entityTypeCopier, AttributeCopier attributeCopier)
	{
		super(delegateRepositoryCollection);
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
		this.entityTypeCopier = requireNonNull(entityTypeCopier);
		this.attributeCopier = requireNonNull(attributeCopier);
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
		EntityType updatedEntityType = entityTypeCopier.copy(entityType);
		Attribute attributeCopy = attributeCopier.copy(attribute);
		updatedEntityType.addAttribute(attributeCopy);

		entityTypeRegistry.registerEntityType(updatedEntityType);
		delegate().addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		EntityType updatedEntityType = entityTypeCopier.copy(entityType);
		Attribute updatedAttributeCopy = attributeCopier.copy(updatedAttr);
		updatedEntityType.removeAttribute(attr);
		updatedEntityType.addAttribute(updatedAttributeCopy);

		entityTypeRegistry.registerEntityType(updatedEntityType);
		delegate().updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		EntityType updatedEntityType = entityTypeCopier.copy(entityType);
		updatedEntityType.removeAttribute(attr);

		entityTypeRegistry.registerEntityType(updatedEntityType);
		delegate().deleteAttribute(entityType, attr);
	}
}
