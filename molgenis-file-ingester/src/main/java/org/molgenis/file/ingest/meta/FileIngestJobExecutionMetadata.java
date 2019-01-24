package org.molgenis.file.ingest.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.molgenis.data.file.model.FileMetaMetadata;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

@Component
public class FileIngestJobExecutionMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "FileIngestJobExecution";
  public static final String FILE_INGEST_JOB_EXECUTION =
      PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String URL = "url";
  public static final String LOADER = "loader";
  public static final List<String> LOADERS = ImmutableList.of("CSV");

  public static final String FILE = "file";
  public static final String TARGET_ENTITY_ID = "targetEntityId";
  public static final String FILE_INGEST_JOB_TYPE = "FileIngesterJob";

  private final FileMetaMetadata fileMetaMetadata;
  private final JobExecutionMetaData jobExecutionMetaData;
  private final JobPackage jobPackage;

  FileIngestJobExecutionMetadata(
      FileMetaMetadata fileMetaMetadata,
      JobExecutionMetaData jobExecutionMetaData,
      JobPackage jobPackage) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.fileMetaMetadata = requireNonNull(fileMetaMetadata);
    this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
    this.jobPackage = requireNonNull(jobPackage);
  }

  @Override
  public void init() {
    setLabel("File ingest job execution");
    setExtends(jobExecutionMetaData);
    setPackage(jobPackage);
    addAttribute(FILE)
        .setLabel("File")
        .setDescription("The imported file.")
        .setDataType(XREF)
        .setRefEntity(fileMetaMetadata)
        .setNillable(true);
    addAttribute(URL)
        .setLabel("Url")
        .setDescription("Url of the file to download.")
        .setNillable(false);
    addAttribute(LOADER)
        .setDataType(ENUM)
        .setEnumOptions(LOADERS)
        .setLabel("Loader type")
        .setNillable(false);
    addAttribute(TARGET_ENTITY_ID)
        .setDataType(STRING)
        .setLabel("Target EntityType ID")
        .setNillable(false);
  }
}
