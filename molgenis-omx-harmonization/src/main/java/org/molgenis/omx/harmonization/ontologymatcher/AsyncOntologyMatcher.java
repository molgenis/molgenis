package org.molgenis.omx.harmonization.ontologymatcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.molgenis.omx.harmonization.tupleTables.StoreMappingTable;
import org.molgenis.omx.harmonization.utils.NGramMatchingModel;
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
	private static PorterStemmer stemmer = new PorterStemmer();
	private static boolean complete = false;
	private static int totalNumber = 0;
	private static int finishedNumber = 0;

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

	public boolean isComplete()
	{
		return complete;
	}

	public void initCompleteState()
	{
		complete = false;
	}

	@Override
	public Double matchPercentage()
	{
		DecimalFormat df = new DecimalFormat("#.##");
		Double percentage = totalNumber == 0 ? new Double(0) : ((double) finishedNumber) / totalNumber;
		return Double.parseDouble(df.format(percentage * 100));
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

			List<Hit> listOfHits = result.getSearchHits();

			totalNumber = listOfHits.size();

			List<ObservationSet> listOfNewObservationSets = new ArrayList<ObservationSet>();
			List<ObservedValue> listOfNewObservedValues = new ArrayList<ObservedValue>();

			for (Hit hit : listOfHits)
			{
				Integer featureId = Integer.parseInt(hit.getColumnValueMap().get(ObservableFeature.ID.toString())
						.toString());
				String description = hit.getColumnValueMap().get(ObservableFeature.DESCRIPTION.toLowerCase())
						.toString();
				if (description == null || description.equals("")) description = hit.getColumnValueMap()
						.get(ObservableFeature.NAME.toLowerCase()).toString();

				ObservableFeature feature = db.findById(ObservableFeature.class, featureId);
				if (feature != null)
				{
					Map<Integer, Set<String>> position = null;
					List<QueryRule> rules = new ArrayList<QueryRule>();
					if (feature.getDefinition() != null && feature.getDefinition().size() > 0)
					{
						position = createQueryRules(description, feature.getDefinition());
						rules.addAll(makeQueryForOntologyTerms(position));
					}
					rules.add(new QueryRule(ObservableFeature.DESCRIPTION.toLowerCase(), Operator.SEARCH,
							stemmerString(description)));
					QueryRule finalQuery = new QueryRule(rules);
					finalQuery.setOperator(Operator.DIS_MAX);

					for (Integer catalogueId : dataSetsToMatch)
					{
						StringBuilder dataSetIdentifier = new StringBuilder();
						dataSetIdentifier.append(selectedDataSet).append('-').append(catalogueId);
						Iterator<Hit> mappedFeatureHits = searchDisMaxQuery(CATALOGUE_PREFIX + catalogueId, finalQuery);
						int count = 0;

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

							DecimalValue decimalForScore = new DecimalValue();
							decimalForScore.setValue(score);
							ObservedValue valueForMappedFeatureScore = new ObservedValue();
							valueForMappedFeatureScore.setFeature_Identifier(STORE_MAPPING_SCORE);
							valueForMappedFeatureScore.setObservationSet(observation);
							valueForMappedFeatureScore.setValue(decimalForScore);
							listOfNewObservedValues.add(valueForMappedFeatureScore);

							// if (count < 5)
							// {
							// if (finishedNumber == 6)
							// {
							// System.out.println();
							// }
							// DecimalValue decimalForAbsoluteScore = new
							// DecimalValue();
							// decimalForAbsoluteScore
							// .setValue(calculateSimilarityScore(finalQuery,
							// mappedFeatureHit));
							// ObservedValue valueForMappedFeatureAbsoluteScore
							// = new ObservedValue();
							// valueForMappedFeatureAbsoluteScore.setFeature_Identifier(STORE_MAPPING_ABSOLUTE_SCORE);
							// valueForMappedFeatureAbsoluteScore.setObservationSet(observation);
							// valueForMappedFeatureAbsoluteScore.setValue(decimalForAbsoluteScore);
							// listOfNewObservedValues.add(valueForMappedFeatureAbsoluteScore);
							// }

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
			complete = true;
			totalNumber = 0;
			finishedNumber = 0;
			DatabaseUtil.closeQuietly(db);
		}
	}

	private double calculateSimilarityScore(QueryRule finalQueryRule, Hit mappedFeatureHit)
	{
		String description = mappedFeatureHit.getColumnValueMap().get(ObservableFeature.DESCRIPTION.toLowerCase())
				.toString();
		double maxScore = 0;
		for (String eachQuery : constructSimilarityQuery(finalQueryRule))
		{
			double score = NGramMatchingModel.stringMatching(eachQuery, description, true);
			maxScore = score > maxScore ? score : maxScore;
			if ((int) maxScore == 100) return maxScore;
		}
		return maxScore;
	}

	private Set<String> constructSimilarityQuery(QueryRule finalQuery)
	{
		Set<String> queries = new HashSet<String>();
		if (finalQuery.getOperator().equals(Operator.DIS_MAX))
		{
			if (finalQuery.getNestedRules() != null)
			{
				for (QueryRule rule : finalQuery.getNestedRules())
				{
					queries.addAll(constructSimilarityQuery(rule));
				}
			}

		}
		else if (finalQuery.getOperator().equals(Operator.SHOULD))
		{
			List<Set<String>> listOfTokens = new ArrayList<Set<String>>();
			if (finalQuery.getNestedRules() != null)
			{
				for (QueryRule rule : finalQuery.getNestedRules())
				{
					listOfTokens.add(constructSimilarityQuery(rule));
				}
			}
			int count = 0;
			StringBuilder combinedToken = new StringBuilder();
			while (count < listOfTokens.size())
			{
				if (count == 0)
				{
					queries.addAll(listOfTokens.get(count));
				}
				else
				{
					Set<String> tempHolders = new HashSet<String>();
					for (String newToken : listOfTokens.get(count))
					{
						for (String oldToken : queries)
						{
							if (combinedToken.length() > 0) combinedToken.delete(0, combinedToken.length());
							tempHolders.add(combinedToken.append(oldToken).append(' ').append(newToken).toString());
						}
					}
					queries = tempHolders;
				}
				count++;
			}
		}
		else
		{
			queries.add(finalQuery.getValue().toString());
		}
		return queries;
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
		List<QueryRule> finalQuery = new ArrayList<QueryRule>();
		finalQuery.add(new QueryRule(ENTITY_TYPE, Operator.EQUALS, ObservableFeature.class.getSimpleName()
				.toLowerCase()));
		finalQuery.add(new QueryRule(Operator.AND));
		finalQuery.add(disMaxQuery);
		finalQuery.add(new QueryRule(Operator.LIMIT, 50));
		SearchRequest request = new SearchRequest(documentType, finalQuery, null);
		SearchResult result = searchService.search(request);
		return result.iterator();

	}

	private Map<Integer, Set<String>> createQueryRules(String dataItem, List<OntologyTerm> definitions)
	{
		Map<Integer, Set<String>> position = new HashMap<Integer, Set<String>>();
		List<String> uniqueTokens = new ArrayList<String>();

		for (String token : dataItem.replaceAll("[^a-zA-Z0-9 ]", " ").split(" +"))
		{
			if (!uniqueTokens.contains(token.toLowerCase())) uniqueTokens.add(stemmerString(token).toLowerCase());
		}

		Set<String> allPaths = new HashSet<String>();
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
				allPaths.add(hit.getColumnValueMap().get(NODE_PATH).toString());
			}
		}
		for (String path : allPaths)
		{
			Integer finalIndexPosition = -1;
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			queryRules.add(new QueryRule(NODE_PATH, Operator.LIKE, path));
			queryRules.add(new QueryRule(Operator.LIMIT, 100));
			SearchRequest request = new SearchRequest(null, queryRules, null);
			SearchResult result = searchService.search(request);
			Iterator<Hit> iterator = result.iterator();
			Set<String> terms = new HashSet<String>();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				StringBuilder ontologyTermSynonym = new StringBuilder();
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				Boolean boost = (Boolean) columnValueMap.get(BOOST);
				for (String eachTerm : columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString().trim().toLowerCase()
						.split(" +"))
				{
					String stemmedTerm = stemmerString(eachTerm);
					int termIndex = uniqueTokens.indexOf(stemmedTerm);
					if (termIndex != -1) finalIndexPosition = termIndex;
					ontologyTermSynonym.append(stemmedTerm).append(' ');
				}
				ontologyTermSynonym.delete(ontologyTermSynonym.length() - 1, ontologyTermSynonym.length());
				if (boost) ontologyTermSynonym.append("(^3)");
				if (!ontologyTermSynonym.toString().equals("")) terms.add(ontologyTermSynonym.toString());
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

	private String stemmerString(String originalString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (String token : originalString.trim().split(" +"))
		{
			stemmer.setCurrent(token.trim());
			stemmer.stem();
			stringBuilder.append(stemmer.getCurrent()).append(' ');
		}
		return stringBuilder.toString().trim();
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
}