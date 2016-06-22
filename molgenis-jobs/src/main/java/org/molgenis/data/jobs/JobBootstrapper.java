package org.molgenis.data.jobs;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.JobExecution.Status.FAILED;
import static org.molgenis.data.jobs.JobExecution.Status.RUNNING;
import static org.molgenis.data.jobs.JobExecutionMetaData.JOB_EXECUTION;
import static org.molgenis.data.jobs.JobExecutionMetaData.PROGRESS_MESSAGE;
import static org.molgenis.data.jobs.JobExecutionMetaData.STATUS;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bootstraps the scheduling framework
 */
@Component
public class JobBootstrapper
{
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final DataService dataService;

	@Autowired
	public JobBootstrapper(SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, DataService dataService)
	{
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.dataService = requireNonNull(dataService);
	}

	public void bootstrap()
	{
		systemEntityMetaDataRegistry.getSystemEntityMetaDatas().filter(this::isJobExecution).forEach(this::bootstrap);
	}

	private void bootstrap(SystemEntityMetaData systemEntityMetaData)
	{
		dataService.query(systemEntityMetaData.getName()).eq(STATUS, RUNNING).findAll().forEach(this::setFailed);
	}

	private void setFailed(Entity jobExecutionEntity)
	{
		jobExecutionEntity.set(STATUS, FAILED.toString());
		jobExecutionEntity.set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
		dataService.update(jobExecutionEntity.getEntityMetaData().getName(), jobExecutionEntity);
	}

	private boolean isJobExecution(EntityMetaData entityMetaData)
	{
		return entityMetaData.getExtends() != null && entityMetaData.getExtends().getName().equals(JOB_EXECUTION);
	}
}
