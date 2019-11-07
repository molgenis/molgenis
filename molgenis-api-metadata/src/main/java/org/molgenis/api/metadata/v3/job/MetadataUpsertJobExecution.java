package org.molgenis.api.metadata.v3.job;

import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MetadataUpsertJobExecution extends JobExecution {
  public MetadataUpsertJobExecution(Entity entity) {
    super(entity);
    setType(MetadataUpsertJobExecutionMetadata.METADATA_UPSERT_JOB_TYPE);
  }

  public MetadataUpsertJobExecution(EntityType entityType) {
    super(entityType);
    setType(MetadataUpsertJobExecutionMetadata.METADATA_UPSERT_JOB_TYPE);
  }

  public MetadataUpsertJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(MetadataUpsertJobExecutionMetadata.METADATA_UPSERT_JOB_TYPE);
  }

  public void setAction(Action action) {
    set(MetadataUpsertJobExecutionMetadata.ACTION, action.toString());
  }

  public Action getAction() {
    String action = getString(MetadataUpsertJobExecutionMetadata.ACTION);
    return Action.valueOf(action);
  }

  public void setEntityTypeData(String entityTypeData) {
    set(MetadataUpsertJobExecutionMetadata.ENTITY_TYPE_DATA, entityTypeData);
  }

  public String getEntityTypeData() {
    return getString(MetadataUpsertJobExecutionMetadata.ENTITY_TYPE_DATA);
  }
}
