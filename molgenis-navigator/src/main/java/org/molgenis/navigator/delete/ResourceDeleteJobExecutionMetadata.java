package org.molgenis.navigator.delete;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@SuppressWarnings({"WeakerAccess", "unused"})
@Component
public class ResourceDeleteJobExecutionMetadata extends SystemEntityType {

  private static final String SIMPLE_NAME = "ResourceDeleteJobExecution";
  public static final String DELETE_JOB_EXECUTION = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String RESOURCES = "resources";

  public static final String DELETE_JOB_TYPE = "ResourceDeleteJob";

  private final JobPackage jobPackage;
  private final JobExecutionMetaData jobExecutionMetaData;

  public ResourceDeleteJobExecutionMetadata(
      JobPackage jobPackage, JobExecutionMetaData jobExecutionMetaData) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.jobPackage = requireNonNull(jobPackage);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
  }

  @Override
  public void init() {
    setPackage(jobPackage);
    setExtends(jobExecutionMetaData);
    setLabel("Resource Delete Job Execution");

    addAttribute(RESOURCES)
        .setDataType(TEXT)
        .setLabel("Resources")
        .setDescription("JSON containing the resources to delete.");
  }
}
