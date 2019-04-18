package org.molgenis.api.permissions;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.molgenis.api.permissions.exceptions.InvalidTypeIdException;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.GroupMetadata;
import org.molgenis.data.util.EntityUtils;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;

public class IdentityTools {
  public static final String ENTITY_PREFIX = "entity-";
  private final DataService dataService;

  public IdentityTools(DataService dataService) {
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
      case PluginIdentity.PLUGIN:
        result = PluginMetadata.PLUGIN;
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
}
