package org.molgenis.elasticsearch.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.molgenis.data.AggregateResult;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
			Set<String> xLabels = Sets.newLinkedHashSet();
			Set<String> yLabels = Sets.newLinkedHashSet();

			final int nrAggregations = Iterables.size(aggregations);
			if (nrAggregations != 1)
			{
				throw new RuntimeException("Multiple aggregations [" + nrAggregations + "] not supported");
			}

			Aggregation aggregation = aggregations.iterator().next();
			if (!(aggregation instanceof StringTerms))
			{
				throw new RuntimeException("Aggregation of type [" + aggregation.getClass().getName()
						+ "] not supported");
			}
			StringTerms stringTerms = (StringTerms) aggregation;

			for (Bucket bucket : stringTerms.getBuckets())
			{
				Aggregations subAggregations = bucket.getAggregations();
				final int nrSubAggregations = Iterables.size(subAggregations);
				if (nrSubAggregations > 1)
				{
					throw new RuntimeException("Multiple aggregations [" + nrAggregations + "] not supported");
				}

				StringTerms subStringTerms;
				if (nrSubAggregations == 1)
				{

					Aggregation subAggregation = subAggregations.iterator().next();
					if (!(subAggregation instanceof StringTerms))
					{
						throw new RuntimeException("Aggregation of type [" + subAggregation.getClass().getName()
								+ "] not supported");
					}
					subStringTerms = (StringTerms) subAggregation;
				}
				else
				{
					subStringTerms = null;
				}

				xLabels.add(bucket.getKey());
				long docCount = bucket.getDocCount();
				total += docCount;
				matrix.add(Lists.newArrayList(docCount));
			}

			// TODO finish

			aggregate = new AggregateResult(matrix, xLabels, yLabels);
		}

		return new SearchResult(totalCount, searchHits, aggregate);
	}

	private TermsFacet.Entry findTerm(String text, TermsFacet facet)
	{
		for (TermsFacet.Entry term : facet.getEntries())
		{
			if (term.getTerm().string().equalsIgnoreCase(text))
			{
				return term;
			}
		}

		return null;
	}
}
