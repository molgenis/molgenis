package org.molgenis.compute.ui.model.decorator;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.compute.ui.model.AnalysisStatus;
import org.molgenis.compute.ui.model.JobStatus;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Iterables;

/**
 * AnalysisJob decorator that updates analysis status on job status change
 * 
 * TODO handle analysis job deletes
 */
public class AnalysisJobDecorator implements Repository
{
	private final Repository decoratedRepository;
	private final DataService dataService;

	public AnalysisJobDecorator(Repository decoratedRepository, DataService dataService)
	{
		this.decoratedRepository = decoratedRepository;
		this.dataService = dataService;
	}

	@Override
	public void add(Entity entity)
	{
		// validate job status
		validateNewAnalysisJobStatus(entity);
		decoratedRepository.add(entity);
		updateAnalysisStatus(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		// validate job status
		for (Entity entity : entities)
			validateNewAnalysisJobStatus(entity);

		Integer count = decoratedRepository.add(entities);
		updateAnalysisStatus(entities);
		return count;
	}

	@Override
	public void update(Entity entity)
	{
		// validate job status
		validateExistingAnalysisJobStatus(entity);
		decoratedRepository.update(entity);
		updateAnalysisStatus(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		// validate job status
		for (Entity entity : entities)
			validateExistingAnalysisJobStatus(entity);

		decoratedRepository.update(entities);
		updateAnalysisStatus(entities);
	}

	private void validateNewAnalysisJobStatus(Entity entity)
	{
		JobStatus jobStatus = JobStatus.valueOf(entity.getString(AnalysisJobMetaData.STATUS));
		if (!jobStatus.isInitialState())
		{
			String attributeLabel = AnalysisJobMetaData.INSTANCE.getAttribute(AnalysisJobMetaData.STATUS).getLabel();
			throw new RuntimeException(attributeLabel + " is not an initial state");
		}
	}

	private void validateExistingAnalysisJobStatus(Entity entity)
	{
		AnalysisJob currentAnalysisJob = dataService.findOne(AnalysisJobMetaData.INSTANCE.getName(),
				entity.getIdValue(), AnalysisJob.class);
		JobStatus currentJobStatus = currentAnalysisJob.getStatus();

		JobStatus jobStatus = JobStatus.valueOf(entity.getString(AnalysisJobMetaData.STATUS));
		if (jobStatus != currentJobStatus)
		{
			EnumSet<JobStatus> allowedStateTransitions = currentJobStatus.getStateTransitions();
			if (!allowedStateTransitions.contains(jobStatus))
			{
				String attributeLabel = AnalysisJobMetaData.INSTANCE.getAttribute(AnalysisJobMetaData.STATUS)
						.getLabel();
				throw new RuntimeException(attributeLabel + " transition from " + currentJobStatus + " to " + jobStatus
						+ " is not allowed. Allowed transitions " + allowedStateTransitions);
			}
		}
	}

	private void updateAnalysisStatus(Entity entity)
	{
		Analysis analysis = entity.getEntity(AnalysisJobMetaData.ANALYSIS, Analysis.class);
		if (analysis == null)
		{
			throw new RuntimeException("AnalysisJob [" + entity.getIdValue() + "] must belong to an Analysis");
		}

		// do not update analysis status if status is a final state
		AnalysisStatus currentAnalysisStatus = analysis.getStatus();
		if (!currentAnalysisStatus.isFinalState())
		{
			Iterable<AnalysisJob> analysisJobs = dataService.findAll(AnalysisJobMetaData.INSTANCE.getName(),
					new QueryImpl().eq(AnalysisJobMetaData.ANALYSIS, analysis), AnalysisJob.class);
			AnalysisStatus updatedAnalysisStatus = determineAnalysisStatus(currentAnalysisStatus, analysisJobs);
			if (updatedAnalysisStatus != currentAnalysisStatus)
			{
				analysis.setStatus(updatedAnalysisStatus);
				dataService.update(AnalysisMetaData.INSTANCE.getName(), analysis);
			}
		}
	}

	private void updateAnalysisStatus(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
			updateAnalysisStatus(entity);
	}

	private AnalysisStatus determineAnalysisStatus(AnalysisStatus currentAnalysisStatus,
			Iterable<AnalysisJob> analysisJobs)
	{

		for (AnalysisJob analysisJob : analysisJobs)
		{
			if (analysisJob.getStatus() == JobStatus.RUNNING) return AnalysisStatus.RUNNING;
		}

		for (AnalysisJob analysisJob : analysisJobs)
		{
			if (analysisJob.getStatus() == JobStatus.FAILED) return AnalysisStatus.FAILED;
		}

		if (!Iterables.isEmpty(analysisJobs))
		{
			boolean completed = true;
			for (AnalysisJob analysisJob : analysisJobs)
			{
				if (analysisJob.getStatus() != JobStatus.COMPLETED)
				{
					completed = false;
					break;
				}
			}
			if (completed) return AnalysisStatus.COMPLETED;
		}

		return currentAnalysisStatus;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return this.decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		this.decoratedRepository.close();

	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return this.decoratedRepository.getCapabilities();
	}

	@Override
	public String getName()
	{
		return this.decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return this.decoratedRepository.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return this.decoratedRepository.count();
	}

	@Override
	public Query query()
	{
		return this.decoratedRepository.query();
	}

	@Override
	public long count(Query q)
	{
		return this.decoratedRepository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return this.decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return this.decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return this.decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return this.decoratedRepository.findAll(ids);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return this.decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void delete(Entity entity)
	{
		this.decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		this.decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		this.decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		this.decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		this.decoratedRepository.deleteAll();
	}

	@Override
	public void flush()
	{
		this.decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		this.decoratedRepository.clearCache();
	}
}
