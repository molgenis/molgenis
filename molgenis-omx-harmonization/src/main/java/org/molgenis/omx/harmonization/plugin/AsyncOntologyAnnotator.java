package org.molgenis.omx.harmonization.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.DatabaseUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyAnnotator implements OntologyAnnotator, InitializingBean
{
	private SearchService searchService;

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
	}

	@Async
	public void annotate(Integer protocolId)
	{
		Database db = DatabaseUtil.createDatabase();

		try
		{
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule("type", Operator.SEARCH, "observablefeature"));
			queryRules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchRequest request = new SearchRequest("protocolTree-" + protocolId, queryRules, null);
			SearchResult result = searchService.search(request);

			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Integer featureId = Integer.parseInt(hit.getColumnValueMap().get("id").toString());
				String description = hit.getColumnValueMap().get("description").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				String name = hit.getColumnValueMap().get("name").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				annotateDataItem(db, featureId, description);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtil.closeQuietly(db);
		}
	}

	public List<ObservableFeature> annotateDataItem(Database db, Integer featureId, String description)
	{
		ObservableFeature feature = new ObservableFeature();

		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(description.split(" +")));
		uniqueTerms.removeAll(CreatePotentialTerms.STOPWORDSLIST);
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		for (String term : uniqueTerms)
		{
			if (!term.isEmpty() && !term.matches(" +"))
			{
				term = term.replaceAll("[^(a-zA-Z0-9 )]", "");
				queryRules.add(new QueryRule("ontologyTermSynonym", Operator.SEARCH, term));
				queryRules.add(new QueryRule(Operator.OR));
			}
		}
		if (queryRules.size() > 0) queryRules.remove(queryRules.size() - 1);

		SearchRequest request = new SearchRequest(null, queryRules, null);
		Iterator<Hit> iterator = searchService.search(request).getSearchHits().iterator();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			String ontologyTermSynonym = hit.getColumnValueMap().get("ontologyTermSynonym").toString().toLowerCase();
			if (validateOntologyTerm(uniqueTerms, ontologyTermSynonym))
			{
				System.out.println(hit);
			}
		}

		return null;
	}

	public QueryRule[] toNestedQuery(List<QueryRule> rules)
	{
		QueryRule[] nestedQuery = new QueryRule[rules.size()];
		rules.toArray(nestedQuery);
		return nestedQuery;
	}

	public boolean validateOntologyTerm(Set<String> uniqueSets, String ontologyTermSynonym)
	{
		for (String eachTerm : new HashSet<String>(Arrays.asList(ontologyTermSynonym.split(" +"))))
			if (!uniqueSets.contains(eachTerm)) return false;
		return true;
	}

	@Override
	public float finishedPercentage()
	{
		return 0;
	}
}
