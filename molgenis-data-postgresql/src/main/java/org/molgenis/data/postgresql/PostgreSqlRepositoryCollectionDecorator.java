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
	private final RepositoryCollection repoCollection;
	private final EntityTypeRegistry entityTypeRegistry;
	private final EntityTypeCopier entityTypeCopier;
	private final AttributeCopier attributeCopier;

	PostgreSqlRepositoryCollectionDecorator(RepositoryCollection repoCollection, EntityTypeRegistry entityTypeRegistry,
			EntityTypeCopier entityTypeCopier, AttributeCopier attributeCopier)
	{
		this.repoCollection = requireNonNull(repoCollection);
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
		this.entityTypeCopier = requireNonNull(entityTypeCopier);
		this.attributeCopier = requireNonNull(attributeCopier);
	}

	@Override
	protected RepositoryCollection delegate()
	{
		return repoCollection;
	}

	@Override
	public Repository<Entity> createRepository(EntityType entityType)
	{
		Repository<Entity> repo = repoCollection.createRepository(entityType);
		entityTypeRegistry.registerEntityType(entityType);
		return repo;
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		repoCollection.deleteRepository(entityType);
		entityTypeRegistry.unregisterEntityType(entityType);
	}

	@Override
	public void addAttribute(EntityType entityType, Attribute attribute)
	{
		EntityType updatedEntityType = entityTypeCopier.copy(entityType);
		Attribute attributeCopy = attributeCopier.copy(attribute);
		updatedEntityType.addAttribute(attributeCopy);

		entityTypeRegistry.registerEntityType(updatedEntityType);
		repoCollection.addAttribute(entityType, attribute);
	}

	@Override
	public void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr)
	{
		EntityType updatedEntityType = entityTypeCopier.copy(entityType);
		Attribute updatedAttributeCopy = attributeCopier.copy(updatedAttr);
		updatedEntityType.addAttribute(updatedAttributeCopy);

		entityTypeRegistry.registerEntityType(updatedEntityType);
		repoCollection.updateAttribute(entityType, attr, updatedAttr);
	}

	@Override
	public void deleteAttribute(EntityType entityType, Attribute attr)
	{
		EntityType updatedEntityType = entityTypeCopier.copy(entityType);
		updatedEntityType.removeAttribute(attr);

		entityTypeRegistry.registerEntityType(updatedEntityType);
		repoCollection.deleteAttribute(entityType, attr);
	}
}
