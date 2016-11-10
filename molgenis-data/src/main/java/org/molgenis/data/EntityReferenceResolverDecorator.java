package org.molgenis.data;

import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EntityReferenceResolverDecorator implements Repository<Entity>
{
	private final Repository<Entity> decoratedRepo;
	private final EntityManager entityManager;

	public EntityReferenceResolverDecorator(Repository<Entity> decoratedRepo, EntityManager entityManager)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decoratedRepo.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	public EntityType getEntityType()
	{
		return decoratedRepo.getEntityType();
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return decoratedRepo.count(q);
	}

	// Resolve entity references based on given fetch
	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		Stream<Entity> entities = decoratedRepo.findAll(q);
		return resolveEntityReferences(entities, q.getFetch());
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
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
			List<Entity> resolvedEntities = resolveEntityReferences(entities.stream(), fetch)
					.collect(Collectors.toList());
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

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepo.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		decoratedRepo.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepo.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepo.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return decoratedRepo.add(entities);
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
