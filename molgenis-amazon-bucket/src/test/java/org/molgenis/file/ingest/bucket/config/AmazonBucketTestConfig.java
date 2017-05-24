package org.molgenis.file.ingest.bucket.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.jobs.config.JobTestConfig;
import org.molgenis.file.config.FileTestConfig;
import org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecutionFactory;
import org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecutionMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, FileTestConfig.class, JobTestConfig.class, AmazonBucketJobExecutionMetaData.class,
		AmazonBucketJobExecutionFactory.class })
public class AmazonBucketTestConfig
{
}
