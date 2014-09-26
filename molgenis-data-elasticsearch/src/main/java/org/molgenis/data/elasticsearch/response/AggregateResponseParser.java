package org.molgenis.data.elasticsearch.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class AggregateResponseParser
{
	private static final String MISSING_VALUE_LABEL = "N/A";

	public AggregateResult parseAggregateResponse(AttributeMetaData aggAttr1, AttributeMetaData aggAttr2,
			AttributeMetaData aggAttrDistinct, Aggregations aggregations, DataService dataService,
			Integer anonymizationThreshold)
	{
		List<List<Long>> matrix = Lists.newArrayList();
		Set<String> xLabelsSet = Sets.newHashSet();
		Set<String> yLabelsSet = Sets.newHashSet();
		List<String> xLabels = new ArrayList<String>();
		List<String> yLabels = new ArrayList<String>();

		Terms terms = getTermsAggregation(aggregations);
		Missing missingTerms;
		if (aggAttr1.isNillable())
		{
			missingTerms = getMissingTermsAggregation(aggregations);
		}
		else
		{
			missingTerms = null;
		}

		Collection<Bucket> buckets = terms.getBuckets();
		int nrBuckets = buckets.size() + (missingTerms != null ? 1 : 0);
		if (nrBuckets > 0)
		{
			// create initial values
			for (int i = 0; i < nrBuckets; ++i)
				matrix.add(null);

			// distinguish between 1D and 2D aggregation
			boolean is2dAggregation = false;
			for (Bucket bucket : buckets)
			{
				Aggregations subAggregations = bucket.getAggregations();
				if (subAggregations != null && Iterables.size(subAggregations) > 0)
				{
					is2dAggregation = hasTermsAggregation(subAggregations);
					break;
				}
			}

			// create (sorted) labels for x-axis
			for (Bucket bucket : buckets)
			{
				if (!xLabelsSet.contains(bucket.getKey())) xLabelsSet.add(bucket.getKey());
			}

			xLabels = new ArrayList<String>(xLabelsSet);
			Collections.sort(xLabels);
			if (aggAttr1.isNillable())
			{
				xLabels.add(MISSING_VALUE_LABEL);
			}

			int xIdx = 0;
			Map<String, Integer> xLabelMap = new HashMap<String, Integer>();
			for (String xLabel : xLabels)
			{
				xLabelMap.put(xLabel, xIdx++);
			}

			if (is2dAggregation)
			{
				// create labels
				for (Bucket bucket : buckets)
				{
					Aggregations subAggregations = bucket.getAggregations();
					if (subAggregations != null)
					{
						Terms subTerms = getTermsAggregation(subAggregations);

						for (Bucket subBucket : subTerms.getBuckets())
						{
							yLabelsSet.add(subBucket.getKey());
						}
					}
				}

				yLabels = new ArrayList<String>(yLabelsSet);
				Collections.sort(yLabels);
				if (aggAttr2.isNillable())
				{
					yLabels.add(MISSING_VALUE_LABEL);
				}

				int yIdx = 0;
				Map<String, Integer> yLabelMap = new HashMap<String, Integer>();
				for (String yLabel : yLabels)
				{
					yLabelMap.put(yLabel, yIdx++);
				}

				for (Bucket bucket : buckets)
				{
					// create values
					List<Long> yValues = new ArrayList<Long>();
					for (int i = 0; i < yIdx; ++i)
					{
						yValues.add(0l);
					}

					Aggregations subAggregations = bucket.getAggregations();
					if (subAggregations != null)
					{
						Terms subTerms = getTermsAggregation(subAggregations);
						for (Bucket subBucket : subTerms.getBuckets())
						{
							Aggregations distinctAggregations = subBucket.getAggregations();
							long bucketCount;
							if (distinctAggregations != null && !Iterables.isEmpty(distinctAggregations))
							{
								bucketCount = getCardinalityAggregation(distinctAggregations).getValue();
							}
							else
							{
								bucketCount = subBucket.getDocCount();
							}
							yValues.set(yLabelMap.get(subBucket.getKey()), bucketCount);
						}
						if (aggAttr2.isNillable())
						{
							Missing subMissingTerms = getMissingTermsAggregation(subAggregations);
							yValues.set(yLabelMap.get(MISSING_VALUE_LABEL), subMissingTerms.getDocCount());
						}
					}

					matrix.set(xLabelMap.get(bucket.getKey()), yValues);
				}
				if (aggAttr1.isNillable())
				{
					// create values
					List<Long> yValues = new ArrayList<Long>();
					for (int i = 0; i < yIdx; ++i)
					{
						yValues.add(0l);
					}

					Aggregations subAggregations = missingTerms.getAggregations();
					if (subAggregations != null)
					{
						Terms subTerms = getTermsAggregation(subAggregations);
						for (Bucket subBucket : subTerms.getBuckets())
						{
							Aggregations distinctAggregations = subBucket.getAggregations();
							long bucketCount;
							if (distinctAggregations != null && !Iterables.isEmpty(distinctAggregations))
							{
								bucketCount = getCardinalityAggregation(distinctAggregations).getValue();
							}
							else
							{
								bucketCount = subBucket.getDocCount();
							}
							yValues.set(yLabelMap.get(subBucket.getKey()), bucketCount);
						}
						if (aggAttr2.isNillable())
						{
							Missing subMissingTerms = getMissingTermsAggregation(subAggregations);
							yValues.set(yLabelMap.get(MISSING_VALUE_LABEL), subMissingTerms.getDocCount());
						}
					}

					matrix.set(xLabelMap.get(MISSING_VALUE_LABEL), yValues);
				}
			}
			else
			{
				for (Bucket bucket : buckets)
				{
					Aggregations distinctAggregations = bucket.getAggregations();
					long bucketCount;
					if (distinctAggregations != null)
					{
						bucketCount = getCardinalityAggregation(distinctAggregations).getValue();
					}
					else
					{
						bucketCount = bucket.getDocCount();
					}
					matrix.set(xLabelMap.get(bucket.getKey()), Lists.newArrayList(Long.valueOf(bucketCount)));
				}
			}

			// matrix labels are ids for categorical/xref/mref aggregates, convert to label attribute values
			AttributeMetaData xAggregateField = aggAttr1;
			if (xAggregateField != null)
			{
				FieldTypeEnum xDataType = xAggregateField.getDataType().getEnumType();
				switch (xDataType)
				{
					case CATEGORICAL:
					case MREF:
					case XREF:
						convertIdtoLabelLabels(xLabels, xAggregateField.getRefEntity(), dataService);
						// $CASES-OMITTED$
					default:
						break;
				}
			}
			AttributeMetaData yAggregateField = aggAttr2;
			if (yAggregateField != null)
			{
				FieldTypeEnum yDataType = yAggregateField.getDataType().getEnumType();
				switch (yDataType)
				{
					case CATEGORICAL:
					case MREF:
					case XREF:
						convertIdtoLabelLabels(yLabels, yAggregateField.getRefEntity(), dataService);
						// $CASES-OMITTED$
					default:
						break;
				}
			}
		}

		return new AggregateResult(matrix, xLabels, yLabels, anonymizationThreshold);
	}

	/**
	 * Unwrap nesting and reverse nesting aggregations and return terms aggregation
	 * 
	 * @param aggregations
	 * @return
	 */
	private Terms getTermsAggregation(Aggregations aggregations)
	{
		for (Aggregation aggregation : aggregations)
		{
			if (aggregation instanceof ReverseNested)
			{
				Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
				return getTermsAggregation(reverseNestedAggregations);
			}
			else if (aggregation instanceof Nested)
			{
				Aggregations nestedAggregations = ((Nested) aggregation).getAggregations();
				return getTermsAggregation(nestedAggregations);
			}
			else if (aggregation instanceof Terms)
			{
				return (Terms) aggregation;
			}
		}
		throw new RuntimeException("Aggregations does not contain Terms aggregation");
	}

	/**
	 * Unwrap nesting and reverse nesting aggregations and return missing aggregation
	 * 
	 * @param aggregations
	 * @return
	 */
	private Missing getMissingTermsAggregation(Aggregations aggregations)
	{
		for (Aggregation aggregation : aggregations)
		{
			if (aggregation instanceof ReverseNested)
			{
				Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
				return getMissingTermsAggregation(reverseNestedAggregations);
			}
			else if (aggregation instanceof Nested)
			{
				Aggregations nestedAggregations = ((Nested) aggregation).getAggregations();
				return getMissingTermsAggregation(nestedAggregations);
			}
			else if (aggregation instanceof Missing)
			{
				return (Missing) aggregation;
			}
		}
		throw new RuntimeException("Aggregations does not contain Missing aggregation");
	}

	/**
	 * Unwrap possible nested and reverse nested aggregations and check if resulting aggregation is a terms aggregation
	 * 
	 * @param aggregations
	 * @return
	 */
	private boolean hasTermsAggregation(Aggregations aggregations)
	{
		for (Aggregation aggregation : aggregations)
		{
			if (aggregation instanceof ReverseNested)
			{
				Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
				return hasTermsAggregation(reverseNestedAggregations);
			}
			else if (aggregation instanceof Nested)
			{
				Aggregations nestedAggregations = ((Nested) aggregation).getAggregations();
				return hasTermsAggregation(nestedAggregations);
			}
			else if (aggregation instanceof Terms)
			{
				return true;
			}
		}
		return false;
	}

	private Cardinality getCardinalityAggregation(Aggregations aggregations)
	{
		int nrCardinalityAggregations = Iterables.size(aggregations);
		if (nrCardinalityAggregations > 1)
		{
			throw new RuntimeException("Multiple aggregations [" + nrCardinalityAggregations + "] not supported");
		}
		Aggregation aggregation = aggregations.iterator().next();
		if (aggregation instanceof ReverseNested)
		{
			Aggregations reverseNestedAggregations = ((ReverseNested) aggregation).getAggregations();
			aggregation = reverseNestedAggregations.iterator().next();
		}
		if (aggregation instanceof Nested)
		{
			Aggregations nestedAggregations = ((Nested) aggregation).getAggregations();
			aggregation = nestedAggregations.iterator().next();
		}
		if (!(aggregation instanceof Cardinality))
		{
			throw new RuntimeException("Aggregation of type [" + aggregation.getClass().getName() + "] not supported");
		}
		return (Cardinality) aggregation;
	}

	/**
	 * Convert matrix labels that contain ids to label attribute values. Keeps in mind that the last label on a axis is
	 * "Total".
	 * 
	 * @param idLabels
	 * @param entityMetaData
	 * @param dataService
	 */
	private void convertIdtoLabelLabels(List<String> idLabels, EntityMetaData entityMetaData, DataService dataService)
	{
		final int nrLabels = idLabels.size();

		// Get entities for ids
		// Use Iterables.transform to work around List<String> to Iterable<Object> cast error
		Iterable<Entity> entities = dataService.findAll(entityMetaData.getName(),
				Iterables.transform(idLabels, new Function<String, Object>()
				{
					@Override
					public Object apply(String id)
					{
						return id;
					}
				}));

		// Map entity ids to labels
		Map<String, String> idToLabelMap = new HashMap<String, String>();
		for (Entity entity : entities)
		{
			idToLabelMap.put(entity.getIdValue().toString(), entity.getLabelValue());
		}

		for (int i = 0; i < nrLabels; ++i)
		{
			String id = idLabels.get(i);
			idLabels.set(i, idToLabelMap.get(id));
		}
	}

}
