package org.molgenis.api.metadata.v3.job;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@Component
public class MetadataUpsertJobExecutionMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "MetadataUpsertJobExecution";

  public enum Action {
    CREATE,
    UPDATE
  }

  static final String ACTION = "action";
  static final String ENTITY_TYPE_DATA = "entityTypeData";

  public static final String METADATA_UPSERT_JOB_EXECUTION =
      PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private final JobExecutionMetaData jobExecutionMetaData;
  private final JobPackage jobPackage;

  static final String METADATA_UPSERT_JOB_TYPE = "MetadataUpsertJob";

  MetadataUpsertJobExecutionMetadata(
      JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
    this.jobPackage = requireNonNull(jobPackage);
  }

  @Override
  public void init() {
    setLabel("Metadata create/update job execution");
    setExtends(jobExecutionMetaData);
    setPackage(jobPackage);

    addAttribute(ACTION)
        .setLabel("Action")
        .setDataType(ENUM)
        .setEnumOptions(stream(Action.values()).map(Action::toString).collect(toList()))
        .setNillable(false);

    addAttribute(ENTITY_TYPE_DATA)
        .setLabel("Entity type data")
        .setDataType(TEXT)
        .setNillable(false);

    setRowLevelSecured(true);
  }
}
