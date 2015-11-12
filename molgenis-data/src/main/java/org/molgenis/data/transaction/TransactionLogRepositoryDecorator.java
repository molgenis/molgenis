package org.molgenis.data.transaction;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

public class TransactionLogRepositoryDecorator implements Repository
{
	private final Repository decorated;
	private final TransactionLogService transactionLogService;

	public TransactionLogRepositoryDecorator(Repository decorated, TransactionLogService transactionLogService)
	{
		this.decorated = requireNonNull(decorated);
		this.transactionLogService = requireNonNull(transactionLogService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
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
	public Query query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query q)
	{
		return decorated.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decorated.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decorated.findOne(id, fetch);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.UPDATE);
		}

		decorated.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.UPDATE);
		}
		decorated.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.DELETE);
		}
		decorated.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.DELETE);
		}
		decorated.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.DELETE);
		}
		decorated.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.DELETE);
		}
		decorated.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.DELETE);
		}
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.ADD);
		}
		decorated.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		if (!TransactionLogService.EXCLUDED_ENTITIES.contains(getName()))
		{
			transactionLogService.log(getEntityMetaData(), MolgenisTransactionLogEntryMetaData.Type.ADD);
		}
		return decorated.add(entities);
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
	public void create()
	{
		decorated.create();
	}

	@Override
	public void drop()
	{
		decorated.drop();
	}

	@Override
	public void rebuildIndex()
	{
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
}
