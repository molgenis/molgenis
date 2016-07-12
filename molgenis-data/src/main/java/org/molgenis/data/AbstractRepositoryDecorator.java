package org.molgenis.data;

import com.google.common.collect.ForwardingObject;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Abstract superclass for {@link Repository} decorators that delegates everything to the
 * decorated repository.
 */
public abstract class AbstractRepositoryDecorator extends ForwardingObject implements Repository<Entity>
{
	@Override
	protected abstract Repository<Entity> delegate();

	@Override
	public Iterator<Entity> iterator()
	{
		return delegate().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		delegate().forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		delegate().close();
	}

	@Override
	public String getName()
	{
		return delegate().getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return delegate().getEntityMetaData();
	}

	@Override
	public long count()
	{
		return delegate().count();
	}

	@Override
	public Query<Entity> query()
	{
		return delegate().query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return delegate().count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return delegate().findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		return delegate().findOne(q);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return delegate().aggregate(aggregateQuery);
	}

	@Override
	public void flush()
	{
		delegate().flush();
	}

	@Override
	public void clearCache()
	{
		delegate().clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		// FIXME GitHub #4809
		delegate().rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		delegate().addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		delegate().removeEntityListener(entityListener);
	}

	@Override
	public Entity findOneById(Object id)
	{
		return delegate().findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return delegate().findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return delegate().findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return delegate().findAll(ids, fetch);
	}

	@Override
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return delegate().getQueryOperators();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return delegate().getCapabilities();
	}

	@Override
	public void update(Entity entity)
	{
		delegate().update(entity);
	}

	@Override
	public void delete(Entity entity)
	{
		delegate().delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return delegate().add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		delegate().update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		delegate().delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids);
	}

}
