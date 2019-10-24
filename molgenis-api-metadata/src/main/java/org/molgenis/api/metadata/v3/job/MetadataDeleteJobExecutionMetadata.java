package org.molgenis.api.metadata.v3.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@Component
public class MetadataDeleteJobExecutionMetadata extends SystemEntityType {

  private static final String SIMPLE_NAME = "MetadataDeleteJobExecution";
  public static final String METADATA_DELETE_JOB_EXECUTION =
      PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  static final String METADATA_DELETE_JOB_TYPE = "MetadataDeleteJob";

  public static final String ENTITY_TYPE_IDS = "entityTypeIds";

  private final JobExecutionMetaData jobExecutionMetaData;
  private final JobPackage jobPackage;

  public MetadataDeleteJobExecutionMetadata(
      JobPackage jobPackage, JobExecutionMetaData jobExecutionMetaData) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.jobPackage = requireNonNull(jobPackage);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
  }

  @Override
  public void init() {
    setExtends(jobExecutionMetaData);
    setPackage(jobPackage);
    setRowLevelSecured(true);

    setLabel("Metadata delete job execution");

    addAttribute(ENTITY_TYPE_IDS)
        .setDataType(TEXT)
        .setLabel("EntityType IDs")
        .setDescription("Comma-separated list of entity type IDs")
        .setNillable(false);
  }
}
