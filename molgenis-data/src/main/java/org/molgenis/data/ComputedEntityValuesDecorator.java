package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.support.EntityWithComputedAttributes;

public class ComputedEntityValuesDecorator implements Repository
{
	private final Repository decoratedRepo;

	public ComputedEntityValuesDecorator(Repository decoratedRepo)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
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

	@Override
	public Stream<Entity> findAll(Query q)
	{
		Stream<Entity> entities = decoratedRepo.findAll(q);
		// compute values with attributes with expressions
		return toComputedValuesEntities(entities);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public Entity findOne(Query q)
	{
		Entity entity = decoratedRepo.findOne(q);
		// compute values with attributes with expressions
		return entity != null ? toComputedValuesEntity(entity) : null;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		Iterator<Entity> it = decoratedRepo.iterator();
		// compute values with attributes with expressions
		return toComputedValuesEntities(it);
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		Stream<Entity> entities = decoratedRepo.stream(fetch);
		// compute values with attributes with expressions
		return toComputedValuesEntities(entities);
	}

	@Override
	public Entity findOne(Object id)
	{
		Entity entity = decoratedRepo.findOne(id);
		// compute values with attributes with expressions
		return entity != null ? toComputedValuesEntity(entity) : null;
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		Entity entity = decoratedRepo.findOne(id, fetch);
		// compute values with attributes with expressions
		return entity != null ? toComputedValuesEntity(entity) : null;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = decoratedRepo.findAll(ids);
		// compute values with attributes with expressions
		return toComputedValuesEntities(entities);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		Stream<Entity> entities = decoratedRepo.findAll(ids, fetch);
		// compute values with attributes with expressions
		return toComputedValuesEntities(entities);
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
	public void update(Stream<? extends Entity> entities)
	{
		decoratedRepo.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
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
	public Integer add(Stream<? extends Entity> entities)
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

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	private Entity toComputedValuesEntity(Entity entity)
	{
		if (getEntityMetaData().hasAttributeWithExpression())
		{
			return new EntityWithComputedAttributes(entity);
		}
		else
		{
			return entity;
		}
	}

	private Iterable<Entity> toComputedValuesEntities(Iterable<Entity> entities)
	{
		if (getEntityMetaData().hasAttributeWithExpression())
		{
			return new Iterable<Entity>()
			{
				@Override
				public Iterator<Entity> iterator()
				{
					return StreamSupport.stream(entities.spliterator(), false)
							.map(entity -> (Entity) new EntityWithComputedAttributes(entity)).iterator();
				}
			};
		}
		else
		{
			return entities;
		}
	}

	private Iterator<Entity> toComputedValuesEntities(Iterator<Entity> it)
	{
		if (getEntityMetaData().hasAttributeWithExpression())
		{
			Iterable<Entity> entities = () -> it;
			return toComputedValuesEntities(entities).iterator();
		}
		else
		{
			return it;
		}
	}

	private Stream<Entity> toComputedValuesEntities(Stream<Entity> entities)
	{
		if (getEntityMetaData().hasAttributeWithExpression())
		{
			return entities.map(entity -> {
				return new EntityWithComputedAttributes(entity);
			});
		}
		else
		{
			return entities;
		}
	}
}
