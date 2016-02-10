package org.molgenis.data.mem;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Sets;

/**
 * Repository that uses a hashmap as store.
 * 
 * For testing purposes
 */
public class InMemoryRepository implements Repository
{
	private final EntityMetaData metadata;
	private final Map<Object, Entity> entities = new LinkedHashMap<Object, Entity>();

	public InMemoryRepository(EntityMetaData entityMetaData)
	{
		this.metadata = entityMetaData;
	}

	@Override
	public String getName()
	{
		return metadata.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metadata;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return entities.values().iterator();
	}

	@Override
	public void close() throws IOException
	{

	}

	@Override
	public Query query()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count(Query q)
	{
		return entities.size();
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		if (new QueryImpl().equals(q))
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
					return entities.entrySet().stream().map((e) -> {
						if (e.getValue().get(r.getField()).equals(r.getValue()))
						{
							return e.getValue();
						}
						else
						{
							return null;
						}
					});
				}
			}
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Entity findOne(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(Object id)
	{
		return findOne(id, null);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return entities.get(id);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return ids.map(id -> entities.get(id));
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return ids.map(id -> entities.get(id));
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
	public void update(Stream<? extends Entity> entities)
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
	public void delete(Stream<? extends Entity> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		entities.remove(id);
	}

	@Override
	public void deleteById(Stream<Object> ids)
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
	public Integer add(Stream<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.forEach(entity -> {
			add(entity);
			count.incrementAndGet();
		});
		return count.get();
	}

	@Override
	public void flush()
	{

	}

	@Override
	public void clearCache()
	{

	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(RepositoryCapability.QUERYABLE, RepositoryCapability.WRITABLE);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void create()
	{
		// Repo is not MANAGABLE
		throw new UnsupportedOperationException();
	}

	@Override
	public void drop()
	{
		// Repo is not MANAGABLE
		throw new UnsupportedOperationException();
	}

	@Override
	public void rebuildIndex()
	{
		// Repo is not INDEXABLE
		throw new UnsupportedOperationException();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		throw new UnsupportedOperationException();
	}
}
