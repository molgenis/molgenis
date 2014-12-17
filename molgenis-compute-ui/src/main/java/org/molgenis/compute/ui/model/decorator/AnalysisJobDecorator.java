package org.molgenis.compute.ui.model.decorator;

import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.compute.ui.model.AnalysisStatus;
import org.molgenis.compute.ui.model.JobStatus;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;

public class AnalysisJobDecorator extends CrudRepositoryDecorator
{
	private final CrudRepository decoratedRepository;
	private final DataService dataService;

	public AnalysisJobDecorator(CrudRepository decoratedRepository, DataService dataService)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
		this.dataService = dataService;
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
		updateAnalysisStatus(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = decoratedRepository.add(entities);
		updateAnalysisStatus(entities);
		return count;
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
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
		if (!currentAnalysisStatus.isEndPoint())
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

		for (AnalysisJob analysisJob : analysisJobs)
		{
			if (analysisJob.getStatus() == JobStatus.COMPLETED) return AnalysisStatus.COMPLETED;
		}

		return currentAnalysisStatus;
	}
}
