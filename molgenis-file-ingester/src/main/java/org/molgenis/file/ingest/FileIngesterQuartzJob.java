package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionFactory;
import org.molgenis.file.model.FileMeta;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST_JOB_EXECUTION;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.FILE_INGEST;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * FileIngester quartz job
 * <p>
 * It prevents concurrent execution of jobs with the same JobKey
 */
@DisallowConcurrentExecution
public class FileIngesterQuartzJob implements Job
{
	public static final String ENTITY_KEY = "entity";
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
		Object fileIngestId = context.getMergedJobDataMap().get(ENTITY_KEY);
		runAsSystem(() -> run(fileIngestId));
	}

	private void run(Object fileIngestId)
	{
		FileIngest fileIngest = dataService.findOneById(FILE_INGEST, fileIngestId, FileIngest.class);
		FileIngestJobExecution jobExecution = fileIngestJobExecutionFactory.create();
		jobExecution.setUser("admin");// TODO system
		jobExecution.setFileIngest(fileIngest);
		jobExecution.setFailureEmail(fileIngest.getFailureEmail());
		try
		{
			jobExecution.setResultUrl(
					"/menu/main/dataexplorer?entity=" + URLEncoder.encode(fileIngest.getTargetEntityName(), "UTF-8"));
		}
		catch (UnsupportedEncodingException ex)
		{
			LOG.error("UTF-8 not supported by URLEncoder", ex);
		}
		FileIngestJob job = fileIngestJobFactory.createJob(jobExecution);
		job.call();
	}
}
