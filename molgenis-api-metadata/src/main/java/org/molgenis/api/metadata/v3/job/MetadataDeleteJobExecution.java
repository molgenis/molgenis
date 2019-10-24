package org.molgenis.api.metadata.v3.job;

import static java.util.Arrays.asList;
import static org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata.ENTITY_TYPE_IDS;
import static org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata.METADATA_DELETE_JOB_TYPE;

import java.util.List;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

@SuppressWarnings("unused")
public class MetadataDeleteJobExecution extends JobExecution {

  public MetadataDeleteJobExecution(Entity entity) {
    super(entity);
    setType(METADATA_DELETE_JOB_TYPE);
  }

  public MetadataDeleteJobExecution(EntityType entityType) {
    super(entityType);
    setType(METADATA_DELETE_JOB_TYPE);
  }

  public MetadataDeleteJobExecution(String id, EntityType entityType) {
    super(entityType);
    setType(METADATA_DELETE_JOB_TYPE);
  }

  public List<String> getEntityTypeIds() {
    return asList(getString(ENTITY_TYPE_IDS).split(","));
  }

  public void setEntityTypeIds(List<String> entityTypeIds) {
    set(ENTITY_TYPE_IDS, String.join(",", entityTypeIds));
  }
}
