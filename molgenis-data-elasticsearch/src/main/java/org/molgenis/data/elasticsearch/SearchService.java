package org.molgenis.data.elasticsearch;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;

public interface SearchService
{

	/**
	 * Returns all type names for this index
	 * 
	 * @return
	 */
	Iterable<String> getTypes();

	/**
	 * @deprecated see search(Query, EntityMetaData) or aggregate(AggregateQuery, EntityMetaData)
	 */
	@Deprecated
	SearchResult search(SearchRequest request);

	boolean hasMapping(EntityMetaData entityMetaData);

	void createMappings(EntityMetaData entityMetaData);

	void createMappings(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex);

	/**
	 * Refresh index, making all operations performed since the last refresh available for search
	 */
	void refresh();

	long count(EntityMetaData entityMetaData);

	long count(Query q, EntityMetaData entityMetaData);

	void index(Entity entity, EntityMetaData entityMetaData, IndexingMode indexingMode);

	/**
	 * Adds or updated the given entities in the index
	 *
	 * @param entities
	 * @param entityMetaData
	 * @param indexingMode
	 * @return number of indexed entities, which equals the size of the input entities iterable
	 */
	long index(Iterable<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode);

	void delete(Entity entity, EntityMetaData entityMetaData);

	void deleteById(String id, EntityMetaData entityMetaData);

	void deleteById(Iterable<String> ids, EntityMetaData entityMetaData);

	void delete(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);

	/**
	 * Deletes data and meta data
	 * 
	 * @param entityName
	 */
	void delete(String entityName);

	/**
	 * Returns entity with given id or null if entity does not exist
	 * 
	 * @param entityId
	 * @param entityMetaData
	 * @return entity or null
	 */
	Entity get(Object entityId, EntityMetaData entityMetaData);

	/**
	 * Returns entity with given id and attribute values defined by fetch or null if entity does not exist
	 * 
	 * @param entityId
	 * @param entityMetaData
	 * @param fetch
	 * @return entity or null
	 */
	Entity get(Object entityId, EntityMetaData entityMetaData, Fetch fetch);

	/**
	 * Returns entities with given ids
	 * 
	 * @param entityIds
	 * @param entityMetaData
	 * @param fetch
	 * @return entities
	 */
	Iterable<Entity> get(Iterable<Object> entityIds, EntityMetaData entityMetaData);

	/**
	 * Returns entities with given ids and attribute values defined by fetch
	 * 
	 * @param entityIds
	 * @param entityMetaData
	 * @param fetch
	 * @return entities with attribute values defined by fetch
	 */
	Iterable<Entity> get(Iterable<Object> entityIds, EntityMetaData entityMetaData, Fetch fetch);

	// TODO replace Iterable<Entity> with EntityCollection and add EntityCollection.getTotal()
	Iterable<Entity> search(Query q, EntityMetaData entityMetaData);

	AggregateResult aggregate(AggregateQuery aggregateQuery, EntityMetaData entityMetaData);

	/**
	 * Frees memory from the index by flushing data to the index storage and clearing the internal transaction log
	 */
	void flush();

	void rebuildIndex(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);

	/**
	 * Optimize the index for faster search operations, remove documents that are marked as deleted.
	 */
	void optimizeIndex();
}