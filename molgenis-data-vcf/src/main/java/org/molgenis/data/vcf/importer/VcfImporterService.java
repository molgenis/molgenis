package org.molgenis.data.vcf.importer;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.molgenis.data.*;
import org.molgenis.data.importer.*;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.vcf.VcfFileExtensions;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VcfImporterService implements ImportService {
  private static final Logger LOG = LoggerFactory.getLogger(VcfImporterService.class);
  private static final int BATCH_SIZE = 10000;

  private final DataService dataService;
  private final PermissionSystemService permissionSystemService;
  private final MetaDataService metaDataService;

  public VcfImporterService(
      DataService dataService,
      PermissionSystemService permissionSystemService,
      MetaDataService metaDataService) {

    this.dataService = requireNonNull(dataService);
    this.metaDataService = requireNonNull(metaDataService);
    this.permissionSystemService = requireNonNull(permissionSystemService);
  }

  @Transactional
  @Override
  public EntityImportReport doImport(
      RepositoryCollection source,
      MetadataAction metadataAction,
      DataAction dataAction,
      @Nullable String packageId) {
    if (dataAction != DataAction.ADD) {
      throw new IllegalArgumentException("Only ADD is supported");
    }

    Package importPackage = null;
    if (packageId != null) {
      importPackage = metaDataService.getPackage(packageId);
      if (importPackage == null) {
        throw new UnknownEntityException(PACKAGE, packageId);
      }
    }

    List<EntityType> addedEntities = new ArrayList<>();
    EntityImportReport report;

    Iterator<String> it = source.getEntityTypeIds().iterator();
    if (it.hasNext()) {
      try (Repository<Entity> repo = source.getRepository(it.next())) {
        report = importVcf(repo, addedEntities, importPackage);
      } catch (IOException e) {
        throw new MolgenisDataException(e);
      }
    } else {
      report = new EntityImportReport();
    }
    return report;
  }

  @Override
  public EntitiesValidationReport validateImport(File file, RepositoryCollection source) {
    EntitiesValidationReport report = new EntitiesValidationReportImpl();
    Iterator<String> it = source.getEntityTypeIds().iterator();
    if (it.hasNext()) {
      String entityTypeId = it.next();
      EntityType emd = source.getRepository(entityTypeId).getEntityType();

      // Vcf entity
      boolean entityExists = runAsSystem(() -> dataService.hasRepository(entityTypeId));
      report.getSheetsImportable().put(entityTypeId, !entityExists);

      // Available Attributes
      List<String> availableAttributeNames = Lists.newArrayList();
      for (Attribute attr : emd.getAtomicAttributes()) {
        availableAttributeNames.add(attr.getName());
      }
      report.getFieldsImportable().put(entityTypeId, availableAttributeNames);

      // Sample entity
      Attribute sampleAttribute = emd.getAttribute(VcfAttributes.SAMPLES);
      if (sampleAttribute != null) {
        String sampleEntityName = sampleAttribute.getRefEntity().getId();
        boolean sampleEntityExists = runAsSystem(() -> dataService.hasRepository(sampleEntityName));
        report.getSheetsImportable().put(sampleEntityName, !sampleEntityExists);

        List<String> availableSampleAttributeNames = Lists.newArrayList();
        for (Attribute attr : sampleAttribute.getRefEntity().getAtomicAttributes()) {
          availableSampleAttributeNames.add(attr.getName());
        }
        report.getFieldsImportable().put(sampleEntityName, availableSampleAttributeNames);
      }
    }

    return report;
  }

  @Override
  public boolean canImport(File file, RepositoryCollection source) {
    for (String extension : getSupportedFileExtensions()) {
      if (file.getName().toLowerCase().endsWith(extension)) {
        return true;
      }
    }

    return false;
  }

  private EntityImportReport importVcf(
      Repository<Entity> inRepository, List<EntityType> addedEntities, Package importPackage)
      throws IOException {

    EntityImportReport report = new EntityImportReport();

    String entityTypeId = inRepository.getName();

    if (runAsSystem(() -> dataService.hasRepository(entityTypeId))) {
      throw new MolgenisDataException("Can't overwrite existing " + entityTypeId);
    }

    EntityType entityType = inRepository.getEntityType();
    entityType.setBackend(metaDataService.getDefaultBackend().getName());
    entityType.setPackage(importPackage);

    Repository<Entity> sampleRepository =
        createSampleRepository(addedEntities, entityType, importPackage);

    try (Repository<Entity> outRepository = dataService.getMeta().createRepository(entityType)) {
      permissionSystemService.giveUserWriteMetaPermissions(entityType);

      addedEntities.add(entityType);

      if (sampleRepository != null) {
        int sampleEntityCount = addSampleEntities(sampleRepository, inRepository);

        report.addNewEntity(sampleRepository.getName());
        if (sampleEntityCount > 0) {
          report.addEntityCount(sampleRepository.getName(), sampleEntityCount);
        }
      }

      AtomicInteger vcfEntityCount = new AtomicInteger();
      inRepository.forEachBatched(
          rowBatch -> {
            outRepository.add(rowBatch.stream());
            vcfEntityCount.addAndGet(rowBatch.size());
          },
          VcfRepository.BATCH_SIZE);

      if (vcfEntityCount.get() > 0) {
        report.addEntityCount(entityTypeId, vcfEntityCount.get());
      }
    }

    report.addNewEntity(entityTypeId);

    return report;
  }

  private int addSampleEntities(
      Repository<Entity> sampleRepository, Repository<Entity> inRepository) {
    final AtomicInteger sampleEntityCount = new AtomicInteger(0);
    List<Entity> sampleBatch = new ArrayList<>();
    inRepository.forEachBatched(
        rowBatch -> {
          for (Entity entity : rowBatch) {
            Iterable<Entity> samples = entity.getEntities(VcfAttributes.SAMPLES);
            if (samples != null) {
              for (Entity sample : samples) {
                sampleBatch.add(sample);

                if (sampleBatch.size() == BATCH_SIZE) {
                  sampleRepository.add(sampleBatch.stream());
                  sampleEntityCount.addAndGet(sampleBatch.size());
                  sampleBatch.clear();
                }
              }
            }
          }
        },
        1000);

    if (!sampleBatch.isEmpty()) {
      sampleRepository.add(sampleBatch.stream());
      sampleEntityCount.addAndGet(sampleBatch.size());
    }
    return sampleEntityCount.get();
  }

  private Repository<Entity> createSampleRepository(
      List<EntityType> addedEntities, EntityType entityType, Package samplePackage) {
    Repository<Entity> sampleRepository;
    Attribute sampleAttribute = entityType.getAttribute(VcfAttributes.SAMPLES);
    if (sampleAttribute != null) {
      EntityType samplesEntityType = sampleAttribute.getRefEntity();
      samplesEntityType.setBackend(metaDataService.getDefaultBackend().getName());
      samplesEntityType.setPackage(samplePackage);
      sampleRepository = dataService.getMeta().createRepository(samplesEntityType);
      permissionSystemService.giveUserWriteMetaPermissions(samplesEntityType);
      addedEntities.add(sampleAttribute.getRefEntity());
    } else {
      sampleRepository = null;
    }
    return sampleRepository;
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
    return true;
  }

  @Override
  public Set<String> getSupportedFileExtensions() {
    return VcfFileExtensions.getVCF();
  }

  @Override
  public Map<String, Boolean> determineImportableEntities(
      MetaDataService metaDataService,
      RepositoryCollection repositoryCollection,
      String defaultPackage) {
    return metaDataService.determineImportableEntities(repositoryCollection);
  }
}
