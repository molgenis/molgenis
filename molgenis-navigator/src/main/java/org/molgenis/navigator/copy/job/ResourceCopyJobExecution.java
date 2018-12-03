package org.molgenis.navigator.copy.job;

import static org.molgenis.navigator.copy.job.ResourceCopyJobExecutionMetadata.COPY_JOB_TYPE;
import static org.molgenis.navigator.copy.job.ResourceCopyJobExecutionMetadata.RESOURCES;
import static org.molgenis.navigator.copy.job.ResourceCopyJobExecutionMetadata.TARGET_PACKAGE;
import static org.molgenis.navigator.util.ResourceIdentifierUtil.getResourcesFromJson;

import com.google.gson.Gson;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.ResourceIdentifier;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ResourceCopyJobExecution extends JobExecution {

  public ResourceCopyJobExecution(Entity entity) {
    super(entity);
    setType(COPY_JOB_TYPE);
  }

  public ResourceCopyJobExecution(EntityType entityType) {
    super(entityType);
    setType(COPY_JOB_TYPE);
  }

  public ResourceCopyJobExecution(String id, EntityType entityType) {
    super(id, entityType);
    setType(COPY_JOB_TYPE);
  }

  public void setResources(List<ResourceIdentifier> resourceIdentifiers) {
    set(RESOURCES, new Gson().toJson(resourceIdentifiers));
  }

  public List<ResourceIdentifier> getResources() {
    return getResourcesFromJson(getString(RESOURCES));
  }

  public void setTargetPackage(@Nullable String targetPackageId) {
    set(TARGET_PACKAGE, targetPackageId);
  }

  @Nullable
  public String getTargetPackage() {
    return getString(TARGET_PACKAGE);
  }
}
