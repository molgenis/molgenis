package org.molgenis.data.semanticsearch.explain.service;

import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.util.Arrays;
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
import org.molgenis.data.QueryRule;
import org.molgenis.data.elasticsearch.request.QueryGenerator;
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

	public Set<Entry<String, Double>> reverseSearchQueryStrings(QueryRule disMaxQueryRule, Explanation explanation)
	{
		Set<Entry<String, Double>> matchedQueryStrings = new LinkedHashSet<Entry<String, Double>>();
		String discoverMatchedQueries = explainServiceHelper.discoverMatchedQueries(explanation);

		for (String queryPart : discoverMatchedQueries.split("\\|"))
		{
			Map<String, Double> matchedQueryRule = explainServiceHelper.recursivelyFindQuery(queryPart,
					Arrays.asList(disMaxQueryRule));
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
				matchedQueryStrings.add(entry);
			}
		}
		return matchedQueryStrings;
	}
}
