package org.molgenis.navigator.copy.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class ResourceCopyJobExecutionFactory
    extends AbstractSystemEntityFactory<
        ResourceCopyJobExecution, ResourceCopyJobExecutionMetadata, String> {

  ResourceCopyJobExecutionFactory(
      ResourceCopyJobExecutionMetadata copyJobExecutionMetadata, EntityPopulator entityPopulator) {
    super(ResourceCopyJobExecution.class, copyJobExecutionMetadata, entityPopulator);
  }
}
