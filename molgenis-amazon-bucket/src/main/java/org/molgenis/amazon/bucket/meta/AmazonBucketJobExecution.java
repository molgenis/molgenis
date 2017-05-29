package org.molgenis.amazon.bucket.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.model.FileMeta;

public class AmazonBucketJobExecution extends JobExecution
{
	public AmazonBucketJobExecution(Entity entity)
	{
		super(entity);
		setType(AmazonBucketJobExecutionMetaData.AMAZON_BUCKET_JOB_TYPE);
	}

	public AmazonBucketJobExecution(EntityType entityType)
	{
		super(entityType);
		setType(AmazonBucketJobExecutionMetaData.AMAZON_BUCKET_JOB_TYPE);
	}

	public AmazonBucketJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType(AmazonBucketJobExecutionMetaData.AMAZON_BUCKET_JOB_TYPE);
	}

	public String getKey()
	{
		return getString(AmazonBucketJobExecutionMetaData.KEY);
	}

	public void setKey(String key)
	{
		set(AmazonBucketJobExecutionMetaData.KEY, key);
	}

	public String getBucket()
	{
		return getString(AmazonBucketJobExecutionMetaData.BUCKET);
	}

	public void setBucket(String bucket)
	{
		set(AmazonBucketJobExecutionMetaData.BUCKET, bucket);
	}

	public boolean isExpression()
	{
		return getBoolean(AmazonBucketJobExecutionMetaData.EXPRESSION);
	}

	public void setExpression(boolean expression)
	{
		set(AmazonBucketJobExecutionMetaData.EXPRESSION, expression);
	}

	public FileMeta getFile()
	{
		return getEntity(AmazonBucketJobExecutionMetaData.FILE, FileMeta.class);
	}

	public void setFile(FileMeta value)
	{
		set(AmazonBucketJobExecutionMetaData.FILE, value);
	}

	public String getTargetEntityId()
	{
		return getString(AmazonBucketJobExecutionMetaData.TARGET_ENTITY_ID);
	}

	public void setTargetEntityId(String targetEntityId)
	{
		set(AmazonBucketJobExecutionMetaData.TARGET_ENTITY_ID, targetEntityId);
	}

	public String getRegion()
	{
		return getString(AmazonBucketJobExecutionMetaData.REGION);
	}

	public void setRegion(String region)
	{
		set(AmazonBucketJobExecutionMetaData.REGION, region);
	}

	public void setAccessKey(String key)
	{
		set(AmazonBucketJobExecutionMetaData.ACCESS_KEY, key);
	}

	public String getAccessKey()
	{
		return getString(AmazonBucketJobExecutionMetaData.ACCESS_KEY);
	}

	public void setSecretKey(String key)
	{
		set(AmazonBucketJobExecutionMetaData.SECRET_KEY, key);
	}

	public String getSecretKey()
	{
		return getString(AmazonBucketJobExecutionMetaData.SECRET_KEY);
	}
}
