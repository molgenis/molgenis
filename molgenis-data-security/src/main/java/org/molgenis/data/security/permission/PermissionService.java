package org.molgenis.data.security.permission;

import java.util.Map;
import java.util.Set;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.security.exception.DuplicatePermissionException;
import org.molgenis.data.security.exception.InsufficientInheritancePermissionsException;
import org.molgenis.data.security.exception.PermissionNotSuitableException;
import org.molgenis.data.security.exception.ReadPermissionDeniedException;
import org.molgenis.data.security.exception.SidPermissionException;
import org.molgenis.data.security.exception.UnknownAceException;
import org.molgenis.data.security.exception.UnknownRoleException;
import org.molgenis.data.security.permission.model.LabelledObject;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface PermissionService {

  /**
   * Creates an access control list for the given object identity
   *
   * @throws AlreadyExistsException if this ACL already exists
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws PermissionNotSuitableException if the permission is not suitable for the type of object
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void createAcl(ObjectIdentity objectIdentity);

  /**
   * Creates a permission for a user on an object identity
   *
   * @throws DuplicatePermissionException if there is already a permission set for this combination
   *     of sid and objectIdentity
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws PermissionNotSuitableException if the permission is not suitable for the type of object
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void createPermission(Permission permissions);

  /**
   * Creates permissions for a user on an object identity
   *
   * @throws DuplicatePermissionException if there is already a permission set for a combination of
   *     sid and objectIdentity
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws PermissionNotSuitableException if the permission is not suitable for the type of object
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void createPermissions(Set<Permission> permissions);

  /**
   * Updates a existing permission for a user on an object identity
   *
   * @throws UnknownAceException if there can be no permission found to update
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws PermissionNotSuitableException if the permission is not suitable for the type of object
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void updatePermission(Permission permissions);

  /**
   * Updates existing permissions for a user on an object identity
   *
   * @throws UnknownAceException if there can be no permission found to update
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws PermissionNotSuitableException if the permission is not suitable for the type of object
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void updatePermissions(Set<Permission> permissions);

  /**
   * Deletes a existing permission for a user on an object identity
   *
   * @throws UnknownAceException if there can be no permission found to delete
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void deletePermission(Sid sid, ObjectIdentity objectIdentity);

  /**
   * Add a Type to the database
   *
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void addType(String typeId);

  /**
   * Removes a Type from the database
   *
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  void deleteType(String typeId);

  /**
   * Retrieves the permissions for all objects of a type for one or more users
   *
   * @throws ReadPermissionDeniedException if the user misses the permissions required to retrieve
   *     permissions for this type
   * @throws SidPermissionException if the user misses the permissions required to retrieve
   *     permissions for
   * @throws InsufficientInheritancePermissionsException if the user misses the permissions required
   *     to retrieve inheritance information
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, boolean includeInheritance);

  /**
   * Retrieves the permissions for all objects of a type for one or more users
   *
   * @throws ReadPermissionDeniedException if the user misses the permissions required to retrieve
   *     permissions for this type
   * @throws SidPermissionException if the user misses the permissions required to retrieve
   *     permissions for
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  Map<String, Set<LabelledPermission>> getPermissionsForType(
      String typeId, Set<Sid> sids, int page, int pageSize);

  /**
   * Retrieves the permissions for a object for one or more users
   *
   * @throws ReadPermissionDeniedException if the user misses the permissions required to retrieve
   *     permissions for this object
   * @throws SidPermissionException if the user misses the permissions required to retrieve
   *     permissions for
   * @throws InsufficientInheritancePermissionsException if the user misses the permissions required
   *     to retrieve inheritance information
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  Set<LabelledPermission> getPermissionsForObject(
      ObjectIdentity objectIdentity, Set<Sid> sids, boolean includeInheritance);

  /**
   * Retrieves all permissions for one or more users
   *
   * @throws InsufficientInheritancePermissionsException if the user misses the permissions required
   *     to retrieve inheritance information
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   */
  Set<LabelledPermission> getPermissions(Set<Sid> sids, boolean includeInheritance);

  /** Retrieves the types that are available in the system */
  Set<LabelledType> getTypes();

  /**
   * Retrieves the objects of a type in the system
   *
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  Set<LabelledObject> getObjects(String typeId, int page, int pageSize);

  /** Retrieves the permissions that can be granted of objects of a certain type */
  Set<PermissionSet> getSuitablePermissionsForType(String typeId);

  /**
   * Checks if a there is a permission in the system for the combination of a sid and an
   * objectIdentity
   *
   * @throws ReadPermissionDeniedException if the user misses the permissions required to retrieve
   *     permissions for this object
   * @throws SidPermissionException if the user misses the permissions required to retrieve
   *     permissions for
   * @throws UnknownUserException if no user could be found for a PrincipalSid
   * @throws UnknownRoleException if no role could be found for a GrantedAuthoritySid
   * @throws UnknownEntityException if the combination of Type and Identifier of the object identity
   *     does not corresponds to an entity
   * @throws UnknownEntityTypeException if the Type of the object identity does not corresponds to
   *     an existing entity type
   */
  boolean exists(ObjectIdentity objectIdentity, Sid sid);
}
