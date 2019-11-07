package org.molgenis.api.metadata.v3.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class MetadataDeleteJobExecutionFactory
    extends AbstractSystemEntityFactory<
        MetadataDeleteJobExecution, MetadataDeleteJobExecutionMetadata, String> {

  MetadataDeleteJobExecutionFactory(
      MetadataDeleteJobExecutionMetadata metadataDeleteJobExecutionMetadata,
      EntityPopulator entityPopulator) {
    super(MetadataDeleteJobExecution.class, metadataDeleteJobExecutionMetadata, entityPopulator);
  }
}
