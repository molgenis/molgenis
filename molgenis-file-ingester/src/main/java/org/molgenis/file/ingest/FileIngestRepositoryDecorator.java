package org.molgenis.file.ingest;

import org.molgenis.data.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;

import java.util.stream.Stream;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST_JOB_EXECUTION;

public class FileIngestRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
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
	protected Repository<Entity> delegate()
	{
		return decorated;
	}

	@Override
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
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

	private void removeJobExecutions(String entityId)
	{
		Query<Entity> query = dataService.query(FILE_INGEST_JOB_EXECUTION)
				.eq(FileIngestJobExecutionMetaData.FILE_INGEST, entityId);
		dataService.delete(FILE_INGEST_JOB_EXECUTION, dataService.findAll(FILE_INGEST_JOB_EXECUTION, query));
	}
}
