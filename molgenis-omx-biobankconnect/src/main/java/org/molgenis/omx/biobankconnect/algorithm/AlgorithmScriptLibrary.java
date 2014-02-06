package org.molgenis.omx.biobankconnect.algorithm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class AlgorithmScriptLibrary
{
	@Autowired
	private SearchService searchService;
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";

	private final Map<String, String> scriptLibrary = new HashMap<String, String>();

	public AlgorithmScriptLibrary()
	{
		populateLibrary();
	}

	public String findScriptTemplate(ObservableFeature feature)
	{
		SearchResult searchResult = findOntologyTerm(Arrays.asList(feature.getName(), feature.getDescription()));
		if (searchResult.getTotalHitCount() > 0)
		{
			Hit hit = searchResult.getSearchHits().get(0);
			for (String synonym : findOntologyTermSynonyms(hit))
			{
				if (scriptLibrary.containsKey(synonym.toLowerCase()))
				{
					return scriptLibrary.get(synonym.toLowerCase());
				}
			}
		}
		return StringUtils.EMPTY;
	}

	public SearchResult findOntologyTerm(List<String> queryStrings)
	{
		QueryImpl query = new QueryImpl();
		for (String queryString : queryStrings)
		{
			if (query.getRules().size() > 0) query.addRule(new QueryRule(Operator.OR));
			query.addRule(new QueryRule(ONTOLOGYTERM_SYNONYM, Operator.EQUALS, queryString));
		}
		query.pageSize(100);

		SearchRequest searchRequest = new SearchRequest(null, query, null);
		return searchService.search(searchRequest);
	}

	public Set<String> findOntologyTermSynonyms(Hit ontologyTermHit)
	{
		String ontologyTermIRI = ontologyTermHit.getColumnValueMap().get(ONTOLOGY_TERM_IRI).toString();
		Set<String> synonyms = new HashSet<String>();
		QueryImpl query = new QueryImpl();
		query.addRule(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, ontologyTermIRI));
		query.pageSize(100000);

		SearchRequest searchRequest = new SearchRequest(null, query, null);
		SearchResult searchResult = searchService.search(searchRequest);
		for (Hit hit : searchResult.getSearchHits())
		{
			synonyms.add(hit.getColumnValueMap().get(ONTOLOGYTERM_SYNONYM).toString().toLowerCase());
		}
		return synonyms;
	}

	// Load library from index or database
	private void populateLibrary()
	{
		scriptLibrary.put("body mass index", "$('weight').div($('height').pow(2))");
	}
}
