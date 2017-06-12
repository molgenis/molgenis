package org.molgenis.data.elasticsearch.request;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;

public class AggregateQueryGenerator
{
	public static final String AGGREGATION_MISSING_POSTFIX = "_missing";
	public static final String AGGREGATION_REVERSE_POSTFIX = "_reverse";
	public static final String AGGREGATION_NESTED_POSTFIX = "_nested";
	public static final String AGGREGATION_DISTINCT_POSTFIX = "_distinct";
	public static final String AGGREGATION_TERMS_POSTFIX = "_terms";

	private final DocumentIdGenerator documentIdGenerator;

	public AggregateQueryGenerator(DocumentIdGenerator documentIdGenerator)
	{
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
	}

	public void generate(SearchRequestBuilder searchRequestBuilder, Attribute aggAttr1, Attribute aggAttr2,
			Attribute aggAttrDistinct)
	{
		// validate request
		if (aggAttr1 == null)
		{
			throw new IllegalArgumentException("Aggregation requires at least one isAggregatable attribute");
		}
		if (!aggAttr1.isAggregatable())
		{
			throw new IllegalArgumentException("Attribute is not isAggregatable [ " + aggAttr1.getName() + "]");
		}
		if (aggAttr2 != null && !aggAttr2.isAggregatable())
		{
			throw new IllegalArgumentException("Attribute is not isAggregatable [ " + aggAttr2.getName() + "]");
		}
		if (aggAttrDistinct != null && aggAttrDistinct.isNillable())
		{
			// see: https://github.com/molgenis/molgenis/issues/1938
			throw new IllegalArgumentException("Distinct isAggregatable attribute cannot be nillable");
		}
		AttributeType dataType1 = aggAttr1.getDataType();
		if (aggAttr1.isNillable() && isReferenceType(aggAttr1))
		{
			// see: https://github.com/molgenis/molgenis/issues/1937
			throw new IllegalArgumentException("Aggregatable attribute of type [" + dataType1 + "] cannot be nillable");
		}
		if (aggAttr2 != null)
		{
			// see: https://github.com/molgenis/molgenis/issues/1937
			AttributeType dataType2 = aggAttr2.getDataType();
			if (aggAttr2.isNillable() && isReferenceType(aggAttr2))
			{
				throw new IllegalArgumentException(
						"Aggregatable attribute of type [" + dataType2 + "] cannot be nillable");
			}
		}

		// collect aggregates
		searchRequestBuilder.setSize(0);

		LinkedList<Attribute> aggAttrs = new LinkedList<Attribute>();
		aggAttrs.add(aggAttr1);
		if (aggAttr2 != null)
		{
			aggAttrs.add(aggAttr2);
		}
		List<AggregationBuilder<?>> aggregationBuilders = createAggregations(aggAttrs, null, aggAttrDistinct);

		// add all aggregations to builder
		for (AggregationBuilder<?> aggregationBuilder : aggregationBuilders)
		{
			searchRequestBuilder.addAggregation(aggregationBuilder);
		}
	}

