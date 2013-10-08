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

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tartarus.snowball.ext.PorterStemmer;

public class AsyncOntologyAnnotator implements OntologyAnnotator, InitializingBean
{
	@Autowired
	@Qualifier("unsecuredDatabase")
	private Database unsecuredDatabase;

	private SearchService searchService;

	private static final AtomicInteger runningProcesses = new AtomicInteger();
	private static final Logger logger = Logger.getLogger(AsyncOntologyAnnotator.class);
	private static boolean complete = false;
	// TODO : solve this guy
	public static final Set<String> STOPWORDSLIST;
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

	@Override
	public boolean isRunning()
	{
		if (runningProcesses.get() == 0) return false;
		return true;
	}

	@Override
	public boolean isComplete()
	{
		return complete;
	}

	@Override
	public void initComplete()
	{
		complete = false;
	}

	@Override
	public void removeAnnotations(Integer dataSetId, List<String> documentTypes)
	{
		try
		{
			DataSet dataSet = unsecuredDatabase.findById(DataSet.class, dataSetId);
			// Protocol protocol = dataSet.getProtocolUsed();
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule("type", Operator.SEARCH, "observablefeature"));
			queryRules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchRequest request = new SearchRequest("protocolTree-" + dataSet.getId(), queryRules, null);
			SearchResult result = searchService.search(request);

			List<Integer> listOfFeatureIds = new ArrayList<Integer>();
			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Map<String, Object> columnMapValues = hit.getColumnValueMap();
				Integer featureId = Integer.parseInt(columnMapValues.get("id").toString());
				listOfFeatureIds.add(featureId);
			}
			List<ObservableFeature> featuresToUpdate = new ArrayList<ObservableFeature>();
			for (ObservableFeature feature : unsecuredDatabase.find(ObservableFeature.class, new QueryRule(
					ObservableFeature.ID, Operator.IN, listOfFeatureIds)))
			{
				List<String> ontologyTerms = new ArrayList<String>();
				List<OntologyTerm> definitions = feature.getDefinition();

				if (definitions != null && definitions.size() > 0)
				{
					for (OntologyTerm ontologyTerm : definitions)
					{
						if (!documentTypes.contains(ontologyTerm.getOntology().getOntologyURI()))
						{
							ontologyTerms.add(ontologyTerm.getIdentifier());
						}
					}
					if (ontologyTerms.size() != definitions.size())
					{
						ObservableFeature newFeature = copyObject(feature);
						newFeature.setDefinition_Identifier(ontologyTerms);
						featuresToUpdate.add(newFeature);
					}
				}
			}
			unsecuredDatabase.update(featuresToUpdate);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public ObservableFeature copyObject(ObservableFeature feature) throws Exception
	{
		ObservableFeature newFeature = new ObservableFeature();
		for (String field : feature.getFields())
		{
			newFeature.set(field, feature.get(field));
		}
		return newFeature;
	}

	@Override
	public void annotate(Integer dataSetId, List<String> documentTypes)
	{
		runningProcesses.incrementAndGet();
		try
		{
			if (documentTypes == null) documentTypes = searchAllOntologies();

			PorterStemmer stemmer = new PorterStemmer();
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule("type", Operator.SEARCH, "observablefeature"));
			queryRules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchRequest request = new SearchRequest("protocolTree-" + dataSetId, queryRules, null);
			SearchResult result = searchService.search(request);
			List<ObservableFeature> featuresToUpdate = new ArrayList<ObservableFeature>();

			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Integer featureId = Integer.parseInt(hit.getColumnValueMap().get("id").toString());
				ObservableFeature feature = toObservableFeature(unsecuredDatabase.findById(ObservableFeature.class,
						featureId));
				String name = hit.getColumnValueMap().get("name").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				String description = hit.getColumnValueMap().get("description").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				List<String> definitions = new ArrayList<String>();

				for (String documentType : documentTypes)
				{
					definitions.addAll(annotateDataItem(unsecuredDatabase, documentType, feature, name, stemmer));
					definitions
							.addAll(annotateDataItem(unsecuredDatabase, documentType, feature, description, stemmer));
				}

				if (definitions.size() > 0)
				{
					definitions.addAll(feature.getDefinition_Identifier());
					feature.setDefinition_Identifier(definitions);
				}
				featuresToUpdate.add(feature);
			}

