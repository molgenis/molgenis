package org.molgenis.ontology.core.importer;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.DataAction;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.importer.repository.OntologyFileExtensions;
import org.molgenis.ontology.core.meta.OntologyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OntologyImportService implements ImportService {
  private static final Logger LOG = LoggerFactory.getLogger(OntologyImportService.class);

  private final DataService dataService;

  public OntologyImportService(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  @Transactional
  public EntityImportReport doImport(
      RepositoryCollection source,
      MetadataAction metadataAction,
      DataAction dataAction,
      @Nullable @CheckForNull String packageId) {
    if (dataAction != DataAction.ADD) {
      throw new IllegalArgumentException("Only ADD is supported");
    }

    EntityImportReport report = new EntityImportReport();

    for (String entityTypeId : source.getEntityTypeIds()) {
      try (Repository<Entity> sourceRepository = source.getRepository(entityTypeId)) {
        Repository<Entity> targetRepository = dataService.getRepository(entityTypeId);
        Integer count = targetRepository.add(stream(sourceRepository));
        report.addEntityCount(entityTypeId, count);
      } catch (IOException e) {
        LOG.error("", e);
        throw new MolgenisDataException(e);
      }
    }
    return report;
  }

  @Override
  public EntitiesValidationReport validateImport(File file, RepositoryCollection source) {
    EntitiesValidationReport report = new EntitiesValidationReportImpl();

    if (source.getRepository(ONTOLOGY) == null)
      throw new MolgenisDataException("Exception Repository [" + ONTOLOGY + "] is missing");

    boolean ontologyExists = false;
    for (Entity ontologyEntity : source.getRepository(ONTOLOGY)) {
      String ontologyIRI = ontologyEntity.getString(OntologyMetadata.ONTOLOGY_IRI);
      String ontologyName = ontologyEntity.getString(OntologyMetadata.ONTOLOGY_NAME);

      Entity ontologyQueryEntity =
          dataService.findOne(
              ONTOLOGY,
              new QueryImpl<>()
                  .eq(OntologyMetadata.ONTOLOGY_IRI, ontologyIRI)
                  .or()
                  .eq(OntologyMetadata.ONTOLOGY_NAME, ontologyName));
      ontologyExists = ontologyQueryEntity != null;
    }

    if (ontologyExists)
      throw new MolgenisDataException("The ontology you are trying to import already exists");

    for (String entityTypeId : source.getEntityTypeIds()) {
      report.getSheetsImportable().put(entityTypeId, !ontologyExists);
    }
    return report;
  }

  @Override
  public boolean canImport(File file, RepositoryCollection source) {
    for (String extension : OntologyFileExtensions.getOntology()) {
      if (file.getName().toLowerCase().endsWith(extension)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int getOrder() {
    return 10;
  }

  @Override
  public List<MetadataAction> getSupportedMetadataActions() {
    return ImmutableList.of(MetadataAction.ADD);
  }

  @Override
  public List<DataAction> getSupportedDataActions() {
    return Lists.newArrayList(DataAction.ADD);
  }

  @Override
  public boolean getMustChangeEntityName() {
    return false;
  }

  @Override
  public Set<String> getSupportedFileExtensions() {
    return OntologyFileExtensions.getOntology();
  }

  @Override
  public Map<String, Boolean> determineImportableEntities(
      MetaDataService metaDataService,
      RepositoryCollection repositoryCollection,
      String defaultPackage) {
    return metaDataService.determineImportableEntities(repositoryCollection);
  }
}
