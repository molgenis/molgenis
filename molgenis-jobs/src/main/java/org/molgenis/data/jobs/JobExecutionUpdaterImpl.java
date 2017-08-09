package org.molgenis.data.jobs;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.support.DynamicEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@Component
public class JobExecutionUpdaterImpl implements JobExecutionUpdater
{
	private static final Logger LOG = LoggerFactory.getLogger(JobExecutionUpdater.class);
	@Autowired
	private DataService dataService;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void update(JobExecution jobExecution)
	{
		Entity copy = new DynamicEntity(jobExecution.getEntityType());
		copy.set(jobExecution);
		executorService.execute(() -> updateInternal(copy));
	}

	private void updateInternal(Entity jobExecution)
	{
		runAsSystem(() -> tryUpdate(jobExecution));
	}

	private void tryUpdate(Entity jobExecution)
	{
		try
		{
			dataService.update(jobExecution.getEntityType().getId(), jobExecution);
		}
		catch (Exception ex)
		{
			LOG.warn("Error updating job execution", ex);
		}
	}

}
