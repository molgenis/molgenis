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

import com.google.common.collect.Lists;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmxExportServiceImpl implements EmxExportService {

  private final DataService dataService;
  private static final int BATCH_SIZE = 10000;

  public EmxExportServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  @Transactional
  public void download(List<String> entityTypeIds, List<String> packageIds, File downloadFile) {
    if (!(entityTypeIds.isEmpty() && packageIds.isEmpty())) {

      try (ExcelWriter writer = ExcelWriterFactory.create(downloadFile)) {
        downloadEmx(entityTypeIds, packageIds, writer);
      } catch (IOException e) {
        throw new EmxExportException();
      }
    } else {
      throw new EmptyDownloadRequestException();
    }
  }

  private void downloadEmx(List<String> entityTypeIds, List<String> packageIds, ExcelWriter writer)
      throws IOException {
    Set<Package> packages = new LinkedHashSet<>();
    Set<EntityType> entityTypes = new LinkedHashSet<>();
    resolveMetadata(entityTypeIds, packageIds, packages, entityTypes);
    writeEntityTypeSheets(writer, entityTypes);
    writePackageSheets(writer, packages);
  }

  protected void resolveMetadata(
      List<String> entityTypeIds,
      List<String> packageIds,
      Set<Package> packages,
      Set<EntityType> entityTypes) {
    resolvePackageIds(packageIds, packages, entityTypes);
    resolveEntityTypeIds(entityTypeIds, entityTypes);
  }

  private void writePackageSheets(ExcelWriter writer, Set<Package> packages) throws IOException {
    for (Package pack : packages) {
      writePackage(pack, writer);
    }
  }

  private void writeEntityTypeSheets(ExcelWriter writer, Set<EntityType> entityTypes)
      throws IOException {
    for (EntityType entityType : entityTypes) {
      writeAttributes(entityType.getAllAttributes(), writer);
      writeEntityType(entityType, writer);
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
    entityTypes.addAll(Lists.newLinkedList(pack.getEntityTypes()));
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
        dataService.findAll(entityType.getId()).map(DataRowMapper::mapDataRow),
        entityType.getId(),
        BATCH_SIZE);
  }

  private void writeEntityType(EntityType entityType, ExcelWriter writer) throws IOException {
    if (!writer.hasSheet(EMX_ENTITIES)) {
      writer.createSheet(EMX_ENTITIES, newArrayList(ENTITIES_ATTRS.keySet()));
    }
    writer.writeRow(EntityTypeMapper.map(entityType), EMX_ENTITIES);
  }

  private void writeAttributes(Iterable<Attribute> attrs, ExcelWriter writer) throws IOException {
    if (!writer.hasSheet(EMX_ATTRIBUTES)) {
      writer.createSheet(EMX_ATTRIBUTES, newArrayList(ATTRIBUTE_ATTRS.keySet()));
    }
    for (Attribute attr : attrs) {
      writer.writeRow(map(attr), EMX_ATTRIBUTES);
    }
  }

  private void writePackage(Package pack, ExcelWriter writer) throws IOException {
    if (!writer.hasSheet(EMX_PACKAGES)) {
      writer.createSheet(EMX_PACKAGES, newArrayList(PACKAGE_ATTRS.keySet()));
    }
    writer.writeRow(PackageMapper.map(pack), EMX_PACKAGES);
  }
}
