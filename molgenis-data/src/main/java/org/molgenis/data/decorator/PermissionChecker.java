package org.molgenis.data.decorator;

import org.molgenis.data.Entity;

public interface PermissionChecker<E extends Entity> {
  boolean isAddAllowed(E entity);

  boolean isCountAllowed(E entity);

  boolean isReadAllowed(E entity);

  boolean isUpdateAllowed(E entity);

  boolean isDeleteAllowed(E entity);
}
