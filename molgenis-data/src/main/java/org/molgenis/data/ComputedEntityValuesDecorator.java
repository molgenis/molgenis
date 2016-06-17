package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.collect.Iterators;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.EntityWithComputedAttributes;

public class ComputedEntityValuesDecorator implements Repository<Entity>
{
	private final Repository<Entity> decoratedRepo;

	public ComputedEntityValuesDecorator(Repository<Entity> decoratedRepo)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
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
	public Query<Entity> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
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
	public Entity findOne(Query<Entity> q)
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
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		if(getEntityMetaData().hasAttributeWithExpression())
		{
			decoratedRepo.forEachBatched(fetch, entities -> consumer
					.accept(Lists.transform(entities, EntityWithComputedAttributes::new)), batchSize);
		}
		else
		{
			decoratedRepo.forEachBatched(fetch, consumer, batchSize);
		}

	}

	@Override
	public Entity findOneById(Object id)
	{
		Entity entity = decoratedRepo.findOneById(id);
		// compute values with attributes with expressions
		return entity != null ? toComputedValuesEntity(entity) : null;
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		Entity entity = decoratedRepo.findOneById(id, fetch);
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

	private Iterator<Entity> toComputedValuesEntities(Iterator<Entity> it)
	{
		if (getEntityMetaData().hasAttributeWithExpression())
		{
			return Iterators.transform(it, EntityWithComputedAttributes::new);
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
			return entities.map(EntityWithComputedAttributes::new);
		}
		else
		{
			return entities;
		}
	}
}
