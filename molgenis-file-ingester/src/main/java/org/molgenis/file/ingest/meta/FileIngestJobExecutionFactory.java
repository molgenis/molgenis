package org.molgenis.file.ingest.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionFactory
    extends AbstractSystemEntityFactory<
        FileIngestJobExecution, FileIngestJobExecutionMetadata, String> {
  FileIngestJobExecutionFactory(
      FileIngestJobExecutionMetadata fileIngestJobExecutionMetadata,
      EntityPopulator entityPopulator) {
    super(FileIngestJobExecution.class, fileIngestJobExecutionMetadata, entityPopulator);
  }
}
