package org.molgenis.file.ingest.execution;

import org.molgenis.data.jobs.JobInterface;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.model.FileMeta;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FileIngestJob implements JobInterface<FileMeta>
{
	private FileIngester fileIngester;
	private final String entityTypeId;
	private final String url;
	private final String loader;
	private final String jobExecutionID;

	FileIngestJob(FileIngester fileIngester, String entityTypeId, String url, String loader, String jobExecutionID)
	{
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

	@Override
	public boolean isTransactional()
	{
		return false;
	}
}
