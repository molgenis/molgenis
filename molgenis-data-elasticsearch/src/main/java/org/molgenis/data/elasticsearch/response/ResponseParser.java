package org.molgenis.data.elasticsearch.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.suggest.term.TermSuggestion.Score;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;

/**
 * Generates a SearchResult from the ElasticSearch SearchResponse object
 * 
 * @author erwin
 * 
 */
public class ResponseParser
{
	private final AggregateResponseParser aggregateResponseParser;

	public ResponseParser()
	{
		this.aggregateResponseParser = new AggregateResponseParser();
	}

	public SearchResult parseSearchResponse(SearchRequest request, SearchResponse response,
			EntityMetaData entityMetaData, DataService dataService)
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
					// Check if the field is MREF, if so, only extract the
					// information for labelAttribute from refeEntity and put it
					// in the Hit result map
					String fieldName = entry.getKey();
					if (entityMetaData == null
							|| entityMetaData.getAttribute(fieldName) == null
							|| !entityMetaData.getAttribute(fieldName).getDataType().getEnumType().toString()
									.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString()))

					{
						columnValueMap.put(entry.getKey(), entry.getValue());
					}
					else
					{
						AttributeMetaData attributeMetaData = entityMetaData.getAttribute(fieldName).getRefEntity()
								.getLabelAttribute();
						List<Object> values = new ArrayList<Object>();
						if (entry.getValue() instanceof List<?>)
						{
							for (Object eachElement : (List<?>) entry.getValue())
							{
								if (eachElement instanceof Map<?, ?>)
								{
									for (Map.Entry<?, ?> entrySet : ((Map<?, ?>) eachElement).entrySet())
									{
										if (entrySet.getKey().toString().equalsIgnoreCase(attributeMetaData.getName()))
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

			searchHits.add(new Hit(hit.id(), hit.type(), columnValueMap));
		}

		AggregateResult aggregate = null;
		Aggregations aggregations = response.getAggregations();
		if (aggregations != null)
		{
			aggregate = aggregateResponseParser.parseAggregateResponse(request.getAggregateField1(),
					request.getAggregateField2(), request.getAggregateFieldDistinct(), aggregations, dataService);
		}

		return new SearchResult(totalCount, searchHits, aggregate);
	}
}
