package org.molgenis.file.ingest.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.file.config.FileTestConfig;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetadata;
import org.molgenis.jobs.config.JobTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  FileTestConfig.class,
  JobTestConfig.class,
  FileIngestJobExecutionMetadata.class,
  FileIngestJobExecutionFactory.class
})
public class FileIngestTestConfig {}
