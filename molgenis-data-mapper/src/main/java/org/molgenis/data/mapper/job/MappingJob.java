package org.molgenis.data.mapper.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.service.MappingService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class MappingJob extends Job<Void>
{
	private final MappingService mappingService;
	private final MappingJobExecution mappingJobExecution;
	private final DataService dataService;

	MappingJob(String username, Progress progress, Authentication userAuthentication,
			TransactionTemplate transactionTemplate, MappingService mappingService,
			MappingJobExecution mappingJobExecution, DataService dataService)
	{
		super(progress, transactionTemplate, userAuthentication);

		this.mappingService = mappingService;
		this.mappingJobExecution = mappingJobExecution;
		this.dataService = dataService;
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		MappingProject mappingProject = mappingService.getMappingProject(mappingJobExecution.getMappingProjectId());
		MappingTarget mappingTarget = mappingProject.getMappingTargets().get(0);

		progress.setProgressMax(calculateMaxProgress(mappingTarget));

		mappingService.applyMappings(mappingTarget, mappingJobExecution.getNewEntityTypeId(),
				mappingJobExecution.isAddSourceAttribute(), progress);

		return null;
	}

	private int calculateMaxProgress(MappingTarget mappingTarget)
	{
		return (int) mappingTarget.getEntityMappings().stream().map(this::countProgressBatches).count();
	}

	private Long countProgressBatches(EntityMapping entityMapping)
	{
		long sourceRows = dataService.count(entityMapping.getSourceEntityType().getId());

		long batches = sourceRows / 1000;
		long remainder = sourceRows % 1000;

		if (remainder > 0 || batches == 0)
		{
			batches++;
		}

		return batches;
	}
}