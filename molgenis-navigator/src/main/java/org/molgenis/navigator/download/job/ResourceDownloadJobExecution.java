package org.molgenis.navigator.download.job;

import com.google.gson.Gson;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.util.ResourceIdentifierUtil;

public class ResourceDownloadJobExecution extends JobExecution {
  public ResourceDownloadJobExecution(Entity entity) {
    super(entity);
    setType(ResourceDownloadJobExecutionMetaData.DOWNLOAD_JOB_TYPE);
  }

  public ResourceDownloadJobExecution(EntityType entityType) {
    super(entityType);
    setType(ResourceDownloadJobExecutionMetaData.DOWNLOAD_JOB_TYPE);
  }

  public ResourceDownloadJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(ResourceDownloadJobExecutionMetaData.DOWNLOAD_JOB_TYPE);
  }

  public void setResources(List<ResourceIdentifier> resourceIdentifiers) {
    set(ResourceDownloadJobExecutionMetaData.RESOURCES, new Gson().toJson(resourceIdentifiers));
  }

  @Nullable
  public List<ResourceIdentifier> getResources() {
    String resourceJson = getString(ResourceDownloadJobExecutionMetaData.RESOURCES);
    List<ResourceIdentifier> resourceIdentifiers =
        ResourceIdentifierUtil.getResourcesFromJson(resourceJson);
    return resourceIdentifiers;
  }
}
