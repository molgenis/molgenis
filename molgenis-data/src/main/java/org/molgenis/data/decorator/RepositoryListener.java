package org.molgenis.data.decorator;

import org.molgenis.data.Entity;

public interface RepositoryListener<E extends Entity>
{
	void beforeAdd(E entity);

	void beforeUpdate(E entity);

	void beforeDelete(Object id);
}
