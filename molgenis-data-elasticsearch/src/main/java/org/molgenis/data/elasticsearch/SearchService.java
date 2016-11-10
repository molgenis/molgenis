package org.molgenis.data.elasticsearch;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

public interface SearchService
{

	/**
	 * Returns all type names for this index
	 *
	 * @return
	 */
	Iterable<String> getTypes();

	boolean hasMapping(EntityType entityType);

	boolean hasMapping(String entityName);

	void createMappings(EntityType entityType);

	void createMappings(EntityType entityType, boolean enableNorms, boolean createAllIndex);

	/**
	 * Refresh index, making all operations performed since the last refresh available for search
	 */
	void refresh();

	long count(EntityType entityType);

	long count(Query<Entity> q, EntityType entityType);

	void index(Entity entity, EntityType entityType, IndexingMode indexingMode);

	/**
	 * Adds or updated the given entities in the index
	 *
	 * @param entities
	 * @param entityType
	 * @param indexingMode
	 * @return number of indexed entities, which equals the size of the input entities iterable
	 */
	long index(Iterable<? extends Entity> entities, EntityType entityType, IndexingMode indexingMode);

	/**
	 * Adds or updated the given entities in the index
	 *
	 * @param entities
	 * @param entityType
	 * @param indexingMode
	 * @return number of indexed entities
	 */
	long index(Stream<? extends Entity> entities, EntityType entityType, IndexingMode indexingMode);

	void delete(Entity entity, EntityType entityType);

	void deleteById(String id, EntityType entityType);

	void deleteById(Stream<String> ids, EntityType entityType);

	void delete(Iterable<? extends Entity> entities, EntityType entityType);

	/**
	 * Deletes entities from index
	 *
	 * @param entities       entity stream
	 * @param entityType
	 */
	void delete(Stream<? extends Entity> entities, EntityType entityType);

	/**
	 * Deletes data and meta data
	 *
	 * @param entityName
	 */
	void delete(String entityName);

	// TODO replace Iterable<Entity> with EntityCollection and add EntityCollection.getTotal()
	Iterable<Entity> search(Query<Entity> q, EntityType entityType);

	/**
	 * TODO replace Stream<Entity> with EntityCollection and add EntityCollection.getTotal()
	 *
	 * @param q
	 * @param entityType
	 * @return
	 */
	Stream<Entity> searchAsStream(Query<Entity> q, EntityType entityType);

	AggregateResult aggregate(AggregateQuery aggregateQuery, EntityType entityType);

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

	Entity findOne(Query<Entity> q, EntityType entityType);
}