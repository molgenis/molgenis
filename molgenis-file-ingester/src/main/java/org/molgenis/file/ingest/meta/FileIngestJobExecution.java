package org.molgenis.file.ingest.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.model.FileMeta;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData.*;

public class FileIngestJobExecution extends JobExecution
{
	public FileIngestJobExecution(Entity entity)
	{
		super(entity);
	}

	public FileIngestJobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public FileIngestJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
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
