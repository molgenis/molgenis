package org.molgenis.data.transaction.index;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.CudType;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData.DataType;

public class IndexTransactionLogRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decorated;
	private final IndexTransactionLogService indexTransactionLogService;

	public IndexTransactionLogRepositoryDecorator(Repository<Entity> decorated,
			IndexTransactionLogService indexTransactionLogService)
	{
		this.decorated = requireNonNull(decorated);
		this.indexTransactionLogService = requireNonNull(indexTransactionLogService);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		return decorated.stream(fetch);
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
	public void update(Entity entity)
	{
		decorated.update(entity);
		indexTransactionLogService.log(getEntityMetaData(), CudType.UPDATE, DataType.DATA, entity.getIdValue()
				.toString());
	}

	@Override
	public void delete(Entity entity)
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.DELETE, DataType.DATA, entity.getIdValue()
				.toString());
		decorated.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.DELETE, DataType.DATA, id.toString());
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		indexTransactionLogService.log(getEntityMetaData(), CudType.ADD, DataType.DATA, entity.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.ADD, DataType.DATA, null);
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
		indexTransactionLogService.log(getEntityMetaData(), CudType.UPDATE, DataType.METADATA, null);
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
	public void update(Stream<Entity> entities)
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.UPDATE, DataType.DATA, null);
		decorated.update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		indexTransactionLogService.log(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll(ids);
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}
}
