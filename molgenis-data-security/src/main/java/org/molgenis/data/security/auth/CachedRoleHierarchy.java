package org.molgenis.data.security.auth;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

public interface CachedRoleHierarchy extends RoleHierarchy {
  /** Signals the role hierarchy that its cache is dirty. */
  void markRoleHierarchyCacheDirty();
}
