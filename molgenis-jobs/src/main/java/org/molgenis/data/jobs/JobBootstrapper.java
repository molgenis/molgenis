package org.molgenis.data.jobs;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.abbreviateMiddle;
import static org.molgenis.data.jobs.model.JobExecution.MAX_LOG_LENGTH;
import static org.molgenis.data.jobs.model.JobExecution.Status.FAILED;
import static org.molgenis.data.jobs.model.JobExecution.Status.RUNNING;
import static org.molgenis.data.jobs.model.JobExecution.TRUNCATION_BANNER;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.*;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Bootstraps the scheduling framework
 */
@Component
public class JobBootstrapper
{
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final DataService dataService;

	@Autowired
	public JobBootstrapper(SystemEntityTypeRegistry systemEntityTypeRegistry, DataService dataService)
	{
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.dataService = requireNonNull(dataService);
	}

	public void bootstrap()
	{
		systemEntityTypeRegistry.getSystemEntityTypes().filter(this::isJobExecution).forEach(this::bootstrap);
	}

	private void bootstrap(SystemEntityType systemEntityType)
	{
		dataService.query(systemEntityType.getId()).eq(STATUS, RUNNING).or().eq(STATUS, PENDING).findAll()
				.forEach(this::setFailed);
	}

	private void setFailed(Entity jobExecutionEntity)
	{
		jobExecutionEntity.set(STATUS, FAILED.toString());
		jobExecutionEntity.set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
		StringBuilder log = new StringBuilder();
		if (!isEmpty(jobExecutionEntity.get(LOG)))
		{
			log.append(jobExecutionEntity.get(LOG));
			log.append('\n');
		}
		log.append("FAILED - Application terminated unexpectedly");
		String abbreviatedLog = abbreviateMiddle(log.toString(), "...\n" + TRUNCATION_BANNER + "\n...", MAX_LOG_LENGTH);
		jobExecutionEntity.set(LOG, abbreviatedLog);
		dataService.update(jobExecutionEntity.getEntityType().getId(), jobExecutionEntity);
	}

	private boolean isJobExecution(EntityType entityType)
	{
		return entityType.getExtends() != null && entityType.getExtends().getId().equals(JOB_EXECUTION);
	}
}
