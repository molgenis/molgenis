package org.molgenis.data.support;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.validation.EntityValidator;

public class AbstractAggregateableCrudRepository extends AbstractCrudRepository implements Aggregateable
{

	public AbstractAggregateableCrudRepository(String url, EntityValidator validator)
	{
		super(url, validator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public long count(Query q)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Object id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Object id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void flush()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCache()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Integer addInternal(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addInternal(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateInternal(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateInternal(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateInternal(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		// TODO Auto-generated method stub

	}

}
