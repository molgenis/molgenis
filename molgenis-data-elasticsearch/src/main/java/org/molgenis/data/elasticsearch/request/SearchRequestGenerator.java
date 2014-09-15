package org.molgenis.data.elasticsearch.request;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;

/**
 * Builds a ElasticSearch search request
 * 
 * @author erwin
 * 
 */
public class SearchRequestGenerator
{
	private final List<? extends QueryPartGenerator> generators = Arrays.asList(new QueryGenerator(),
			new SortGenerator(), new LimitOffsetGenerator(), new DisMaxQueryGenerator());

	/**
	 * Add the 'searchType', 'fields', 'types' and 'query' of the SearchRequestBuilder
	 * 
	 * @param searchRequestBuilder
	 * @param entityNames
	 * @param searchType
	 * @param query
	 * @param fieldsToReturn
	 * @param aggregateField1
	 *            First Field to aggregate on
	 * @param aggregateField2
	 *            Second Field to aggregate on
	 * @param entityMetaData
	 */
	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, List<String> entityNames,
			SearchType searchType, Query query, List<String> fieldsToReturn, AttributeMetaData aggregateField1,
			AttributeMetaData aggregateField2, AttributeMetaData aggregateFieldDistinct, EntityMetaData entityMetaData)
	{
		searchRequestBuilder.setSearchType(searchType);

		// Document type
		if (entityNames != null)
		{
			searchRequestBuilder.setTypes(entityNames.toArray(new String[entityNames.size()]));
		}

		// Fields
		if ((fieldsToReturn != null) && !fieldsToReturn.isEmpty())
		{
			searchRequestBuilder.addFields(fieldsToReturn.toArray(new String[fieldsToReturn.size()]));
		}

		// Generate query
		if (query != null)
		{
			for (QueryPartGenerator generator : generators)
			{
				generator.generate(searchRequestBuilder, query, entityMetaData);
			}
		}

		// Aggregates
		if (aggregateField1 != null || aggregateField2 != null)
		{
			// TODO add missing aggregation for nillable attributes
			searchRequestBuilder.setSize(0);

			AggregationBuilder<?> aggregationBuilder;
			if (aggregateField1 != null && aggregateField2 != null)
			{
				// see: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations.html
				AggregationBuilder<?> aggregationBuilder1 = createTermAggregateBuilder(aggregateField1);
				AggregationBuilder<?> aggregationBuilder2 = createTermAggregateBuilder(aggregateField2);

				boolean shouldNestAggregation1 = isRequiresNestedAggregation(aggregateField1);
				boolean shouldNestAggregation2 = isRequiresNestedAggregation(aggregateField2);

				if (aggregateFieldDistinct != null)
				{
					AbstractAggregationBuilder cardinalityBuilder = createDistinctAggregateBuilder(aggregateFieldDistinct);

					// order is important
					if (shouldNestAggregation2)
					{
						cardinalityBuilder = AggregationBuilders.reverseNested("reverse").subAggregation(
								cardinalityBuilder);
					}
					aggregationBuilder2.subAggregation(cardinalityBuilder);
				}
				if (shouldNestAggregation2)
				{
					aggregationBuilder2 = nestAggregateBuilder(aggregateField2, aggregationBuilder2);
				}
				if (shouldNestAggregation1)
				{
					aggregationBuilder2 = AggregationBuilders.reverseNested("reverse").subAggregation(
							aggregationBuilder2);
				}
				aggregationBuilder1.subAggregation(aggregationBuilder2);

				if (shouldNestAggregation1)
				{
					aggregationBuilder1 = nestAggregateBuilder(aggregateField1, aggregationBuilder1);
				}
				aggregationBuilder = aggregationBuilder1;
			}
			else if (aggregateField1 != null)
			{
				aggregationBuilder = createTermAggregateBuilder(aggregateField1);

				boolean shouldNestAggregation1 = isRequiresNestedAggregation(aggregateField1);
				if (aggregateFieldDistinct != null)
				{
					AbstractAggregationBuilder cardinalityBuilder = createDistinctAggregateBuilder(aggregateFieldDistinct);
					if (shouldNestAggregation1)
					{
						cardinalityBuilder = AggregationBuilders.reverseNested("reverse").subAggregation(
								cardinalityBuilder);
					}
					aggregationBuilder.subAggregation(cardinalityBuilder);
				}
				if (isRequiresNestedAggregation(aggregateField1))
				{
					aggregationBuilder = nestAggregateBuilder(aggregateField1, aggregationBuilder);
				}
			}
			else
			{
				aggregationBuilder = createTermAggregateBuilder(aggregateField2);

				boolean shouldNestAggregation2 = isRequiresNestedAggregation(aggregateField2);
				if (aggregateFieldDistinct != null)
				{
					AbstractAggregationBuilder cardinalityBuilder = createDistinctAggregateBuilder(aggregateFieldDistinct);
					if (shouldNestAggregation2)
					{
						cardinalityBuilder = AggregationBuilders.reverseNested("reverse").subAggregation(
								cardinalityBuilder);
					}
					aggregationBuilder.subAggregation(cardinalityBuilder);
				}
				if (shouldNestAggregation2)
				{
					aggregationBuilder = nestAggregateBuilder(aggregateField2, aggregationBuilder);
				}
			}
			searchRequestBuilder.addAggregation(aggregationBuilder);
		}
	}

	public void buildSearchRequest(SearchRequestBuilder searchRequestBuilder, String entityName, SearchType searchType,
			Query query, List<String> fieldsToReturn, AttributeMetaData aggregateField1,
			AttributeMetaData aggregateField2, AttributeMetaData aggregateFieldDistinct, EntityMetaData entityMetaData)
	{
		buildSearchRequest(searchRequestBuilder, entityName == null ? null : Arrays.asList(entityName), searchType,
				query, fieldsToReturn, aggregateField1, aggregateField2, aggregateFieldDistinct, entityMetaData);
	}

	private boolean isRequiresNestedAggregation(AttributeMetaData attr)
	{
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
		return dataType == FieldTypeEnum.CATEGORICAL || dataType == FieldTypeEnum.XREF
				|| dataType == FieldTypeEnum.MREF;
	}

	private AggregationBuilder<?> nestAggregateBuilder(AttributeMetaData attr, AggregationBuilder<?> aggregationBuilder)
	{
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
		switch (dataType)
		{
			case CATEGORICAL:
			case MREF:
			case XREF:
				String attrName = attr.getName();
				return AggregationBuilders.nested(attrName).path(attrName).subAggregation(aggregationBuilder);
				// $CASES-OMITTED$
			default:
				throw new RuntimeException("Nested aggregation not possible for data type [" + dataType + "]");
		}
	}

	private TermsBuilder createTermAggregateBuilder(AttributeMetaData attr)
	{
		String attrName = attr.getName();
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
				// work around elasticsearch bug for boolean multi-fields:
				// http://elasticsearch-users.115913.n3.nabble.com/boolean-multi-field-silently-ignored-in-1-
				// 2-1-td4058107.html
				return AggregationBuilders.terms(attrName).size(Integer.MAX_VALUE).field(attrName);
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case SCRIPT:
			case STRING:
			case TEXT:
				// use non-analyzed field
				return AggregationBuilders.terms(attrName).size(Integer.MAX_VALUE)
						.field(attrName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED);
			case CATEGORICAL:
			case XREF:
			case MREF:
				// use non-analyzed nested field
				// do not wrap in a nested aggregation builder yet, sub aggregations might have to be added
				AttributeMetaData refIdAttribute = attr.getRefEntity().getIdAttribute();
				String fieldName = attrName + '.' + refIdAttribute.getName() + '.' + MappingsBuilder.FIELD_NOT_ANALYZED;
				return AggregationBuilders.terms(attrName).size(Integer.MAX_VALUE).field(fieldName);
			case COMPOUND:
			case FILE:
			case IMAGE:
				throw new UnsupportedOperationException();
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	private CardinalityBuilder createDistinctAggregateBuilder(AttributeMetaData attr)
	{
		// http://www.elasticsearch.org/guide/en/elasticsearch/reference/1.x/search-aggregations-metrics-cardinality-aggregation.html
		// The precision_threshold options allows to trade memory for accuracy, and defines a unique count below which
		// counts are expected to be close to accurate. Above this value, counts might become a bit more fuzzy. The
		// maximum supported value is 40000, thresholds above this number will have the same effect as a threshold of
		// 40000.
		return AggregationBuilders.cardinality("distinct").field(attr.getName()).precisionThreshold(40000l);
	}
}
