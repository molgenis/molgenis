package org.molgenis.elasticsearch.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.molgenis.data.AggregateResult;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchResult;

import com.google.common.collect.Lists;

/**
 * Generates a SearchResult from the ElasticSearch SearchResponse object
 * 
 * @author erwin
 * 
 */
public class ResponseParser
{
	public SearchResult parseSearchResponse(SearchResponse response)
	{
		ShardSearchFailure[] failures = response.getShardFailures();
		if ((failures != null) && (failures.length > 0))
		{
			StringBuilder sb = new StringBuilder("Exception while searching:\n");
			for (ShardSearchFailure failure : failures)
			{
				sb.append(failure.shard()).append(":").append(failure.reason());
			}

			return new SearchResult(sb.toString());
		}

		List<Hit> searchHits = new ArrayList<Hit>();
		long totalCount = response.getHits().totalHits();

		for (SearchHit hit : response.getHits().hits())
		{
			Map<String, Object> columnValueMap = new LinkedHashMap<String, Object>();

			// If fieldsToReturn is used the "fields" field of the SearchHit is
			// filled if not the "source" field is filled
			if ((hit.fields() != null) && !hit.fields().isEmpty())
			{
				for (SearchHitField searchHitField : hit.fields().values())
				{
					columnValueMap.put(searchHitField.name(), searchHitField.value());
				}
			}

			if ((hit.sourceAsMap() != null) && !hit.sourceAsMap().isEmpty())
			{
				for (Map.Entry<String, Object> entry : hit.sourceAsMap().entrySet())
				{
					columnValueMap.put(entry.getKey(), entry.getValue());
				}
				if ((hit.getScore() + "").equals("NaN"))
				{
					columnValueMap.put("score", 0);
				}
				else columnValueMap.put("score", hit.getScore());
			}

			searchHits.add(new Hit(hit.id(), hit.type(), columnValueMap));
		}

		AggregateResult aggregate = null;
		Aggregations aggregations = response.getAggregations();
		if (aggregations != null)
		{
			List<List<Long>> matrix = Lists.newArrayList();
			Set<String> xLabelsSet = Sets.newHashSet();
			Set<String> yLabelsSet = Sets.newHashSet();
			List<String> xLabels = new ArrayList<String>();
			List<String> yLabels = new ArrayList<String>();
			final int nrAggregations = Iterables.size(aggregations);
			if (nrAggregations != 1)
			{
				throw new RuntimeException("Multiple aggregations [" + nrAggregations + "] not supported");
			}

			Aggregation aggregation = aggregations.iterator().next();
			if (!(aggregation instanceof Terms))
			{
				throw new RuntimeException("Aggregation of type [" + aggregation.getClass().getName()
						+ "] not supported");
			}
			Terms terms = (Terms) aggregation;

			Collection<Bucket> buckets = terms.getBuckets();
			if (buckets.size() > 0)
			{
				// distinguish between 1D and 2D aggregation
				boolean hasSubAggregations = false;
				for (Bucket bucket : buckets)
				{
					Aggregations subAggregations = bucket.getAggregations();
					if (subAggregations != null && Iterables.size(subAggregations) > 0)
					{
						hasSubAggregations = true;
						break;
					}
				}

				if (hasSubAggregations)
				{
					// create labels
					for (Bucket bucket : buckets)
					{
						if (!xLabelsSet.contains(bucket.getKey())) xLabelsSet.add(bucket.getKey());

						Aggregations subAggregations = bucket.getAggregations();
						if (subAggregations != null)
						{
							if (Iterables.size(subAggregations) > 1)
							{
								throw new RuntimeException("Multiple aggregations [" + nrAggregations
										+ "] not supported");
							}
							Aggregation subAggregation = subAggregations.iterator().next();

							if (!(subAggregation instanceof Terms))
							{
								throw new RuntimeException("Aggregation of type ["
										+ subAggregation.getClass().getName() + "] not supported");
							}
							Terms subTerms = (Terms) subAggregation;

							for (Bucket subBucket : subTerms.getBuckets())
							{
								yLabelsSet.add(subBucket.getKey());
							}

						}
					}

					xLabels = new ArrayList<String>(xLabelsSet);
					Collections.sort(xLabels);
					xLabels.add("Total");
					yLabels = new ArrayList<String>(yLabelsSet);
					Collections.sort(yLabels);
					yLabels.add("Total");

					// create label map
					int idx = 0;
					Map<String, Integer> yLabelMap = new HashMap<String, Integer>();
					for (String yLabel : yLabels)
					{
						yLabelMap.put(yLabel, idx++);
					}

					for (Bucket bucket : buckets)
					{
						// create values
						List<Long> yValues = new ArrayList<Long>();
						for (int i = 0; i < idx; ++i)
						{
							yValues.add(0l);
						}

						Aggregations subAggregations = bucket.getAggregations();
						if (subAggregations != null)
						{
							long count = 0;
							Terms subTerms = (Terms) subAggregations.iterator().next();
							for (Bucket subBucket : subTerms.getBuckets())
							{
								long bucketCount = subBucket.getDocCount();
								yValues.set(yLabelMap.get(subBucket.getKey()), bucketCount);
								count += bucketCount;
							}
							yValues.set(yLabelMap.get("Total"), count);
						}

						matrix.add(yValues);
					}

					// create value totals
					List<Long> xTotals = new ArrayList<Long>();
					for (int i = 0; i < idx; ++i)
					{
						xTotals.add(0l);
					}

					for (List<Long> values : matrix)
					{
						int nrValues = values.size();
						for (int i = 0; i < nrValues; ++i)
						{
							xTotals.set(i, xTotals.get(i) + values.get(i));
						}
					}
					matrix.add(xTotals);
				}
				else
				{
					long total = 0;
					for (Bucket bucket : buckets)
					{
						xLabels.add(bucket.getKey());
						long docCount = bucket.getDocCount();
						matrix.add(Lists.newArrayList(Long.valueOf(docCount)));
						total += docCount;
					}
					matrix.add(Lists.newArrayList(Long.valueOf(total)));
					xLabels.add("Total");
					yLabels.add("Count");
				}
			}

			aggregate = new AggregateResult(matrix, xLabels, yLabels);
		}

		return new SearchResult(totalCount, searchHits, aggregate);
	}
}
