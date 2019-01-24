package org.molgenis.jobs.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobTypeMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "ScheduledJobType";
  public static final String SCHEDULED_JOB_TYPE = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String NAME = "name";
  public static final String LABEL = "label";
  public static final String DESCRIPTION = "description";
  public static final String JOB_EXECUTION_TYPE = "jobExecutionType";
  public static final String SCHEMA = "schema";
  private EntityTypeMetadata entityTypeMetadata;
  private final JobPackage jobPackage;

  ScheduledJobTypeMetadata(EntityTypeMetadata entityTypeMetadata, JobPackage jobPackage) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
    this.jobPackage = requireNonNull(jobPackage);
  }

  @Override
  public void init() {
    setLabel("Scheduled Job Type");
    setPackage(jobPackage);
    addAttribute(NAME, ROLE_ID).setLabel("Name");
    addAttribute(LABEL, ROLE_LABEL).setDataType(STRING).setLabel("Label");
    addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true);
    addAttribute(JOB_EXECUTION_TYPE)
        .setDataType(XREF)
        .setRefEntity(entityTypeMetadata)
        .setNillable(false);
    addAttribute(SCHEMA)
        .setDataType(TEXT)
        .setLabel("JSON Schema for the job parameters")
        .setNillable(true);
  }
}