			unsecuredDatabase.update(featuresToUpdate);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			runningProcesses.decrementAndGet();
			complete = true;
		}
	}

	public List<String> searchAllOntologies()
	{
		List<String> ontologyUris = new ArrayList<String>();
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		queryRules.add(new QueryRule("entity_type", Operator.SEARCH, "indexedOntology"));
		queryRules.add(new QueryRule(Operator.LIMIT, 100000));
		SearchResult result = searchService.search(new SearchRequest(null, queryRules, null));
		for (Hit hit : result.getSearchHits())
		{
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			if (columnValueMap.containsKey("url")) ontologyUris.add("ontologyTerm-"
					+ columnValueMap.get("url").toString());
		}
		return ontologyUris;
	}

	@Override
	public void updateIndex(UpdateIndexRequest request)
	{
		try
		{
			for (String documentId : request.getDocumentIds())
			{
				searchService.updateDocumentById(request.getDocumentType(), documentId, request.getUpdateScript());
			}
		}
		catch (Exception e)
		{
			logger.error("Exception calling searchservice for request [" + request + "]", e);
		}
	}

	private ObservableFeature toObservableFeature(ObservableFeature feature) throws Exception
	{
		ObservableFeature newFeature = new ObservableFeature();
		for (String field : feature.getFields())
			newFeature.set(field, feature.get(field));
		return newFeature;
	}

	public List<String> annotateDataItem(Database db, String documentType, ObservableFeature feature,
			String description, PorterStemmer stemmer) throws DatabaseException
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

		SearchRequest request = new SearchRequest(documentType, queryRules, null);
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
		Map<String, String> ontologyInfo = new HashMap<String, String>();

		for (Entry<String, Map<String, Object>> entry : mapUriTerm.entrySet())
		{
			String uri = entry.getKey();
			Map<String, Object> data = entry.getValue();

			String ontologyUri = data.get("ontologyIRI").toString();
			String ontologyName = data.get("ontologyName").toString();
			ontologyInfo.put(ontologyUri, ontologyName);

			String ontologyLabel = data.get("ontologyLabel").toString();
			String term = ontologyLabel == null ? data.get("ontologyTerm").toString().toLowerCase() : ontologyLabel
					+ ":" + data.get("ontologyTerm").toString().toLowerCase();
			OntologyTerm ot = new OntologyTerm();
			ot.setIdentifier(uri);
			ot.setTermAccession(uri);
			ot.setName(term);
			ot.setOntology_Identifier(ontologyUri);

			listOfOntologyTerms.add(ot);
		}
		if (listOfOntologyTerms.size() > 0) addOntologies(ontologyInfo);
		if (listOfOntologyTerms.size() > 0) db.add(listOfOntologyTerms);

		return identifiers;
	}

	private void addOntologies(Map<String, String> ontologyInfo) throws DatabaseException
	{
		List<String> ontologyUris = new ArrayList<String>();
		List<Ontology> listOfOntologies = new ArrayList<Ontology>();

		for (Ontology ontology : unsecuredDatabase.find(Ontology.class, new QueryRule(Ontology.ONTOLOGYURI,
				Operator.IN, new ArrayList<String>(ontologyInfo.keySet()))))
		{
			ontologyUris.add(ontology.getOntologyURI());
		}

		for (Entry<String, String> entry : ontologyInfo.entrySet())
		{
			String ontologyUri = entry.getKey();
			String ontologyName = entry.getValue();

			if (!ontologyUris.contains(ontologyUri))
			{
				Ontology ontology = new Ontology();
				ontology.setName(ontologyName);
				ontology.setIdentifier(ontologyUri);
				ontology.setOntologyURI(ontologyUri);
				listOfOntologies.add(ontology);
			}
		}
		if (listOfOntologies.size() != 0) unsecuredDatabase.add(listOfOntologies);
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

	@Override
	public float finishedPercentage()
	{
		return 0;
	}
}