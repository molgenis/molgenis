package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionFactory
		extends AbstractSystemEntityFactory<FileIngestJobExecution, FileIngestJobExecutionMetaData, String>
{
	FileIngestJobExecutionFactory(FileIngestJobExecutionMetaData fileIngestJobExecutionMetaData,
			EntityPopulator entityPopulator)
	{
		super(FileIngestJobExecution.class, fileIngestJobExecutionMetaData, entityPopulator);
	}
}
