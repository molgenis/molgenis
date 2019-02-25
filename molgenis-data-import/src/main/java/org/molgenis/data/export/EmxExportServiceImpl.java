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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.jobs.Progress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmxExportServiceImpl implements EmxExportService {

  private static final int BATCH_SIZE = 1000;
  private final DataService dataService;
  private final ContextMessageSource contextMessageSource;
  private final TimeZoneProvider timeZoneProvider;

  EmxExportServiceImpl(
      DataService dataService,
      ContextMessageSource contextMessageSource,
      TimeZoneProvider timeZoneProvider) {
    this.dataService = requireNonNull(dataService);
    this.contextMessageSource = requireNonNull(contextMessageSource);
    this.timeZoneProvider = requireNonNull(timeZoneProvider);
  }

  /**
   * Isolation level needs to be 'SERIALIZABLE' in case IndexActions are being downloaded. The
   * entities have their own transaction and can change during the download when executed with a
   * default isolation level.
   *
   * @param entityTypes entityTypes to be exported to EMX.
   * @param packages packages to be exported to EMX.
   * @param downloadFilePath path for the resulting EMX file.
   * @param progress Progress object to be updated during the export.
   */
  @Override
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public void export(
      List<EntityType> entityTypes,
      List<Package> packages,
      Path downloadFilePath,
      Progress progress) {
    requireNonNull(progress);
    if (!(entityTypes.isEmpty() && packages.isEmpty())) {
      try (XlsxWriter writer =
          XlsxWriterFactory.create(downloadFilePath, timeZoneProvider.getSystemTimeZone())) {
        exportEmx(entityTypes, packages, writer, progress);
      } catch (CodedRuntimeException e) {
        throw e;
      } catch (IOException | RuntimeException e) {
        throw new EmxExportException(e);
      }
    } else {
      throw new EmptyExportRequestException();
    }
  }

  private void exportEmx(
      List<EntityType> entityTypes, List<Package> packages, XlsxWriter writer, Progress progress) {
    Map<String, Package> deduppedPackageMap = new LinkedHashMap<>();
    Map<String, EntityType> deduppedEntityTypeMap = new LinkedHashMap<>();
    resolveMetadata(entityTypes, packages, deduppedPackageMap, deduppedEntityTypeMap);

    // Progress per entity type plus package sheet + finished message
    progress.setProgressMax(entityTypes.size() + 2);
    writePackageSheet(deduppedPackageMap, deduppedEntityTypeMap, writer, progress);
    writeEntityTypeSheets(deduppedEntityTypeMap.values(), writer, progress);
  }

  void resolveMetadata(
      List<EntityType> entityTypes,
      List<Package> packages,
      Map<String, Package> packageSet,
      Map<String, EntityType> entityTypeSet) {
    resolvePackages(packages, packageSet, entityTypeSet);
    resolveEntityTypes(entityTypes, entityTypeSet);
  }

  private void writeEntityTypeSheets(
      Collection<EntityType> entityTypes, XlsxWriter writer, Progress progress) {
    writeEntityTypes(entityTypes, writer);
    for (EntityType entityType : entityTypes) {
      String progressMessage =
          contextMessageSource.getMessage(
              "emx_export_progress_message", new Object[] {entityType.getLabel()});
      if (progress != null) {
        progress.status(progressMessage);
        progress.increment(1);
      }
      writeAttributes(entityType.getOwnAllAttributes(), writer);
      if (!entityType.isAbstract()) {
        downloadData(entityType, writer);
      }
    }
  }

  private void resolvePackages(
      List<Package> packages,
      Map<String, Package> packageSet,
      Map<String, EntityType> entityTypes) {
    for (Package pack : packages) {
      resolvePackage(pack, packageSet, entityTypes);
    }
  }

  private void resolveEntityTypes(
      List<EntityType> entityTypes, Map<String, EntityType> entityTypeSet) {
    for (EntityType entityType : entityTypes) {
      checkIfEmxIdentifier(entityType, entityType.getPackage());
      entityTypeSet.put(entityType.getId(), entityType);
    }
  }

  private void resolvePackage(
      Package pack, Map<String, Package> packages, Map<String, EntityType> entityTypes) {
    packages.put(pack.getId(), pack);
    for (EntityType entityType : pack.getEntityTypes()) {
      checkIfEmxIdentifier(entityType, entityType.getPackage());
      entityTypes.put(entityType.getId(), entityType);
    }
    for (Package child : pack.getChildren()) {
      resolvePackage(child, packages, entityTypes);
    }
  }

  private void checkIfEmxIdentifier(Entity entity, Package pack) {
    String entityId = entity.getIdValue().toString();
    String packageName = pack != null ? pack.getId() : null;
    // Entity type name should be fully qualified if it resides in a package
    if (!(Strings.isNullOrEmpty(packageName) || entityId.startsWith(packageName))) {
      throw new InvalidEmxIdentifierException(entity);
    }
  }

  private void downloadData(EntityType entityType, XlsxWriter writer) {
    List<Object> headers =
        Streams.stream(entityType.getAtomicAttributes()).map(Attribute::getName).collect(toList());
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

  private void writeAttributes(Iterable<Attribute> attrs, XlsxWriter writer) {
    if (!writer.hasSheet(EMX_ATTRIBUTES)) {
      writer.createSheet(EMX_ATTRIBUTES, newArrayList(ATTRIBUTE_ATTRS.keySet()));
    }
    writer.writeRows(Streams.stream(attrs).map(AttributeMapper::map), EMX_ATTRIBUTES);
  }

  void writePackageSheet(
      Map<String, Package> packages,
      Map<String, EntityType> entityTypes,
      XlsxWriter writer,
      Progress progress) {
    if (!writer.hasSheet(EMX_PACKAGES)) {
      writer.createSheet(EMX_PACKAGES, newArrayList(PACKAGE_ATTRS.keySet()));
    }

    // add the packages that should be write to the packages sheet, but for which the entityTypes
    // should not be exported
    addEntityPackages(entityTypes.values(), packages);
    addParentPackages(packages);

    writer.writeRows(packages.values().stream().map(PackageMapper::map), EMX_PACKAGES);
    progress.status(contextMessageSource.getMessage("emx_export_metadata_message"));
    progress.increment(1);
  }

  private void addEntityPackages(
      Collection<EntityType> entityTypes, Map<String, Package> packages) {
    for (EntityType entityType : entityTypes) {
      Package pack = entityType.getPackage();
      if (pack != null) {
        checkIfEmxIdentifier(pack, pack.getParent());
        packages.put(pack.getId(), pack);
      }
    }
  }

  private void addParentPackages(Map<String, Package> packages) {
    List<Package> parentPackages = new ArrayList<>();
    for (Package pack : packages.values()) {
      parentPackages.addAll(getParentPackages(pack));
    }
    parentPackages.forEach(parentPackage -> packages.put(parentPackage.getId(), parentPackage));
  }

  private Collection<Package> getParentPackages(Package pack) {
    List<Package> parents = new ArrayList<>();
    Package parent = pack.getParent();
    if (parent != null) {
      checkIfEmxIdentifier(parent, parent.getParent());
      parents.add(parent);
      parents.addAll(getParentPackages(parent));
    }
    return parents;
  }
}
