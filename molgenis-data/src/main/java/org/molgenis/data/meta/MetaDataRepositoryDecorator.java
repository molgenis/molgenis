package org.molgenis.data.meta;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

/**
 * Repository decorator for entities, attributes and packages repositories.
 * 
 * Removes the WRITABLE and MANAGEABLE capabilities, because the user must not directly edit these repos but use the
 * MetaDataServices
 */
public class MetaDataRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decorated;

	public MetaDataRepositoryDecorator(Repository<Entity> decorated)
	{
		this.decorated = decorated;
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
		Set<RepositoryCapability> capabilities = new HashSet<>(decorated.getCapabilities());
		capabilities.remove(RepositoryCapability.WRITABLE);
		capabilities.remove(RepositoryCapability.MANAGABLE);
		return capabilities;
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
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		decorated.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decorated.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decorated.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decorated.deleteAll(ids);
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
