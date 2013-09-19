package org.molgenis.elasticsearch.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchResult;

/**
 * Generates a SearchResult from the ElasticSearch SearchResponse object
 * 
 * @author erwin
 * 
 */
public class ResponseParser
{
	private final String apiBaseUrl;

	public ResponseParser(String apiBaseUrl)
	{
		this.apiBaseUrl = apiBaseUrl;
	}

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
		long totalCount = response.hits().totalHits();

		for (SearchHit hit : response.hits().hits())
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
				columnValueMap.put("score", hit.getScore());
			}

			searchHits.add(new Hit(hit.id(), hit.type(), createHref(hit.id(), hit.type()), columnValueMap));
		}

		return new SearchResult(totalCount, searchHits);
	}

	private String createHref(String id, String type)
	{
		// TODO how do we now if it is an entity, so has a rest url?
		// for now assume numeric id is entity

		if (StringUtils.isNumeric(id))
		{

			return apiBaseUrl + "/" + type + "/" + id;
		}

		return null;
	}
}
