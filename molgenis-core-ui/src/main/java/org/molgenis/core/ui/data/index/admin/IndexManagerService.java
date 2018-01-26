package org.molgenis.core.ui.data.index.admin;

import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public interface IndexManagerService
{
	/**
	 * Returns all indexed entity meta data sorted by entity label
	 */
	List<EntityType> getIndexedEntities();

	/**
	 * Rebuilds the index for the given entity type
	 */
	void rebuildIndex(String entityTypeId);
}
