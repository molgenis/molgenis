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

	void rebuildIndex(Repository<? extends Entity> repository);

	void refreshIndex();

	long count(EntityType entityType);

	long count(EntityType entityType, Query<Entity> q);

	Object searchOne(EntityType entityType, Query<Entity> q);

	Stream<Object> search(EntityType entityType, Query<Entity> q);

	AggregateResult aggregate(EntityType entityType, AggregateQuery aggregateQuery);

	void index(EntityType entityType, Entity entity, IndexingMode indexingMode);

	long index(EntityType entityType, Stream<? extends Entity> entities, IndexingMode indexingMode);

	void delete(EntityType entityType, Entity entity);

	void delete(EntityType entityType, Stream<? extends Entity> entities);

	void deleteById(EntityType entityType, Object entityId);

	void deleteAll(EntityType entityType, Stream<Object> entityIds);
}