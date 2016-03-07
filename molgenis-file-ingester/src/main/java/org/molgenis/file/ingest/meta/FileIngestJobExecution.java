package org.molgenis.file.ingest.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.file.FileMeta;

public class FileIngestJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1L;

	public static final EntityMetaData META_DATA = new FileIngestJobExecutionMetaData();

	public FileIngestJobExecution(DataService dataService)
	{
		super(dataService, META_DATA);
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
