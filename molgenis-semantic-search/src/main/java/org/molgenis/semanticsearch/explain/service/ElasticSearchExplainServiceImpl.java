package org.molgenis.semanticsearch.explain.service;

import org.apache.lucene.search.Explanation;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ElasticSearchExplainServiceImpl implements ElasticSearchExplainService
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchExplainServiceImpl.class);

	private final ElasticsearchService elasticsearchService;
	private final ExplainServiceHelper explainServiceHelper;

	public ElasticSearchExplainServiceImpl(ElasticsearchService elasticsearchService,
			ExplainServiceHelper explainServiceHelper)
	{
		this.elasticsearchService = requireNonNull(elasticsearchService);
		this.explainServiceHelper = requireNonNull(explainServiceHelper);
	}

	public Explanation explain(Query<Entity> q, EntityType entityType, Object entityId)
	{
		Explanation explanation = elasticsearchService.explain(entityType, entityId, q);
		if (explanation != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(explanation.toString());
			}

			return explanation;
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
			Map<String, Double> matchedQueryRule = explainServiceHelper.findMatchQueries(matchedQueryTerm,
					originalQueryInMap);

			if (matchedQueryRule.size() > 0)
			{
				Entry<String, Double> entry = matchedQueryRule.entrySet()
															  .stream()
															  .max(Comparator.comparingDouble(Entry::getValue))
															  .get();

				matchedQueryStrings.add(ExplainedQueryString.create(matchedQueryTerm, entry.getKey(),
						originalQueryInMap.get(entry.getKey()), entry.getValue()));
			}
		}
		return matchedQueryStrings;
	}
}
