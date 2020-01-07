package org.molgenis.api.metadata.v3.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class MetadataUpsertJobExecutionFactory
    extends AbstractSystemEntityFactory<
        MetadataUpsertJobExecution, MetadataUpsertJobExecutionMetadata, String> {
  MetadataUpsertJobExecutionFactory(
      MetadataUpsertJobExecutionMetadata metadataUpsertJobExecutionMetadata,
      EntityPopulator entityPopulator) {
    super(MetadataUpsertJobExecution.class, metadataUpsertJobExecutionMetadata, entityPopulator);
  }
}
