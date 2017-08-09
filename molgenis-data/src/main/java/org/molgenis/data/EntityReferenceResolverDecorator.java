package org.molgenis.data;

import org.molgenis.data.support.QueryImpl;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EntityReferenceResolverDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decoratedRepo;
	private final EntityManager entityManager;

	public EntityReferenceResolverDecorator(Repository<Entity> decoratedRepo, EntityManager entityManager)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepo;
	}

	// Resolve entity references based on given fetch
	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		Stream<Entity> entities = decoratedRepo.findAll(q);
		return resolveEntityReferences(entities, q.getFetch());
	}

	// Resolve entity references based on given fetch
	@Override
	public Entity findOne(Query<Entity> q)
	{
		Entity entity = decoratedRepo.findOne(q);
		return entity != null ? resolveEntityReferences(entity, q.getFetch()) : null;
	}

	// Resolve entity references
	@Override
	public Iterator<Entity> iterator()
	{
		Stream<Entity> entities = decoratedRepo.findAll(new QueryImpl<>());
		return resolveEntityReferences(entities).iterator();
	}

	// Resolve entity references
	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decoratedRepo.forEachBatched(fetch, entities ->
		{
			List<Entity> resolvedEntities = resolveEntityReferences(entities.stream(), fetch).collect(
					Collectors.toList());
			consumer.accept(resolvedEntities);
		}, batchSize);
	}

	// Resolve entity references
	@Override
	public Entity findOneById(Object id)
	{
		Entity entity = decoratedRepo.findOneById(id);
		return entity != null ? resolveEntityReferences(entity) : null;
	}

	// Resolve entity references based on given fetch
	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		Entity entity = decoratedRepo.findOneById(id, fetch);
		return entity != null ? resolveEntityReferences(entity, fetch) : null;
	}

	// Resolve entity references
	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = decoratedRepo.findAll(ids);
		return resolveEntityReferences(entities);
	}

	// Resolve entity references based on given fetch
	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		Stream<Entity> entities = decoratedRepo.findAll(ids, fetch);
		return resolveEntityReferences(entities, fetch);
	}

	private Entity resolveEntityReferences(Entity entity)
	{
		return entityManager.resolveReferences(getEntityType(), entity, null);
	}

	private Entity resolveEntityReferences(Entity entity, Fetch fetch)
	{
		return entityManager.resolveReferences(getEntityType(), entity, fetch);
	}

	private Stream<Entity> resolveEntityReferences(Stream<Entity> entities)
	{
		return entityManager.resolveReferences(getEntityType(), entities, null);
	}

	private Stream<Entity> resolveEntityReferences(Stream<Entity> entities, Fetch fetch)
	{
		return entityManager.resolveReferences(getEntityType(), entities, fetch);
	}
}
