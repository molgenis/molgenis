package org.molgenis.navigator.download.job;

import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

public class DownloadJobExecution extends JobExecution {
  public DownloadJobExecution(Entity entity) {
    super(entity);
    setType(DownloadJobExecutionMetaData.DOWNLOAD_JOB_TYPE);
  }

  public DownloadJobExecution(EntityType entityType) {
    super(entityType);
    setType(DownloadJobExecutionMetaData.DOWNLOAD_JOB_TYPE);
  }

  public DownloadJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(DownloadJobExecutionMetaData.DOWNLOAD_JOB_TYPE);
  }

  public void setResources(String resourceJson) {
    set(DownloadJobExecutionMetaData.RESOURCES, resourceJson);
  }

  @Nullable
  public String getResources() {
    return getString(DownloadJobExecutionMetaData.RESOURCES);
  }
}
