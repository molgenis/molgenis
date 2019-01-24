package org.molgenis.amazon.bucket.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class AmazonBucketJobExecutionFactory
    extends AbstractSystemEntityFactory<
        AmazonBucketJobExecution, AmazonBucketJobExecutionMetadata, String> {
  AmazonBucketJobExecutionFactory(
      AmazonBucketJobExecutionMetadata fileIngestJobExecutionMetaData,
      EntityPopulator entityPopulator) {
    super(AmazonBucketJobExecution.class, fileIngestJobExecutionMetaData, entityPopulator);
  }
}
