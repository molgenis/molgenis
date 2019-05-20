package org.molgenis.data.security.permission;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import java.io.Serializable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.security.exception.InvalidTypeIdException;
import org.molgenis.data.security.exception.SystemRlsModificationException;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.util.EntityUtils;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Component;

@Component
public class EntityHelper {
  public static final String ENTITY_PREFIX = "entity-";
  public static final String PLUGIN = "plugin";
  public static final String SYS_PLUGIN = "sys_Plugin";
  private final DataService dataService;

  public EntityHelper(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  String getEntityTypeIdFromType(String typeId) {
    String result;
    switch (typeId) {
      case PackageIdentity.PACKAGE:
        result = PackageMetadata.PACKAGE;
        break;
      case EntityTypeIdentity.ENTITY_TYPE:
        result = EntityTypeMetadata.ENTITY_TYPE_META_DATA;
        break;
      case PLUGIN:
        result = SYS_PLUGIN;
        break;
      case GroupIdentity.GROUP:
        result = GroupMetadata.GROUP;
        break;
      default:
        if (typeId.startsWith(ENTITY_PREFIX)) {
          result = typeId.substring(7);
        } else {
          throw new InvalidTypeIdException(typeId);
        }
        break;
    }
    return result;
  }

  public ObjectIdentity getObjectIdentity(String classId, String objectIdentifier) {
    return new ObjectIdentityImpl(classId, getTypedValue(objectIdentifier, classId));
  }

  private Serializable getTypedValue(String untypedId, String classId) {
    if (classId.startsWith(ENTITY_PREFIX)) {
      EntityType entityType = dataService.getEntityType(getEntityTypeIdFromType(classId));
      return (Serializable) EntityUtils.getTypedValue(untypedId, entityType.getIdAttribute(), null);
    } else {
      return untypedId;
    }
  }

  public String getLabel(String typeId, String identifier) {
    String entityTypeId = getEntityTypeIdFromType(typeId);
    Entity[] result = new Entity[1];
    runAsSystem(
        () -> {
          result[0] = getEntityByIdentifier(identifier, entityTypeId);
        });
    if (result[0] == null) {
      throw new UnknownEntityException(entityTypeId, identifier);
    }
    return result[0].getLabelValue().toString();
  }

  private Entity getEntityByIdentifier(Object identifier, String entityTypeId) {
    if (entityTypeId.equals(GroupMetadata.GROUP)) {
      return dataService
          .getRepository(entityTypeId)
          .query()
          .eq(GroupMetadata.NAME, identifier)
          .findOne();
    }
    return dataService.getRepository(entityTypeId).findOneById(identifier);
  }

  public String getLabel(String typeId) {
    String entityTypeId = getEntityTypeIdFromType(typeId);
    String[] result = new String[1];
    runAsSystem(
        () -> {
          result[0] = dataService.getRepository(entityTypeId).getEntityType().getLabel();
        });
    return result[0];
  }

  public LabelledObjectIdentity getLabelledObjectIdentity(ObjectIdentity objectIdentity) {
    String typeLabel = getLabel(objectIdentity.getType());
    String entityTypeId = getEntityTypeIdFromType(objectIdentity.getType());
    String identifierLabel =
        getLabel(objectIdentity.getType(), objectIdentity.getIdentifier().toString());

    return LabelledObjectIdentity.create(
        objectIdentity.getType(),
        entityTypeId,
        typeLabel,
        objectIdentity.getIdentifier().toString(),
        identifierLabel);
  }

  public void checkEntityExists(ObjectIdentity objectIdentity) {
    checkEntityTypeExists(objectIdentity.getType());
    String entityTypeId = getEntityTypeIdFromType(objectIdentity.getType());
    if (getEntityByIdentifier(objectIdentity.getIdentifier(), entityTypeId) == null) {
      throw new UnknownEntityException(entityTypeId, objectIdentity.getIdentifier());
    }
  }

  public void checkEntityTypeExists(String typeId) {
    String entityTypeId = getEntityTypeIdFromType(typeId);
    if (!dataService.hasEntityType(entityTypeId)) {
      throw new UnknownEntityTypeException(typeId);
    }
  }

  public void checkIsNotSystem(String typeId) {
    String entityTypeId = getEntityTypeIdFromType(typeId);
    EntityType entityType = dataService.getEntityType(entityTypeId);
    if (EntityTypeUtils.isSystemEntity(entityType)) {
      throw new SystemRlsModificationException(entityTypeId);
    }
  }
}
