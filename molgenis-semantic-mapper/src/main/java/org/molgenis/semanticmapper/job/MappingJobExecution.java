package org.molgenis.semanticmapper.job;

import static org.molgenis.semanticmapper.job.MappingJobExecutionMetadata.ADD_SOURCE_ATTRIBUTE;
import static org.molgenis.semanticmapper.job.MappingJobExecutionMetadata.LABEL;
import static org.molgenis.semanticmapper.job.MappingJobExecutionMetadata.MAPPING_PROJECT_ID;
import static org.molgenis.semanticmapper.job.MappingJobExecutionMetadata.PACKAGE_ID;
import static org.molgenis.semanticmapper.job.MappingJobExecutionMetadata.TARGET_ENTITY_TYPE_ID;

import javax.annotation.CheckForNull;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

public class MappingJobExecution extends JobExecution {
  public MappingJobExecution(Entity entity) {
    super(entity);
    setType(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
  }

  public MappingJobExecution(EntityType entityType) {
    super(entityType);
    setType(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
  }

  public MappingJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(MappingJobExecutionMetadata.MAPPING_JOB_TYPE);
  }

  public String getMappingProjectId() {
    return getString(MAPPING_PROJECT_ID);
  }

  public void setMappingProjectId(String mappingProjectId) {
    set(MAPPING_PROJECT_ID, mappingProjectId);
  }

  public String getTargetEntityTypeId() {
    return getString(TARGET_ENTITY_TYPE_ID);
  }

  public void setTargetEntityTypeId(String targetEntityTypeId) {
    set(TARGET_ENTITY_TYPE_ID, targetEntityTypeId);
  }

  @CheckForNull
  public Boolean isAddSourceAttribute() {
    return getBoolean(ADD_SOURCE_ATTRIBUTE);
  }

  public void setAddSourceAttribute(Boolean addSourceAttribute) {
    set(ADD_SOURCE_ATTRIBUTE, addSourceAttribute);
  }

  @CheckForNull
  public String getPackageId() {
    return getString(PACKAGE_ID);
  }

  public void setPackageId(String packageId) {
    set(PACKAGE_ID, packageId);
  }

  @CheckForNull
  public String getLabel() {
    return getString(LABEL);
  }

  public void setLabel(String label) {
    set(LABEL, label);
  }
}
