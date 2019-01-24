package org.molgenis.amazon.bucket.config;

import org.molgenis.amazon.bucket.meta.AmazonBucketJobExecutionFactory;
import org.molgenis.amazon.bucket.meta.AmazonBucketJobExecutionMetadata;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.file.config.FileTestConfig;
import org.molgenis.jobs.config.JobTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  FileTestConfig.class,
  JobTestConfig.class,
  AmazonBucketJobExecutionMetadata.class,
  AmazonBucketJobExecutionFactory.class
})
public class AmazonBucketTestConfig {}
