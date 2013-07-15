package org.molgenis.omx.ontologyAnnotator.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;
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
	public void annotate(Integer protocolId) throws DatabaseException
	{
		Database db = DatabaseUtil.createDatabase();

		try
		{
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule("type", Operator.SEARCH, "observablefeature"));
			queryRules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchRequest request = new SearchRequest("protocolTree-" + protocolId, queryRules, null);
			SearchResult result = searchService.search(request);
			List<ObservableFeature> featuresToUpdate = new ArrayList<ObservableFeature>();

			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Integer featureId = Integer.parseInt(hit.getColumnValueMap().get("id").toString());
				ObservableFeature feature = toObservableFeature(db.findById(ObservableFeature.class, featureId));
				String name = hit.getColumnValueMap().get("name").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				String description = hit.getColumnValueMap().get("description").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				List<String> definitions = new ArrayList<String>();
				definitions.addAll(annotateDataItem(db, feature, name));
				definitions.addAll(annotateDataItem(db, feature, description));

				if (definitions.size() > 0)
				{
					definitions.addAll(feature.getDefinition_Identifier());
					feature.setDefinition_Identifier(definitions);
				}
				featuresToUpdate.add(feature);
			}

			db.update(featuresToUpdate);
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

	private ObservableFeature toObservableFeature(ObservableFeature feature) throws Exception
	{
		ObservableFeature newFeature = new ObservableFeature();
		for (String field : feature.getFields())
			newFeature.set(field, feature.get(field));
		return newFeature;
	}

	public List<String> annotateDataItem(Database db, ObservableFeature feature, String description)
			throws DatabaseException
	{
		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(description.split(" +")));
		uniqueTerms.removeAll(OntologyAnnotatorModel.STOPWORDSLIST);
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		queryRules.add(new QueryRule(Operator.LIMIT, 100));
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

		Map<String, Map<String, Object>> mapUriTerm = new HashMap<String, Map<String, Object>>();
		List<Hit> hitSecondChoice = new ArrayList<Hit>();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			Map<String, Object> data = hit.getColumnValueMap();
			String ontologyTermSynonym = data.get("ontologyTermSynonym").toString().toLowerCase();
			String ontologyTerm = data.get("ontologyTerm").toString().toLowerCase();
			if (ontologyTerm.equals(ontologyTermSynonym))
			{
				if (validateOntologyTerm(uniqueTerms, ontologyTermSynonym)) mapUriTerm.put(data.get("ontologyTermIRI")
						.toString(), data);
			}
			else hitSecondChoice.add(hit);
		}

		for (Hit hit : hitSecondChoice)
		{
			Map<String, Object> data = hit.getColumnValueMap();
			String ontologyTermSynonym = data.get("ontologyTermSynonym").toString().toLowerCase();
			String ontologyTerm = data.get("ontologyTerm").toString().toLowerCase();
			if (!mapUriTerm.containsValue(ontologyTermSynonym))
			{
				if (validateOntologyTerm(uniqueTerms, ontologyTermSynonym)) mapUriTerm.put(data.get("ontologyTermIRI")
						.toString(), data);
			}
		}

		List<String> identifiers = new ArrayList<String>();
		if (feature.getDefinition_Identifier() != null) identifiers.addAll(feature.getDefinition_Identifier());
		for (String uri : mapUriTerm.keySet())
			if (!identifiers.contains(uri)) identifiers.add(uri);
		feature.setDefinition_Identifier(identifiers);

		if (mapUriTerm.size() > 0)
		{
			for (OntologyTerm ot : db.find(OntologyTerm.class, new QueryRule(OntologyTerm.TERMACCESSION, Operator.IN,
					new ArrayList<String>(mapUriTerm.keySet()))))
				mapUriTerm.remove(ot.getTermAccession());
		}

		List<OntologyTerm> listOfOntologyTerms = new ArrayList<OntologyTerm>();

		for (Entry<String, Map<String, Object>> entry : mapUriTerm.entrySet())
		{
			String uri = entry.getKey();
			Map<String, Object> data = entry.getValue();
			String ontologyLabel = data.get("ontologyLabel").toString();
			String term = ontologyLabel == null ? data.get("ontologyTerm").toString().toLowerCase() : ontologyLabel
					+ ":" + data.get("ontologyTerm").toString().toLowerCase();
			OntologyTerm ot = new OntologyTerm();
			ot.setIdentifier(uri);
			ot.setTermAccession(uri);
			ot.setName(term);
			listOfOntologyTerms.add(ot);
		}
		if (listOfOntologyTerms.size() > 0) db.add(listOfOntologyTerms);

		return identifiers;
	}

	private boolean validateOntologyTerm(Set<String> uniqueSets, String ontologyTermSynonym)
	{
		for (String eachTerm : new HashSet<String>(Arrays.asList(ontologyTermSynonym.split(" +"))))
			if (!uniqueSets.contains(eachTerm)) return false;
		return true;
	}

	public QueryRule[] toNestedQuery(List<QueryRule> rules)
	{
		QueryRule[] nestedQuery = new QueryRule[rules.size()];
		rules.toArray(nestedQuery);
		return nestedQuery;
	}

	@Override
	public float finishedPercentage()
	{
		return 0;
	}
}