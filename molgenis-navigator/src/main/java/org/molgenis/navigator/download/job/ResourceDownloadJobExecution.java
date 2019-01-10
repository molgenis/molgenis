package org.molgenis.navigator.download.job;

import com.google.gson.Gson;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.util.ResourceIdentifierUtil;

public class ResourceDownloadJobExecution extends JobExecution {
  @SuppressWarnings("unused")
  public ResourceDownloadJobExecution(Entity entity) {
    super(entity);
    setType(ResourceDownloadJobExecutionMetadata.DOWNLOAD_JOB_TYPE);
  }

  @SuppressWarnings("unused")
  public ResourceDownloadJobExecution(EntityType entityType) {
    super(entityType);
    setType(ResourceDownloadJobExecutionMetadata.DOWNLOAD_JOB_TYPE);
  }

  @SuppressWarnings("unused")
  public ResourceDownloadJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(ResourceDownloadJobExecutionMetadata.DOWNLOAD_JOB_TYPE);
  }

  public void setResources(List<ResourceIdentifier> resourceIdentifiers) {
    set(ResourceDownloadJobExecutionMetadata.RESOURCES, new Gson().toJson(resourceIdentifiers));
  }

  @Nullable
  @CheckForNull
  public List<ResourceIdentifier> getResources() {
    String resourceJson = getString(ResourceDownloadJobExecutionMetadata.RESOURCES);
    return ResourceIdentifierUtil.getResourcesFromJson(resourceJson);
  }
}
