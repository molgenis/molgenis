package org.molgenis.data.postgresql;

import org.molgenis.data.AbstractRepositoryCollectionDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.EntityType;
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

	PostgreSqlRepositoryCollectionDecorator(RepositoryCollection repoCollection, EntityTypeRegistry entityTypeRegistry,
			EntityTypeCopier entityTypeCopier)
	{
		this.repoCollection = requireNonNull(repoCollection);
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
		this.entityTypeCopier = requireNonNull(entityTypeCopier);
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
	public Repository<Entity> updateRepository(EntityType entityType)
	{
		Repository<Entity> repo = repoCollection.updateRepository(entityType);
		entityTypeRegistry.registerEntityType(entityType);
		return repo;
	}

	@Override
	public void deleteRepository(EntityType entityType)
	{
		repoCollection.deleteRepository(entityType);
		entityTypeRegistry.unregisterEntityType(entityType);
	}
}
