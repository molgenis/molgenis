package org.molgenis.file.ingest.meta;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.FILE_INGEST_JOB_TYPE;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.file.model.FileMeta;

public class FileIngestJobExecution extends JobExecution
{
	public FileIngestJobExecution(Entity entity)
	{
		super(entity);
	}

	public FileIngestJobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public FileIngestJobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(identifier, entityMeta);
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
