package org.molgenis.data.importer.emx;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import javax.annotation.Nullable;
import org.molgenis.data.DataAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.importer.ParsedMetaData;

/** Parameter object for the import job. */
public class EmxImportJob {
  private final MetadataAction metadataAction;
  private final DataAction dataAction;

  // TODO: there is some overlap between source and parsedMetaData
  public final RepositoryCollection source;
  final ParsedMetaData parsedMetaData;

  public final EntityImportReport report = new EntityImportReport();
  private final String packageId;

  EmxImportJob(
      MetadataAction metadataAction,
      DataAction dataAction,
      RepositoryCollection source,
      ParsedMetaData parsedMetaData,
      @Nullable String packageId) {
    this.metadataAction = requireNonNull(metadataAction);
    this.dataAction = dataAction;
    this.source = source;
    this.parsedMetaData = parsedMetaData;
    this.packageId = packageId;
  }

  public MetadataAction getMetadataAction() {
    return metadataAction;
  }

  public DataAction getDataAction() {
    return dataAction;
  }

  RepositoryCollection getSource() {
    return source;
  }

  ParsedMetaData getParsedMetaData() {
    return parsedMetaData;
  }

  public Optional<String> getPackageId() {
    return packageId != null ? Optional.of(packageId) : Optional.empty();
  }

  public EntityImportReport getEntityImportReport() {
    return report;
  }
}
