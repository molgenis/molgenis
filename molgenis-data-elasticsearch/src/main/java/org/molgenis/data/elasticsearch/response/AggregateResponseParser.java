package org.molgenis.data.elasticsearch.response;

import com.google.common.collect.Iterables;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.request.AggregateQueryGenerator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.*;
import java.util.stream.Stream;

import static org.molgenis.data.elasticsearch.request.AggregateQueryGenerator.*;

public class AggregateResponseParser
{
	@SuppressWarnings("unchecked")
	public AggregateResult parseAggregateResponse(AttributeMetaData aggAttr1, AttributeMetaData aggAttr2,
			AttributeMetaData aggAttrDistinct, Aggregations aggs, DataService dataService)
	{
		Map<String, Object> aggsMap = parseAggregations(aggAttr1, aggAttr2, aggAttrDistinct, aggs);

		// create labels
		Map<Object, Integer> xLabelsIdx = new HashMap<>();
		Map<Object, Integer> yLabelsIdx = aggAttr2 != null ? new HashMap<>() : null;
		for (Map.Entry<String, Object> entry : aggsMap.entrySet())
		{
			String xLabel = entry.getKey();
			xLabelsIdx.put(xLabel, null);
			if (aggAttr2 != null)
			{
				Map<String, Object> subAggsMap = (Map<String, Object>) entry.getValue();
				for (Map.Entry<String, Object> subEntry : subAggsMap.entrySet())
					yLabelsIdx.put(subEntry.getKey(), null);
			}
		}

		List<Object> xLabels = new ArrayList<>(xLabelsIdx.keySet());
		Collections.sort(xLabels, new AggregateLabelComparable());
		int nrXLabels = xLabels.size();
		for (int i = 0; i < nrXLabels; ++i)
			xLabelsIdx.put(xLabels.get(i), i);

		List<Object> yLabels;
		if (aggAttr2 != null)
		{
			yLabels = new ArrayList<>(yLabelsIdx.keySet());
			Collections.sort(yLabels, new AggregateLabelComparable());
			int nrYLabels = yLabels.size();
			for (int i = 0; i < nrYLabels; ++i)
				yLabelsIdx.put(yLabels.get(i), i);
		}
		else yLabels = Collections.emptyList();

		// create value matrix
		List<List<Long>> matrix = new ArrayList<List<Long>>(nrXLabels);
		int nrYLabels = aggAttr2 != null ? yLabels.size() : 1;
		for (int i = 0; i < nrXLabels; ++i)
		{
			List<Long> yValues = new ArrayList<Long>(nrYLabels);
			for (int j = 0; j < nrYLabels; ++j)
				yValues.add(0L);
			matrix.add(yValues);
		}

		for (Map.Entry<String, Object> entry : aggsMap.entrySet())
		{
			String key = entry.getKey();
			Integer idx = xLabelsIdx.get(key);
			List<Long> yValues = matrix.get(idx);

			if (aggAttr2 != null)
			{
				Map<String, Long> subValues = (Map<String, Long>) entry.getValue();
				for (Map.Entry<String, Long> subEntry : subValues.entrySet())
				{
					String subKey = subEntry.getKey();
					Integer subIdx = yLabelsIdx.get(subKey);
					yValues.set(subIdx, subEntry.getValue());
				}
			}
			else
			{
				Long count = (Long) entry.getValue();
				yValues.set(0, count);
			}
		}

		if (AggregateQueryGenerator.isNestedType(aggAttr1))
		{
			convertIdtoLabelLabels(xLabels, aggAttr1.getRefEntity(), dataService);
		}
		if (aggAttr2 != null && AggregateQueryGenerator.isNestedType(aggAttr2))
		{
			convertIdtoLabelLabels(yLabels, aggAttr2.getRefEntity(), dataService);
		}

		return new AggregateResult(matrix, xLabels, yLabels);
	}

