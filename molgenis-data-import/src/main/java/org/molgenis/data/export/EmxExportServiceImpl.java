package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.export.mapper.AttributeMapper.ATTRIBUTE_ATTRS;
import static org.molgenis.data.export.mapper.EntityTypeMapper.ENTITIES_ATTRS;
import static org.molgenis.data.export.mapper.PackageMapper.PACKAGE_ATTRS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ENTITIES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGES;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.excel.xlsx.XlsxWriter;
import org.molgenis.data.excel.xlsx.XlsxWriterFactory;
import org.molgenis.data.export.exception.EmptyExportRequestException;
import org.molgenis.data.export.exception.EmxExportException;
import org.molgenis.data.export.exception.InvalidEmxIdentifierException;
import org.molgenis.data.export.mapper.AttributeMapper;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmxExportServiceImpl implements EmxExportService {

  private static final int BATCH_SIZE = 1000;
  private final DataService dataService;

  public EmxExportServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public void export(
      List<EntityType> entityTypes,
      List<Package> packages,
      Path downloadFilePath,
      Progress progress) {
    requireNonNull(progress);
    if (!(entityTypes.isEmpty() && packages.isEmpty())) {
      try (XlsxWriter writer = XlsxWriterFactory.create(downloadFilePath)) {
        exportEmx(entityTypes, packages, writer, progress);
      } catch (IOException e) {
        throw new EmxExportException(e);
      }
    } else {
      throw new EmptyExportRequestException();
    }
  }

  private void exportEmx(
      List<EntityType> entityTypes, List<Package> packages, XlsxWriter writer, Progress progress)
      throws IOException {
    Set<Package> packageSet = new LinkedHashSet<>();
    Set<EntityType> entityTypeSet = new LinkedHashSet<>();
    resolveMetadata(entityTypes, packages, packageSet, entityTypeSet);

    // Progress per entity type plus package sheet + finished message
    progress.setProgressMax(entityTypes.size() + 2);
    writePackageSheet(packageSet, entityTypeSet, writer, progress);
    writeEntityTypeSheets(entityTypeSet, writer, progress);
  }

  protected void resolveMetadata(
      List<EntityType> entityTypes,
      List<Package> packages,
      Set<Package> packageSet,
      Set<EntityType> entityTypeSet) {
    resolvePackages(packages, packageSet, entityTypeSet);
    resolveEntityTypes(entityTypes, entityTypeSet);
  }

  private void writeEntityTypeSheets(
      Set<EntityType> entityTypes, XlsxWriter writer, Progress progress) throws IOException {
    writeEntityTypes(entityTypes, writer);
    for (EntityType entityType : entityTypes) {
      String progressMessage =
          MessageSourceHolder.getMessageSource()
              .getMessage(
                  "emx_export_progress_message",
                  new Object[] {entityType.getLabel(LocaleContextHolder.getLocale().getLanguage())},
                  "Downloading: ",
                  LocaleContextHolder.getLocale());
      if (progress != null) {
        progress.status(progressMessage + entityType.getLabel());
        progress.increment(1);
      }
      writeAttributes(entityType.getAllAttributes(), writer);
      if (!entityType.isAbstract()) {
        downloadData(entityType, writer);
      }
    }
  }

  private void resolvePackages(
      List<Package> packages, Set<Package> packageSet, Set<EntityType> entityTypes) {
    for (Package pack : packages) {
      resolvePackage(pack, packageSet, entityTypes);
    }
  }

  private void resolveEntityTypes(List<EntityType> entityTypes, Set<EntityType> entityTypeSet) {
    for (EntityType entityType : entityTypes) {
      checkIfEmxEntityType(entityType);
      entityTypeSet.add(entityType);
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
    String entityTypeId = entityType.getId();
    String packageName = entityType.getPackage() != null ? entityType.getPackage().getId() : null;
    // Entity type name should be fully qualified if it resides in a package
    if (!(Strings.isNullOrEmpty(packageName) || entityTypeId.startsWith(packageName))) {
      throw new InvalidEmxIdentifierException(entityTypeId);
    }
  }

  private void downloadData(EntityType entityType, XlsxWriter writer) {
    List<Object> headers =
        Streams.stream(entityType.getAtomicAttributes())
            .map(Attribute::getName)
            .collect(toList());
    if (!writer.hasSheet(entityType.getId())) {
      writer.createSheet(entityType.getId(), headers);
    }

    dataService
        .getRepository(entityType.getId())
        .forEachBatched(entities -> writeRows(entities, entityType, writer), BATCH_SIZE);
  }

  private void writeRows(List<Entity> entities, EntityType entityType, XlsxWriter writer) {
    writer.writeRows(entities.stream().map(DataRowMapper::mapDataRow), entityType.getId());
  }

  private void writeEntityTypes(Iterable<EntityType> entityTypes, XlsxWriter writer) {
    if (!writer.hasSheet(EMX_ENTITIES)) {
      writer.createSheet(EMX_ENTITIES, newArrayList(ENTITIES_ATTRS.keySet()));
    }
    writer.writeRows(Streams.stream(entityTypes).map(EntityTypeMapper::map), EMX_ENTITIES);
  }

  private void writeAttributes(Iterable<Attribute> attrs, XlsxWriter writer) throws IOException {
    if (!writer.hasSheet(EMX_ATTRIBUTES)) {
      writer.createSheet(EMX_ATTRIBUTES, newArrayList(ATTRIBUTE_ATTRS.keySet()));
    }
    writer.writeRows(Streams.stream(attrs).map(AttributeMapper::map), EMX_ATTRIBUTES);
  }

  protected void writePackageSheet(
      Set<Package> packages, Set<EntityType> entityTypes, XlsxWriter writer, Progress progress) {
    if (!writer.hasSheet(EMX_PACKAGES)) {
      writer.createSheet(EMX_PACKAGES, newArrayList(PACKAGE_ATTRS.keySet()));
    }

    // add the packages that should be write to the packages sheet, but for which the entityTypes
    // should not be exported
    addEntityPackages(entityTypes, packages);
    addParentPackages(packages);

    writer.writeRows(packages.stream().map(PackageMapper::map), EMX_PACKAGES);
    progress.status(
        MessageSourceHolder.getMessageSource()
            .getMessage(
                "emx_export_metadata_message",
                new Object[] {},
                "Finished downloading package metadata",
                LocaleContextHolder.getLocale()));
    progress.increment(1);
  }

  private void addEntityPackages(Set<EntityType> entityTypes, Set<Package> packages) {
    for (EntityType entityType : entityTypes) {
      Package pack = entityType.getPackage();
      if (pack != null) {
        packages.add(pack);
      }
    }
  }

  private void addParentPackages(Set<Package> packages) {
    List<Package> parents = new ArrayList<>();
    for (Package pack : packages) {
      parents.addAll(getParentPackages(pack));
    }
    packages.addAll(parents);
  }

  private Collection<Package> getParentPackages(Package pack) {
    List<Package> parents = new ArrayList<>();
    Package parent = pack.getParent();
    if (parent != null) {
      parents.add(parent);
      parents.addAll(getParentPackages(parent));
    }
    return parents;
  }
}
