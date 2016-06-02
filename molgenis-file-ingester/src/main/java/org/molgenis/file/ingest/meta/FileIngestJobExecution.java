package org.molgenis.file.ingest.meta;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST_JOB_TYPE;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.file.FileMeta;

public class FileIngestJobExecution extends JobExecution
{
	public FileIngestJobExecution(Entity entity)
	{
		super(entity);
		setDefaultValues();
	}

	public FileIngestJobExecution(FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData)
	{
		super(fileIngestJobExecutionMetaData);
		setDefaultValues();
	}

	public FileIngestJobExecution(String identifier, FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData)
	{
		super(identifier, fileIngestJobExecutionMetaData);
		setDefaultValues();
	}

	public FileMeta getFile()
	{
		return getEntity(FILE, FileMeta.class);
	}

	public void setFile(FileMeta value)
	{
		set(FILE, value);
	}

	public Entity getFileIngest()
	{
		return getEntity(FILE_INGEST);
	}

	public void setFileIngest(FileIngest fileIngest)
	{
		set(FILE_INGEST, fileIngest);
	}

	private void setDefaultValues()
	{
		setType(FILE_INGEST_JOB_TYPE);
	}
}
