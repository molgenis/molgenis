package org.molgenis.file.ingest.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.jobs.config.JobTestConfig;
import org.molgenis.file.config.FileTestConfig;
import org.molgenis.file.ingest.meta.FileIngestFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, FileTestConfig.class, JobTestConfig.class, FileIngestMetaData.class,
		FileIngestFactory.class, FileIngestJobExecutionMetaData.class, FileIngestJobExecutionFactory.class })
public class FileIngestTestConfig
{
}
