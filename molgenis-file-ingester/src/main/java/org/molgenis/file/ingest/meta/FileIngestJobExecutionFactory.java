package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionFactory
		extends AbstractSystemEntityFactory<FileIngestJobExecution, FileIngestJobExecutionMetaData, String>
{
	@Autowired
	FileIngestJobExecutionFactory(FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData)
	{
		super(FileIngestJobExecution.class, fileIngestJobExecutionMetaData, String.class);
	}
}
