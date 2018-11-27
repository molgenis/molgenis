package org.molgenis.navigator.download.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@Component
public class ResourceDownloadJobExecutionMetaData extends SystemEntityType {
  private static final String SIMPLE_NAME = "ResourceDownloadJobExecution";
  public static final String RESOURCES = "resources";

  private final JobExecutionMetaData jobExecutionMetaData;
  private final JobPackage jobPackage;

  public static final String DOWNLOAD_JOB_TYPE = "ResourceDownloadJob";

  ResourceDownloadJobExecutionMetaData(
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
