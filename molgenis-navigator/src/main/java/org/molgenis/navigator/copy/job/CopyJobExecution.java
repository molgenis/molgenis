package org.molgenis.navigator.copy.job;

import static org.molgenis.navigator.copy.job.CopyJobExecutionMetadata.COPY_JOB_TYPE;
import static org.molgenis.navigator.copy.job.CopyJobExecutionMetadata.RESOURCES;
import static org.molgenis.navigator.copy.job.CopyJobExecutionMetadata.TARGET_PACKAGE;
import static org.molgenis.navigator.util.ResourceIdentifierUtil.getResourcesFromJson;

import com.google.gson.Gson;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.ResourceIdentifier;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CopyJobExecution extends JobExecution {

  public CopyJobExecution(Entity entity) {
    super(entity);
    setType(COPY_JOB_TYPE);
  }

  public CopyJobExecution(EntityType entityType) {
    super(entityType);
    setType(COPY_JOB_TYPE);
  }

  public CopyJobExecution(String id, EntityType entityType) {
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
