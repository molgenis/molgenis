package org.molgenis.data.elasticsearch;

import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.stream.Stream;

public interface SearchService
{

	/**
	 * Returns all type names for this index
	 * 
	 * @return
	 */
	Iterable<String> getTypes();

	boolean hasMapping(EntityMetaData entityMetaData);

	void createMappings(EntityMetaData entityMetaData);

	void createMappings(EntityMetaData entityMetaData, boolean enableNorms, boolean createAllIndex);

	/**
	 * Refresh index, making all operations performed since the last refresh available for search
	 */
	void refresh();

	long count(EntityMetaData entityMetaData);

	long count(Query<Entity> q, EntityMetaData entityMetaData);

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

	/**
	 * Adds or updated the given entities in the index
	 * 
	 * @param entities
	 * @param entityMetaData
	 * @param indexingMode
	 * @return number of indexed entities
	 */
	long index(Stream<? extends Entity> entities, EntityMetaData entityMetaData, IndexingMode indexingMode);

	void delete(Entity entity, EntityMetaData entityMetaData);

	void deleteById(String id, EntityMetaData entityMetaData);

	void deleteById(Stream<String> ids, EntityMetaData entityMetaData);

	void delete(Iterable<? extends Entity> entities, EntityMetaData entityMetaData);

	/**
	 * Deletes entities from index
	 * 
	 * @param entities
	 *            entity stream
	 * @param entityMetaData
	 */
	void delete(Stream<? extends Entity> entities, EntityMetaData entityMetaData);

	/**
	 * Deletes data and meta data
	 * 
	 * @param entityName
	 */
	void delete(String entityName);

	// TODO replace Iterable<Entity> with EntityCollection and add EntityCollection.getTotal()
	Iterable<Entity> search(Query<Entity> q, EntityMetaData entityMetaData);

	/**
	 * TODO replace Stream<Entity> with EntityCollection and add EntityCollection.getTotal()
	 * 
	 * @param q
	 * @param entityMetaData
	 * @return
	 */
	Stream<Entity> searchAsStream(Query<Entity> q, EntityMetaData entityMetaData);

	AggregateResult aggregate(AggregateQuery aggregateQuery, EntityMetaData entityMetaData);

	/**
	 * Frees memory from the index by flushing data to the index storage and clearing the internal transaction log
	 */
	void flush();

	void rebuildIndex(Repository<? extends Entity> repository);

	/**
	 * Optimize the index for faster search operations, remove documents that are marked as deleted.
	 */
	void optimizeIndex();

	void refreshIndex();

	Entity findOne(Query<Entity> q, EntityMetaData entityMetaData);
}