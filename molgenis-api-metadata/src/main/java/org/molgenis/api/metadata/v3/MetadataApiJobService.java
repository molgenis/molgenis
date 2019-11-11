package org.molgenis.api.metadata.v3;

import java.util.List;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.data.meta.model.EntityType;

public interface MetadataApiJobService {

  /**
   * Schedule an entity type create job.
   *
   * @param entityType entity type
   * @return job execution
   */
  MetadataUpsertJobExecution scheduleCreate(EntityType entityType);

  /**
   * Schedule an entity type update job.
   *
   * @param entityType updated entity type
   * @return job execution
   */
  MetadataUpsertJobExecution scheduleUpdate(EntityType entityType);

  MetadataDeleteJobExecution scheduleDelete(List<String> entityTypeIds);
}
