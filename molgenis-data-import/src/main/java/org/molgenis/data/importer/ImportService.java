package org.molgenis.data.importer;

import io.micrometer.core.annotation.Timed;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.molgenis.data.DataAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.springframework.core.Ordered;

public interface ImportService extends Ordered {
  @Timed(
      value = "service.import",
      description = "Timing information for the import service.",
      histogram = true)
  EntityImportReport doImport(
      RepositoryCollection source,
      MetadataAction metadataAction,
      DataAction dataAction,
      @CheckForNull String packageId);

  EntitiesValidationReport validateImport(File file, RepositoryCollection source);

  boolean canImport(File file, RepositoryCollection source);

  List<MetadataAction> getSupportedMetadataActions();

  List<DataAction> getSupportedDataActions();

  boolean getMustChangeEntityName();

  Set<String> getSupportedFileExtensions();

  Map<String, Boolean> determineImportableEntities(
      MetaDataService metaDataService,
      RepositoryCollection repositoryCollection,
      String defaultPackage);
}
