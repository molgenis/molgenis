package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.model.FileMeta;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class FileIngestJob extends Job<FileMeta>
{
	private final FileIngester fileIngester;
	private final String entityTypeId;
	private final String url;
	private final String loader;
	private final String jobExecutionID;

	public FileIngestJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			FileIngester fileIngester, String entityTypeId, String url, String loader, String jobExecutionID)
	{
		super(progress, transactionTemplate, authentication);
		this.fileIngester = fileIngester;
		this.entityTypeId = entityTypeId;
		this.url = url;
		this.loader = loader;
		this.jobExecutionID = jobExecutionID;
	}

	@Override
	public FileMeta call(Progress progress) throws Exception
	{
		return fileIngester.ingest(entityTypeId, url, loader, jobExecutionID, progress);
	}

}
