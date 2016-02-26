package org.molgenis.data.jobs;

import static org.molgenis.data.jobs.JobExecution.Status.CANCELED;
import static org.molgenis.data.jobs.JobExecution.Status.FAILED;
import static org.molgenis.data.jobs.JobExecution.Status.RUNNING;
import static org.molgenis.data.jobs.JobExecution.Status.SUCCESS;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Date;

import org.molgenis.data.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks progress and stores it in a {@link JobExecution} entity. The entity may be a subclass of {@link JobExecution}.
 */
public class ProgressImpl implements Progress
{
	private final JobExecution jobMetaData;
	private final DataService dataService;
	private final static Logger LOG = LoggerFactory.getLogger(ProgressImpl.class);

	public ProgressImpl(JobExecution jobMetaData, DataService dataService)
	{
		this.jobMetaData = jobMetaData;
		this.dataService = dataService;
	}

	private void update()
	{
		runAsSystem(() -> {
			dataService.update(jobMetaData.getEntityMetaData().getName(), jobMetaData);
		});
	}

	@Override
	public void start()
	{
		LOG.info("start ()");
		jobMetaData.setStartDate(new Date());
		jobMetaData.setStatus(RUNNING);
		update();
	}

	@Override
	public void progress(int progress, String message)
	{
		jobMetaData.setProgressInt(progress);
		jobMetaData.setProgressMessage(message);
		LOG.info("progress ({}, {})", progress, message);
		update();
	}

	@Override
	public void success()
	{
		jobMetaData.setEndDate(new Date());
		jobMetaData.setStatus(SUCCESS);
		jobMetaData.setProgressInt(jobMetaData.getProgressMax());
		LOG.info("success");
		update();
	}

	@Override
	public void failed(Exception ex)
	{
		LOG.error("Failed", ex);
		jobMetaData.setEndDate(new Date());
		jobMetaData.setStatus(FAILED);
		update();
	}

	@Override
	public void canceled()
	{
		jobMetaData.setEndDate(new Date());
		jobMetaData.setStatus(CANCELED);
		update();
	}

	@Override
	public Long timeRunning()
	{
		Date startDate = jobMetaData.getStartDate();
		if (startDate == null)
		{
			return null;
		}
		return System.currentTimeMillis() - startDate.getTime();
	}

	@Override
	public void setProgressMax(int max)
	{
		jobMetaData.setProgressMax(max);
		update();
	}

	@Override
	public void status(String message)
	{
		jobMetaData.setProgressMessage(message);
		update();
	}

}
