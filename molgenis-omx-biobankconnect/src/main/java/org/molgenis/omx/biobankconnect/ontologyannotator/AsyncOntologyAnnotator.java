package org.molgenis.omx.biobankconnect.ontologyannotator;

import java.io.File;
import java.io.IOException;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.protocol.CategoryRepository;
import org.molgenis.omx.protocol.ProtocolTreeRepository;
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
	public String uploadFeatures(File uploadFile, String datasetName) throws IOException
	{
		CsvRepository csvRepository = null;
		try
		{
			boolean existingDataSet = dataService.findOne(DataSet.ENTITY_NAME,
					new QueryImpl().eq(DataSet.IDENTIFIER, datasetName + "_dataset" + "_identifier")) != null;
			if (existingDataSet) return "the dataset name has existed";

			// load the features into memory
			List<CellProcessor> cellProcessors = Arrays.<CellProcessor> asList(new TrimProcessor(),
					new LowerCaseProcessor(true, false));
			csvRepository = new CsvRepository(uploadFile, cellProcessors);
			List<String> requiredColumns = new ArrayList<String>(Arrays.asList(ObservableFeature.NAME.toLowerCase(),
					ObservableFeature.DESCRIPTION));
			Iterator<AttributeMetaData> columnNamesIterator = csvRepository.getAttributes().iterator();
			while (columnNamesIterator.hasNext())
			{
				requiredColumns.remove(columnNamesIterator.next().getName());
			}
			if (requiredColumns.size() > 0) return "The header(s) " + requiredColumns.toString() + " is missing";

			List<String> featureIdentifiers = new ArrayList<String>();
			List<ObservableFeature> fList = new ArrayList<ObservableFeature>();
			Iterator<org.molgenis.data.Entity> entityIterator = csvRepository.iterator();
			while (entityIterator.hasNext())
			{
				org.molgenis.data.Entity t = entityIterator.next();
				ObservableFeature f = new ObservableFeature();
				f.setName(t.getString(ObservableFeature.NAME.toLowerCase()));
				f.setDescription(t.getString(ObservableFeature.DESCRIPTION));
				f.setIdentifier(datasetName + "_" + f.getName() + "_identifier");
				featureIdentifiers.add(f.getIdentifier());
				fList.add(f);
			}

			if (featureIdentifiers.size() == 0) return "Please check the uploaded file, there are no features in the file!";
			List<String> checkExistingFeatures = checkExistingFeatures(featureIdentifiers);
			if (checkExistingFeatures.size() > 0) return "The features : " + checkExistingFeatures
					+ " exist in the database already! Please remove them from uploaded file!";

			// create protocol and link the features
			Protocol prot = new Protocol();
			prot.setName(datasetName + "_protocol");
			prot.setIdentifier(datasetName + "_protocol" + "_identifier");
			prot.setFeatures(fList);

			// create dataset
			DataSet dataSet = new DataSet();
			dataSet.setName(datasetName);
			dataSet.setIdentifier(datasetName + "_dataset" + "_identifier");
			dataSet.setProtocolUsed(prot);

			dataService.add(ObservableFeature.ENTITY_NAME, fList);
			dataService.add(Protocol.ENTITY_NAME, prot);
			dataService.add(DataSet.ENTITY_NAME, dataSet);

			searchService.indexRepository(new ProtocolTreeRepository(dataSet.getProtocolUsed(), dataService,
					"protocolTree-" + dataSet.getId()));
			searchService.indexRepository(new CategoryRepository(dataSet.getProtocolUsed(), dataSet.getId(),
					dataService));
		}
		catch (IOException e)
		{
			logger.error("Failed to read CSV file!");
			return "Failed to import the features, please check your file again.";
		}
		finally
		{
			if (csvRepository != null) csvRepository.close();
		}

		return "";
	}

	private List<String> checkExistingFeatures(List<String> featureIdentifiers)
	{
		List<String> existingFeatures = new ArrayList<String>();
		if (featureIdentifiers.size() > 0)
		{
			Iterable<ObservableFeature> features = dataService.findAll(ObservableFeature.ENTITY_NAME,
					new QueryImpl().in(ObservableFeature.IDENTIFIER, new ArrayList<String>(featureIdentifiers)),
					ObservableFeature.class);

			for (ObservableFeature feature : features)
			{
				existingFeatures.add(feature.getName());
			}
		}
		return existingFeatures;
	}

	@Override
	@Transactional
	public void removeAnnotations(Integer dataSetId)
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, dataSetId, DataSet.class);

		QueryImpl q = new QueryImpl();
		q.pageSize(100000);
		q.addRule(new QueryRule("type", Operator.SEARCH, "observablefeature"));

		SearchRequest request = new SearchRequest("protocolTree-" + dataSet.getId(), q, null);
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

		if (!listOfFeatureIds.isEmpty())
		{
			Iterable<ObservableFeature> features = dataService.findAll(ObservableFeature.ENTITY_NAME,
					new QueryImpl().in(ObservableFeature.ID, listOfFeatureIds), ObservableFeature.class);
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
		}

		if (!featuresToUpdate.isEmpty())
		{
			dataService.update(ObservableFeature.ENTITY_NAME, featuresToUpdate);
		}
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

			QueryImpl q = new QueryImpl();
			q.pageSize(100000);
			q.addRule(new QueryRule("type", Operator.SEARCH, "observablefeature"));
			SearchRequest request = new SearchRequest("protocolTree-" + dataSetId, q, null);
			SearchResult result = searchService.search(request);

			List<ObservableFeature> featuresToUpdate = new ArrayList<ObservableFeature>();

			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Integer featureId = Integer.parseInt(hit.getColumnValueMap().get("id").toString());

				ObservableFeature f = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId,
						ObservableFeature.class);
				ObservableFeature feature = toObservableFeature(f);

				String name = hit.getColumnValueMap().get("name").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				String description = hit.getColumnValueMap().get("description").toString().toLowerCase()
						.replaceAll("[^(a-zA-Z0-9\\s)]", "").trim();
				List<OntologyTerm> definitions = new ArrayList<OntologyTerm>();

				for (String documentType : documentTypes)
				{
					addIfNotExists(definitions, (annotateDataItem(documentType, feature, name, stemmer)));
					addIfNotExists(definitions, (annotateDataItem(documentType, feature, description, stemmer)));
				}
				addIfNotExists(definitions, feature.getDefinitions());

				feature.setDefinitions(definitions);
				featuresToUpdate.add(feature);
			}
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

		QueryImpl q = new QueryImpl();
		q.pageSize(100000);
		q.addRule(new QueryRule("entity_type", Operator.SEARCH, "indexedOntology"));

		SearchResult result = searchService.search(new SearchRequest(null, q, null));
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

	public List<OntologyTerm> annotateDataItem(String documentType, ObservableFeature feature, String description,
			PorterStemmer stemmer)
	{
		Set<String> uniqueTerms = new HashSet<String>();
		for (String eachTerm : Arrays.asList(description.split(" +")))
		{
			eachTerm = eachTerm.toLowerCase();
			if (!NGramMatchingModel.STOPWORDSLIST.contains(eachTerm) && !uniqueTerms.contains(eachTerm)) uniqueTerms
					.add(eachTerm);
		}

		QueryImpl q = new QueryImpl();
		q.pageSize(100);

		boolean first = true;
		for (String term : uniqueTerms)
		{
			if (!term.isEmpty() && !term.matches(" +"))
			{
				if (!first)
				{
					q.addRule(new QueryRule(Operator.OR));
				}
				term = term.replaceAll("[^(a-zA-Z0-9 )]", "");
				q.addRule(new QueryRule("ontologyTermSynonym", Operator.SEARCH, term));
				first = false;
			}
		}

		SearchRequest request = new SearchRequest(documentType, q, null);
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
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			String ontologyTermSynonym = columnValueMap.get("ontologyTermSynonym").toString().toLowerCase();
			String ontologyTerm = columnValueMap.get("ontologyTerm").toString().toLowerCase();
			if (ontologyTerm.equals(ontologyTermSynonym) || !addedCandidates.contains(ontologyTermSynonym))
			{
				if (validateOntologyTerm(uniqueTerms, ontologyTermSynonym, stemmer, positionFilter))
				{
					String uri = columnValueMap.get("ontologyTermIRI").toString();
					String ontologyLabel = columnValueMap.get("ontologyLabel").toString();
					String termIdentifier = ontologyLabel == null ? uri : ontologyLabel + ":" + uri;
					mapUriTerm.put(termIdentifier, columnValueMap);
					addedCandidates.add(ontologyTermSynonym);
				}
			}
		}

		List<String> identifiers = new ArrayList<String>();

		if (feature.getDefinitions() != null)
		{
			for (OntologyTerm ot : feature.getDefinitions())
			{
				identifiers.add(ot.getIdentifier());
			}
		}

		for (String uri : mapUriTerm.keySet())
		{

			if (!identifiers.contains(uri))
			{
				identifiers.add(uri);
			}
		}

		if (mapUriTerm.size() > 0)
		{
			Iterable<OntologyTerm> ots = dataService.findAll(OntologyTerm.ENTITY_NAME,
					new QueryImpl().in(OntologyTerm.IDENTIFIER, new ArrayList<String>(mapUriTerm.keySet())),
					OntologyTerm.class);

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

			String ontologyLabel = data.get("ontologyLabel") == null ? StringUtils.EMPTY : data.get("ontologyLabel")
					.toString();
			String ontologyTermSynonym = data.get("ontologyTermSynonym").toString();
			String term = ontologyLabel.isEmpty() ? ontologyTermSynonym.toLowerCase() : ontologyLabel + ":"
					+ ontologyTermSynonym.toLowerCase();
			String termIdentifier = ontologyLabel.isEmpty() ? uri : ontologyLabel + ":" + uri;
			OntologyTerm ot = new OntologyTerm();
			ot.setIdentifier(termIdentifier);
			ot.setTermAccession(uri);
			ot.setName(term);
			ot.setDefinition(ontologyTermSynonym);

			Ontology ontology = dataService.findOne(Ontology.ENTITY_NAME,
					new QueryImpl().eq(Ontology.IDENTIFIER, ontologyUri), Ontology.class);

			ot.setOntology(ontology);

			listOfOntologyTerms.add(ot);
		}
		if (listOfOntologyTerms.size() > 0) addOntologies(ontologyInfo);
		if (listOfOntologyTerms.size() > 0) dataService.add(OntologyTerm.ENTITY_NAME, listOfOntologyTerms);

		if (identifiers.isEmpty()) return Collections.emptyList();
		Iterable<OntologyTerm> definitions = dataService.findAll(OntologyTerm.ENTITY_NAME,
				new QueryImpl().in(OntologyTerm.IDENTIFIER, identifiers), OntologyTerm.class);

		return Lists.newArrayList(definitions);
	}

	private void addOntologies(Map<String, String> ontologyInfo)
	{
		List<String> ontologyUris = new ArrayList<String>();
		List<Ontology> listOfOntologies = new ArrayList<Ontology>();

		Iterable<Ontology> ontologies = dataService.findAll(Ontology.ENTITY_NAME,
				new QueryImpl().in(Ontology.ONTOLOGYURI, new ArrayList<String>(ontologyInfo.keySet())), Ontology.class);

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