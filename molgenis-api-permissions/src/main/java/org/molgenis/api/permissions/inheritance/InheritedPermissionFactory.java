package org.molgenis.api.permissions.inheritance;

import static org.molgenis.api.permissions.PermissionApiService.ENTITY_PREFIX;
import static org.molgenis.api.permissions.SidConversionUtils.getRole;

import java.util.ArrayList;
import java.util.List;
import org.molgenis.api.permissions.exceptions.InvalidTypeIdException;
import org.molgenis.api.permissions.inheritance.model.InheritedAclPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.inheritance.model.InheritedUserPermissionsResult;
import org.molgenis.api.permissions.model.response.InheritedPermission;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.GroupMetadata;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class InheritedPermissionFactory {

  private final DataService dataService;

  public InheritedPermissionFactory(DataService dataService) {
    this.dataService = dataService;
  }

  public List<InheritedPermission> convertToInheritedPermissions(
      InheritedPermissionsResult inheritedPermissionsResult) {
    List<InheritedUserPermissionsResult> parentRolePermissions =
        inheritedPermissionsResult.getRequestedAclParentRolesPermissions();
    InheritedAclPermissionsResult parentAclPermissions =
        inheritedPermissionsResult.getParentAclPermission();
    return convertToInheritedPermissions(parentRolePermissions, parentAclPermissions);
  }

  private List<InheritedPermission> convertToInheritedPermissions(
      List<InheritedUserPermissionsResult> parentRolePermissions,
      InheritedAclPermissionsResult parentAclPermission) {
    List<InheritedPermission> inheritedPermissions = new ArrayList<>();
    inheritedPermissions.addAll(convertInheritedRolePermissions(parentRolePermissions));
    if (parentAclPermission != null) {
      inheritedPermissions.add(convertInheritedAclPermissions(parentAclPermission));
    }
    return inheritedPermissions;
  }

  private InheritedPermission convertInheritedAclPermissions(
      InheritedAclPermissionsResult parentAclPermission) {
    Acl acl = parentAclPermission.getAcl();
    ObjectIdentity objectIdentity = acl.getObjectIdentity();
    String ownPermission = parentAclPermission.getOwnPermission();
    List<InheritedPermission> inheritedPermissions =
        convertToInheritedPermissions(
            parentAclPermission.getParentRolePermissions(),
            parentAclPermission.getParentAclPermissions());
    return InheritedPermission.create(
        null,
        objectIdentity.getType(),
        getLabel(getEntityTypeIdFromClass(objectIdentity.getType())),
        objectIdentity.getIdentifier().toString(),
        getLabel(
            getEntityTypeIdFromClass(objectIdentity.getType()),
            objectIdentity.getIdentifier().toString()),
        ownPermission,
        inheritedPermissions);
  }

  private List<InheritedPermission> convertInheritedRolePermissions(
      List<InheritedUserPermissionsResult> requestedAclParentRolesPermissions) {
    List<InheritedPermission> results = new ArrayList<>();
    for (InheritedUserPermissionsResult parentRolePermission : requestedAclParentRolesPermissions) {
      String ownPermission = parentRolePermission.getOwnPermission();
      Sid sid = parentRolePermission.getSid();
      List<InheritedPermission> inheritedPermissions =
          convertInheritedRolePermissions(parentRolePermission.getInheritedUserPermissionsResult());
      results.add(
          InheritedPermission.create(
              getRole(sid), null, null, null, null, ownPermission, inheritedPermissions));
    }
    return results;
  }

  private String getLabel(String entityTypeId, String identifier) {
    Entity entity = dataService.getRepository(entityTypeId).findOneById(identifier);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, identifier);
    }
    return entity.getLabelValue().toString();
  }

  private String getLabel(String entityTypeId) {
    return dataService.getRepository(entityTypeId).getEntityType().getLabel();
  }

  private String getEntityTypeIdFromClass(String typeId) {
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
}
