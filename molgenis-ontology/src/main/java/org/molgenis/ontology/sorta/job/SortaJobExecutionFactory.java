package org.molgenis.ontology.sorta.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetadata;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionFactory
    extends AbstractSystemEntityFactory<SortaJobExecution, SortaJobExecutionMetadata, String> {
  SortaJobExecutionFactory(
      SortaJobExecutionMetadata sortaJobExecutionMetaData, EntityPopulator entityPopulator) {
    super(SortaJobExecution.class, sortaJobExecutionMetaData, entityPopulator);
  }
}
