package org.molgenis.data.jobs;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;

public class JobExecutionUpdaterImpl implements JobExecutionUpdater
{
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
		runAsSystem(() -> dataService.update(jobExecution.getEntityMetaData().getName(), jobExecution));
	}

}
