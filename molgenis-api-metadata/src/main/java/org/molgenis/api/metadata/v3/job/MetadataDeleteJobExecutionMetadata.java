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
public class MetadataDeleteJobExecutionMetadata extends SystemEntityType {

  private static final String SIMPLE_NAME = "MetadataDeleteJobExecution";
  public static final String METADATA_DELETE_JOB_EXECUTION =
      PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public enum DeleteType {
    ENTITY_TYPE,
    ATTRIBUTE
  }

  static final String METADATA_DELETE_JOB_TYPE = "MetadataDeleteJob";

  public static final String IDS = "ids";
  public static final String DELETE_TYPE = "deleteType";
  public static final String ENTITYTYPEID = "entityTypeId";

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

    addAttribute(DELETE_TYPE)
        .setLabel("Delete type")
        .setDescription("The type of resource to delete")
        .setDataType(ENUM)
        .setEnumOptions(stream(DeleteType.values()).map(DeleteType::toString).collect(toList()))
        .setNillable(false);

    addAttribute(IDS)
        .setDataType(TEXT)
        .setLabel("IDs")
        .setDescription("Comma-separated list of IDs")
        .setNillable(false);

    addAttribute(ENTITYTYPEID)
        .setDataType(TEXT)
        .setLabel("EntityType Id")
        .setDescription("The id of the entitytype of which this attributes are part of.")
        .setNillable(true);
  }
}
