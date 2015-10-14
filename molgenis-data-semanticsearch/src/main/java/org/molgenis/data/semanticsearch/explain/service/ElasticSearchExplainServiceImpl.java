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
	private static final String DEFAULT_INDEX_NAME = "molgenis";
	private final ExplainServiceHelper explainServiceHelper;
	private final Client client;

	private final QueryGenerator queryGenerator = new QueryGenerator();

	@Autowired
	public ElasticSearchExplainServiceImpl(Client client, ExplainServiceHelper explainServiceHelper)
	{
		this.explainServiceHelper = explainServiceHelper;
		this.client = client;
	}

	public Explanation explain(Query q, EntityMetaData entityMetaData, String documentId)
	{
		String type = sanitizeMapperType(entityMetaData.getName());
		ExplainRequestBuilder explainRequestBuilder = new ExplainRequestBuilder(client, DEFAULT_INDEX_NAME, type,
				documentId);
		QueryBuilder queryBuilder = queryGenerator.createQueryBuilder(q.getRules(), entityMetaData);
		explainRequestBuilder.setQuery(queryBuilder);
		ExplainResponse explainResponse = explainRequestBuilder.get();
		if (explainResponse.hasExplanation())
		{
			return explainResponse.getExplanation();
		}
		return null;
	}

	public Set<ExplainedQueryString> findQueriesFromExplanation(Map<String, String> originalQueryInMap,
			Explanation explanation)
	{
		Set<ExplainedQueryString> matchedQueryStrings = new LinkedHashSet<ExplainedQueryString>();
		Set<String> matchedQueryTerms = explainServiceHelper.findMatchedWords(explanation);
		for (String matchedQueryTerm : matchedQueryTerms)
		{
			Map<String, Double> matchedQueryRule = explainServiceHelper.findMatchQueries(matchedQueryTerm,
					originalQueryInMap);

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

				matchedQueryStrings.add(ExplainedQueryString.create(matchedQueryTerm, entry.getKey(),
						originalQueryInMap.get(entry.getKey()), entry.getValue()));
			}
		}
		return matchedQueryStrings;
	}
}
