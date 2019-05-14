package org.molgenis.security.acl;

import java.util.List;
import java.util.Set;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

/** Service to retrieve lists of {@link ObjectIdentity} in the system in the system. */
public interface ObjectIdentityService {

  /**
   * Get the total number of object identities in the system for a type
   *
   * @param typeId the identifier for the type/class for of the requested objectIdentities
   */
  Integer getNrOfObjectIdentities(String typeId);

  /**
   * Get all the object identities in the system for a type
   *
   * @param typeId the identifier for the type/class for of the requested objectIdentities
   */
  public List<ObjectIdentity> getObjectIdentities(String typeId);

  /**
   * Get the total number of object identities in the system for a type for a list of sids
   *
   * @param typeId the identifier for the type/class for of the requested objectIdentities.
   * @param sids the sids {@link Sid} to filter on, only object identities are returned where these
   *     sids have one or more permissions on.
   */
  Integer getNrOfObjectIdentities(String typeId, Set<Sid> sids);

  /**
   * Get a page of object identities in the system for a type
   *
   * @param typeId the identifier for the type/class for of the requested objectIdentities
   * @param limit the maximum number of results returned.
   * @param offset the number of items to skip before returning results.
   */
  public List<ObjectIdentity> getObjectIdentities(String typeId, int limit, int offset);

  /**
   * Get a page of object identities in the system for a type for a list of sids
   *
   * @param typeId the identifier for the type/class for of the requested objectIdentities.
   * @param sids the sids {@link Sid} to filter on, only object identities are returned where these
   *     sids have one or more permissions on.
   * @param limit the maximum number of results returned.
   * @param offset the number of items to skip before returning results.
   */
  public List<ObjectIdentity> getObjectIdentities(
      String typeId, Set<Sid> sids, int limit, int offset);

  /**
   * Get all the object identities in the system for a type for a list of sids
   *
   * @param typeId the identifier for the type/class for of the requested objectIdentities.
   * @param sids the sids {@link Sid} to filter on, only object identities are returned where these
   *     sids have one or more permissions on.
   */
  public List<ObjectIdentity> getObjectIdentities(String typeId, Set<Sid> sids);
}
