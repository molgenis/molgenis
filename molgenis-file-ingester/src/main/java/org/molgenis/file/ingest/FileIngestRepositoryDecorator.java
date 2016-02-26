package org.molgenis.file.ingest;

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
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.file.ingest.meta.FileIngestMetaData;

public class FileIngestRepositoryDecorator implements Repository
{
	private final Repository decorated;
	private final FileIngesterJobScheduler scheduler;

	public FileIngestRepositoryDecorator(Repository decorated, FileIngesterJobScheduler scheduler)
	{
		this.decorated = decorated;
		this.scheduler = scheduler;
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
	public Stream<Entity> findAll(Query q)
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
		scheduler.schedule(entity);
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		decorated.update(entities.filter(e -> {
			scheduler.schedule(e);
			return true;
		}));
	}

	@Override
	public void delete(Entity entity)
	{
		decorated.delete(entity);
		scheduler.unschedule(entity.getString(FileIngestMetaData.ID));
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		decorated.delete(entities.filter(e -> {
			scheduler.unschedule(e.getString(FileIngestMetaData.ID));
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		decorated.deleteById(id);

		if (id instanceof String)
		{
			scheduler.unschedule((String) id);
		}
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		decorated.deleteById(ids.filter(id -> {
			if (id instanceof String)
			{
				scheduler.unschedule((String) id);
			}
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		for (Entity e : this)
		{
			scheduler.unschedule(e.getString(FileIngestMetaData.ID));
		}

		decorated.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decorated.add(entity);
		scheduler.schedule(entity);
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		return decorated.add(entities.filter(e -> {
			scheduler.schedule(e);
			return true;
		}));
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
