package org.molgenis.data.importer.emx;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.DataAction;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.importer.MetadataParser;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

public class EmxImportService implements ImportService {
  private static final Logger LOG = LoggerFactory.getLogger(EmxImportService.class);

  private final MetadataParser parser;
  private final ImportWriter writer;
  private final DataService dataService;

  public EmxImportService(MetadataParser parser, ImportWriter writer, DataService dataService) {
    this.parser = requireNonNull(parser);
    this.writer = requireNonNull(writer);
    this.dataService = requireNonNull(dataService);
  }

  @Override
  public boolean canImport(File file, RepositoryCollection source) {
    String fileNameExtension = StringUtils.getFilenameExtension(file.getName());
    if (getSupportedFileExtensions().contains(fileNameExtension.toLowerCase())) {
      for (String entityTypeId : source.getEntityTypeIds()) {
        if (isMetadataSheet(entityTypeId) || isI18nSheet(entityTypeId)) {
          return true;
        }
        if (dataService.getMeta().hasEntityType(entityTypeId)) return true;
      }
    }

    return false;
  }

  private boolean isMetadataSheet(String entityTypeId) {
    return entityTypeId.equalsIgnoreCase(EmxMetadataParser.EMX_ATTRIBUTES)
        || entityTypeId.equalsIgnoreCase(EmxMetadataParser.EMX_PACKAGES);
  }

  private boolean isI18nSheet(String entityTypeId) {
    return entityTypeId.equalsIgnoreCase(EmxMetadataParser.EMX_LANGUAGES)
        || entityTypeId.equalsIgnoreCase(EmxMetadataParser.EMX_I18NSTRINGS);
  }

  @Override
  public EntityImportReport doImport(
      final RepositoryCollection source,
      MetadataAction metadataAction,
      DataAction dataAction,
      @Nullable @CheckForNull String packageId) {
    ParsedMetaData parsedMetaData = parser.parse(source, packageId);

    // TODO altered entities (merge, see getEntityType)
    return doImport(
        new EmxImportJob(metadataAction, dataAction, source, parsedMetaData, packageId));
  }

  /**
   * Does the import in a transaction. Manually rolls back schema changes if something goes wrong.
   * Refreshes the metadata.
   *
   * @return {@link EntityImportReport} describing what happened
   */
  public EntityImportReport doImport(EmxImportJob job) {
    try {
      return writer.doImport(job);
    } catch (Exception e) {
      LOG.error("Error handling EmxImportJob", e);
      throw e;
    }
  }

  @Override
  public EntitiesValidationReport validateImport(File file, RepositoryCollection source) {
    return parser.validate(source);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public List<MetadataAction> getSupportedMetadataActions() {
    return ImmutableList.of(MetadataAction.UPSERT, MetadataAction.IGNORE);
  }

  @Override
  public List<DataAction> getSupportedDataActions() {
    return newArrayList(DataAction.values());
  }

  @Override
  public boolean getMustChangeEntityName() {
    return false;
  }

  @Override
  public Set<String> getSupportedFileExtensions() {
    return EmxFileExtensions.getEmx();
  }

  @Override
  public LinkedHashMap<String, Boolean> determineImportableEntities(
      MetaDataService metaDataService,
      RepositoryCollection repositoryCollection,
      String selectedPackage) {
    List<String> skipEntities =
        newArrayList(
            EmxMetadataParser.EMX_ATTRIBUTES,
            EmxMetadataParser.EMX_PACKAGES,
            EmxMetadataParser.EMX_ENTITIES,
            EmxMetadataParser.EMX_TAGS);
    ImmutableMap<String, EntityType> entityTypeMap =
        parser.parse(repositoryCollection, selectedPackage).getEntityMap();

    LinkedHashMap<String, Boolean> importableEntitiesMap = newLinkedHashMap();
    entityTypeMap
        .keySet()
        .forEach(
            entityTypeId -> {
              boolean importable =
                  skipEntities.contains(entityTypeId)
                      || metaDataService.isEntityTypeCompatible(entityTypeMap.get(entityTypeId));

              importableEntitiesMap.put(entityTypeId, importable);
            });

    return importableEntitiesMap;
  }
}
