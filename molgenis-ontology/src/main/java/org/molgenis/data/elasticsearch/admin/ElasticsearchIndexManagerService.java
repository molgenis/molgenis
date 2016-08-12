package org.molgenis.data.elasticsearch.admin;

import org.molgenis.data.meta.model.EntityMetaData;

import java.util.List;

public interface ElasticsearchIndexManagerService
{
	/**
	 * Returns all indexed entity meta data sorted by entity label
	 *
	 * @return
	 */
	List<EntityMetaData> getIndexedEntities();

	/**
	 * Rebuilds the index for the given entity type
	 *
	 * @param entityName
	 */
	void rebuildIndex(String entityName);
}
