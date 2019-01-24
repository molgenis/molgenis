package org.molgenis.oneclickimporter.job;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@Component
public class OneClickImportJobExecutionMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "OneClickImportJobExecution";
  public static final String ONE_CLICK_IMPORT_JOB_EXECUTION =
      PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String FILE = "file";
  public static final String ENTITY_TYPES = "entityTypes";
  public static final String PACKAGE = "package";

  private final JobExecutionMetaData jobExecutionMetaData;
  private final JobPackage jobPackage;

  public static final String ONE_CLICK_IMPORT_JOB_TYPE = "OneClickImportJob";

  OneClickImportJobExecutionMetadata(
      JobExecutionMetaData jobExecutionMetaData, JobPackage jobPackage) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
    this.jobPackage = requireNonNull(jobPackage);
  }

  @Override
  public void init() {
    setLabel("One click import job execution");
    setExtends(jobExecutionMetaData);
    setPackage(jobPackage);
    setRowLevelSecured(true);

    addAttribute(FILE)
        .setLabel("Imported file")
        .setDescription("The file that was imported")
        .setNillable(false);
    addAttribute(ENTITY_TYPES)
        .setLabel("Entity types")
        .setDescription("Imported entity types")
        .setDataType(TEXT);
    addAttribute(PACKAGE).setLabel("Package name");
  }
}
