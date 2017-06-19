package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

public interface SearchService
{
	long count(EntityType entityType);

	long count(EntityType entityType, Query<Entity> q);

	Object searchOne(EntityType entityType, Query<Entity> q);

	Stream<Object> search(EntityType entityType, Query<Entity> q);

	AggregateResult aggregate(EntityType entityType, AggregateQuery aggregateQuery);
}