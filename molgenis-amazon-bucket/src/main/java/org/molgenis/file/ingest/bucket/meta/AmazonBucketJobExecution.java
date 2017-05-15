package org.molgenis.file.ingest.bucket.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.model.FileMeta;

import static org.molgenis.file.ingest.bucket.meta.AmazonBucketJobExecutionMetaData.*;

public class AmazonBucketJobExecution extends JobExecution
{
	public AmazonBucketJobExecution(Entity entity)
	{
		super(entity);
		setType(AMAZON_BUCKET_JOB_TYPE);
	}

	public AmazonBucketJobExecution(EntityType entityType)
	{
		super(entityType);
		setType(AMAZON_BUCKET_JOB_TYPE);
	}

	public AmazonBucketJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType(AMAZON_BUCKET_JOB_TYPE);
	}

	public String getKey()
	{
		return getString(KEY);
	}

	public void setKey(String key)
	{
		set(KEY, key);
	}

	public String getBucket()
	{
		return getString(BUCKET);
	}

	public void setBucket(String bucket)
	{
		set(BUCKET, bucket);
	}

	public String getProfile()
	{
		return getString(PROFILE);
	}

	public void setProfile(String profile)
	{
		set(PROFILE, profile);
	}

	public boolean isExpression()
	{
		return getBoolean(EXPRESSION);
	}

	public void setExpression(boolean expression)
	{
		set(EXPRESSION, expression);
	}

	public FileMeta getFile()
	{
		return getEntity(FILE, FileMeta.class);
	}

	public void setFile(FileMeta value)
	{
		set(FILE, value);
	}

	public String getTargetEntityId()
	{
		return getString(TARGET_ENTITY_ID);
	}

	public void setTargetEntityId(String targetEntityId)
	{
		set(TARGET_ENTITY_ID, targetEntityId);
	}
}
