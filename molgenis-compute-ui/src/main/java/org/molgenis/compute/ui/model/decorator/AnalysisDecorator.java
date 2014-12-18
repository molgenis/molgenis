package org.molgenis.compute.ui.model.decorator;

import java.util.EnumSet;

import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisStatus;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

/**
 * AnalysisJob decorator that updates analysis status on job status change
 * 
 * TODO handle analysis job deletes
 */
public class AnalysisDecorator extends CrudRepositoryDecorator
{
	private final CrudRepository decoratedRepository;
	private final DataService dataService;

	public AnalysisDecorator(CrudRepository decoratedRepository, DataService dataService)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
		this.dataService = dataService;
	}

	@Override
	public void add(Entity entity)
	{
		// validate job status
		validateNewAnalysisStatus(entity);

		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		// validate job status
		for (Entity entity : entities)
			validateNewAnalysisStatus(entity);

		return decoratedRepository.add(entities);
	}

	@Override
	public void update(Entity entity)
	{
		// validate job status
		validateExistingAnalysisStatus(entity);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		// validate job status
		for (Entity entity : entities)
			validateExistingAnalysisStatus(entity);

		decoratedRepository.update(entities);
	}

	private void validateNewAnalysisStatus(Entity entity)
	{
		AnalysisStatus analysisStatus = AnalysisStatus.valueOf(entity.getString(AnalysisMetaData.STATUS));
		if (!analysisStatus.isInitialState())
		{
			String attributeLabel = AnalysisMetaData.INSTANCE.getAttribute(AnalysisMetaData.STATUS).getLabel();
			throw new RuntimeException(attributeLabel + " is not an initial state");
		}
	}

	private void validateExistingAnalysisStatus(Entity entity)
	{
		Analysis currentAnalysis = dataService.findOne(AnalysisMetaData.INSTANCE.getName(), entity.getIdValue(),
				Analysis.class);
		AnalysisStatus currentAnalysisStatus = currentAnalysis.getStatus();

		AnalysisStatus analysisStatus = AnalysisStatus.valueOf(entity.getString(AnalysisMetaData.STATUS));
		if (analysisStatus != currentAnalysisStatus)
		{
			EnumSet<AnalysisStatus> allowedStateTransitions = currentAnalysisStatus.getStateTransitions();
			if (!allowedStateTransitions.contains(analysisStatus))
			{
				String attributeLabel = AnalysisMetaData.INSTANCE.getAttribute(AnalysisMetaData.STATUS).getLabel();
				throw new RuntimeException(attributeLabel + " transition from " + currentAnalysisStatus + " to "
						+ analysisStatus + " is not allowed. Allowed transitions " + allowedStateTransitions);
			}
		}
	}
}
