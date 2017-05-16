package org.molgenis.file.ingest.bucket;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecution;
import org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecutionMetaData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Objects.requireNonNull;

@Configuration
@Import(AmazonBucketIngester.class)
public class AmazonBucketConfig
{
	private final AmazonBucketIngester ingester;
	private final ScheduledJobTypeFactory scheduledJobTypeFactory;
	private final AmazonBucketJobExecutionMetaData amazonBucketJobExecutionMetaData;
	private final Gson gson;

	public AmazonBucketConfig(AmazonBucketIngester ingester, ScheduledJobTypeFactory scheduledJobTypeFactory,
			AmazonBucketJobExecutionMetaData amazonBucketJobExecutionMetaData, Gson gson)
	{
		this.ingester = requireNonNull(ingester);
		this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
		this.amazonBucketJobExecutionMetaData = requireNonNull(amazonBucketJobExecutionMetaData);
		this.gson = requireNonNull(gson);
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
				final String profile = amazonBucketJobExecution.getProfile();
				final boolean isExpression = amazonBucketJobExecution.isExpression();
				return progress -> ingester
						.ingest(jobExecutionID, targetEntityId, bucket, key, profile, isExpression, progress);
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
		result.setSchema(gson.toJson(of("title", "Bucket Ingest Job", "type", "object", "properties",
				of("bucket", of("type", "string", "description", "The name of the bucket."), "key",
						of("type", "string", "description", "Expression to match the file key"), "expression",
						of("type", "boolean", "description", "Is the key an expression or an exact match"), "profile",
						of("type", "string", "description", "the profile to be used to login to the bucket"),
						"targetEntityId", of("type", "string", "description", "Target EntityType ID")), "required",
				ImmutableList.of("bucket", "key", "profile"))));
		result.setJobExecutionType(amazonBucketJobExecutionMetaData);
		return result;
	}
}
