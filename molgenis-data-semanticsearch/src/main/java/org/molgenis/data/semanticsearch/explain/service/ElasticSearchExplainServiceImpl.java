package org.molgenis.data.semanticsearch.explain.service;

import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.QueryGenerator;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticSearchExplainServiceImpl implements ElasticSearchExplainService
{
	private final Client client;
	private final String indexName;
	private final ExplainServiceHelper explainServiceHelper;

	private final QueryGenerator queryGenerator = new QueryGenerator();

	@Autowired
	public ElasticSearchExplainServiceImpl(Client client, String indexName, ExplainServiceHelper explainServiceHelper)
	{
		this.client = client;
		this.indexName = indexName;
		this.explainServiceHelper = explainServiceHelper;
	}

	public Explanation explain(Query q, EntityMetaData entityMetaData, String documentId)
	{
		String type = sanitizeMapperType(entityMetaData.getName());
		ExplainRequestBuilder explainRequestBuilder = new ExplainRequestBuilder(client, indexName, type, documentId);
		QueryBuilder queryBuilder = queryGenerator.createQueryBuilder(q.getRules(), entityMetaData);
		explainRequestBuilder.setQuery(queryBuilder);
		ExplainResponse explainResponse = explainRequestBuilder.get();
		if (explainResponse.hasExplanation())
		{
			return explainResponse.getExplanation();
		}
		return null;
	}

	public Set<ExplainedQueryString> reverseSearchQueryStrings(Map<String, String> collectExpanedQueryMap,
			Explanation explanation)
	{
		Set<ExplainedQueryString> matchedQueryStrings = new LinkedHashSet<ExplainedQueryString>();

		String discoverMatchedQueries = explainServiceHelper.discoverMatchedQueries(explanation);
		for (String queryPart : discoverMatchedQueries.split("\\|"))
		{
			Map<String, Double> matchedQueryRule = explainServiceHelper.findMatchQueries(queryPart,
					collectExpanedQueryMap);
			if (matchedQueryRule.size() > 0)
			{
				Entry<String, Double> entry = matchedQueryRule.entrySet().stream()
						.max(new Comparator<Entry<String, Double>>()
						{
							public int compare(Entry<String, Double> o1, Entry<String, Double> o2)
							{
								return Double.compare(o1.getValue(), o2.getValue());
							}
						}).get();

				matchedQueryStrings.add(new ExplainedQueryString(queryPart, entry.getKey(), collectExpanedQueryMap
						.get(entry.getKey()), entry.getValue()));
			}
		}
		return matchedQueryStrings;
	}
}
