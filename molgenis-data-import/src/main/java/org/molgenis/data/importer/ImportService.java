package org.molgenis.data.importer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.data.DataAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.springframework.core.Ordered;

public interface ImportService extends Ordered {
  EntityImportReport doImport(
      RepositoryCollection source,
      MetadataAction metadataAction,
      DataAction dataAction,
      @Nullable String packageId);

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
