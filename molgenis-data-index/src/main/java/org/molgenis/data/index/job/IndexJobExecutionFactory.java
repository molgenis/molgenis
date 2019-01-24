package org.molgenis.data.index.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class IndexJobExecutionFactory
    extends AbstractSystemEntityFactory<IndexJobExecution, IndexJobExecutionMetadata, String> {
  IndexJobExecutionFactory(
      IndexJobExecutionMetadata indexJobExecutionMeta, EntityPopulator entityPopulator) {
    super(IndexJobExecution.class, indexJobExecutionMeta, entityPopulator);
  }
}
