package org.molgenis.navigator.delete.job;

import static org.molgenis.navigator.copy.job.ResourceCopyJobExecutionMetadata.RESOURCES;
import static org.molgenis.navigator.delete.job.ResourceDeleteJobExecutionMetadata.DELETE_JOB_TYPE;
import static org.molgenis.navigator.util.ResourceIdentifierUtil.getResourcesFromJson;

import com.google.gson.Gson;
import java.util.List;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.ResourceIdentifier;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ResourceDeleteJobExecution extends JobExecution {

  public ResourceDeleteJobExecution(Entity entity) {
    super(entity);
    setType(DELETE_JOB_TYPE);
  }

  public ResourceDeleteJobExecution(EntityType entityType) {
    super(entityType);
    setType(DELETE_JOB_TYPE);
  }

  public ResourceDeleteJobExecution(String id, EntityType entityType) {
    super(id, entityType);
    setType(DELETE_JOB_TYPE);
  }

  public void setResources(List<ResourceIdentifier> resourceIdentifiers) {
    set(RESOURCES, new Gson().toJson(resourceIdentifiers));
  }

  public List<ResourceIdentifier> getResources() {
    return getResourcesFromJson(getString(RESOURCES));
  }
}
