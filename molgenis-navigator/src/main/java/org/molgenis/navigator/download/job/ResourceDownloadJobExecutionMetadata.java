package org.molgenis.navigator.download.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@Component
public class ResourceDownloadJobExecutionMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "ResourceDownloadJobExecution";
  static final String RESOURCES = "resources";

  public static final String RESOURCE_DOWNLOAD_JOB_EXECUTION =
      PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  private final JobExecutionMetaData jobExecutionMetaData;
  private final JobPackage jobPackage;

  static final String DOWNLOAD_JOB_TYPE = "ResourceDownloadJob";

  ResourceDownloadJobExecutionMetadata(
      JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
    this.jobPackage = requireNonNull(jobPackage);
  }

  @Override
  public void init() {
    setLabel("Download job execution");
    setExtends(jobExecutionMetaData);
    setPackage(jobPackage);
    addAttribute(RESOURCES)
        .setLabel(RESOURCES)
        .setDataType(TEXT)
        .setDescription("List of resources to be downloaded.")
        .setNillable(true);
  }
}
