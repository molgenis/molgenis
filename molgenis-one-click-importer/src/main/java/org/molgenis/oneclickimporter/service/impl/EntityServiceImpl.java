package org.molgenis.oneclickimporter.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.util.AttributeUtils.getValidIdAttributeTypes;

import java.util.List;
import java.util.Optional;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.NoWritablePackageException;
import org.molgenis.data.security.PackagePermissionUtils;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.AttributeTypeService;
import org.molgenis.oneclickimporter.service.EntityService;
import org.molgenis.oneclickimporter.service.OneClickImporterNamingService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.stereotype.Component;

@Component
public class EntityServiceImpl implements EntityService {
  private static final String ID_ATTR_NAME = "auto_identifier";

  private final EntityTypeFactory entityTypeFactory;
  private final AttributeFactory attributeFactory;
  private final IdGenerator idGenerator;
  private final DataService dataService;
  private final MetaDataService metaDataService;
  private final EntityManager entityManager;
  private final AttributeTypeService attributeTypeService;
  private final OneClickImporterService oneClickImporterService;
  private final OneClickImporterNamingService oneClickImporterNamingService;
  private final PackageFactory packageFactory;
  private final PermissionSystemService permissionSystemService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  public EntityServiceImpl(
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attributeFactory,
      IdGenerator idGenerator,
      DataService dataService,
      MetaDataService metaDataService,
      EntityManager entityManager,
      AttributeTypeService attributeTypeService,
      OneClickImporterService oneClickImporterService,
      OneClickImporterNamingService oneClickImporterNamingService,
      PackageFactory packageFactory,
      PermissionSystemService permissionSystemService,
      UserPermissionEvaluator userPermissionEvaluator) {
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeFactory = requireNonNull(attributeFactory);
    this.idGenerator = requireNonNull(idGenerator);
    this.dataService = requireNonNull(dataService);
    this.metaDataService = requireNonNull(metaDataService);
    this.entityManager = requireNonNull(entityManager);
    this.attributeTypeService = requireNonNull(attributeTypeService);
    this.oneClickImporterService = requireNonNull(oneClickImporterService);
    this.oneClickImporterNamingService = requireNonNull(oneClickImporterNamingService);
    this.packageFactory = requireNonNull(packageFactory);
    this.permissionSystemService = requireNonNull(permissionSystemService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @Override
  public EntityType createEntityType(DataCollection dataCollection, String packageName) {
    String entityTypeId = idGenerator.generateId();

    // Create a dataTable
    EntityType entityType = entityTypeFactory.create();

    final Package parentPackage = getParentPackage().orElseThrow(NoWritablePackageException::new);
    final String fullyQualifiedPackageName = parentPackage.getId() + "_" + packageName;
    Package aPackage = metaDataService.getPackage(fullyQualifiedPackageName);

    if (aPackage == null) {
      aPackage = packageFactory.create(fullyQualifiedPackageName);
      aPackage.setLabel(packageName);
      aPackage.setParent(parentPackage);
      metaDataService.addPackage(aPackage);
    }

    entityType.setPackage(aPackage);
    entityType.setId(entityTypeId);
    entityType.setLabel(
        oneClickImporterNamingService.getLabelWithPostFix(dataCollection.getName()));

    // Check if first column can be used as id ( has unique values )
    List<Column> columns = dataCollection.getColumns();
    Column firstColumn = columns.get(0);
    final boolean isFirstColumnUnique = oneClickImporterService.hasUniqueValues(firstColumn);

    AttributeType type = attributeTypeService.guessAttributeType(firstColumn.getDataValues());
    final boolean isValidAttributeType = getValidIdAttributeTypes().contains(type);
    final boolean useAutoId = !isFirstColumnUnique || !isValidAttributeType;

    Attribute idAttribute = useAutoId ? createIdAttribute() : createAttribute(firstColumn);
    entityType.addAttribute(idAttribute, ROLE_ID);

    // Add all columns to the dataTable
    columns.forEach(
        column -> {
          if (useAutoId || column != firstColumn) {
            Attribute attribute = createAttribute(column);
            entityType.addAttribute(attribute);
          }
        });

    // Store the dataTable (metadata only)
    metaDataService.addEntityType(entityType);
    // TODO: the user who adds/owns should get WRITE META always.
    permissionSystemService.giveUserWriteMetaPermissions(entityType);

    List<Entity> rows = newArrayList();
    int numberOfRows = dataCollection.getColumns().get(0).getDataValues().size();

    for (int index = 0; index < numberOfRows; index++) {
      Entity row = entityManager.create(entityType, NO_POPULATE);

      if (useAutoId) {
        row.setIdValue(idGenerator.generateId());
      }

      for (Column column : columns) {
        setRowValueForAttribute(row, index, column);
      }
      rows.add(row);
    }
    dataService.add(entityType.getId(), rows.stream());

    return entityType;
  }

  /** @return the first writable package */
  private Optional<Package> getParentPackage() {
    return metaDataService.getPackages().stream().filter(this::isWritablePackage).findFirst();
  }

  private boolean isWritablePackage(Package aPackage) {
    return PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator);
  }

  private void setRowValueForAttribute(Entity row, int index, Column column) {
    String attributeName = oneClickImporterNamingService.asValidColumnName(column.getName());
    Object dataValue = column.getDataValues().get(index);

    EntityType rowType = row.getEntityType();
    Attribute attribute = rowType.getAttribute(attributeName);

    Object castedValue =
        oneClickImporterService.castValueAsAttributeType(dataValue, attribute.getDataType());
    row.set(attributeName, castedValue);
  }

  private Attribute createIdAttribute() {
    Attribute idAttribute = attributeFactory.create();
    idAttribute.setName(ID_ATTR_NAME);
    idAttribute.setAuto(true);
    idAttribute.setIdAttribute(true);
    return idAttribute;
  }

  private Attribute createAttribute(Column column) {
    Attribute attribute = attributeFactory.create();
    attribute.setName(oneClickImporterNamingService.asValidColumnName(column.getName()));
    attribute.setLabel(column.getName());
    attribute.setDataType(attributeTypeService.guessAttributeType(column.getDataValues()));
    return attribute;
  }
}
