package org.molgenis.data.jobs;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.model.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class JobExecutionUpdaterImpl implements JobExecutionUpdater
{
	private static final Logger LOG = LoggerFactory.getLogger(JobExecutionUpdater.class);
	@Autowired
	private DataService dataService;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void update(JobExecution jobExecution)
	{
		executorService.execute(() -> updateInternal(jobExecution));
	}

	private void updateInternal(JobExecution jobExecution)
	{
		runAsSystem(() -> tryUpdate(jobExecution));
	}

	private void tryUpdate(JobExecution jobExecution)
	{
		try
		{
			dataService.update(jobExecution.getEntityMetaData().getName(), jobExecution);
		}
		catch (Exception ex)
		{
			LOG.warn("Error updating job execution", ex);
		}
	}

}
