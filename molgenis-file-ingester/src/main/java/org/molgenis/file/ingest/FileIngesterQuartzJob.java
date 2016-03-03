package org.molgenis.file.ingest;

import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.execution.FileIngester;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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

	@Autowired
	FileIngester fileIngester;

	@Autowired
	FileIngestJobFactory fileIngestJobFactory;

	@Autowired
	DataService dataService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		Entity fileIngestEntity = (Entity) context.getMergedJobDataMap().get(ENTITY_KEY);
		JobExecution fileIngestJobExecution = new JobExecution(dataService, new FileIngestJobExecutionMetaData());
		fileIngestJobExecution.set(JobExecution.TYPE, "FileIngesterJob");
		fileIngestJobExecution.set(JobExecution.USER,
				dataService.query(MolgenisUser.ENTITY_NAME).eq(MolgenisUser.USERNAME, "admin").findOne());// TODO system
		fileIngestJobExecution.set(FileIngestJobExecutionMetaData.FILE_INGEST, fileIngestEntity);
		fileIngestJobExecution.set(JobExecution.FAILURE_EMAIL,
				fileIngestEntity.getString(FileIngestMetaData.FAILURE_EMAIL));
		FileIngestJob job = fileIngestJobFactory.createJob(fileIngestJobExecution);
		job.run();
	}
}
