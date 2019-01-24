package org.molgenis.amazon.bucket.meta;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

public class AmazonBucketJobExecution extends JobExecution {
  public AmazonBucketJobExecution(Entity entity) {
    super(entity);
    setType(AmazonBucketJobExecutionMetadata.AMAZON_BUCKET_JOB_TYPE);
  }

  public AmazonBucketJobExecution(EntityType entityType) {
    super(entityType);
    setType(AmazonBucketJobExecutionMetadata.AMAZON_BUCKET_JOB_TYPE);
  }

  public AmazonBucketJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(AmazonBucketJobExecutionMetadata.AMAZON_BUCKET_JOB_TYPE);
  }

  public String getKey() {
    return getString(AmazonBucketJobExecutionMetadata.KEY);
  }

  public void setKey(String key) {
    set(AmazonBucketJobExecutionMetadata.KEY, key);
  }

  public String getBucket() {
    return getString(AmazonBucketJobExecutionMetadata.BUCKET);
  }

  public void setBucket(String bucket) {
    set(AmazonBucketJobExecutionMetadata.BUCKET, bucket);
  }

  public boolean isExpression() {
    return getBoolean(AmazonBucketJobExecutionMetadata.EXPRESSION);
  }

  public void setExpression(boolean expression) {
    set(AmazonBucketJobExecutionMetadata.EXPRESSION, expression);
  }

  @Nullable
  @CheckForNull
  public FileMeta getFile() {
    return getEntity(AmazonBucketJobExecutionMetadata.FILE, FileMeta.class);
  }

  public void setFile(FileMeta value) {
    set(AmazonBucketJobExecutionMetadata.FILE, value);
  }

  @Nullable
  @CheckForNull
  public String getTargetEntityId() {
    return getString(AmazonBucketJobExecutionMetadata.TARGET_ENTITY_ID);
  }

  public void setTargetEntityId(String targetEntityId) {
    set(AmazonBucketJobExecutionMetadata.TARGET_ENTITY_ID, targetEntityId);
  }

  public String getRegion() {
    return getString(AmazonBucketJobExecutionMetadata.REGION);
  }

  public void setRegion(String region) {
    set(AmazonBucketJobExecutionMetadata.REGION, region);
  }

  public void setAccessKey(String key) {
    set(AmazonBucketJobExecutionMetadata.ACCESS_KEY, key);
  }

  public String getAccessKey() {
    return getString(AmazonBucketJobExecutionMetadata.ACCESS_KEY);
  }

  public void setSecretKey(String key) {
    set(AmazonBucketJobExecutionMetadata.SECRET_KEY, key);
  }

  public String getSecretKey() {
    return getString(AmazonBucketJobExecutionMetadata.SECRET_KEY);
  }

  public void setExtension(String extension) {
    set(AmazonBucketJobExecutionMetadata.EXTENSION, extension);
  }

  @Nullable
  @CheckForNull
  public String getExtension() {
    return getString(AmazonBucketJobExecutionMetadata.EXTENSION);
  }
}
