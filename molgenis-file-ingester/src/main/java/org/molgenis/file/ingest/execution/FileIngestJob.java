package org.molgenis.file.ingest.execution;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class FileIngestJob extends Job
{
	private final FileIngester fileIngester;
	private final String entityName;
	private final String url;
	private final String loader;
	private final Entity fileIngestJobExecution;
	private final String failureEmail;

	public FileIngestJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			FileIngester fileIngester, String entityName, String url, String loader, String failureEmail,
			Entity fileIngestJobExecution)
	{
		super(progress, transactionTemplate, authentication);
		this.fileIngester = fileIngester;
		this.entityName = entityName;
		this.url = url;
		this.loader = loader;
		this.failureEmail = failureEmail;
		this.fileIngestJobExecution = fileIngestJobExecution;
	}

	@Override
	public void run(Progress progress) throws Exception
	{
		String jobExecutionID = fileIngestJobExecution.getIdValue().toString();
		fileIngester.ingest(entityName, url, loader, jobExecutionID, progress, failureEmail, fileIngestJobExecution);
	}

}
