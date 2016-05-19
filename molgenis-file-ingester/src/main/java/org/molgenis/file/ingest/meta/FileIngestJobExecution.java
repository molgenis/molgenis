package org.molgenis.file.ingest.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistrySingleton;
import org.molgenis.file.FileMeta;

public class FileIngestJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1L;

	public FileIngestJobExecution(DataService dataService)
	{
		super(dataService, SystemEntityMetaDataRegistrySingleton.INSTANCE.getSystemEntityMetaData(FileIngestJobExecutionMetaData.ENTITY_NAME));
		setType("FileIngesterJob");
	}

	public FileMeta getFile()
	{
		return getEntity(FileIngestJobExecutionMetaData.FILE, FileMeta.class);
	}

	public void setFile(FileMeta value)
	{
		set(FileIngestJobExecutionMetaData.FILE, value);
	}

	public Entity getFileIngest()
	{
		return getEntity(FileIngestJobExecutionMetaData.FILE_INGEST);
	}

	public void setFileIngest(FileIngest fileIngest)
	{
		set(FileIngestJobExecutionMetaData.FILE_INGEST, fileIngest);
	}

}
