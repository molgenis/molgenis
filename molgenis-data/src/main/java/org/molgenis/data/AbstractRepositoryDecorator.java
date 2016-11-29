package org.molgenis.data;

import com.google.common.collect.ForwardingObject;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;

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
public abstract class AbstractRepositoryDecorator<E extends Entity> extends ForwardingObject implements Repository<E>
{
	@Override
	protected abstract Repository<E> delegate();

	@Override
	public Iterator<E> iterator()
	{
		return delegate().iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize)
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
	public EntityType getEntityType()
	{
		return delegate().getEntityType();
	}

	@Override
	public long count()
	{
		return delegate().count();
	}

	@Override
	public Query<E> query()
	{
		// do not forward to delegate, since we want for example query.findAll() to be called on this repository
		return new QueryImpl<>(this);
	}

	@Override
	public long count(Query<E> q)
	{
		return delegate().count(q);
	}

	@Override
	public Stream<E> findAll(Query<E> q)
	{
		return delegate().findAll(q);
	}

	@Override
	public E findOne(Query<E> q)
	{
		return delegate().findOne(q);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return delegate().aggregate(aggregateQuery);
	}

	@Override
	public E findOneById(Object id)
	{
		return delegate().findOneById(id);
	}

	@Override
	public E findOneById(Object id, Fetch fetch)
	{
		return delegate().findOneById(id, fetch);
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids)
	{
		return delegate().findAll(ids);
	}

	@Override
	public Stream<E> findAll(Stream<Object> ids, Fetch fetch)
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
	public void update(E entity)
	{
		delegate().update(entity);
	}

	@Override
	public void delete(E entity)
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
	public void add(E entity)
	{
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return delegate().add(entities);
	}

	@Override
	public void update(Stream<E> entities)
	{
		delegate().update(entities);
	}

	@Override
	public void delete(Stream<E> entities)
	{
		delegate().delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids);
	}

}
