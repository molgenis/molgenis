package org.molgenis.data.support;

import java.io.IOException;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;

/**
 * Base class for repositories. Subclasses can override supported methods
 */
public abstract class AbstractRepository implements Repository
{
	private String name;

	@Override
	public String getName()
	{
		if (name == null) name = getEntityMetaData().getName();
		return name;
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public void close() throws IOException
	{
	}

	@Override
	public long count(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void clearCache()
	{
	}
}