	private Map<String, Object> parseAggregations(AttributeMetaData aggAttr1, AttributeMetaData aggAttr2,
			AttributeMetaData aggAttrDistinct, Aggregations aggs)
	{
		Map<String, Object> counts = new HashMap<String, Object>();

		boolean isAttr1Nested = AggregateQueryGenerator.isNestedType(aggAttr1);
		boolean isAttr1Nillable = aggAttr1.isNillable();

		if (isAttr1Nested) aggs = removeNesting(aggs);
		Terms terms = getTermsAggregation(aggs, aggAttr1);

		for (Bucket bucket : terms.getBuckets())
		{
			String key = bucket.getKey();
			Object value;
			if (aggAttr2 != null)
			{
				Map<String, Long> subCounts = new HashMap<String, Long>();

				boolean isAttr2Nested = AggregateQueryGenerator.isNestedType(aggAttr2);
				boolean isAttr2Nillable = aggAttr2.isNillable();

				Aggregations subAggs = bucket.getAggregations();
				if (isAttr1Nested) subAggs = removeReverseNesting(subAggs);
				if (isAttr2Nested) subAggs = removeNesting(subAggs);
				Terms subTerms = getTermsAggregation(subAggs, aggAttr2);

				for (Bucket subBucket : subTerms.getBuckets())
				{
					String subKey = subBucket.getKey();
					Long subValue;

					if (aggAttrDistinct != null)
					{
						boolean isAttrDistinctNested = AggregateQueryGenerator.isNestedType(aggAttrDistinct);

						Aggregations distinctAggs = subBucket.getAggregations();
						if (isAttr2Nested) distinctAggs = removeReverseNesting(distinctAggs);
						if (isAttrDistinctNested) distinctAggs = removeNesting(distinctAggs);
						Cardinality distinctAgg = getDistinctAggregation(distinctAggs, aggAttrDistinct);
						subValue = distinctAgg.getValue();
					}
					else
					{
						subValue = subBucket.getDocCount();
					}

					subCounts.put(subKey, subValue);
				}

				if (isAttr2Nillable)
				{
					Missing subMissing = getMissingAggregation(subAggs, aggAttr2);
					String subKey = null;
					Long subValue;

					if (aggAttrDistinct != null)
					{
						boolean isAttrDistinctNested = AggregateQueryGenerator.isNestedType(aggAttrDistinct);

						Aggregations subDistinctAggs = subMissing.getAggregations();
						if (isAttr2Nested) subDistinctAggs = removeReverseNesting(subDistinctAggs);
						if (isAttrDistinctNested) subDistinctAggs = removeNesting(subDistinctAggs);
						Cardinality distinctAgg = getDistinctAggregation(subDistinctAggs, aggAttrDistinct);
						subValue = distinctAgg.getValue();
					}
					else
					{
						subValue = subMissing.getDocCount();
					}
					subCounts.put(subKey, subValue);
				}
				value = subCounts;
			}
			else
			{
				if (aggAttrDistinct != null)
				{
					boolean isAttrDistinctNested = AggregateQueryGenerator.isNestedType(aggAttrDistinct);

					Aggregations distinctAggs = bucket.getAggregations();
					if (isAttr1Nested) distinctAggs = removeReverseNesting(distinctAggs);
					if (isAttrDistinctNested) distinctAggs = removeNesting(distinctAggs);
					Cardinality distinctAgg = getDistinctAggregation(distinctAggs, aggAttrDistinct);
					value = distinctAgg.getValue();
				}
				else
				{
					value = bucket.getDocCount();
				}
			}
			counts.put(key, value);
		}

		if (isAttr1Nillable)
		{
			Missing missing = getMissingAggregation(aggs, aggAttr1);
			String key = null;
			Object value;
			if (aggAttr2 != null)
			{
				Map<String, Long> subCounts = new HashMap<String, Long>();

				boolean isAttr2Nested = AggregateQueryGenerator.isNestedType(aggAttr2);
				boolean isAttr2Nillable = aggAttr2.isNillable();

				Aggregations subAggs = missing.getAggregations();
				if (isAttr1Nested) subAggs = removeReverseNesting(subAggs);
				if (isAttr2Nested) subAggs = removeNesting(subAggs);
				Terms subTerms = getTermsAggregation(subAggs, aggAttr2);

				for (Bucket subBucket : subTerms.getBuckets())
				{
					String subKey = subBucket.getKey();
					Long subValue;

					if (aggAttrDistinct != null)
					{
						boolean isAttrDistinctNested = AggregateQueryGenerator.isNestedType(aggAttrDistinct);

						Aggregations distinctAggs = subBucket.getAggregations();
						if (isAttr2Nested) distinctAggs = removeReverseNesting(distinctAggs);
						if (isAttrDistinctNested) distinctAggs = removeNesting(distinctAggs);
						Cardinality distinctAgg = getDistinctAggregation(distinctAggs, aggAttrDistinct);
						subValue = distinctAgg.getValue();
					}
					else
					{
						subValue = subBucket.getDocCount();
					}

					subCounts.put(subKey, subValue);
				}

				if (isAttr2Nillable)
				{
					Missing subMissing = getMissingAggregation(subAggs, aggAttr2);
					String subKey = null;
					Long subValue;

					if (aggAttrDistinct != null)
					{
						boolean isAttrDistinctNested = AggregateQueryGenerator.isNestedType(aggAttrDistinct);

						Aggregations subDistinctAggs = subMissing.getAggregations();
						if (isAttr2Nested) subDistinctAggs = removeReverseNesting(subDistinctAggs);
						if (isAttrDistinctNested) subDistinctAggs = removeNesting(subDistinctAggs);
						Cardinality distinctAgg = getDistinctAggregation(subDistinctAggs, aggAttrDistinct);
						subValue = distinctAgg.getValue();
					}
					else
					{
						subValue = subMissing.getDocCount();
					}
					subCounts.put(subKey, subValue);
				}
				value = subCounts;
			}
			else
			{
				if (aggAttrDistinct != null)
				{
					boolean isAttrDistinctNested = AggregateQueryGenerator.isNestedType(aggAttrDistinct);

					Aggregations distinctAggs = missing.getAggregations();
					if (isAttr1Nested) distinctAggs = removeReverseNesting(distinctAggs);
					if (isAttrDistinctNested) distinctAggs = removeNesting(distinctAggs);
					Cardinality distinctAgg = getDistinctAggregation(distinctAggs, aggAttrDistinct);
					value = distinctAgg.getValue();
				}
				else
				{
					value = missing.getDocCount();
				}
			}
			counts.put(key, value);
		}
		return counts;
	}

