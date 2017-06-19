package org.molgenis.data.elasticsearch.response;

import com.google.common.base.Joiner;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.suggest.term.TermSuggestion.Score;
import org.molgenis.data.DataService;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.data.meta.AttributeType.MREF;

/**
 * Generates a SearchResult from the ElasticSearch SearchResponse object
 *
 * @author erwin
 */
public class ResponseParser
{
	private final AggregateResponseParser aggregateResponseParser;

	public ResponseParser()
	{
		this.aggregateResponseParser = new AggregateResponseParser();
	}

	public SearchResult parseSearchResponse(SearchRequest request, SearchResponse response, DataService dataService)
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

		List<Hit> searchHits = new ArrayList<>();
		long totalCount = response.getHits().getTotalHits();

		EntityType entityType = request.getEntityType();
		for (SearchHit hit : response.getHits().getHits())
		{
			Map<String, Object> columnValueMap = new LinkedHashMap<>();

			// If fieldsToReturn is used the "fields" field of the SearchHit is
			// filled if not the "source" field is filled
			if ((hit.getFields() != null) && !hit.getFields().isEmpty())
			{
				for (SearchHitField searchHitField : hit.getFields().values())
				{
					columnValueMap.put(searchHitField.getName(), searchHitField.getValue());
				}
			}

			if ((hit.getSourceAsMap() != null) && !hit.getSourceAsMap().isEmpty())
			{
				for (Map.Entry<String, Object> entry : hit.getSourceAsMap().entrySet())
				{
					// Check if the field is MREF, if so, only extract the
					// information for labelAttribute from refeEntity and put it
					// in the Hit result map
					String fieldName = entry.getKey();
					if (entityType == null || entityType.getAttribute(fieldName) == null
							|| entityType.getAttribute(fieldName).getDataType() != MREF)

					{
						columnValueMap.put(entry.getKey(), entry.getValue());
					}
					else
					{
						Attribute attribute = entityType.getAttribute(fieldName).getRefEntity().getLabelAttribute();
						List<Object> values = new ArrayList<>();
						if (entry.getValue() instanceof List<?>)
						{
							for (Object eachElement : (List<?>) entry.getValue())
							{
								if (eachElement instanceof Map<?, ?>)
								{
									for (Map.Entry<?, ?> entrySet : ((Map<?, ?>) eachElement).entrySet())
									{
										if (entrySet.getKey().toString().equalsIgnoreCase(attribute.getName()))
										{
											Object value = entrySet.getValue();
											if (value != null)
											{
												if (value instanceof List<?>)
												{
													values.addAll((List<?>) value);
												}
												else
												{
													values.add(value);
												}
												break;
											}
										}
									}
								}
							}
						}
						columnValueMap.put(entry.getKey(), Joiner.on(',').join(values));
					}
				}
				columnValueMap.put(Score.class.getSimpleName().toLowerCase(),
						String.valueOf(hit.getScore()).equals("NaN") ? 0 : hit.getScore());
			}

			searchHits.add(new Hit(hit.getId(), hit.getType(), columnValueMap));
		}

		AggregateResult aggregate = null;
		Aggregations aggregations = response.getAggregations();
		if (aggregations != null)
		{
			aggregate = aggregateResponseParser.parseAggregateResponse(request.getAggregateAttribute1(),
					request.getAggregateAttribute2(), request.getAggregateAttributeDistinct(), aggregations,
					dataService);
		}

		return new SearchResult(totalCount, searchHits, aggregate);
	}
}
