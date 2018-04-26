package org.molgenis.amazon.bucket;

import org.molgenis.amazon.bucket.meta.AmazonBucketJobExecution;
import org.molgenis.amazon.bucket.meta.AmazonBucketJobExecutionMetaData;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import static java.util.Objects.requireNonNull;

@Configuration
@Import(AmazonBucketIngester.class)
public class AmazonBucketConfig
{
	private final AmazonBucketIngester ingester;
	private final ScheduledJobTypeFactory scheduledJobTypeFactory;
	private final AmazonBucketJobExecutionMetaData amazonBucketJobExecutionMetaData;

	public AmazonBucketConfig(AmazonBucketIngester ingester, ScheduledJobTypeFactory scheduledJobTypeFactory,
			AmazonBucketJobExecutionMetaData amazonBucketJobExecutionMetaData)
	{
		this.ingester = requireNonNull(ingester);
		this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
		this.amazonBucketJobExecutionMetaData = requireNonNull(amazonBucketJobExecutionMetaData);
	}

	@Bean
	public JobFactory<AmazonBucketJobExecution> amazonBucketJobFactory()
	{
		return new JobFactory<AmazonBucketJobExecution>()
		{
			@Override
			public Job createJob(AmazonBucketJobExecution amazonBucketJobExecution)
			{
				final String targetEntityId = amazonBucketJobExecution.getTargetEntityId();
				final String jobExecutionID = amazonBucketJobExecution.getIdentifier();
				final String bucket = amazonBucketJobExecution.getBucket();
				final String key = amazonBucketJobExecution.getKey();
				final String extension = amazonBucketJobExecution.getExtension();
				final String accessKey = amazonBucketJobExecution.getAccessKey();
				final String secretKey = amazonBucketJobExecution.getSecretKey();
				final String region = amazonBucketJobExecution.getRegion();
				final boolean isExpression = amazonBucketJobExecution.isExpression();
				return progress -> ingester.ingest(jobExecutionID, targetEntityId, bucket, key, extension, accessKey,
						secretKey, region, isExpression, progress);
			}
		};
	}

	@Bean
	@Lazy
	public ScheduledJobType bucketIngest()
	{
		ScheduledJobType result = scheduledJobTypeFactory.create("BucketIngest");
		result.setLabel("Bucket ingest");
		result.setDescription("This job downloads a file from a URL and imports it into MOLGENIS.");
		result.setSchema(
				"{'title': 'Bucket Ingest Job','type': 'object','properties': { 'bucket': {'type': 'string', 'description': 'The name of the bucket.'},"
						+ "'key': {'type': 'string', 'description': 'Expression to match the file key'}, 'accessKey': { 'type': 'string', "
						+ "'description': 'the access key to be used to login to the amazon bucket'},'secretKey': {'type': 'string', "
						+ "'description': 'the secretkey to be used to login to the amazon bucket'},'expression': {'type': 'boolean', "
						+ "'description': 'Is the key an expression or an exact match'},'extension': {'type': 'string', "
						+ "'description': 'Optional extension of the file, is not part of the key in the bucket'},'region': {'type': 'string', "
						+ "'description': 'The region where the amazon bucket is located'},'targetEntityId': {'type': 'string', "
						+ "'description': 'Target EntityType ID'}},"
						+ "'required': ['bucket','key','accessKey','secretKey','expression','region']}");
		result.setJobExecutionType(amazonBucketJobExecutionMetaData);
		return result;
	}
}
