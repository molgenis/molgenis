package org.molgenis.data.decorator;

import org.molgenis.data.Entity;

public interface PermissionChecker<E extends Entity>
{
	boolean isAddAllowed(E entity);

	boolean isReadAllowed(Object id);

	boolean isUpdateAllowed(Object id);

	boolean isDeleteAllowed(Object id);
}
