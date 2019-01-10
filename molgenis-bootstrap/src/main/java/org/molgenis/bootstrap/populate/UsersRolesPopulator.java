package org.molgenis.bootstrap.populate;

import org.molgenis.data.security.auth.RoleMetadata;
import org.molgenis.data.security.auth.UserMetadata;

/**
 * Populates empty data store with security entities such as {@link UserMetadata users} and {@link
 * RoleMetadata roles}.
 */
public interface UsersRolesPopulator {
  /** Populates an empty data store with users, groups and authorities. */
  void populate();
}
