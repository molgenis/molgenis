package org.molgenis.data.rest.copy;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class CopyJobExecutionFactory
    extends AbstractSystemEntityFactory<CopyJobExecution, CopyJobExecutionMetadata, String> {

  CopyJobExecutionFactory(
      CopyJobExecutionMetadata copyJobExecutionMetadata, EntityPopulator entityPopulator) {
    super(CopyJobExecution.class, copyJobExecutionMetadata, entityPopulator);
  }
}
