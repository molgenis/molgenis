package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.meta.model.EntityMetaData;

/**
 * Abstract superclass for {@link Repository} decorators that delegates everything to the
 * decorated repository.
 */
public abstract class AbstractRepositoryDecorator implements Repository<Entity>
{
	protected final Repository<Entity> decorated;

	public AbstractRepositoryDecorator(Repository<Entity> decoratedRepository)
	{
		this.decorated = requireNonNull(decoratedRepository);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decorated.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decorated.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		return decorated.findOne(q);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void flush()
	{
		decorated.flush();
	}

	@Override
	public void clearCache()
	{
		decorated.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		// FIXME GitHub #4809
		decorated.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decorated.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decorated.removeEntityListener(entityListener);
	}

	@Override
	public Entity findOneById(Object id)
	{
		return decorated.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return decorated.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
	}

	@Override
	public void delete(Entity entity)
	{
		decorated.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return decorated.add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		decorated.update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decorated.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decorated.deleteAll(ids);
	}

}
