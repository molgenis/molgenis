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
	private final EntityManager entityManager;

	public EntityReferenceResolverDecorator(Repository<Entity> delegateRepository, EntityManager entityManager)
	{
		super(delegateRepository);
		this.entityManager = requireNonNull(entityManager);
	}

	// Resolve entity references based on given fetch
	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		Stream<Entity> entities = delegate().findAll(q);
		return resolveEntityReferences(entities, q.getFetch());
	}

	// Resolve entity references based on given fetch
	@Override
	public Entity findOne(Query<Entity> q)
	{
		Entity entity = delegate().findOne(q);
		return entity != null ? resolveEntityReferences(entity, q.getFetch()) : null;
	}

	// Resolve entity references
	@Override
	public Iterator<Entity> iterator()
	{
		Stream<Entity> entities = delegate().findAll(new QueryImpl<>());
		return resolveEntityReferences(entities).iterator();
	}

	// Resolve entity references
	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, entities ->
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
		Entity entity = delegate().findOneById(id);
		return entity != null ? resolveEntityReferences(entity) : null;
	}

	// Resolve entity references based on given fetch
	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		Entity entity = delegate().findOneById(id, fetch);
		return entity != null ? resolveEntityReferences(entity, fetch) : null;
	}

	// Resolve entity references
	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = delegate().findAll(ids);
		return resolveEntityReferences(entities);
	}

	// Resolve entity references based on given fetch
	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		Stream<Entity> entities = delegate().findAll(ids, fetch);
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
