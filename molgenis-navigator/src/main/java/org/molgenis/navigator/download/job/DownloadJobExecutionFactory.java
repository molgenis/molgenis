package org.molgenis.navigator.download.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class DownloadJobExecutionFactory
    extends AbstractSystemEntityFactory<
        DownloadJobExecution, DownloadJobExecutionMetaData, String> {
  DownloadJobExecutionFactory(
      DownloadJobExecutionMetaData downloadJobExecutionMetaData, EntityPopulator entityPopulator) {
    super(DownloadJobExecution.class, downloadJobExecutionMetaData, entityPopulator);
  }
}
