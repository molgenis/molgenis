package org.molgenis.file.ingest;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST_JOB_EXECUTION;

public class FileIngestRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decorated;
	private final FileIngesterJobScheduler scheduler;
	private final DataService dataService;

	public FileIngestRepositoryDecorator(Repository<Entity> decorated, FileIngesterJobScheduler scheduler,
			DataService dataService)
	{
		this.decorated = decorated;
		this.scheduler = scheduler;
		this.dataService = dataService;
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
	public Set<Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	public EntityType getEntityType()
	{
		return decorated.getEntityType();
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
		scheduler.schedule(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		decorated.update(entities.filter(e ->
		{
			scheduler.schedule(e);
			return true;
		}));
	}

	private void removeJobExecutions(String entityId)
	{
		Query<Entity> query = dataService.query(FILE_INGEST_JOB_EXECUTION)
				.eq(FileIngestJobExecutionMetaData.FILE_INGEST, entityId);
		dataService.delete(FILE_INGEST_JOB_EXECUTION, dataService.findAll(FILE_INGEST_JOB_EXECUTION, query));
	}

	@Override
	public void delete(Entity entity)
	{
		String entityId = entity.getString(FileIngestMetaData.ID);
		scheduler.unschedule(entityId);
		removeJobExecutions(entityId);
		decorated.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decorated.delete(entities.filter(e ->
		{
			String entityId = e.getString(FileIngestMetaData.ID);
			scheduler.unschedule(entityId);
			removeJobExecutions(entityId);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		if (id instanceof String)
		{
			String entityId = (String) id;
			scheduler.unschedule(entityId);
			removeJobExecutions(entityId);
		}
		decorated.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decorated.deleteAll(ids.filter(id ->
		{
			if (id instanceof String)
			{
				String entityId = (String) id;
				scheduler.unschedule(entityId);
				removeJobExecutions(entityId);
			}
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		for (Entity e : this)
		{
			String entityId = e.getString(FileIngestMetaData.ID);
			scheduler.unschedule(entityId);
			removeJobExecutions(entityId);
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
	public Integer add(Stream<Entity> entities)
	{
		return decorated.add(entities.filter(e ->
		{
			scheduler.schedule(e);
			return true;
		}));
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decorated.forEachBatched(fetch, consumer, 1000);
	}

}
