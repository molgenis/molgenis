package org.molgenis.data.elasticsearch.request;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.missing.MissingBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AggregateQueryGenerator
{
	private static final Logger LOG = Logger.getLogger(AggregateQueryGenerator.class);
	public static String AGGREGATION_MISSING_POSTFIX = "_missing";

	public void generate(SearchRequestBuilder searchRequestBuilder, AttributeMetaData aggAttr1,
			AttributeMetaData aggAttr2, AttributeMetaData aggAttrDistinct)
	{
		searchRequestBuilder.setSize(0);

		List<AggregationBuilder<?>> aggregationBuilders;
		if (aggAttr1 != null && aggAttr2 != null)
		{
			// see: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations.html
			List<AggregationBuilder<?>> aggregationBuilders1 = createAggregations(aggAttr1, null);
			List<AggregationBuilder<?>> aggregationBuilders2 = createAggregations(aggAttr2, aggAttrDistinct);

			if (isNestedType(aggAttr1))
			{
				// reverse nest if attribute is a nested type
				aggregationBuilders2 = Lists.transform(aggregationBuilders2,
						new Function<AggregationBuilder<?>, AggregationBuilder<?>>()
						{
							@Override
							public AggregationBuilder<?> apply(AggregationBuilder<?> aggBuilder)
							{
								return AggregationBuilders.reverseNested("reverse").subAggregation(aggBuilder);
							}
						});
			}

			// add second aggregate builders to first aggregate builders
			for (AggregationBuilder<?> aggregationBuilder1 : aggregationBuilders1)
			{
				for (AggregationBuilder<?> aggregationBuilder2 : aggregationBuilders2)
				{
					aggregationBuilder1.subAggregation(aggregationBuilder2);
				}
			}

			aggregationBuilders = aggregationBuilders1;
		}
		else if (aggAttr1 != null)
		{
			aggregationBuilders = createAggregations(aggAttr1, aggAttrDistinct);
		}
		else
		{
			aggregationBuilders = createAggregations(aggAttr2, aggAttrDistinct);
		}

		// add all aggregations to builder
		for (AggregationBuilder<?> aggregationBuilder : aggregationBuilders)
		{
			searchRequestBuilder.addAggregation(aggregationBuilder);
		}
		if(LOG.isDebugEnabled())
		{
			LOG.debug("SearchRequest: " + searchRequestBuilder);
		}
	}

	private List<AggregationBuilder<?>> createAggregations(AttributeMetaData attr, AttributeMetaData distinctAttr)
	{
		boolean nestedType = isNestedType(attr);

		// term aggregation
		AggregationBuilder<?> aggBuilder = createTermAggregateBuilder(attr);

		// missing term aggregation
		AggregationBuilder<?> missingAggBuilder;
		if (attr.isNillable())
		{
			missingAggBuilder = createMissingTermAggregateBuilder(attr);
		}
		else
		{
			missingAggBuilder = null;
		}

		// distinct term aggregation
		AbstractAggregationBuilder distinctBuilder;
		if (distinctAttr != null)
		{
			distinctBuilder = createDistinctAggregateBuilder(distinctAttr);

			// reverse nest if attribute is a nested type
			if (nestedType)
			{
				distinctBuilder = AggregationBuilders.reverseNested("reverse").subAggregation(distinctBuilder);
			}

			// add aggregation to both term and missing term aggregation
			aggBuilder.subAggregation(distinctBuilder);
			if (missingAggBuilder != null)
			{
				missingAggBuilder.subAggregation(distinctBuilder);
			}
		}

		if (nestedType)
		{
			// nest if attribute is a nested type
			aggBuilder = nestAggregateBuilder(attr, aggBuilder);
			if (missingAggBuilder != null)
			{
				missingAggBuilder = nestAggregateBuilder(attr, missingAggBuilder);
			}
		}

		if (missingAggBuilder != null)
		{
			return Arrays.<AggregationBuilder<?>> asList(aggBuilder, missingAggBuilder);
		}
		else
		{
			return Collections.<AggregationBuilder<?>> singletonList(aggBuilder);
		}
	}

	public static boolean isNestedType(AttributeMetaData attr)
	{
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
		return dataType == FieldTypeEnum.CATEGORICAL || dataType == FieldTypeEnum.XREF
				|| dataType == FieldTypeEnum.MREF;
	}

	private AggregationBuilder<?> nestAggregateBuilder(AttributeMetaData attr, AggregationBuilder<?> aggregationBuilder)
	{
		String attrName = attr.getName();
		return AggregationBuilders.nested(attrName).path(attrName).subAggregation(aggregationBuilder);
	}

	private TermsBuilder createTermAggregateBuilder(AttributeMetaData attr)
	{
		String fieldName = getAggregateFieldName(attr);
		return AggregationBuilders.terms(attr.getName()).size(Integer.MAX_VALUE).field(fieldName);
	}

	private MissingBuilder createMissingTermAggregateBuilder(AttributeMetaData attr)
	{
		String fieldName = getAggregateFieldName(attr);
		return AggregationBuilders.missing(attr.getName() + "_missing").field(fieldName);
	}

	private AbstractAggregationBuilder createDistinctAggregateBuilder(AttributeMetaData attr)
	{
		// http://www.elasticsearch.org/guide/en/elasticsearch/reference/1.x/search-aggregations-metrics-cardinality-aggregation.html
		// The precision_threshold options allows to trade memory for accuracy, and defines a unique count below which
		// counts are expected to be close to accurate. Above this value, counts might become a bit more fuzzy. The
		// maximum supported value is 40000, thresholds above this number will have the same effect as a threshold of
		// 40000.
		CardinalityBuilder distinctAggregationBuilder = AggregationBuilders.cardinality("distinct")
				.field(getAggregateFieldName(attr)).precisionThreshold(40000l);

		switch (attr.getDataType().getEnumType())
		{
			case CATEGORICAL:
			case MREF:
			case XREF:
				return AggregationBuilders.nested("distinct").path(attr.getName())
						.subAggregation(distinctAggregationBuilder);
				// $CASES-OMITTED$
			default:
				return distinctAggregationBuilder;
		}
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
			case XREF:
			case MREF:
				// use id attribute of nested field
				return attrName + '.' + getAggregateFieldName(attr.getRefEntity().getIdAttribute());
			case COMPOUND:
			case FILE:
			case IMAGE:
				throw new UnsupportedOperationException();
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}
}
