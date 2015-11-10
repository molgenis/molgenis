package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class EntityReferenceResolverDecorator implements Repository
{
	private final Repository decoratedRepo;
	private final EntityManager entityManager;

	public EntityReferenceResolverDecorator(Repository decoratedRepo, EntityManager entityManager)
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
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public Query query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepo.count(q);
	}

	// Resolve entity references based on given fetch
	@Override
	public Iterable<Entity> findAll(Query q)
	{
		Iterable<Entity> entities = decoratedRepo.findAll(q);
		return resolveEntityReferences(entities, q.getFetch());
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	// Resolve entity references based on given fetch
	@Override
	public Entity findOne(Query q)
	{
		Entity entity = decoratedRepo.findOne(q);
		return entity != null ? resolveEntityReferences(entity, q.getFetch()) : null;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepo.findOne(id);
	}

	// Resolve entity references based on given fetch
	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		Entity entity = decoratedRepo.findOne(id, fetch);
		return entity != null ? resolveEntityReferences(entity, fetch) : null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	// Resolve entity references based on given fetch
	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids, Fetch fetch)
	{
		Iterable<Entity> entities = decoratedRepo.findAll(ids, fetch);
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
	public void update(Iterable<? extends Entity> records)
	{
		decoratedRepo.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		decoratedRepo.deleteById(ids);
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
	public Integer add(Iterable<? extends Entity> entities)
	{
		return decoratedRepo.add(entities);
	}

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	@Override
	public void create()
	{
		decoratedRepo.create();
	}

	@Override
	public void drop()
	{
		decoratedRepo.drop();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepo.rebuildIndex();
	}

	private Entity resolveEntityReferences(Entity entity, Fetch fetch)
	{
		return entityManager.resolveReferences(getEntityMetaData(), entity, fetch);
	}

	private Iterable<Entity> resolveEntityReferences(Iterable<Entity> entities, Fetch fetch)
	{
		return entityManager.resolveReferences(getEntityMetaData(), entities, fetch);
	}
}
