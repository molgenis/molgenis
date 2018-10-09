package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.export.mapper.AttributeMapper.ATTRIBUTE_ATTRS;
import static org.molgenis.data.export.mapper.AttributeMapper.map;
import static org.molgenis.data.export.mapper.EntityTypeMapper.ENTITIES_ATTRS;
import static org.molgenis.data.export.mapper.PackageMapper.PACKAGE_ATTRS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGES;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.excel.simple.ExcelWriter;
import org.molgenis.data.excel.simple.ExcelWriterFactory;
import org.molgenis.data.export.exception.EmptyDownloadRequestException;
import org.molgenis.data.export.exception.EmxExportException;
import org.molgenis.data.export.exception.InvalidEntityIdentifierException;
import org.molgenis.data.export.mapper.DataRowMapper;
import org.molgenis.data.export.mapper.EntityTypeMapper;
import org.molgenis.data.export.mapper.PackageMapper;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmxExportServiceImpl implements EmxExportService {

  private final DataService dataService;

  public EmxExportServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  @Transactional(readOnly = true)
  public void download(List<String> entityTypeIds, List<String> packageIds, File downloadFile) {
    download(entityTypeIds, packageIds, downloadFile, Optional.empty());
  }

  @Override
  @Transactional(readOnly = true)
  public void download(
      List<String> entityTypeIds,
      List<String> packageIds,
      File downloadFile,
      Optional<Progress> progress) {
    if (!(entityTypeIds.isEmpty() && packageIds.isEmpty())) {

      try (ExcelWriter writer = ExcelWriterFactory.create(downloadFile)) {
        downloadEmx(entityTypeIds, packageIds, writer, progress);
      } catch (IOException e) {
        throw new EmxExportException(e);
      }
    } else {
      throw new EmptyDownloadRequestException();
    }
  }

  private void downloadEmx(
      List<String> entityTypeIds,
      List<String> packageIds,
      ExcelWriter writer,
      Optional<Progress> progress)
      throws IOException {
    Set<Package> packages = new LinkedHashSet<>();
    Set<EntityType> entityTypes = new LinkedHashSet<>();
    resolveMetadata(entityTypeIds, packageIds, packages, entityTypes);

    if (progress.isPresent()) {
      // Progress per entity plus package sheet + finished message
      progress.get().setProgressMax(entityTypes.size() + 2);
    }
    writePackageSheets(packages, writer, progress);
    writeEntityTypeSheets(entityTypes, writer, progress);
  }

  protected void resolveMetadata(
      List<String> entityTypeIds,
      List<String> packageIds,
      Set<Package> packages,
      Set<EntityType> entityTypes) {
    resolvePackageIds(packageIds, packages, entityTypes);
    resolveEntityTypeIds(entityTypeIds, entityTypes);
  }

  private void writeEntityTypeSheets(
      Set<EntityType> entityTypes, ExcelWriter writer, Optional<Progress> progressOptional)
      throws IOException {
    writeEntityTypes(entityTypes, writer);
    for (EntityType entityType : entityTypes) {
      String progressMessage =
          MessageSourceHolder.getMessageSource()
              .getMessage(
                  "emx_export_progress_message",
                  new Object[] {entityType.getLabel(LocaleContextHolder.getLocale().getLanguage())},
                  "Downloading: ",
                  LocaleContextHolder.getLocale());
      if (progressOptional.isPresent()) {
        Progress progress = progressOptional.get();
        progress.status(progressMessage);
        progress.increment(1);
      }
      writeAttributes(entityType.getAllAttributes(), writer);
      if (!entityType.isAbstract()) {
        downloadData(entityType, writer);
      }
    }
  }

  private void resolvePackageIds(
      List<String> packageIds, Set<Package> packages, Set<EntityType> entityTypes) {
    for (String id : packageIds) {
      Optional<Package> optional = dataService.getMeta().getPackage(id);
      if (optional.isPresent()) {
        Package pack = optional.get();
        resolvePackage(pack, packages, entityTypes);
      } else {
        throw new UnknownPackageException(id);
      }
    }
  }

  private void resolveEntityTypeIds(List<String> entityTypeIds, Set<EntityType> entityTypes) {
    for (String entityTypeName : entityTypeIds) {
      Optional<EntityType> entityTypeOptional = dataService.getMeta().getEntityType(entityTypeName);
      if (entityTypeOptional.isPresent()) {
        EntityType entityType = entityTypeOptional.get();
        checkIfEmxEntityType(entityType);
        entityTypes.add(entityType);
      } else {
        throw new UnknownEntityTypeException(entityTypeName);
      }
    }
  }

  private void resolvePackage(Package pack, Set<Package> packages, Set<EntityType> entityTypes) {
    packages.add(pack);
    for (EntityType entityType : pack.getEntityTypes()) {
      checkIfEmxEntityType(entityType);
      entityTypes.add(entityType);
    }
    for (Package child : pack.getChildren()) {
      resolvePackage(child, packages, entityTypes);
    }
  }

  private void checkIfEmxEntityType(EntityType entityType) {
    String entityName = entityType.getId();
    String packageName = entityType.getPackage() != null ? entityType.getPackage().getId() : "";
    // Entity name should be fully qualified if it resides in a package
    if (!(packageName.isEmpty() || entityName.startsWith(packageName))) {
      throw new InvalidEntityIdentifierException(entityName);
    }
  }

  private void downloadData(EntityType entityType, ExcelWriter writer) throws IOException {
    List<Object> headers =
        StreamSupport.stream(entityType.getAttributes().spliterator(), false)
            .filter(attr -> attr.getDataType() != AttributeType.COMPOUND)
            .map(Attribute::getName)
            .collect(Collectors.toList());
    if (!writer.hasSheet(entityType.getId())) {
      writer.createSheet(entityType.getId(), headers);
    }
    writer.writeRows(
        dataService.findAll(entityType.getId()).map(DataRowMapper::mapDataRow), entityType.getId());
  }

  private void writeEntityTypes(Iterable<EntityType> entityTypes, ExcelWriter writer)
      throws IOException {
    if (!writer.hasSheet(EMX_ENTITIES)) {
      writer.createSheet(EMX_ENTITIES, newArrayList(ENTITIES_ATTRS.keySet()));
    }
    writer.writeRows(
        StreamSupport.stream(entityTypes.spliterator(), false)
            .map(entityType -> EntityTypeMapper.map(entityType)),
        EMX_ENTITIES);
  }

  private void writeAttributes(Iterable<Attribute> attrs, ExcelWriter writer) throws IOException {
    if (!writer.hasSheet(EMX_ATTRIBUTES)) {
      writer.createSheet(EMX_ATTRIBUTES, newArrayList(ATTRIBUTE_ATTRS.keySet()));
    }
    writer.writeRows(
        StreamSupport.stream(attrs.spliterator(), false).map(attr -> map(attr)), EMX_ATTRIBUTES);
  }

  private void writePackageSheets(
      Iterable<Package> packages, ExcelWriter writer, Optional<Progress> progressOptional)
      throws IOException {
    if (!writer.hasSheet(EMX_PACKAGES)) {
      writer.createSheet(EMX_PACKAGES, newArrayList(PACKAGE_ATTRS.keySet()));
    }
    writer.writeRows(
        StreamSupport.stream(packages.spliterator(), false).map(pack -> PackageMapper.map(pack)),
        EMX_PACKAGES);
    if (progressOptional.isPresent()) {
      Progress progress = progressOptional.get();
      progress.status(
          MessageSourceHolder.getMessageSource()
              .getMessage(
                  "emx_export_metadata_message",
                  new Object[] {},
                  "Finished downloading package metadata",
                  LocaleContextHolder.getLocale()));
      progress.increment(1);
    }
  }
}
