package org.molgenis.data.elasticsearch.reindex;

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
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.CudType;
import org.molgenis.data.elasticsearch.reindex.ReindexActionMetaData.DataType;

public class ReindexActionRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decorated;
	private final ReindexActionRegisterService reindexActionRegisterService;

	public ReindexActionRepositoryDecorator(Repository<Entity> decorated,
			ReindexActionRegisterService reindexActionRegisterService)
	{
		this.decorated = requireNonNull(decorated);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
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
		reindexActionRegisterService.register(getEntityMetaData(), CudType.UPDATE, DataType.DATA, entity.getIdValue()
				.toString());
	}

	@Override
	public void delete(Entity entity)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, entity.getIdValue()
				.toString());
		decorated.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, id.toString());
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		reindexActionRegisterService.register(getEntityMetaData(), CudType.ADD, DataType.DATA, entity.getIdValue().toString());
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.ADD, DataType.DATA, null);
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
	public void update(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.UPDATE, DataType.DATA, null);
		decorated.update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		reindexActionRegisterService.register(getEntityMetaData(), CudType.DELETE, DataType.DATA, null);
		decorated.deleteAll(ids);
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}
}