	private Aggregations removeNesting(Aggregations aggs)
	{
		if (Iterables.size(aggs) != 1)
		{
			throw new RuntimeException("Invalid number of aggregations");
		}
		Aggregation agg = aggs.iterator().next();
		if (!(agg instanceof Nested))
		{
			throw new RuntimeException("Aggregation is not a nested aggregation");
		}
		return ((Nested) agg).getAggregations();
	}

	private Aggregations removeReverseNesting(Aggregations aggs)
	{
		if (Iterables.size(aggs) != 1)
		{
			throw new RuntimeException("Invalid number of aggregations");
		}
		Aggregation agg = aggs.iterator().next();
		if (!(agg instanceof ReverseNested))
		{
			throw new RuntimeException("Aggregation is not a reverse nested aggregation");
		}
		return ((ReverseNested) agg).getAggregations();
	}

	private Terms getTermsAggregation(Aggregations aggs, AttributeMetaData attr)
	{
		Aggregation agg = aggs.get(attr.getName() + AGGREGATION_TERMS_POSTFIX);
		if (agg == null)
		{
			throw new RuntimeException("Missing terms aggregation");
		}
		if (!(agg instanceof Terms))
		{
			throw new RuntimeException("Aggregation is not a terms aggregation");
		}
		return (Terms) agg;
	}

	private Missing getMissingAggregation(Aggregations aggs, AttributeMetaData attr)
	{
		Aggregation agg = aggs.get(attr.getName() + AGGREGATION_MISSING_POSTFIX);
		if (agg == null)
		{
			throw new RuntimeException("Missing missing aggregation");
		}
		if (!(agg instanceof Missing))
		{
			throw new RuntimeException("Aggregation is not a missing aggregation");
		}
		return (Missing) agg;
	}

	private Cardinality getDistinctAggregation(Aggregations aggs, AttributeMetaData attr)
	{
		Aggregation agg = aggs.get(attr.getName() + AGGREGATION_DISTINCT_POSTFIX);
		if (agg == null)
		{
			throw new RuntimeException("Missing cardinality aggregation");
		}
		if (!(agg instanceof Cardinality))
		{
			throw new RuntimeException("Aggregation is not a cardinality aggregation");
		}
		return (Cardinality) agg;
	}

	/**
	 * Convert matrix labels that contain ids to label attribute values. Keeps in mind that the last label on a axis is
	 * "Total".
	 *
	 * @param idLabels
	 * @param entityMetaData
	 * @param dataService
	 */
	private void convertIdtoLabelLabels(List<Object> idLabels, EntityMetaData entityMetaData, DataService dataService)
	{
		final int nrLabels = idLabels.size();
		if (nrLabels > 0)
		{
			// Get entities for ids
			// Use Iterables.transform to work around List<String> to Iterable<Object> cast error
			Stream<Object> idLabelsWithoutNull = idLabels.stream().filter(idLabel -> idLabel != null);

			// Map entity ids to labels
			Map<String, Entity> idToLabelMap = new HashMap<>();
			dataService.findAll(entityMetaData.getName(), idLabelsWithoutNull).forEach(entity ->
			{
				idToLabelMap.put(entity.getIdValue().toString(), entity);
			});

			for (int i = 0; i < nrLabels; ++i)
			{
				Object id = idLabels.get(i);
				if (id != null) // missing value label
				{
					idLabels.set(i, idToLabelMap.get(id));
				}
			}
		}
	}

	private static class AggregateLabelComparable implements Comparator<Object>
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			return o1 == null ? 1 : (o2 == null ? -1 : o1.toString().compareTo(o2.toString())); // FIXME check if this
			// is allowed?
		}
	}
}
