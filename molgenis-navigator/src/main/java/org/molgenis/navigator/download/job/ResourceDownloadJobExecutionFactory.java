package org.molgenis.navigator.download.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ResourceDownloadJobExecutionFactory
    extends AbstractSystemEntityFactory<
        ResourceDownloadJobExecution, ResourceDownloadJobExecutionMetadata, String> {
  ResourceDownloadJobExecutionFactory(
      ResourceDownloadJobExecutionMetadata downloadJobExecutionMetaData,
      EntityPopulator entityPopulator) {
    super(ResourceDownloadJobExecution.class, downloadJobExecutionMetaData, entityPopulator);
  }
}
