package org.molgenis.omx.biobankconnect.ontologyannotator;

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
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
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
import org.springframework.transaction.annotation.Transactional;
import org.tartarus.snowball.ext.PorterStemmer;

public class AsyncOntologyAnnotator implements OntologyAnnotator, InitializingBean
{
	@Autowired
	private DataService dataService;

	private SearchService searchService;

	private static final AtomicInteger runningProcesses = new AtomicInteger();
	private static final Logger logger = Logger.getLogger(AsyncOntologyAnnotator.class);
	private boolean complete = false;

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Override
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
	@Transactional
	public void removeAnnotations(Integer dataSetId)
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, dataSetId);
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

		Iterable<ObservableFeature> features = dataService.findAll(ObservableFeature.ENTITY_NAME,
				new QueryImpl().in(ObservableFeature.ID, listOfFeatureIds));
		for (ObservableFeature feature : features)
		{
			List<OntologyTerm> definitions = feature.getDefinitions();
			if (definitions != null && definitions.size() > 0)
			{
				ObservableFeature newFeature = copyObject(feature);
				newFeature.setDefinitions(new ArrayList<OntologyTerm>());
				featuresToUpdate.add(newFeature);
			}
		}
		dataService.update(ObservableFeature.ENTITY_NAME, featuresToUpdate);

	}

	public ObservableFeature copyObject(ObservableFeature feature)
	{
		ObservableFeature newFeature = new ObservableFeature();
		for (String field : feature.getAttributeNames())
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

				ObservableFeature f = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId);
				ObservableFeature feature = toObservableFeature(f);

				String name = hit.getColumnValueMap().get("name").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				String description = hit.getColumnValueMap().get("description").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				List<OntologyTerm> definitions = new ArrayList<OntologyTerm>();

				for (String documentType : documentTypes)
				{
					addIfNotExists(definitions, (annotateDataItem(dataService, documentType, feature, name, stemmer)));
					addIfNotExists(definitions,
							(annotateDataItem(dataService, documentType, feature, description, stemmer)));
				}
				addIfNotExists(definitions, feature.getDefinitions());

				feature.setDefinitions(definitions);
				featuresToUpdate.add(feature);
			}
			System.out.println(featuresToUpdate);
			dataService.update(ObservableFeature.ENTITY_NAME, featuresToUpdate);
		}
		finally
		{
			runningProcesses.decrementAndGet();
			complete = true;
		}
	}

	private void addIfNotExists(List<OntologyTerm> existing, List<OntologyTerm> toAdd)
	{
		for (OntologyTerm ot : toAdd)
		{
			if (!contains(existing, ot))
			{
				existing.add(ot);
			}
		}
	}

	private boolean contains(List<OntologyTerm> ontologyTerms, OntologyTerm ontologyTerm)
	{
		for (OntologyTerm ot : ontologyTerms)
		{
			if (ot.getIdentifier().equals(ontologyTerm.getIdentifier()))
			{
				return true;
			}
		}

		return false;
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

	private ObservableFeature toObservableFeature(ObservableFeature feature)
	{
		ObservableFeature newFeature = new ObservableFeature();
		for (String field : feature.getAttributeNames())
			newFeature.set(field, feature.get(field));
		return newFeature;
	}

	public List<OntologyTerm> annotateDataItem(DataService dataService, String documentType, ObservableFeature feature,
			String description, PorterStemmer stemmer)
	{
		Set<String> uniqueTerms = new HashSet<String>();
		for (String eachTerm : Arrays.asList(description.split(" +")))
		{
			eachTerm = eachTerm.toLowerCase();
			if (!NGramMatchingModel.STOPWORDSLIST.contains(eachTerm) && !uniqueTerms.contains(eachTerm)) uniqueTerms
					.add(eachTerm);
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

		uniqueTerms = stemMembers(new ArrayList<String>(uniqueTerms), stemmer);
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
					String uri = data.get("ontologyTermIRI").toString();
					String ontologyLabel = data.get("ontologyLabel").toString();
					String termIdentifier = ontologyLabel == null ? uri : ontologyLabel + ":" + uri;
					mapUriTerm.put(termIdentifier, data);
					addedCandidates.add(ontologyTermSynonym);
				}
			}
		}

		List<String> identifiers = new ArrayList<String>();

		// List<OntologyTerm> definitions = new ArrayList<OntologyTerm>();
		if (feature.getDefinitions() != null)
		{
			for (OntologyTerm ot : feature.getDefinitions())
			{
				identifiers.add(ot.getIdentifier());
			}
		}

		// definitions.addAll(feature.getDefinitions());
		for (String uri : mapUriTerm.keySet())
		{

			if (!identifiers.contains(uri))
			{
				identifiers.add(uri);
			}

			// if (!definitions.contains(definition))
			// {
			// definitions.add(definition);
			// }
		}

		if (mapUriTerm.size() > 0)
		{
			Iterable<OntologyTerm> ots = dataService.findAll(OntologyTerm.ENTITY_NAME,
					new QueryImpl().in(OntologyTerm.IDENTIFIER, new ArrayList<String>(mapUriTerm.keySet())));

			for (OntologyTerm ot : ots)
				mapUriTerm.remove(ot.getIdentifier());
		}

		List<OntologyTerm> listOfOntologyTerms = new ArrayList<OntologyTerm>();
		Map<String, String> ontologyInfo = new HashMap<String, String>();

		for (Map<String, Object> data : mapUriTerm.values())
		{
			String uri = data.get("ontologyTermIRI").toString();
			String ontologyUri = data.get("ontologyIRI").toString();
			String ontologyName = data.get("ontologyName").toString();
			ontologyInfo.put(ontologyUri, ontologyName);

			String ontologyLabel = data.get("ontologyLabel").toString();
			String oontologyTermSynonym = data.get("ontologyTermSynonym").toString();
			String term = ontologyLabel == null ? oontologyTermSynonym.toLowerCase() : ontologyLabel + ":"
					+ oontologyTermSynonym.toLowerCase();
			String termIdentifier = ontologyLabel == null ? uri : ontologyLabel + ":" + uri;
			OntologyTerm ot = new OntologyTerm();
			ot.setIdentifier(termIdentifier);
			ot.setTermAccession(uri);
			ot.setName(term);
			ot.setDefinition(oontologyTermSynonym);

			Ontology ontology = dataService.findOne(Ontology.ENTITY_NAME,
					new QueryImpl().eq(Ontology.IDENTIFIER, ontologyUri));

			ot.setOntology(ontology);

			listOfOntologyTerms.add(ot);
		}
		if (listOfOntologyTerms.size() > 0) addOntologies(ontologyInfo);
		if (listOfOntologyTerms.size() > 0) dataService.add(OntologyTerm.ENTITY_NAME, listOfOntologyTerms);

		if (identifiers.isEmpty())
		{
			return Collections.emptyList();
		}

		System.out.println(identifiers);
		List<OntologyTerm> definitions = dataService.findAllAsList(OntologyTerm.ENTITY_NAME,
				new QueryImpl().in(OntologyTerm.IDENTIFIER, identifiers));

		return definitions;
	}

	private void addOntologies(Map<String, String> ontologyInfo)
	{
		List<String> ontologyUris = new ArrayList<String>();
		List<Ontology> listOfOntologies = new ArrayList<Ontology>();

		Iterable<Ontology> ontologies = dataService.findAll(Ontology.ENTITY_NAME,
				new QueryImpl().in(Ontology.ONTOLOGYURI, new ArrayList<String>(ontologyInfo.keySet())));

		for (Ontology ontology : ontologies)
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
		if (listOfOntologies.size() != 0) dataService.add(Ontology.ENTITY_NAME, listOfOntologies);
	}

	private boolean validateOntologyTerm(Set<String> uniqueSets, String ontologyTermSynonym, PorterStemmer stemmer,
			Set<String> positionFilter)
	{
		Set<String> termsFromDescription = stemMembers(Arrays.asList(ontologyTermSynonym.split(" +")), stemmer);
		for (String eachTerm : termsFromDescription)
		{
			if (!uniqueSets.contains(eachTerm)) return false;
		}

		for (String eachTerm : termsFromDescription)
		{
			if (positionFilter.contains(eachTerm)) return false;
			else positionFilter.add(eachTerm);
		}
		return true;
	}

	private Set<String> stemMembers(List<String> originalList, PorterStemmer stemmer)
	{
		Set<String> newList = new HashSet<String>();
		for (String eachTerm : originalList)
		{
			stemmer.setCurrent(eachTerm);
			stemmer.stem();
			eachTerm = stemmer.getCurrent();
			newList.add(eachTerm);
		}
		return newList;
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