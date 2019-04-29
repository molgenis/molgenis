package org.molgenis.data.security.permission;

import java.util.Map;
import java.util.Set;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface PermissionService {

  /**
   * Creates an access control list for the given object identity
   *
   * @throws org.springframework.security.acls.model.AlreadyExistsException if this ACL already
   *     exists
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.security.exception.PermissionNotSuitableException if the permission
   *     is not suitable for the type of object
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void createAcl(ObjectIdentity objectIdentity);

  /**
   * Creates a permission for a user on an object identity
   *
   * @throws org.molgenis.data.security.exception.DuplicatePermissionException if there is already a
   *     permission set for this combination of sid and objectIdentity
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.security.exception.PermissionNotSuitableException if the permission
   *     is not suitable for the type of object
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void createPermission(Permission permissions);

  /**
   * Creates permissions for a user on an object identity
   *
   * @throws org.molgenis.data.security.exception.DuplicatePermissionException if there is already a
   *     permission set for a combination of sid and objectIdentity
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.security.exception.PermissionNotSuitableException if the permission
   *     is not suitable for the type of object
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void createPermissions(Set<Permission> permissions);

  /**
   * Updates a existing permission for a user on an object identity
   *
   * @throws org.molgenis.data.security.exception.UnknownAceException if there can be no permission
   *     found to update
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.security.exception.PermissionNotSuitableException if the permission
   *     is not suitable for the type of object
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void updatePermission(Permission permissions);

  /**
   * Updates existing permissions for a user on an object identity
   *
   * @throws org.molgenis.data.security.exception.UnknownAceException if there can be no permission
   *     found to update
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.security.exception.PermissionNotSuitableException if the permission
   *     is not suitable for the type of object
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void updatePermissions(Set<Permission> permissions);

  /**
   * Deletes a existing permission for a user on an object identity
   *
   * @throws org.molgenis.data.security.exception.UnknownAceException if there can be no permission
   *     found to delete
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void deletePermission(Sid sid, ObjectIdentity objectIdentity);

  /**
   * Add a Type to the database
   *
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void addType(String typeId);

  /**
   * Removes a Type from the database
   *
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  void deleteType(String typeId);

  /**
   * Retrieves the permissions for all objects of a type for one or more users
   *
   * @throws org.molgenis.data.security.exception.ReadPermissionDeniedException if the user misses
   *     the permissions required to retrieve permissions for this type
   * @throws org.molgenis.data.security.exception.SidPermissionException if the user misses the
   *     permissions required to retrieve permissions for
   * @throws org.molgenis.data.security.exception.InsufficientInheritancePermissionsException if the
   *     user misses the permissions required to retrieve inheritance information
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean includeInheritance);

  /**
   * Retrieves the permissions for all objects of a type for one or more users
   *
   * @throws org.molgenis.data.security.exception.ReadPermissionDeniedException if the user misses
   *     the permissions required to retrieve permissions for this type
   * @throws org.molgenis.data.security.exception.SidPermissionException if the user misses the
   *     permissions required to retrieve permissions for
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize);

  /**
   * Retrieves the permissions for a object for one or more users
   *
   * @throws org.molgenis.data.security.exception.ReadPermissionDeniedException if the user misses
   *     the permissions required to retrieve permissions for this object
   * @throws org.molgenis.data.security.exception.SidPermissionException if the user misses the
   *     permissions required to retrieve permissions for
   * @throws org.molgenis.data.security.exception.InsufficientInheritancePermissionsException if the
   *     user misses the permissions required to retrieve inheritance information
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  Set<LabelledPermission> getPermissionsForObject(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean includeInheritance);

  /**
   * Retrieves all permissions for one or more users
   *
   * @throws org.molgenis.data.security.exception.InsufficientInheritancePermissionsException if the
   *     user misses the permissions required to retrieve inheritance information
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   */
  Set<LabelledPermission> getPermissions(Set<Sid> sids, boolean includeInheritance);

  /** Retrieves the types that are available in the system */
  Set<LabelledType> getTypes();

  /**
   * Retrieves the objects of a type in the system
   *
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  Set<LabelledObject> getObjects(String typeId, int page, int pageSize);

  /** Retrieves the permissions that can be granted of objects of a certain type */
  Set<PermissionSet> getSuitablePermissionsForType(String typeId);

  /**
   * Checks if a there is a permission in the system for the combination of a sid and an
   * objectIdentity
   *
   * @throws org.molgenis.data.security.exception.ReadPermissionDeniedException if the user misses
   *     the permissions required to retrieve permissions for this object
   * @throws org.molgenis.data.security.exception.SidPermissionException if the user misses the
   *     permissions required to retrieve permissions for
   * @throws org.molgenis.data.security.user.UnknownUserException if no user could be found for a
   *     PrincipalSid
   * @throws org.molgenis.data.security.exception.UnknownRoleException if no role could be found for
   *     a GrantedAuthoritySid
   * @throws org.molgenis.data.UnknownEntityException if the combination of Type and Identifier of
   *     the object identity does not corresponds to an entity
   * @throws org.molgenis.data.UnknownEntityTypeException if the Type of the object identity does
   *     not corresponds to an existing entity type
   */
  boolean exists(ObjectIdentity objectIdentity, Sid sid);
}
