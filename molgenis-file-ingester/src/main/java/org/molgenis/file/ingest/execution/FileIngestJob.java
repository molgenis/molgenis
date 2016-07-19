package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.model.FileMeta;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class FileIngestJob extends Job<FileMeta>
{
	private final FileIngester fileIngester;
	private final String entityName;
	private final String url;
	private final String loader;
	private final String jobExecutionID;
	private final String failureEmail;

	public FileIngestJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			FileIngester fileIngester, String entityName, String url, String loader, String failureEmail,
			String jobExecutionID)
	{
		super(progress, transactionTemplate, authentication);
		this.fileIngester = fileIngester;
		this.entityName = entityName;
		this.url = url;
		this.loader = loader;
		this.failureEmail = failureEmail;
		this.jobExecutionID = jobExecutionID;
	}

	@Override
	public FileMeta call(Progress progress) throws Exception
	{
		return fileIngester.ingest(entityName, url, loader, jobExecutionID, progress, failureEmail);
	}

}
