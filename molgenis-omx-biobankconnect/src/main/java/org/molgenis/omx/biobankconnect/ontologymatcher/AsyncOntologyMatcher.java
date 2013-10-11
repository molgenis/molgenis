package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.text.DecimalFormat;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.biobankconnect.utils.StoreMappingTable;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyMatcher implements OntologyMatcher, InitializingBean
{
	private static final Logger logger = Logger.getLogger(AsyncOntologyMatcher.class);
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private static final String STORE_MAPPING_SCORE = "store_mapping_score";
	private static final String STORE_MAPPING_ABSOLUTE_SCORE = "store_mapping_absolute_score";
	private static final String CATALOGUE_PREFIX = "protocolTree-";
	private static final String BOOST = "boost";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String NODE_PATH = "nodePath";
	private static final String ENTITY_ID = "id";
	private static final String LUCENE_SCORE = "score";
	private static final String ENTITY_TYPE = "type";
	private static final AtomicInteger runningProcesses = new AtomicInteger();
	private static long totalNumber = 0;
	private static int finishedNumber = 0;

	@Autowired
	@Qualifier("unsecuredDatabase")
	private Database database;
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
	public Integer matchPercentage()
	{
		DecimalFormat df = new DecimalFormat("#.##");
		Double percentage = totalNumber == 0 ? new Double(0) : ((double) finishedNumber) / totalNumber;
		percentage = Double.parseDouble(df.format(percentage * 100));
		return percentage.intValue();
	}

	public void deleteDocumentByIds(String documentType, List<String> documentIds)
	{
		searchService.deleteDocumentByIds(documentType, documentIds);
	}

	private void preprocessing(Integer selectedDataSet, List<Integer> dataSetsToMatch, Database db)
			throws DatabaseException
	{
		createMappingStore(selectedDataSet, dataSetsToMatch, db);

		for (Integer catalogueId : dataSetsToMatch)
		{
			StringBuilder dataSetIdentifier = new StringBuilder();
			dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
			deleteExistingRecords(dataSetIdentifier.toString(), db);
		}
	}

	@Async
	public void match(Integer selectedDataSet, List<Integer> dataSetsToMatch) throws DatabaseException
	{
		runningProcesses.incrementAndGet();
		List<ObservationSet> listOfNewObservationSets = new ArrayList<ObservationSet>();
		List<ObservedValue> listOfNewObservedValues = new ArrayList<ObservedValue>();

		try
		{
			preprocessing(selectedDataSet, dataSetsToMatch, database);
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule(ENTITY_TYPE, Operator.SEARCH, ObservableFeature.class.getSimpleName()
					.toLowerCase()));
			queryRules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchResult result = searchService.search(new SearchRequest(CATALOGUE_PREFIX + selectedDataSet,
					queryRules, null));
			totalNumber = result.getTotalHitCount();

			for (Hit hit : result.getSearchHits())
			{
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				ObservableFeature feature = database.findById(ObservableFeature.class,
						columnValueMap.get(ObservableFeature.ID.toString()));
				if (feature != null)
				{
					Map<Integer, Set<String>> position = null;
					List<QueryRule> rules = new ArrayList<QueryRule>();
					String description = feature.getDescription() == null || feature.getDescription().isEmpty() ? feature
							.getName() : feature.getDescription();
					rules.add(new QueryRule(ObservableFeature.DESCRIPTION.toLowerCase(), Operator.SEARCH, description));
					if (feature.getDefinition() != null && feature.getDefinition().size() > 0)
					{
						position = createQueryRules(description, feature.getDefinition());
						rules.addAll(makeQueryForOntologyTerms(position));
					}
					QueryRule finalQuery = new QueryRule(rules);
					finalQuery.setOperator(Operator.DIS_MAX);

					for (Integer catalogueId : dataSetsToMatch)
					{
						StringBuilder dataSetIdentifier = new StringBuilder();
						dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
						Iterator<Hit> mappedFeatureHits = searchDisMaxQuery(CATALOGUE_PREFIX + catalogueId, finalQuery);

						while (mappedFeatureHits.hasNext())
						{
							Hit mappedFeatureHit = mappedFeatureHits.next();
							Map<String, Object> columValueMap = mappedFeatureHit.getColumnValueMap();
							Integer mappedId = Integer.parseInt(columValueMap.get(ENTITY_ID).toString());
							Double score = Double.parseDouble(columValueMap.get(LUCENE_SCORE).toString());

							ObservationSet observation = new ObservationSet();
							observation.setPartOfDataSet_Identifier(dataSetIdentifier.toString());
							listOfNewObservationSets.add(observation);

							XrefValue xrefForFeature = new XrefValue();
							xrefForFeature.setValue(database.findById(Characteristic.class, feature.getId()));
							ObservedValue valueForFeature = new ObservedValue();
							valueForFeature.setObservationSet(observation);
							valueForFeature.setFeature_Identifier(STORE_MAPPING_FEATURE);
							valueForFeature.setValue(xrefForFeature);
							listOfNewObservedValues.add(valueForFeature);

							XrefValue xrefForMappedFeature = new XrefValue();
							xrefForMappedFeature.setValue(database.findById(Characteristic.class, mappedId));
							ObservedValue valueForMappedFeature = new ObservedValue();
							valueForMappedFeature.setFeature_Identifier(STORE_MAPPING_MAPPED_FEATURE);
							valueForMappedFeature.setObservationSet(observation);
							valueForMappedFeature.setValue(xrefForMappedFeature);
							listOfNewObservedValues.add(valueForMappedFeature);

							DecimalValue decimalForScore = new DecimalValue();
							decimalForScore.setValue(score);
							ObservedValue valueForMappedFeatureScore = new ObservedValue();
							valueForMappedFeatureScore.setFeature_Identifier(STORE_MAPPING_SCORE);
							valueForMappedFeatureScore.setObservationSet(observation);
							valueForMappedFeatureScore.setValue(decimalForScore);
							listOfNewObservedValues.add(valueForMappedFeatureScore);

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

			database.add(listOfNewObservationSets);
			database.add(listOfNewObservedValues);

			for (Integer catalogueId : dataSetsToMatch)
			{
				StringBuilder dataSetIdentifier = new StringBuilder();
				dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
				searchService.indexTupleTable(dataSetIdentifier.toString(),
						new StoreMappingTable(dataSetIdentifier.toString(), database));
			}
		}
		catch (Exception e)
		{
			logger.error("Exception the matching process has failed!", e);
		}
		finally
		{
			runningProcesses.decrementAndGet();
			totalNumber = 0;
			finishedNumber = 0;
		}
	}

	private List<QueryRule> makeQueryForOntologyTerms(Map<Integer, Set<String>> nameTokens)
	{
		List<QueryRule> subQueries = new ArrayList<QueryRule>();
		List<QueryRule> allQueries = new ArrayList<QueryRule>();

		for (Entry<Integer, Set<String>> entries : nameTokens.entrySet())
		{
			List<QueryRule> rules = new ArrayList<QueryRule>();
			Integer index = entries.getKey();

			if (index != -1)
			{
				for (String term : entries.getValue())
				{
					rules.add(new QueryRule(ObservableFeature.DESCRIPTION.toLowerCase(), Operator.SEARCH, term));
				}
				QueryRule queryRule = new QueryRule(rules);
				queryRule.setOperator(Operator.DIS_MAX);
				subQueries.add(queryRule);
			}
			else
			{
				for (String term : entries.getValue())
				{
					allQueries.add(new QueryRule(ObservableFeature.DESCRIPTION.toLowerCase(), Operator.SEARCH, term));
				}
			}
		}
		QueryRule combinedQuery = new QueryRule(subQueries);
		combinedQuery.setOperator(Operator.SHOULD);
		allQueries.add(combinedQuery);

		return allQueries;
	}

	private Iterator<Hit> searchDisMaxQuery(String documentType, QueryRule disMaxQuery)
	{
		SearchResult result = null;
		try
		{
			List<QueryRule> finalQuery = new ArrayList<QueryRule>();
			finalQuery.add(new QueryRule(ENTITY_TYPE, Operator.EQUALS, ObservableFeature.class.getSimpleName()
					.toLowerCase()));
			finalQuery.add(new QueryRule(Operator.AND));
			finalQuery.add(disMaxQuery);
			finalQuery.add(new QueryRule(Operator.LIMIT, 50));
			SearchRequest request = new SearchRequest(documentType, finalQuery, null);
			result = searchService.search(request);
		}
		catch (Exception e)
		{
			result = new SearchResult(e.getMessage());
			logger.error("Exception failed to search the request " + result, e);
		}
		return result.iterator();
	}

	private Map<Integer, Set<String>> createQueryRules(String dataItem, List<OntologyTerm> definitions)
	{
		Map<Integer, Set<String>> position = new HashMap<Integer, Set<String>>();

		Map<String, Set<String>> pathToSynonyms = new HashMap<String, Set<String>>();
		Map<String, Boolean> allPaths = new HashMap<String, Boolean>();
		{
			List<QueryRule> rules = new ArrayList<QueryRule>();
			for (OntologyTerm ot : definitions)
			{
				if (rules.size() != 0) rules.add(new QueryRule(Operator.OR));
				rules.add(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, ot.getTermAccession()));
			}
			rules.add(new QueryRule(Operator.LIMIT, 10000));
			SearchRequest request = new SearchRequest(null, rules, null);
			SearchResult result = searchService.search(request);
			Iterator<Hit> iterator = result.iterator();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				Boolean boost = (Boolean) columnValueMap.get(BOOST);
				String nodePath = columnValueMap.get(NODE_PATH).toString();
				allPaths.put(nodePath, boost);
				if (!pathToSynonyms.containsKey(nodePath)) pathToSynonyms.put(nodePath, new HashSet<String>());
				pathToSynonyms.get(nodePath).add(
						columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString().trim().toLowerCase());
			}
		}

		for (Entry<String, Boolean> entry : allPaths.entrySet())
		{
			String parentNodePath = entry.getKey();
			Boolean boost = entry.getValue();
			Integer finalIndexPosition = -1;

			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule(NODE_PATH, Operator.LIKE, entry.getKey()));
			queryRules.add(new QueryRule(Operator.LIMIT, 5000));

			SearchResult result = searchService.search(new SearchRequest(null, queryRules, null));
			Iterator<Hit> iterator = result.iterator();

			Set<String> existingPaths = new HashSet<String>();
			Set<String> terms = new HashSet<String>();
			Pattern pattern = Pattern.compile("[0-9]+");
			Matcher matcher = null;

			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				String nodePath = columnValueMap.get(NODE_PATH).toString();
				if (!existingPaths.contains(nodePath))
				{
					existingPaths.add(nodePath);
					if (nodePath.startsWith(parentNodePath + ".") || nodePath.equals(parentNodePath))
					{
						String ontologyTermSynonym = columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString().trim()
								.toLowerCase();
						matcher = pattern.matcher(ontologyTermSynonym);
						if (!matcher.find() && !ontologyTermSynonym.equals(""))
						{
							String boostNumber = "^3";
							if (boost && pathToSynonyms.containsKey(parentNodePath)) boostNumber = "^6";

							List<String> listOfSynonyms = new ArrayList<String>(pathToSynonyms.get(parentNodePath));
							Collections.sort(listOfSynonyms, new MyComparator());

							for (String boostedTerm : listOfSynonyms)
							{
								if (ontologyTermSynonym.contains(boostedTerm))
								{
									String orignalTerm = ontologyTermSynonym;
									String replacement = boostedTerm + boostNumber;
									if (boostedTerm.split(" +").length > 1)
									{
										replacement = "\"" + boostedTerm + "\"" + boostNumber;
									}
									terms.add(orignalTerm.replaceAll(boostedTerm, replacement));
									break;
								}
							}
							if (!ontologyTermSynonym.toString().equals("")) terms.add(ontologyTermSynonym);
						}
					}
				}
			}
			if (!position.containsKey(finalIndexPosition)) position.put(finalIndexPosition, new HashSet<String>());
			position.get(finalIndexPosition).addAll(terms);
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

			searchService.deleteDocumentsByType(dataSetIdentifier);
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

		boolean isMappedFetureScore = db.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.IDENTIFIER, Operator.EQUALS, STORE_MAPPING_SCORE)).size() == 0;
		if (isMappedFetureScore)
		{
			ObservableFeature mappedFeatureScore = new ObservableFeature();
			mappedFeatureScore.setIdentifier(STORE_MAPPING_SCORE);
			mappedFeatureScore.setDataType("decimal");
			mappedFeatureScore.setName(STORE_MAPPING_SCORE);
			db.add(mappedFeatureScore);
		}

		boolean isMappedFetureAbsoluteScore = db.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.IDENTIFIER, Operator.EQUALS, STORE_MAPPING_ABSOLUTE_SCORE)).size() == 0;
		if (isMappedFetureAbsoluteScore)
		{
			ObservableFeature mappedFeatureAbsoluteScore = new ObservableFeature();
			mappedFeatureAbsoluteScore.setIdentifier(STORE_MAPPING_ABSOLUTE_SCORE);
			mappedFeatureAbsoluteScore.setDataType("decimal");
			mappedFeatureAbsoluteScore.setName(STORE_MAPPING_ABSOLUTE_SCORE);
			db.add(mappedFeatureAbsoluteScore);
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
					STORE_MAPPING_SCORE, STORE_MAPPING_ABSOLUTE_SCORE, STORE_MAPPING_CONFIRM_MAPPING));
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

	@Override
	public boolean checkExistingMappings(String dataSetIdentifier, Database db) throws DatabaseException
	{
		List<ObservationSet> listOfObservationSets = db.find(ObservationSet.class, new QueryRule(
				ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.EQUALS, dataSetIdentifier));
		if (listOfObservationSets.size() > 0) return true;
		return false;
	}

	class MyComparator implements java.util.Comparator<String>
	{
		@Override
		public int compare(String o1, String o2)
		{
			if (o1.length() > o2.length())
			{
				return -1;
			}
			else if (o1.length() < o2.length())
			{
				return 1;
			}
			return o1.compareTo(o2);
		}
	}
}