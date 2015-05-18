package org.molgenis.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;

/**
 * Translate sql exceptions into user friendly messages
 */
public class IndexedRepositoryExceptionTranslatorDecorator implements IndexedRepository
{
	private IndexedRepository repository;

	public IndexedRepositoryExceptionTranslatorDecorator(IndexedRepository repository)
	{
		this.repository = repository;
	}

	@Override
	public void create()
	{
		repository.create();
	}

	@Override
	public void drop()
	{
		repository.drop();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return repository.getCapabilities();
	}

	@Override
	public String getName()
	{
		return repository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return repository.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return repository.count();
	}

	@Override
	public Query query()
	{
		return repository.query();
	}

	@Override
	public long count(Query q)
	{
		return repository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return repository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return repository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return repository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return repository.findAll(ids);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return repository.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			repository.update(entity);
		});
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			repository.update(records);
		});
	}

	@Override
	public void delete(Entity entity)
	{
		repository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		repository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		repository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		repository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		repository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			repository.add(entity);
		});
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		AtomicInteger result = new AtomicInteger();

		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			Integer count = repository.add(entities);
			if (count != null) result.set(count);
		});

		return result.get();
	}

	@Override
	public void flush()
	{
		repository.flush();
	}

	@Override
	public void clearCache()
	{
		repository.clearCache();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return repository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		repository.close();
	}

	@Override
	public void rebuildIndex()
	{
		repository.rebuildIndex();
	}

}
