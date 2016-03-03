package org.molgenis.file.ingest;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.file.FileMeta;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FileIngester quartz job
 * 
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
		FileIngest fileIngest = dataService.findOne(FileIngestMetaData.ENTITY_NAME, fileIngestId, FileIngest.class);
		MolgenisUser admin = dataService.findOne(MolgenisUser.ENTITY_NAME,
				dataService.query(MolgenisUser.ENTITY_NAME).eq(MolgenisUser.USERNAME, "admin"), MolgenisUser.class);
		FileIngestJobExecution jobExecution = new FileIngestJobExecution(dataService);
		jobExecution.setUser(admin);// TODO system
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
		FileMeta fileMeta = job.call();
		dataService.add(FileMeta.ENTITY_NAME, fileMeta);
		jobExecution.setFile(fileMeta);
		dataService.update(FileIngestJobExecutionMetaData.ENTITY_NAME, jobExecution);
	}
}
