package org.molgenis.jobs.model;

import static org.molgenis.jobs.model.ScheduledJobTypeMetadata.*;

import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

/** Describes a schedulable Job type. */
public class ScheduledJobType extends StaticEntity {
  public ScheduledJobType(Entity entity) {
    super(entity);
  }

  public ScheduledJobType(EntityType entityType) {
    super(entityType);
  }

  public ScheduledJobType(String name, EntityType entityType) {
    super(entityType);
    setName(name);
  }

  public String getName() {
    return getString(NAME);
  }

  public void setName(String name) {
    set(NAME, name);
  }

  @Nullable
  String getLabel() {
    return getString(LABEL);
  }

  public void setLabel(String label) {
    set(LABEL, label);
  }

  @Nullable
  String getDescription() {
    return getString(DESCRIPTION);
  }

  public void setDescription(String description) {
    set(DESCRIPTION, description);
  }

  public EntityType getJobExecutionType() {
    return getEntity(JOB_EXECUTION_TYPE, EntityType.class);
  }

  public void setJobExecutionType(EntityType jobExecutionType) {
    set(JOB_EXECUTION_TYPE, jobExecutionType);
  }

  @Nullable
  public String getSchema() {
    return getString(SCHEMA);
  }

  public void setSchema(String schema) {
    set(SCHEMA, schema);
  }
}
