package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

public interface SearchService
{
	void createIndex(EntityType entityType);

	boolean hasIndex(EntityType entityType);

	void deleteIndex(EntityType entityType);

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

	void rebuildIndex(Repository<? extends Entity> repository);

	void refreshIndex();

	Entity findOne(Query<Entity> q, EntityType entityType);
}