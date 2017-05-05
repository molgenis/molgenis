package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionFactory;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;

import static java.net.URLEncoder.encode;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.*;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * FileIngester quartz job
 * <p>
 * It prevents concurrent execution of jobs with the same JobKey
 */
@DisallowConcurrentExecution
public class FileIngesterQuartzJob implements Job
{
	private static final Logger LOG = LoggerFactory.getLogger(FileIngesterQuartzJob.class);

	@Autowired
	private FileIngestJobFactory fileIngestJobFactory;

	@Autowired
	private FileIngestJobExecutionFactory fileIngestJobExecutionFactory;

	@Autowired
	private DataService dataService;

	public FileIngesterQuartzJob()
	{
	}

	public FileIngesterQuartzJob(FileIngestJobFactory fileIngestJobFactory, DataService dataService)
	{
		this.fileIngestJobFactory = fileIngestJobFactory;
		this.dataService = dataService;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		runAsSystem(() -> run(context.getMergedJobDataMap()));
	}

	private void run(JobDataMap jobDataMap)
	{
		FileIngestJobExecution jobExecution = fileIngestJobExecutionFactory.create();
		jobExecution.setUser("admin");// TODO system

		// set job-specific parameters from job data map
		jobExecution.setUrl(jobDataMap.getString(URL));
		jobExecution.setLoader(jobDataMap.getString(LOADER));
		String targetEntityId = jobDataMap.getString(ENTITY_META_DATA);
		EntityType targetEntity = dataService.getEntityType(targetEntityId);
		jobExecution.setTargetEntity(targetEntity);

		// set generic ScheduledJob parameters from job data map
		jobExecution.setSuccessEmail(jobDataMap.getString(ScheduledJobMetadata.SUCCESS_EMAIL));
		jobExecution.setFailureEmail(jobDataMap.getString(ScheduledJobMetadata.FAILURE_EMAIL));
		try
		{
			jobExecution.setResultUrl("/menu/main/dataexplorer?entity=" + encode(targetEntityId, "UTF-8"));
		}
		catch (UnsupportedEncodingException ex)
		{
			LOG.error("UTF-8 not supported by URLEncoder", ex);
		}
		FileIngestJob job = fileIngestJobFactory.createJob(jobExecution);
		job.call();
	}
}
