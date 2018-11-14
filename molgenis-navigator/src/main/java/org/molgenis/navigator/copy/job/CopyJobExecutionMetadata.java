package org.molgenis.navigator.copy.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@SuppressWarnings({"WeakerAccess", "unused"})
@Component
public class CopyJobExecutionMetadata extends SystemEntityType {

  private static final String SIMPLE_NAME = "CopyJobExecution";
  public static final String COPY_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String RESOURCES = "resources";
  public static final String TARGET_PACKAGE = "targetPackage";

  public static final String COPY_JOB_TYPE = "ResourceCopyJob";

  private final JobPackage jobPackage;
  private final JobExecutionMetaData jobExecutionMetaData;

  public CopyJobExecutionMetadata(
      JobPackage jobPackage, JobExecutionMetaData jobExecutionMetaData) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.jobPackage = requireNonNull(jobPackage);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
  }

  @Override
  public void init() {
    setPackage(jobPackage);
    setExtends(jobExecutionMetaData);
    setLabel("Copy Job Execution");

    addAttribute(RESOURCES)
        .setDataType(TEXT)
        .setLabel("Resources")
        .setDescription("JSON containing the resources to copy.");
    addAttribute(TARGET_PACKAGE)
        .setDataType(STRING)
        .setLabel("Target package")
        .setDescription("The ID of the package to copy the resources to.");
  }
}
