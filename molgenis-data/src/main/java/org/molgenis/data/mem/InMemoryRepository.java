package org.molgenis.data.mem;

import com.google.common.collect.Sets;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.partition;

/**
 * Repository that uses a hash map as store.
 * <p>
 * For testing purposes
 */
public class InMemoryRepository implements Repository<Entity>
{
	private final EntityType metadata;
	private final Map<Object, Entity> entities = new LinkedHashMap<>();

	public InMemoryRepository(EntityType entityType)
	{
		this.metadata = entityType;
	}

	@Override
	public String getName()
	{
		return metadata.getId();
	}

	public EntityType getEntityType()
	{
		return metadata;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return entities.values().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		for (List<Entity> batch : partition(entities.values(), batchSize))
		{
			consumer.accept(batch);
		}
	}

	@Override
	public void close() throws IOException
	{

	}

	@Override
	public Query<Entity> query()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return entities.size();
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (new QueryImpl<>().equals(q))
		{
			return entities.values().stream();
		}
		else
		{
			// partial implementation: one EQUALS rule
			if (q.getRules().size() == 1)
			{
				QueryRule r = q.getRules().iterator().next();
				if (r.getOperator() == Operator.EQUALS)
				{
					return entities.entrySet().stream().map((e) ->
					{
						if (e.getValue().get(r.getField()).equals(r.getValue()))
						{
							return e.getValue();
						}
						else
						{
							return null;
						}
					}).filter(Objects::nonNull);
				}
			}
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOneById(Object id)
	{
		return findOneById(id, null);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return entities.get(id);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return ids.map(entities::get).filter(Objects::nonNull);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return ids.map(entities::get).filter(Objects::nonNull);
	}

	@Override
	public long count()
	{
		return entities.size();
	}

	@Override
	public void update(Entity entity)
	{
		Object id = getId(entity);
		if (!entities.containsKey(id))
		{
			throw new IllegalStateException("No entity with id " + id);
		}
		entities.put(id, entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		entities.forEach(this::update);
	}

	private Object getId(Entity entity)
	{
		return entity.get(metadata.getIdAttribute().getName());
	}

	@Override
	public void delete(Entity entity)
	{
		deleteById(getId(entity));
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		entities.remove(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll()
	{
		entities.clear();
	}

	@Override
	public void add(Entity entity)
	{
		Object id = getId(entity);
		if (id == null)
		{
			throw new NullPointerException("Entity ID is null.");
		}
		if (entities.containsKey(id))
		{
			throw new IllegalStateException("Entity with id " + id + " already exists");
		}
		entities.put(id, entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.forEach(entity ->
		{
			add(entity);
			count.incrementAndGet();
		});
		return count.get();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(RepositoryCapability.QUERYABLE, RepositoryCapability.WRITABLE);
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return EnumSet.allOf(Operator.class);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}
}
