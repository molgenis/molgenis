package org.molgenis.elasticsearch.response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.Facet;
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
		if (response.getFacets() != null)
		{
			// We always have exactly one facet, can be a compount of two aggregateable fields (separated by ~)
			Iterator<Facet> it = response.getFacets().iterator();
			if (it.hasNext())
			{
				List<List<Long>> matrix = Lists.newArrayList();
				Set<String> xLabels = Sets.newLinkedHashSet();
				Set<String> yLabels = Sets.newLinkedHashSet();

				TermsFacet facet = (TermsFacet) it.next();
				if (facet.getName().contains("~"))
				{
					for (TermsFacet.Entry term : facet.getEntries())
					{
						String[] labels = term.getTerm().string().split("~");
						xLabels.add(labels[0]);
						yLabels.add(labels[1]);
					}

					List<Long> yTotals = Lists.newArrayList();
					for (String xLabel : xLabels)
					{
						List<Long> row = Lists.newArrayList();
						matrix.add(row);

						int i = 0;
						long xTotal = 0;
						for (String yLabel : yLabels)
						{
							TermsFacet.Entry term = findTerm(xLabel + "~" + yLabel, facet);
							Long count = term != null ? Long.valueOf(term.getCount()) : 0;
							row.add(count);
							xTotal += count;

							if (yTotals.size() - 1 < i)
							{
								yTotals.add(count);
							}
							else
							{
								yTotals.set(i, yTotals.get(i) + count);
							}
						}
						row.add(xTotal);
					}

					matrix.add(yTotals);
					yLabels.add("Total");
					xLabels.add("Total");
				}
				else
				{
					yLabels.add("Count");
					long total = 0;
					for (TermsFacet.Entry term : facet.getEntries())
					{
						matrix.add(Lists.newArrayList(Long.valueOf(term.getCount())));
						xLabels.add(term.getTerm().string());
						total += term.getCount();
					}
					xLabels.add("Total");
					matrix.add(Lists.newArrayList(total));
				}

				aggregate = new AggregateResult(matrix, xLabels, yLabels);
			}
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
