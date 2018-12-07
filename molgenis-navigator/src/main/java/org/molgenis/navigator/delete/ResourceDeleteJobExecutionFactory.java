package org.molgenis.navigator.delete;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class ResourceDeleteJobExecutionFactory
    extends AbstractSystemEntityFactory<
        ResourceDeleteJobExecution, ResourceDeleteJobExecutionMetadata, String> {

  ResourceDeleteJobExecutionFactory(
      ResourceDeleteJobExecutionMetadata resourceDeleteJobExecutionMetadata,
      EntityPopulator entityPopulator) {
    super(ResourceDeleteJobExecution.class, resourceDeleteJobExecutionMetadata, entityPopulator);
  }
}
