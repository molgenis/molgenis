package org.molgenis.data.elasticsearch.generator;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.generator.model.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Generates Elasticsearch (aggregation) queries and sorts from MOLGENIS (aggregation) queries.
 */
@Component
class QueryContentGenerators
{
	private final QueryGenerator queryGenerator;
	private final SortGenerator sortGenerator;
	private final AggregationGenerator aggregationGenerator;

	public QueryContentGenerators(QueryGenerator queryGenerator, SortGenerator sortGenerator,
			AggregationGenerator aggregationGenerator)
	{
		this.queryGenerator = requireNonNull(queryGenerator);
		this.sortGenerator = requireNonNull(sortGenerator);
		this.aggregationGenerator = requireNonNull(aggregationGenerator);
	}

	public QueryBuilder createQuery(Query<Entity> query, EntityType entityType)
	{
		return queryGenerator.createQueryBuilder(query, entityType);
	}

	public Sort createSorts(org.molgenis.data.Sort sort, EntityType entityType)
	{
		return sortGenerator.generateSort(sort, entityType);
	}

	public List<AggregationBuilder> createAggregations(Attribute aggAttr1, Attribute aggAttr2,
			Attribute aggAttrDistinct)
	{
		return aggregationGenerator.createAggregations(aggAttr1, aggAttr2, aggAttrDistinct);
	}
}
