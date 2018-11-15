package org.molgenis.navigator.download.job;

import com.google.gson.Gson;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.util.ResourceIdentifierUtil;

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

  public void setResources(List<ResourceIdentifier> resourceIdentifiers) {
    set(DownloadJobExecutionMetaData.RESOURCES, new Gson().toJson(resourceIdentifiers));
  }

  @Nullable
  public List<ResourceIdentifier> getResources() {
    String resourceJson = getString(DownloadJobExecutionMetaData.RESOURCES);
    List<ResourceIdentifier> resourceIdentifiers =
        ResourceIdentifierUtil.getResourcesFromJson(resourceJson);
    return resourceIdentifiers;
  }
}
