package org.molgenis.omx.ontologyMatcher.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.omx.ontologyIndexer.table.StoreMappingTable;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.DatabaseUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.tartarus.snowball.ext.PorterStemmer;

public class AsyncOntologyMatcher implements OntologyMatcher, InitializingBean
{
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private static final String CATALOGUE_PREFIX = "protocolTree-";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGY_IRI = "ontologyTermIRI";
	private static final String ENTITY_TYPE = "type";
	private final AtomicInteger runningProcesses = new AtomicInteger();
	private int totalNumber = 0;
	private int finishedNumber = 0;

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

	public boolean isRunning()
	{
		if (runningProcesses.get() == 0) return false;
		return true;
	}

	@Override
	public void matchPercentage()
	{
		System.out.println("Total number of variables is " + totalNumber + ". The number of finished variables is "
				+ finishedNumber);
	}

	public void deleteDocumentByIds(String documentType, List<String> documentIds)
	{
		searchService.deleteDocumentByIds(documentType, documentIds);
	}

	@Async
	public void match(Integer selectedDataSet, List<Integer> dataSetsToMatch) throws DatabaseException
	{
		runningProcesses.incrementAndGet();
		Database db = DatabaseUtil.createDatabase();

		try
		{
			db.beginTx();
			// Initialize the protocol and features that are used to store
			// mappings
			createMappingStore(selectedDataSet, dataSetsToMatch, db);

			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule(ENTITY_TYPE, Operator.SEARCH, ObservableFeature.class.getSimpleName()
					.toLowerCase()));
			queryRules.add(new QueryRule(Operator.LIMIT, 100000));

			SearchRequest request = new SearchRequest(CATALOGUE_PREFIX + selectedDataSet, queryRules, null);
			SearchResult result = searchService.search(request);

			for (Integer catalogueId : dataSetsToMatch)
			{
				StringBuilder dataSetIdentifier = new StringBuilder();
				dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
				deleteExistingRecords(dataSetIdentifier.toString(), db);
			}

			PorterStemmer stemmer = new PorterStemmer();

			List<Hit> listOfHits = result.getSearchHits();

			totalNumber = listOfHits.size();

			List<ObservationSet> listOfNewObservationSets = new ArrayList<ObservationSet>();
			List<ObservedValue> listOfNewObservedValues = new ArrayList<ObservedValue>();

			for (Hit hit : listOfHits)
			{
				Integer featureId = Integer.parseInt(hit.getColumnValueMap().get(ObservableFeature.ID.toString())
						.toString());
				String name = hit.getColumnValueMap().get(ObservableFeature.NAME.toLowerCase()).toString();
				String description = hit.getColumnValueMap().get(ObservableFeature.DESCRIPTION.toLowerCase())
						.toString();
				ObservableFeature feature = db.findById(ObservableFeature.class, featureId);

				if (feature != null)
				{
					List<QueryRule> rules = new ArrayList<QueryRule>();
					rules.add(makeQueryForOntologyTerms(createQueryRules(name, feature.getDefinition(), stemmer)));
					rules.add(makeQueryForOntologyTerms(createQueryRules(description, feature.getDefinition(), stemmer)));
					rules.add(makeQueryForName(name.toLowerCase()));
					rules.add(makeQueryForName(description.toLowerCase()));
					QueryRule finalQuery = new QueryRule(rules);
					finalQuery.setOperator(Operator.DIS_MAX);

					for (Integer catalogueId : dataSetsToMatch)
					{
						StringBuilder dataSetIdentifier = new StringBuilder();
						dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
						List<Integer> mappedFeatureIds = searchDisMaxQuery(CATALOGUE_PREFIX + catalogueId, finalQuery);

						for (Integer mappedId : mappedFeatureIds)
						{
							ObservationSet observation = new ObservationSet();
							observation.setPartOfDataSet_Identifier(dataSetIdentifier.toString());
							listOfNewObservationSets.add(observation);

							XrefValue xrefForFeature = new XrefValue();
							xrefForFeature.setValue(db.findById(Characteristic.class, featureId));
							ObservedValue valueForFeature = new ObservedValue();
							valueForFeature.setObservationSet(observation);
							valueForFeature.setFeature_Identifier(STORE_MAPPING_FEATURE);
							valueForFeature.setValue(xrefForFeature);
							listOfNewObservedValues.add(valueForFeature);

							XrefValue xrefForMappedFeature = new XrefValue();
							xrefForMappedFeature.setValue(db.findById(Characteristic.class, mappedId));
							ObservedValue valueForMappedFeature = new ObservedValue();
							valueForMappedFeature.setFeature_Identifier(STORE_MAPPING_MAPPED_FEATURE);
							valueForMappedFeature.setObservationSet(observation);
							valueForMappedFeature.setValue(xrefForMappedFeature);
							listOfNewObservedValues.add(valueForMappedFeature);

							BoolValue boolValue = new BoolValue();
							boolValue.setValue(false);
							ObservedValue confirmMappingValue = new ObservedValue();
							confirmMappingValue.setFeature_Identifier(STORE_MAPPING_CONFIRM_MAPPING);
							confirmMappingValue.setObservationSet(observation);
							confirmMappingValue.setValue(boolValue);
							listOfNewObservedValues.add(confirmMappingValue);
						}
					}

					finishedNumber++;
				}
			}

			db.add(listOfNewObservationSets);
			db.add(listOfNewObservedValues);

			for (Integer catalogueId : dataSetsToMatch)
			{
				StringBuilder dataSetIdentifier = new StringBuilder();
				dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
				searchService.indexTupleTable(dataSetIdentifier.toString(),
						new StoreMappingTable(dataSetIdentifier.toString(), db));
			}
			db.commitTx();
		}
		catch (Exception e)
		{
			db.rollbackTx();
			e.printStackTrace();
		}
		finally
		{
			runningProcesses.decrementAndGet();
			totalNumber = 0;
			finishedNumber = 0;
			DatabaseUtil.closeQuietly(db);
		}
	}

	private QueryRule makeQueryForName(String name)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();
		rules.add(new QueryRule(ObservableFeature.NAME.toLowerCase(), Operator.SEARCH, name));
		rules.add(new QueryRule(ObservableFeature.DESCRIPTION.toLowerCase(), Operator.SEARCH, name));
		QueryRule finalQuery = new QueryRule(rules);
		finalQuery.setOperator(Operator.DIS_MAX);
		return finalQuery;
	}

	private QueryRule makeQueryForOntologyTerms(Map<Integer, List<String>> nameTokens)
	{
		List<QueryRule> subQueries = new ArrayList<QueryRule>();
		for (List<String> terms : nameTokens.values())
		{
			List<QueryRule> rules = new ArrayList<QueryRule>();
			for (String term : terms)
			{
				rules.add(new QueryRule(ObservableFeature.NAME.toLowerCase(), Operator.SEARCH, term));
				rules.add(new QueryRule(ObservableFeature.DESCRIPTION.toLowerCase(), Operator.SEARCH, term));
			}
			QueryRule queryRule = new QueryRule(rules);
			queryRule.setOperator(Operator.DIS_MAX);
			subQueries.add(queryRule);
		}
		QueryRule finalQuery = new QueryRule(subQueries);
		finalQuery.setOperator(Operator.SHOULD);
		return finalQuery;
	}

	private List<Integer> searchDisMaxQuery(String documentType, QueryRule disMaxQuery)
	{
		List<Integer> featureIds = new ArrayList<Integer>();
		List<QueryRule> finalQuery = new ArrayList<QueryRule>();
		finalQuery.add(new QueryRule(Operator.LIMIT, 50));
		finalQuery.add(disMaxQuery);
		SearchRequest request = new SearchRequest(documentType, finalQuery, null);
		SearchResult result = searchService.search(request);
		Iterator<Hit> iterator = result.iterator();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			Map<String, Object> data = hit.getColumnValueMap();
			Integer id = Integer.parseInt(data.get(ObservableFeature.ID).toString());
			featureIds.add(id);
		}
		return featureIds;
	}

	private Map<Integer, List<String>> createQueryRules(String dataItem, List<OntologyTerm> definitions,
			PorterStemmer stemmer)
	{
		List<String> uniqueTokens = new ArrayList<String>();

		for (String token : Arrays.asList(dataItem.split(" +")))
		{
			stemmer.setCurrent(token);
			stemmer.stem();
			token = stemmer.getCurrent();
			if (!uniqueTokens.contains(token.toLowerCase())) uniqueTokens.add(token.toLowerCase());
		}

		Map<Integer, List<String>> position = new HashMap<Integer, List<String>>();

		for (OntologyTerm ot : definitions)
		{
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule(ONTOLOGY_IRI, Operator.EQUALS, ot.getTermAccession()));
			queryRules.add(new QueryRule(Operator.LIMIT, 100));
			SearchRequest request = new SearchRequest(null, queryRules, null);
			SearchResult result = searchService.search(request);
			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				String ontologyTermSynonym = hit.getColumnValueMap().get(ONTOLOGYTERM_SYNONYM).toString().toLowerCase();
				stemmer.setCurrent(ontologyTermSynonym.split(" +")[0]);
				stemmer.stem();
				ontologyTermSynonym = stemmer.getCurrent();
				int index = uniqueTokens.indexOf(ontologyTermSynonym);
				if (index != -1)
				{
					List<String> terms = null;
					if (!position.containsKey(index)) terms = new ArrayList<String>();
					else terms = position.get(index);
					if (!terms.contains(ontologyTermSynonym)) terms.add(ontologyTermSynonym);
					position.put(index, terms);
				}
			}
		}
		return position;
	}

	private void deleteExistingRecords(String dataSetIdentifier, Database db) throws DatabaseException
	{
		List<ObservationSet> listOfObservationSets = db.find(ObservationSet.class, new QueryRule(
				ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.EQUALS, dataSetIdentifier));

		if (listOfObservationSets.size() > 0)
		{
			List<Integer> listOfObservationIds = new ArrayList<Integer>();
			for (ObservationSet observation : listOfObservationSets)
			{
				listOfObservationIds.add(observation.getId());
			}
			List<ObservedValue> listOfObservedValues = db.find(ObservedValue.class, new QueryRule(
					ObservedValue.OBSERVATIONSET_ID, Operator.IN, listOfObservationIds));
			if (listOfObservedValues.size() > 0) db.remove(listOfObservedValues);
			db.remove(listOfObservationSets);
		}
	}

	private void createMappingStore(Integer selectedDataSet, List<Integer> dataSetsToMatch, Database db)
			throws DatabaseException
	{
		boolean isFeatureExists = db.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.IDENTIFIER, Operator.EQUALS, STORE_MAPPING_FEATURE)).size() == 0;
		if (isFeatureExists)
		{
			ObservableFeature feature = new ObservableFeature();
			feature.setIdentifier(STORE_MAPPING_FEATURE);
			feature.setDataType("xref");
			feature.setName("Features");
			db.add(feature);
		}

		boolean isMappedFeatureExists = db.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.IDENTIFIER, Operator.EQUALS, STORE_MAPPING_MAPPED_FEATURE)).size() == 0;
		if (isMappedFeatureExists)
		{
			ObservableFeature mappedFeature = new ObservableFeature();
			mappedFeature.setIdentifier(STORE_MAPPING_MAPPED_FEATURE);
			mappedFeature.setDataType("xref");
			mappedFeature.setName("Mapped features");
			db.add(mappedFeature);
		}

		boolean isConfirmMappingExists = db.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.IDENTIFIER, Operator.EQUALS, STORE_MAPPING_CONFIRM_MAPPING)).size() == 0;
		if (isConfirmMappingExists)
		{
			ObservableFeature confirmMapping = new ObservableFeature();
			confirmMapping.setIdentifier(STORE_MAPPING_CONFIRM_MAPPING);
			confirmMapping.setDataType("bool");
			confirmMapping.setName("Mapping confirmed");
			db.add(confirmMapping);
		}

		boolean ifProtocolExists = db.find(Protocol.class,
				new QueryRule(Protocol.IDENTIFIER, Operator.EQUALS, PROTOCOL_IDENTIFIER)).size() == 0;
		if (ifProtocolExists)
		{
			Protocol protocol = new Protocol();
			protocol.setIdentifier("store_mapping");
			protocol.setName("store_mapping");
			protocol.setFeatures_Identifier(Arrays.asList(STORE_MAPPING_FEATURE, STORE_MAPPING_MAPPED_FEATURE,
					STORE_MAPPING_CONFIRM_MAPPING));
			db.add(protocol);
		}

		for (Integer dataSetId : dataSetsToMatch)
		{
			String identifier = selectedDataSet + "-" + dataSetId;
			boolean ifDataSetExists = db.find(DataSet.class,
					new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, identifier)).size() == 0;
			if (ifDataSetExists)
			{
				DataSet dataSet = new DataSet();
				dataSet.setIdentifier(identifier);
				dataSet.setName(identifier);
				dataSet.setProtocolUsed_Identifier(PROTOCOL_IDENTIFIER);
				db.add(dataSet);
			}
		}
	}
}