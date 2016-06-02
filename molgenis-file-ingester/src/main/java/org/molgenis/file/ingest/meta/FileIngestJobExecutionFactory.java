package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionFactory
		extends AbstractEntityFactory<FileIngestJobExecution, FileIngestJobExecutionMetaData, String>
{
	@Autowired
	FileIngestJobExecutionFactory(FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData)
	{
		super(FileIngestJobExecution.class, fileIngestJobExecutionMetaData, String.class);
	}
}
