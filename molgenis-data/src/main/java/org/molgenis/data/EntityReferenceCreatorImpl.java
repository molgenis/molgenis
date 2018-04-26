package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.LazyEntity;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@Component
public class EntityReferenceCreatorImpl implements EntityReferenceCreator
{
	private final DataService dataService;
	private final EntityFactoryRegistry entityFactoryRegistry;

	public EntityReferenceCreatorImpl(DataService dataService, EntityFactoryRegistry entityFactoryRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.entityFactoryRegistry = requireNonNull(entityFactoryRegistry);
	}

	@Override
	public Entity getReference(EntityType entityType, Object id)
	{
		Entity lazyEntity = new LazyEntity(entityType, dataService, id);

		EntityFactory<? extends Entity, ?> entityFactory = entityFactoryRegistry.getEntityFactory(entityType);
		if (entityFactory != null)
		{
			// create static entity (e.g. Tag, Language, Package) that wraps the constructed dynamic or partial entity.
			lazyEntity = entityFactory.create(lazyEntity);
		}

		return lazyEntity;
	}

	@Override
	public Iterable<Entity> getReferences(EntityType entityType, Iterable<?> ids)
	{
		EntityFactory<? extends Entity, ?> entityFactory = entityFactoryRegistry.getEntityFactory(entityType);
		return () -> stream(ids.spliterator(), false).map(id ->
		{
			Entity lazyEntity = getReference(entityType, id);
			if (entityFactory != null)
			{
				// create static entity (e.g. Tag, Language, Package) that wraps the constructed dynamic or partial entity.
				lazyEntity = entityFactory.create(lazyEntity);
			}
			return lazyEntity;
		}).iterator();
	}
}
