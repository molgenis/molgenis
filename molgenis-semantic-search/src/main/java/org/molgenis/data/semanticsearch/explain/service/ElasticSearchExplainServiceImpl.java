package org.molgenis.data.semanticsearch.explain.service;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.request.QueryGenerator;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ElasticSearchExplainServiceImpl implements ElasticSearchExplainService
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchExplainServiceImpl.class);

	private final Client client;
	private final ExplainServiceHelper explainServiceHelper;
	private final DocumentIdGenerator documentIdGenerator;
	private final QueryGenerator queryGenerator;

	@Autowired
	public ElasticSearchExplainServiceImpl(Client client, ExplainServiceHelper explainServiceHelper,
			DocumentIdGenerator documentIdGenerator)
	{
		this.explainServiceHelper = explainServiceHelper;
		this.client = client;
		this.documentIdGenerator = requireNonNull(documentIdGenerator);
		this.queryGenerator = new QueryGenerator(documentIdGenerator);
	}

	public Explanation explain(Query<Entity> q, EntityType entityType, String documentId)
	{
		String indexName = documentIdGenerator.generateId(entityType);
		ExplainRequestBuilder explainRequestBuilder = client.prepareExplain(indexName, indexName, documentId);
		QueryBuilder queryBuilder = queryGenerator.createQueryBuilder(q.getRules(), entityType);
		explainRequestBuilder.setQuery(queryBuilder);
		ExplainResponse explainResponse = explainRequestBuilder.get();
		if (explainResponse.hasExplanation())
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(explainResponse.getExplanation().toString());
			}

			return explainResponse.getExplanation();
		}
		return null;
	}

	public Set<ExplainedQueryString> findQueriesFromExplanation(Map<String, String> originalQueryInMap,
			Explanation explanation)
	{
		Set<ExplainedQueryString> matchedQueryStrings = new LinkedHashSet<>();
		Set<String> matchedQueryTerms = explainServiceHelper.findMatchedWords(explanation);
		for (String matchedQueryTerm : matchedQueryTerms)
		{
			Map<String, Double> matchedQueryRule = explainServiceHelper
					.findMatchQueries(matchedQueryTerm, originalQueryInMap);

			if (matchedQueryRule.size() > 0)
			{
				Entry<String, Double> entry = matchedQueryRule.entrySet().stream()
						.max(Comparator.comparingDouble(Entry::getValue)).get();

				matchedQueryStrings.add(ExplainedQueryString
						.create(matchedQueryTerm, entry.getKey(), originalQueryInMap.get(entry.getKey()),
								entry.getValue()));
			}
		}
		return matchedQueryStrings;
	}
}
