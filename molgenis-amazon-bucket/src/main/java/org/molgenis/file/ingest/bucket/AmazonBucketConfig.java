package org.molgenis.file.ingest.bucket;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobType;
import org.molgenis.data.jobs.model.JobTypeFactory;
import org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecution;
import org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecutionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AmazonBucketIngester.class)
public class AmazonBucketConfig
{
	@Autowired
	AmazonBucketIngester ingester;

	@Autowired
	JobTypeFactory jobTypeFactory;

	@Autowired
	AmazonBucketJobExecutionMetaData amazonBucketJobExecutionMetaData;

	@Bean
	public JobFactory amazonBucketJobFactory()
	{
		return new JobFactory()
		{
			@Override
			public Job createJob(JobExecution jobExecution)
			{
				final AmazonBucketJobExecution amazonBucketJobExecution = (AmazonBucketJobExecution) jobExecution;
				final String targetEntityId = amazonBucketJobExecution.getTargetEntityId();
				final String jobExecutionID = amazonBucketJobExecution.getIdentifier();
				final String bucket = amazonBucketJobExecution.getBucket();
				final String key = amazonBucketJobExecution.getKey();
				final String profile = amazonBucketJobExecution.getProfile();
				final boolean isExpression = amazonBucketJobExecution.isExpression();
				return progress -> ingester
						.ingest(jobExecutionID, targetEntityId, bucket, key, profile, isExpression, progress);
			}

			@Override
			public JobType getJobType()
			{
				JobType result = jobTypeFactory.create("BucketIngest");
				result.setLabel("Bucket ingest");
				result.setDescription("This job downloads a file from a URL and imports it into MOLGENIS.");
				result.setSchema("");
				result.setJobExecutionType(amazonBucketJobExecutionMetaData);
				return result;
			}
		};
	}
}
