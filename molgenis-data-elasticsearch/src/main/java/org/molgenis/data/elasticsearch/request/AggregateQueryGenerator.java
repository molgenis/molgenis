package org.molgenis.data.elasticsearch.request;

import static java.lang.Integer.MAX_VALUE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;

public class AggregateQueryGenerator
{
	public static final String AGGREGATION_MISSING_POSTFIX = "_missing";
	public static final String AGGREGATION_REVERSE_POSTFIX = "_reverse";
	public static final String AGGREGATION_NESTED_POSTFIX = "_nested";
	public static final String AGGREGATION_DISTINCT_POSTFIX = "_distinct";
	public static final String AGGREGATION_TERMS_POSTFIX = "_terms";

	public void generate(SearchRequestBuilder searchRequestBuilder, AttributeMetaData aggAttr1,
			AttributeMetaData aggAttr2, AttributeMetaData aggAttrDistinct)
	{
		// validate request
		if (aggAttr1 == null)
		{
			throw new IllegalArgumentException("Aggregation requires at least one aggregateable attribute");
		}
		if (aggAttr1 != null && !aggAttr1.isAggregateable())
		{
			throw new IllegalArgumentException("Attribute is not aggregateable [ " + aggAttr1.getName() + "]");
		}
		if (aggAttr2 != null && !aggAttr2.isAggregateable())
		{
			throw new IllegalArgumentException("Attribute is not aggregateable [ " + aggAttr2.getName() + "]");
		}
		if (aggAttrDistinct != null && aggAttrDistinct.isNillable())
		{
			// see: https://github.com/molgenis/molgenis/issues/1938
			throw new IllegalArgumentException("Distinct aggregateable attribute cannot be nillable");
		}
		FieldTypeEnum dataType1 = aggAttr1.getDataType().getEnumType();
		if (aggAttr1.isNillable()
				&& (dataType1 == CATEGORICAL || dataType1 == CATEGORICAL_MREF || dataType1 == XREF || dataType1 == MREF))
		{
			// see: https://github.com/molgenis/molgenis/issues/1937
			throw new IllegalArgumentException("Aggregateable attribute of type [" + dataType1 + "] cannot be nillable");
		}
		if (aggAttr2 != null)
		{
			// see: https://github.com/molgenis/molgenis/issues/1937
			FieldTypeEnum dataType2 = aggAttr2.getDataType().getEnumType();
			if (aggAttr2.isNillable()
					&& (dataType2 == CATEGORICAL || dataType2 == CATEGORICAL_MREF || dataType2 == XREF || dataType2 == MREF))
			{
				throw new IllegalArgumentException("Aggregateable attribute of type [" + dataType2
						+ "] cannot be nillable");
			}
		}

		// collect aggregates
		searchRequestBuilder.setSize(0);

		LinkedList<AttributeMetaData> aggAttrs = new LinkedList<AttributeMetaData>();
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

	private List<AggregationBuilder<?>> createAggregations(LinkedList<AttributeMetaData> attrs,
			AttributeMetaData parentAttr, AttributeMetaData distinctAttr)
	{
		AttributeMetaData attr = attrs.pop();

		List<AggregationBuilder<?>> aggs = new ArrayList<AggregationBuilder<?>>();

		// term aggregation
		String termsAggName = attr.getName() + AGGREGATION_TERMS_POSTFIX;
		String termsAggFieldName = getAggregateFieldName(attr);
		AggregationBuilder<?> termsAgg = AggregationBuilders.terms(termsAggName).size(MAX_VALUE)
				.field(termsAggFieldName);
		aggs.add(termsAgg);

		// missing term aggregation
		if (attr.isNillable())
		{
			String missingAggName = attr.getName() + AGGREGATION_MISSING_POSTFIX;
			String missingAggFieldName = attr.getName();
			AggregationBuilder<?> missingTermsAgg = AggregationBuilders.missing(missingAggName).field(
					missingAggFieldName);
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
					.field(cardinalityAggFieldName).precisionThreshold(40000l);

			// CardinalityBuilder does not implement AggregationBuilder interface, so we need some more code
			AbstractAggregationBuilder wrappedDistinctAgg;
			if (isNestedType(distinctAttr))
			{
				String nestedAggName = distinctAttr.getName() + AGGREGATION_NESTED_POSTFIX;
				String nestedAggFieldName = distinctAttr.getName();
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
			String nestedAggFieldName = attr.getName();
			NestedBuilder nestedAgg = AggregationBuilders.nested(nestedAggName).path(nestedAggFieldName);
			for (AggregationBuilder<?> agg : aggs)
			{
				nestedAgg.subAggregation(agg);
			}
			aggs = Collections.<AggregationBuilder<?>> singletonList(nestedAgg);
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
			aggs = Collections.<AggregationBuilder<?>> singletonList(reverseNestedAgg);
		}

		return aggs;
	}

	public static boolean isNestedType(AttributeMetaData attr)
	{
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
		return dataType == FieldTypeEnum.CATEGORICAL || dataType == FieldTypeEnum.CATEGORICAL_MREF
				|| dataType == FieldTypeEnum.XREF || dataType == FieldTypeEnum.MREF;
	}

	private String getAggregateFieldName(AttributeMetaData attr)
	{
		String attrName = attr.getName();
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
			case INT:
			case LONG:
			case DECIMAL:
				return attrName;
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
				return attrName + '.' + MappingsBuilder.FIELD_NOT_ANALYZED;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case XREF:
			case MREF:
			case FILE:
				// use id attribute of nested field
				return attrName + '.' + getAggregateFieldName(attr.getRefEntity().getIdAttribute());
			case COMPOUND:
				throw new UnsupportedOperationException();
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}
}
