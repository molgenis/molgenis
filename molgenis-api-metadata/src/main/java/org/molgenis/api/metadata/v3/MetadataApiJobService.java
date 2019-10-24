package org.molgenis.api.metadata.v3;

import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
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

  /**
   * Schedule an entity type delete job.
   *
   * @param entityTypeId entity type to delete
   * @return job execution
   */
  MetadataDeleteJobExecution scheduleDelete(String entityTypeId);

  /**
   * Schedule an entity type delete job.
   *
   * @param q a query describing which entity types to delete
   * @return job execution
   */
  MetadataDeleteJobExecution scheduleDelete(Query q);
}