	private List<AggregationBuilder<?>> createAggregations(LinkedList<Attribute> attrs, Attribute parentAttr,
			Attribute distinctAttr)
	{
		Attribute attr = attrs.pop();

		List<AggregationBuilder<?>> aggs = new ArrayList<AggregationBuilder<?>>();

		// term aggregation
		String termsAggName = attr.getName() + AGGREGATION_TERMS_POSTFIX;
		String termsAggFieldName = getAggregateFieldName(attr);
		AggregationBuilder<?> termsAgg = AggregationBuilders.terms(termsAggName)
															.size(MAX_VALUE)
															.field(termsAggFieldName);
		aggs.add(termsAgg);

		// missing term aggregation
		if (attr.isNillable())
		{
			String missingAggName = attr.getName() + AGGREGATION_MISSING_POSTFIX;
			String missingAggFieldName = getAggregateFieldName(attr);
			AggregationBuilder<?> missingTermsAgg = AggregationBuilders.missing(missingAggName)
																	   .field(missingAggFieldName);
			aggs.add(missingTermsAgg);
		}

		// add distinct term aggregations
		if (attrs.isEmpty() && distinctAttr != null)
		{
			// http://www.elasticsearch.org/guide/en/elasticsearch/reference/1.x/search-aggregations-metrics-cardinality-aggregation.html
			// The precision_threshold options allows to trade memory for accuracy, and defines a unique count below
			// which counts are expected to be close to accurate. Above this value, counts might become a bit more
			// fuzzy. The maximum supported value is 40000, thresholds above this number will have the same effect as a
			// threshold of 40000.
			String cardinalityAggName = distinctAttr.getName() + AGGREGATION_DISTINCT_POSTFIX;
			String cardinalityAggFieldName = getAggregateFieldName(distinctAttr);
			CardinalityBuilder distinctAgg = AggregationBuilders.cardinality(cardinalityAggName)
																.field(cardinalityAggFieldName)
																.precisionThreshold(40000L);

			// CardinalityBuilder does not implement AggregationBuilder interface, so we need some more code
			AbstractAggregationBuilder wrappedDistinctAgg;
			if (isNestedType(distinctAttr))
			{
				String nestedAggName = distinctAttr.getName() + AGGREGATION_NESTED_POSTFIX;
				String nestedAggFieldName = getAggregatePathName(distinctAttr);
				NestedBuilder nestedBuilder = AggregationBuilders.nested(nestedAggName).path(nestedAggFieldName);
				nestedBuilder.subAggregation(distinctAgg);

				if (isNestedType(attr))
				{
					String reverseAggName = attr.getName() + AggregateQueryGenerator.AGGREGATION_REVERSE_POSTFIX;
					ReverseNestedBuilder reverseNestedBuilder = AggregationBuilders.reverseNested(reverseAggName);
					reverseNestedBuilder.subAggregation(nestedBuilder);
					wrappedDistinctAgg = reverseNestedBuilder;
				}
				else
				{
					wrappedDistinctAgg = nestedBuilder;
				}
			}
			else
			{
				if (isNestedType(attr))
				{
					String reverseAggName = attr.getName() + AggregateQueryGenerator.AGGREGATION_REVERSE_POSTFIX;
					ReverseNestedBuilder reverseNestedBuilder = AggregationBuilders.reverseNested(reverseAggName);
					reverseNestedBuilder.subAggregation(distinctAgg);
					wrappedDistinctAgg = reverseNestedBuilder;
				}
				else
				{
					wrappedDistinctAgg = distinctAgg;
				}
			}

			// add wrapped distinct term aggregation to aggregations
			for (AggregationBuilder<?> agg : aggs)
			{
				agg.subAggregation(wrappedDistinctAgg);
			}
		}

		// add sub aggregations
		if (!attrs.isEmpty())
		{
			List<AggregationBuilder<?>> subAggs = createAggregations(attrs, attr, distinctAttr);
			for (AggregationBuilder<?> agg : aggs)
			{
				for (AggregationBuilder<?> subAgg : subAggs)
				{
					agg.subAggregation(subAgg);
				}
			}
		}

		// wrap in nested aggregation is this aggregation is nested
		if (isNestedType(attr))
		{
			String nestedAggName = attr.getName() + AGGREGATION_NESTED_POSTFIX;
			String nestedAggFieldName = getAggregatePathName(attr);
			NestedBuilder nestedAgg = AggregationBuilders.nested(nestedAggName).path(nestedAggFieldName);
			for (AggregationBuilder<?> agg : aggs)
			{
				nestedAgg.subAggregation(agg);
			}
			aggs = Collections.<AggregationBuilder<?>>singletonList(nestedAgg);
		}

		// wrap in reverse nested aggregation if parent aggregation is nested
		if (parentAttr != null && isNestedType(parentAttr))
		{
			String reverseAggName = parentAttr.getName() + AggregateQueryGenerator.AGGREGATION_REVERSE_POSTFIX;
			ReverseNestedBuilder reverseNestedAgg = AggregationBuilders.reverseNested(reverseAggName);
			for (AggregationBuilder<?> agg : aggs)
			{
				reverseNestedAgg.subAggregation(agg);
			}
			aggs = Collections.<AggregationBuilder<?>>singletonList(reverseNestedAgg);
		}

		return aggs;
	}

	public static boolean isNestedType(Attribute attr)
	{
		return isReferenceType(attr);
	}

	private String getAggregatePathName(Attribute attr)
	{
		return documentIdGenerator.generateId(attr);
	}

	private String getAggregateFieldName(Attribute attr)
	{
		String fieldName = documentIdGenerator.generateId(attr);
		AttributeType dataType = attr.getDataType();
		switch (dataType)
		{
			case BOOL:
			case INT:
			case LONG:
			case DECIMAL:
				return fieldName;
			case DATE:
			case DATE_TIME:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				// use non-analyzed field
				return fieldName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case XREF:
			case MREF:
			case FILE:
			case ONE_TO_MANY:
				// use id attribute of nested field
				return fieldName + '.' + getAggregateFieldName(attr.getRefEntity().getIdAttribute());
			case COMPOUND:
				throw new UnsupportedOperationException();
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}
}
