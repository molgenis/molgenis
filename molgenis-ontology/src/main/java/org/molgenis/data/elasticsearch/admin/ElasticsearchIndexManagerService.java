package org.molgenis.data.elasticsearch.admin;

import java.util.List;

import org.molgenis.data.EntityMetaData;

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
