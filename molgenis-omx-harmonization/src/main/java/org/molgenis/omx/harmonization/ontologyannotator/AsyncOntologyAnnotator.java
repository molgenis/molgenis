package org.molgenis.omx.harmonization.ontologyannotator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.tartarus.snowball.ext.PorterStemmer;

public class AsyncOntologyAnnotator implements OntologyAnnotator, InitializingBean
{
	private SearchService searchService;

	// TODO : solve this guy
	public static final Set<String> STOPWORDSLIST;
	private final AtomicInteger runningProcesses = new AtomicInteger();

	static
	{
		STOPWORDSLIST = new HashSet<String>(Arrays.asList("a", "you", "about", "above", "after", "again", "against",
				"all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
				"being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did",
				"didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
				"further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
				"he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
				"i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
				"let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on",
				"once", "only", "or", "other", "ought", "our", "ours ", " ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
				"that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
				"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under",
				"until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't",
				"what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
				"why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves", "many", ")", "("));
	}

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
	}

	public boolean isRunning()
	{
		if (runningProcesses.get() == 0) return false;
		return true;
	}

	@Async
	public void annotate(Integer protocolId)
	{
		runningProcesses.incrementAndGet();
		Database db = DatabaseUtil.createDatabase();

		try
		{
			PorterStemmer stemmer = new PorterStemmer();
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
				definitions.addAll(annotateDataItem(db, feature, name, stemmer));
				definitions.addAll(annotateDataItem(db, feature, description, stemmer));

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
			runningProcesses.decrementAndGet();
		}
	}

	private ObservableFeature toObservableFeature(ObservableFeature feature) throws Exception
	{
		ObservableFeature newFeature = new ObservableFeature();
		for (String field : feature.getFields())
			newFeature.set(field, feature.get(field));
		return newFeature;
	}

	public List<String> annotateDataItem(Database db, ObservableFeature feature, String description,
			PorterStemmer stemmer) throws DatabaseException
	{
		List<String> uniqueTerms = new ArrayList<String>();
		for (String eachTerm : Arrays.asList(description.split(" +")))
		{
			String termLowerCase = eachTerm.toLowerCase();
			if (!STOPWORDSLIST.contains(termLowerCase) && !uniqueTerms.contains(termLowerCase))
			{
				stemmer.setCurrent(termLowerCase);
				stemmer.stem();
				eachTerm = stemmer.getCurrent();
				uniqueTerms.add(eachTerm);
			}
		}
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

		List<TermComparison> listOfHits = new ArrayList<TermComparison>();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			listOfHits.add(new TermComparison(hit));
		}
		Collections.sort(listOfHits);

		Set<String> positionFilter = new HashSet<String>();
		Set<String> addedCandidates = new HashSet<String>();
		Map<String, Map<String, Object>> mapUriTerm = new HashMap<String, Map<String, Object>>();
		// List<Hit> hitSecondChoice = new ArrayList<Hit>();
		for (TermComparison termComparision : listOfHits)
		{
			Hit hit = termComparision.getHit();
			Map<String, Object> data = hit.getColumnValueMap();
			String ontologyTermSynonym = data.get("ontologyTermSynonym").toString().toLowerCase();
			String ontologyTerm = data.get("ontologyTerm").toString().toLowerCase();
			if (ontologyTerm.equals(ontologyTermSynonym) || !addedCandidates.contains(ontologyTermSynonym))
			{
				if (validateOntologyTerm(uniqueTerms, ontologyTermSynonym, stemmer, positionFilter))
				{
					mapUriTerm.put(data.get("ontologyTermIRI").toString(), data);
					addedCandidates.add(ontologyTermSynonym);
				}
			}
			// else hitSecondChoice.add(hit);
		}

		// positionFilter.clear();
		//
		// for (Hit hit : hitSecondChoice)
		// {
		// Map<String, Object> data = hit.getColumnValueMap();
		// String ontologyTermSynonym =
		// data.get("ontologyTermSynonym").toString().toLowerCase();
		// if (!mapUriTerm.containsValue(ontologyTermSynonym))
		// {
		// if (validateOntologyTerm(uniqueTerms, ontologyTermSynonym, stemmer,
		// positionFilter)
		// && !addedCandidates.contains(ontologyTermSynonym))
		// mapUriTerm.put(data.get("ontologyTermIRI")
		// .toString(), data);
		// }
		// }

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

	private boolean validateOntologyTerm(List<String> uniqueSets, String ontologyTermSynonym, PorterStemmer stemmer,
			Set<String> positionFilter)
	{
		Set<String> termsFromDescription = new HashSet<String>(Arrays.asList(ontologyTermSynonym.split(" +")));
		Set<String> stemmedWords = new HashSet<String>();
		for (String eachTerm : termsFromDescription)
		{
			stemmer.setCurrent(eachTerm);
			stemmer.stem();
			eachTerm = stemmer.getCurrent();
			stemmedWords.add(eachTerm);
			if (!uniqueSets.contains(eachTerm)) return false;
		}

		for (String eachTerm : stemmedWords)
		{
			if (positionFilter.contains(eachTerm)) return false;
			else positionFilter.add(eachTerm);
		}
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

	class TermComparison implements Comparable<TermComparison>
	{
		private final Hit hit;
		private final Integer synonymLength;
		private final Integer termLength;

		public TermComparison(Hit hit)
		{
			Map<String, Object> data = hit.getColumnValueMap();
			String ontologyTermSynonym = data.get("ontologyTermSynonym").toString().toLowerCase();
			String ontologyTerm = data.get("ontologyTerm").toString().toLowerCase();
			this.hit = hit;
			this.synonymLength = ontologyTermSynonym.split(" +").length;
			this.termLength = ontologyTerm.split(" +").length;
		}

		private Integer getSynonymLength()
		{
			return synonymLength;
		}

		private Integer getTermLength()
		{
			return termLength;
		}

		public Hit getHit()
		{
			return hit;
		}

		@Override
		public int compareTo(TermComparison other)
		{
			if (synonymLength.compareTo(other.getSynonymLength()) == 0)
			{
				return this.termLength.compareTo(other.getTermLength());
			}
			else return this.synonymLength.compareTo(other.getSynonymLength()) * (-1);
		}
	}
}