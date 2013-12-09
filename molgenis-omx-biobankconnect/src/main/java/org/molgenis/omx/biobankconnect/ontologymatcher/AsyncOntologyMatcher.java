package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.biobankconnect.utils.StoreMappingTable;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus.Stage;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.search.Hit;
import org.molgenis.search.MultiSearchRequest;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.tartarus.snowball.ext.PorterStemmer;

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
	private static final String FEATURE_CATEGORY = "featureCategory-";
	private static final String FIELD_DESCRIPTION_STOPWORDS = "descriptionStopwords";
	private static final String FIELD_BOOST_ONTOLOGYTERM = "boostOntologyTerms";
	private static final String ONTOLOGY_IRI = "ontologyIRI";
	private static final String ONTOLOGY_LABEL = "ontologyLabel";
	private static final String OBSERVATION_SET = "observation_set";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGY_TERM = "ontologyTerm";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	private static final String NODE_PATH = "nodePath";
	private static final String ENTITY_ID = "id";
	private static final String LUCENE_SCORE = "score";
	private static final String ENTITY_TYPE = "type";
	private static final AtomicInteger runningProcesses = new AtomicInteger();
	private static final PorterStemmer stemmer = new PorterStemmer();

	@Autowired
	@Qualifier("unsecuredDatabase")
	private Database database;

	@Autowired
	private CurrentUserStatus currentUserStatus;

	private SearchService searchService;

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
		if (runningProcesses.get() != 0) return true;
		return false;
	}

	@Override
	public Integer matchPercentage(String currentUserName)
	{
		return currentUserStatus.getPercentageOfProcessForUser(currentUserName);
	}

	@Override
	public void deleteDocumentByIds(String documentType, List<String> documentIds)
	{
		searchService.deleteDocumentByIds(documentType, documentIds);
	}

	@Async
	public void match(String userName, Integer selectedDataSet, List<Integer> dataSetsToMatch, Integer featureId)
			throws DatabaseException
	{
		runningProcesses.incrementAndGet();
		currentUserStatus.setUserIsRunning(userName, true);
		dataSetsToMatch.remove(selectedDataSet);
		List<ObservationSet> listOfNewObservationSets = new ArrayList<ObservationSet>();
		List<ObservedValue> listOfNewObservedValues = new ArrayList<ObservedValue>();
		Map<String, List<ObservationSet>> observationSetsPerDataSet = new HashMap<String, List<ObservationSet>>();
		try
		{
			List<QueryRule> queryRules = new ArrayList<QueryRule>();
			if (featureId == null)
			{
				queryRules.add(new QueryRule(ENTITY_TYPE, Operator.SEARCH, ObservableFeature.class.getSimpleName()
						.toLowerCase()));
			}
			else
			{
				queryRules.add(new QueryRule(ENTITY_ID, Operator.EQUALS, featureId));
			}

			queryRules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchResult result = searchService.search(new SearchRequest(CATALOGUE_PREFIX + selectedDataSet,
					queryRules, null));

			currentUserStatus.setUserCurrentStage(userName, Stage.DeleteMapping);
			preprocessing(userName, featureId, selectedDataSet, dataSetsToMatch);

			currentUserStatus.setUserCurrentStage(userName, Stage.CreateMapping);
			currentUserStatus.setUserTotalNumberOfQueries(userName, result.getTotalHitCount());

			for (Hit hit : result.getSearchHits())
			{
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				ObservableFeature feature = database.findById(ObservableFeature.class,
						columnValueMap.get(ObservableFeature.ID.toString()));
				if (feature != null)
				{
					Set<String> boostedOntologyTermUris = new HashSet<String>();
					for (String ontolgoyTermUri : columnValueMap.get(FIELD_BOOST_ONTOLOGYTERM).toString().split(","))
					{
						boostedOntologyTermUris.add(ontolgoyTermUri);
					}
					String description = feature.getDescription() == null || feature.getDescription().isEmpty() ? feature
							.getName() : feature.getDescription();
					description = description.replaceAll("[^a-zA-Z0-9 ]", " ");
					List<OntologyTerm> definitions = feature.getDefinitions();

					List<QueryRule> rules = new ArrayList<QueryRule>();
					if (definitions != null && definitions.size() > 0)
					{
						Map<String, OntologyTermContainer> ontologyTermContainers = collectOntologyTermInfo(
								definitions, boostedOntologyTermUris);
						rules.addAll(makeQueryForOntologyTerms(createQueryRules(description, ontologyTermContainers)));

						for (Map<Integer, List<BoostTermContainer>> alternativeDefinition : addAlternativeDefinition(ontologyTermContainers))
						{
							QueryRule queryRule = new QueryRule(makeQueryForOntologyTerms(alternativeDefinition));
							queryRule.setOperator(Operator.DIS_MAX);
							queryRule.setValue(0.6);
							rules.add(queryRule);
						}
					}
					else rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.SEARCH, description));
					QueryRule finalQuery = new QueryRule(rules);
					finalQuery.setOperator(Operator.DIS_MAX);
					Set<Integer> mappedFeatureIds = new HashSet<Integer>();

					for (Integer dataSetId : dataSetsToMatch)
					{
						StringBuilder dataSetIdentifier = new StringBuilder();
						dataSetIdentifier.append(userName).append('-').append(selectedDataSet).append('-')
								.append(dataSetId);
						if (featureId != null) observationSetsPerDataSet.put(dataSetIdentifier.toString(),
								new ArrayList<ObservationSet>());
						Iterator<Hit> mappedFeatureHits = searchDisMaxQuery(dataSetId.toString(), finalQuery);
						while (mappedFeatureHits.hasNext())
						{
							Hit mappedFeatureHit = mappedFeatureHits.next();
							Map<String, Object> columValueMap = mappedFeatureHit.getColumnValueMap();
							Integer mappedId = Integer.parseInt(columValueMap.get(ENTITY_ID).toString());
							Double score = Double.parseDouble(columValueMap.get(LUCENE_SCORE).toString());
							if (!mappedFeatureIds.contains(mappedId))
							{
								mappedFeatureIds.add(mappedId);

								ObservationSet observation = new ObservationSet();
								observation.setIdentifier(userName + "-" + feature.getId() + "-" + mappedId
										+ "-identifier");
								observation.setPartOfDataSet_Identifier(dataSetIdentifier.toString());
								listOfNewObservationSets.add(observation);
								if (featureId != null) observationSetsPerDataSet.get(dataSetIdentifier.toString()).add(
										observation);

								IntValue xrefForFeature = new IntValue();
								xrefForFeature.setValue(feature.getId());
								ObservedValue valueForFeature = new ObservedValue();
								valueForFeature.setObservationSet(observation);
								valueForFeature.setFeature_Identifier(STORE_MAPPING_FEATURE);
								valueForFeature.setValue(xrefForFeature);
								listOfNewObservedValues.add(valueForFeature);

								IntValue xrefForMappedFeature = new IntValue();
								xrefForMappedFeature.setValue(mappedId);
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
					}
					// finishedNumber++;
					currentUserStatus.incrementFinishedNumberOfQueries(userName);
				}
			}

			database.add(listOfNewObservationSets);

			Set<Integer> processedObservationSets = new HashSet<Integer>();
			List<ObservedValue> valuesForObservationSets = new ArrayList<ObservedValue>();
			for (ObservedValue value : listOfNewObservedValues)
			{
				ObservationSet observationSet = value.getObservationSet();
				Integer observationSetId = observationSet.getId();
				if (!processedObservationSets.contains(observationSetId))
				{
					processedObservationSets.add(observationSetId);
					IntValue observationSetIntValue = new IntValue();
					observationSetIntValue.setValue(observationSetId);
					ObservedValue valueForObservationSet = new ObservedValue();
					valueForObservationSet.setFeature_Identifier(OBSERVATION_SET);
					valueForObservationSet.setObservationSet(observationSet);
					valueForObservationSet.setValue(observationSetIntValue);
					valuesForObservationSets.add(valueForObservationSet);
				}
			}
			listOfNewObservedValues.addAll(valuesForObservationSets);
			database.add(listOfNewObservedValues);

			currentUserStatus.setUserCurrentStage(userName, Stage.StoreMapping);
			currentUserStatus.setUserTotalNumberOfQueries(userName, (long) dataSetsToMatch.size());
			if (featureId != null)
			{
				for (Entry<String, List<ObservationSet>> entry : observationSetsPerDataSet.entrySet())
				{
					searchService.updateIndexTupleTable(entry.getKey(),
							new StoreMappingTable(entry.getKey(), entry.getValue(), database));
					currentUserStatus.incrementFinishedNumberOfQueries(userName);
				}
			}
			else
			{

				for (Integer catalogueId : dataSetsToMatch)
				{
					StringBuilder dataSetIdentifier = new StringBuilder();
					dataSetIdentifier.append(userName).append('-').append(selectedDataSet).append('-')
							.append(catalogueId);
					searchService.indexTupleTable(dataSetIdentifier.toString(),
							new StoreMappingTable(dataSetIdentifier.toString(), database));
					currentUserStatus.incrementFinishedNumberOfQueries(userName);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Exception the matching process has failed!", e);
		}
		finally
		{
			runningProcesses.decrementAndGet();
			currentUserStatus.setUserIsRunning(userName, false);
		}
	}

	private void preprocessing(String userName, Integer featureId, Integer selectedDataSet,
			List<Integer> dataSetsToMatch) throws DatabaseException
	{
		List<String> dataSetsForMapping = new ArrayList<String>();
		for (Integer catalogueId : dataSetsToMatch)
		{
			StringBuilder dataSetIdentifier = new StringBuilder();
			dataSetIdentifier.append(userName).append('-').append(selectedDataSet).append('-').append(catalogueId);
			dataSetsForMapping.add(dataSetIdentifier.toString());
		}
		if (featureId == null)
		{
			createMappingStore(userName, selectedDataSet, dataSetsToMatch);
			deleteExistingRecords(userName, dataSetsForMapping);
		}
		else removeExistingMappings(featureId, dataSetsForMapping);
	}

	private void deleteExistingRecords(String userName, List<String> dataSetsForMapping) throws DatabaseException
	{
		currentUserStatus.setUserTotalNumberOfQueries(userName, (long) dataSetsForMapping.size());
		for (String dataSetIdentifier : dataSetsForMapping)
		{
			List<ObservationSet> listOfObservationSets = database.find(ObservationSet.class, new QueryRule(
					ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.EQUALS, dataSetIdentifier));
			if (listOfObservationSets.size() > 0)
			{
				List<Integer> listOfObservationIdentifiers = new ArrayList<Integer>();
				for (ObservationSet observation : listOfObservationSets)
				{
					listOfObservationIdentifiers.add(observation.getId());
				}
				List<ObservedValue> listOfObservedValues = database.find(ObservedValue.class, new QueryRule(
						ObservedValue.OBSERVATIONSET, Operator.IN, listOfObservationIdentifiers));
				if (listOfObservedValues.size() > 0) database.remove(listOfObservedValues);
				database.remove(listOfObservationSets);
			}
			currentUserStatus.incrementFinishedNumberOfQueries(userName);
		}
		// List<ObservationSet> listOfObservationSets =
		// database.find(ObservationSet.class, new QueryRule(
		// ObservationSet.PARTOFDATASET_IDENTIFIER, Operator.IN,
		// dataSetsForMapping));
		// if (listOfObservationSets.size() > 0)
		// {
		// List<Integer> listOfObservationIdentifiers = new
		// ArrayList<Integer>();
		// for (ObservationSet observation : listOfObservationSets)
		// {
		// listOfObservationIdentifiers.add(observation.getId());
		// }
		// List<ObservedValue> listOfObservedValues =
		// database.find(ObservedValue.class, new QueryRule(
		// ObservedValue.OBSERVATIONSET, Operator.IN,
		// listOfObservationIdentifiers));
		// if (listOfObservedValues.size() > 0)
		// database.remove(listOfObservedValues);
		// database.remove(listOfObservationSets);
		// }
	}

	private void removeExistingMappings(Integer featureId, List<String> dataSetsForMapping) throws DatabaseException
	{
		List<Integer> observationSets = new ArrayList<Integer>();
		for (String dataSet : dataSetsForMapping)
		{
			List<QueryRule> rules = new ArrayList<QueryRule>();
			rules.add(new QueryRule(STORE_MAPPING_FEATURE, Operator.EQUALS, featureId));
			rules.add(new QueryRule(Operator.LIMIT, 100000));
			SearchRequest request = new SearchRequest(dataSet, rules, null);
			SearchResult searchResult = searchService.search(request);
			List<String> indexIds = new ArrayList<String>();
			for (Hit hit : searchResult.getSearchHits())
			{
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				indexIds.add(hit.getId());
				observationSets.add(Integer.parseInt(columnValueMap.get(OBSERVATION_SET).toString()));
			}
			searchService.deleteDocumentByIds(dataSet, indexIds);
		}
		if (observationSets.size() > 0)
		{
			List<ObservationSet> existingObservationSets = database.find(ObservationSet.class, new QueryRule(
					ObservationSet.ID, Operator.IN, observationSets));
			List<ObservedValue> existingObservedValues = database.find(ObservedValue.class, new QueryRule(
					ObservedValue.OBSERVATIONSET, Operator.IN, observationSets));
			if (existingObservedValues.size() > 0) database.remove(existingObservedValues);
			if (existingObservationSets.size() > 0) database.remove(existingObservationSets);
		}
	}

	private List<QueryRule> makeQueryForOntologyTerms(Map<Integer, List<BoostTermContainer>> position)
	{
		boolean boostDesccription = false;
		List<QueryRule> allQueries = new ArrayList<QueryRule>();
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		Map<Integer, Boolean> boostIndex = new HashMap<Integer, Boolean>();
		for (Entry<Integer, List<BoostTermContainer>> entry : position.entrySet())
		{
			Integer index = entry.getKey();
			if (index >= 0)
			{
				boolean boost = false;
				List<QueryRule> subQueries = new ArrayList<QueryRule>();
				for (BoostTermContainer boostTermContainer : entry.getValue())
				{
					List<QueryRule> rules = new ArrayList<QueryRule>();
					for (String term : boostTermContainer.getTerms())
					{
						rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.SEARCH, term.trim()));
						rules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.SEARCH, term.trim()));
					}
					if (!boost) boost = boostTermContainer.isBoost();
					QueryRule queryRule = new QueryRule(rules);
					queryRule.setOperator(Operator.DIS_MAX);
					queryRule.setValue(boostTermContainer.isBoost() ? 10 : null);
					subQueries.add(queryRule);
				}
				if (!boostIndex.containsKey(index))
				{
					boostIndex.put(index, boost);
				}
				else if (!boostIndex.get(index))
				{
					boostIndex.put(index, boost);
				}
				QueryRule queryRule = null;
				if (subQueries.size() == 1)
				{
					queryRule = subQueries.get(0);
				}
				else
				{
					queryRule = new QueryRule(subQueries);
					queryRule.setOperator(Operator.DIS_MAX);
					queryRule.setValue(boost ? 10 : null);
				}
				queryRules.add(queryRule);

				if (!boostDesccription) boostDesccription = boost;
			}
			else if (index == -1)
			{
				for (BoostTermContainer boostTermContainer : entry.getValue())
				{
					if (boostTermContainer.getTerms().size() > 0)
					{
						List<QueryRule> rules = new ArrayList<QueryRule>();
						for (String term : boostTermContainer.getTerms())
						{
							if (!term.isEmpty())
							{
								rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.SEARCH, term.trim()));
								rules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.SEARCH, term.trim()));
							}
						}
						QueryRule queryRule = new QueryRule(rules);
						queryRule.setOperator(Operator.DIS_MAX);
						queryRule.setValue(boostTermContainer.isBoost() ? 10 : null);
						allQueries.add(queryRule);
						if (!boostDesccription) boostDesccription = boostTermContainer.isBoost();
					}
				}
			}
			else
			{
				for (BoostTermContainer boostTermContainer : entry.getValue())
				{
					StringBuilder boostedSynonym = new StringBuilder();
					List<QueryRule> rules = new ArrayList<QueryRule>();
					for (String term : boostTermContainer.getTerms())
					{
						if (!term.isEmpty())
						{
							int count = 0;
							if (boostDesccription)
							{
								for (String eachToken : term.split(" +"))
								{
									if (boostedSynonym.length() != 0) boostedSynonym.append(' ');
									boostedSynonym.append(eachToken);
									if (boostIndex.containsKey(count) && boostIndex.get(count)) boostedSynonym.append(
											'^').append(10);
									count++;
								}
							}
							else boostedSynonym.append(term);

							rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.SEARCH, boostedSynonym
									.toString()));
							rules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.SEARCH, boostedSynonym
									.toString()));
						}
					}
					QueryRule queryRule = new QueryRule(rules);
					queryRule.setOperator(Operator.DIS_MAX);
					queryRule.setValue(boostDesccription || boostTermContainer.isBoost() ? 1.5 : null);
					allQueries.add(queryRule);
				}
			}
		}

		if (queryRules.size() > 0)
		{
			QueryRule combinedQuery = null;
			if (queryRules.size() != 1)
			{
				combinedQuery = new QueryRule(queryRules);
				combinedQuery.setOperator(Operator.SHOULD);
				allQueries.add(combinedQuery);
			}
			else allQueries.add(queryRules.get(0));
		}
		return allQueries;
	}

	private Iterator<Hit> searchDisMaxQuery(String dataSetId, QueryRule disMaxQuery)
	{
		SearchResult result = null;
		try
		{
			List<QueryRule> finalQuery = new ArrayList<QueryRule>();
			finalQuery.add(disMaxQuery);
			finalQuery.add(new QueryRule(Operator.LIMIT, 50));
			MultiSearchRequest request = new MultiSearchRequest(Arrays.asList(CATALOGUE_PREFIX + dataSetId,
					FEATURE_CATEGORY + dataSetId), finalQuery, null);
			result = searchService.multiSearch(request);
		}
		catch (Exception e)
		{
			result = new SearchResult(e.getMessage());
			logger.error("Exception failed to search the request " + result, e);
		}
		return result.iterator();
	}

	private Map<String, OntologyTermContainer> collectOntologyTermInfo(List<OntologyTerm> definitions,
			Set<String> boostedOntologyTermUris)
	{
		Map<String, String> validOntologyTerm = new HashMap<String, String>();
		Map<String, OntologyTermContainer> totalHits = new HashMap<String, OntologyTermContainer>();
		List<QueryRule> rules = new ArrayList<QueryRule>();
		for (OntologyTerm ot : definitions)
		{
			if (rules.size() != 0) rules.add(new QueryRule(Operator.OR));
			rules.add(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, ot.getTermAccession()));
			validOntologyTerm.put(ot.getTermAccession(), ot.getName());
		}
		rules.add(new QueryRule(Operator.LIMIT, 10000));
		SearchRequest request = new SearchRequest(null, rules, null);
		SearchResult result = searchService.search(request);
		Iterator<Hit> iterator = result.iterator();

		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			String ontologyIRI = columnValueMap.get(ONTOLOGY_IRI).toString();
			String ontologyTermUri = columnValueMap.get(ONTOLOGY_TERM_IRI).toString();
			String ontologyTermName = columnValueMap.get(ONTOLOGY_LABEL) + ":"
					+ columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString();
			Boolean boost = boostedOntologyTermUris.contains(ontologyTermUri);

			if (validOntologyTerm.containsKey(ontologyTermUri)
					&& validOntologyTerm.get(ontologyTermUri).equalsIgnoreCase(ontologyTermName))
			{
				String alternativeDefinitions = columnValueMap.get(ALTERNATIVE_DEFINITION) == null ? StringUtils.EMPTY : columnValueMap
						.get(ALTERNATIVE_DEFINITION).toString();
				String nodePath = columnValueMap.get(NODE_PATH).toString();
				if (!totalHits.containsKey(ontologyIRI)) totalHits.put(ontologyIRI, new OntologyTermContainer(
						ontologyIRI));
				totalHits.get(ontologyIRI).getAllPaths().put(nodePath, boost);
				totalHits.get(ontologyIRI).getAlternativeDefinitions().put(nodePath, alternativeDefinitions);
				totalHits.get(ontologyIRI).getSelectedOntologyTerms().add(hit.getId());
			}
		}
		return totalHits;
	}

	private List<Map<Integer, List<BoostTermContainer>>> addAlternativeDefinition(
			Map<String, OntologyTermContainer> ontologyTermContainers)
	{
		List<Map<Integer, List<BoostTermContainer>>> positions = new ArrayList<Map<Integer, List<BoostTermContainer>>>();
		for (Entry<String, OntologyTermContainer> entry : ontologyTermContainers.entrySet())
		{
			String ontologyIRI = entry.getKey();
			OntologyTermContainer container = entry.getValue();
			if (container.getAlternativeDefinitions().size() > 0)
			{
				for (Entry<String, String> entryForAlterDefinition : container.getAlternativeDefinitions().entrySet())
				{
					String definitionString = entryForAlterDefinition.getValue();
					if (!definitionString.isEmpty())
					{
						Boolean boost = container.getAllPaths().get(entryForAlterDefinition.getKey());
						for (String definition : definitionString.split("&&&"))
						{
							Map<String, OntologyTermContainer> totalHits = new HashMap<String, OntologyTermContainer>();
							Set<String> ontologyTerms = new HashSet<String>();
							List<QueryRule> rules = new ArrayList<QueryRule>();
							for (String relatedOntologyTermUri : definition.split(","))
							{
								if (rules.size() != 0) rules.add(new QueryRule(Operator.OR));
								rules.add(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, relatedOntologyTermUri));
							}
							rules.add(new QueryRule(Operator.LIMIT, 10000));
							SearchRequest request = new SearchRequest(null, rules, null);
							SearchResult result = searchService.search(request);
							Iterator<Hit> iterator = result.iterator();
							while (iterator.hasNext())
							{
								Hit hit = iterator.next();
								Map<String, Object> columnValueMap = hit.getColumnValueMap();
								if (columnValueMap.get(ONTOLOGY_IRI).toString().equals(ontologyIRI))
								{
									String nodePath = columnValueMap.get(NODE_PATH).toString();
									String ontologyTerm = columnValueMap.get(ONTOLOGY_TERM).toString().trim()
											.toLowerCase();
									if (!ontologyTerms.contains(ontologyTerm)) ontologyTerms.add(ontologyTerm);
									if (!totalHits.containsKey(ontologyIRI)) totalHits.put(ontologyIRI,
											new OntologyTermContainer(ontologyIRI));
									totalHits.get(ontologyIRI).getAllPaths().put(nodePath, boost);
								}
							}
							positions.add(createQueryRules(StringUtils.join(ontologyTerms.toArray(), ' '), totalHits));
						}
					}
				}
			}
		}
		return positions;
	}

	private Map<Integer, List<BoostTermContainer>> createQueryRules(String description,
			Map<String, OntologyTermContainer> totalHits)
	{
		Map<Integer, List<BoostTermContainer>> position = new HashMap<Integer, List<BoostTermContainer>>();
		List<String> uniqueTokens = stemMembers(Arrays.asList(description.split(" +")));

		for (OntologyTermContainer ontologyTermContainer : totalHits.values())
		{
			Set<String> existingQueryStrings = new HashSet<String>();
			for (Entry<String, Boolean> entry : ontologyTermContainer.getAllPaths().entrySet())
			{
				String documentType = "ontologyTerm-" + ontologyTermContainer.getOntologyIRI();
				String parentNodePath = entry.getKey();
				int parentNodeLevel = parentNodePath.split("\\.").length;
				Boolean boost = entry.getValue();

				List<QueryRule> queryRules = new ArrayList<QueryRule>();
				queryRules.add(new QueryRule(NODE_PATH, Operator.LIKE, entry.getKey()));
				queryRules.add(new QueryRule(Operator.LIMIT, 5000));

				SearchResult result = searchService.search(new SearchRequest(documentType, queryRules, null));
				Iterator<Hit> iterator = result.iterator();

				Pattern pattern = Pattern.compile("[0-9]+");
				Matcher matcher = null;

				BoostTermContainer boostTermContainer = new BoostTermContainer(parentNodePath,
						new LinkedHashSet<String>(), boost);
				int finalIndexPosition = -1;

				while (iterator.hasNext())
				{
					Hit hit = iterator.next();
					Map<String, Object> columnValueMap = hit.getColumnValueMap();
					String nodePath = columnValueMap.get(NODE_PATH).toString();
					String ontologyTermSynonym = columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString().trim()
							.toLowerCase();

					if (!existingQueryStrings.contains(ontologyTermSynonym))
					{
						existingQueryStrings.add(ontologyTermSynonym);

						if (nodePath.equals(parentNodePath))
						{
							if (finalIndexPosition == -1) finalIndexPosition = locateTermInDescription(uniqueTokens,
									ontologyTermSynonym);
							if (!ontologyTermSynonym.toString().equals("")) boostTermContainer.getTerms().add(
									ontologyTermSynonym);
						}
						else if (nodePath.startsWith(parentNodePath + "."))
						{
							matcher = pattern.matcher(ontologyTermSynonym);

							if (!matcher.find() && !ontologyTermSynonym.equals(""))
							{
								int levelDown = nodePath.split("\\.").length - parentNodeLevel;
								double boostedNumber = Math.pow(0.5, levelDown);
								if (finalIndexPosition == -1) finalIndexPosition = locateTermInDescription(
										uniqueTokens, ontologyTermSynonym);

								StringBuilder boostedSynonym = new StringBuilder();
								for (String eachToken : ontologyTermSynonym.split(" +"))
								{
									if (eachToken.length() != 0) boostedSynonym.append(' ');
									boostedSynonym.append(eachToken).append('^').append(boostedNumber);
								}
								ontologyTermSynonym = boostedSynonym.toString();
							}

							if (!ontologyTermSynonym.toString().equals("")) boostTermContainer.getTerms().add(
									ontologyTermSynonym);
						}
					}
				}
				if (!position.containsKey(finalIndexPosition)) position.put(finalIndexPosition,
						new ArrayList<BoostTermContainer>());
				position.get(finalIndexPosition).add(boostTermContainer);
			}
		}

		if (!position.containsKey(-2)) position.put(-2, new ArrayList<BoostTermContainer>());
		BoostTermContainer descriptionBoostTermContainer = new BoostTermContainer(null, new LinkedHashSet<String>(),
				false);
		descriptionBoostTermContainer.getTerms().add(removeStopWords(description));
		position.get(-2).add(descriptionBoostTermContainer);

		return position;
	}

	private String removeStopWords(String originalTerm)
	{
		Set<String> tokens = new LinkedHashSet<String>(Arrays.asList(originalTerm.trim().toLowerCase().split(" +")));
		tokens.removeAll(NGramMatchingModel.STOPWORDSLIST);
		return StringUtils.join(tokens.toArray(), ' ');
	}

	private Integer locateTermInDescription(List<String> uniqueSets, String ontologyTermSynonym)
	{
		int finalIndex = -1;
		List<String> termsFromDescription = stemMembers(Arrays.asList(ontologyTermSynonym.split(" +")));
		for (String eachTerm : termsFromDescription)
		{
			if (!uniqueSets.contains(eachTerm))
			{
				return -1;
			}
			else
			{
				int currentIndex = uniqueSets.indexOf(eachTerm);
				if (finalIndex == -1) finalIndex = currentIndex;
				else finalIndex = finalIndex < currentIndex ? finalIndex : currentIndex;
			}
		}
		return finalIndex;
	}

	private List<String> stemMembers(List<String> originalList)
	{
		List<String> newList = new ArrayList<String>();
		for (String eachTerm : originalList)
		{
			eachTerm = eachTerm.toLowerCase().trim();
			if (!NGramMatchingModel.STOPWORDSLIST.contains(eachTerm))
			{
				stemmer.setCurrent(eachTerm);
				stemmer.stem();
				eachTerm = stemmer.getCurrent().toLowerCase();
				newList.add(eachTerm);
			}
		}
		return newList;
	}

	private void createMappingStore(String userName, Integer selectedDataSet, List<Integer> dataSetsToMatch)
			throws DatabaseException
	{
		boolean isFeatureExists = database.find(ObservableFeature.class,
				new QueryRule(ObservableFeature.IDENTIFIER, Operator.EQUALS, STORE_MAPPING_FEATURE)).size() == 0;
		if (isFeatureExists)
		{
			List<ObservableFeature> features = new ArrayList<ObservableFeature>();

			ObservableFeature feature = new ObservableFeature();
			feature.setIdentifier(STORE_MAPPING_FEATURE);
			feature.setDataType("int");
			feature.setName("Features");
			features.add(feature);

			ObservableFeature mappedFeature = new ObservableFeature();
			mappedFeature.setIdentifier(STORE_MAPPING_MAPPED_FEATURE);
			mappedFeature.setDataType("int");
			mappedFeature.setName("Mapped features");
			features.add(mappedFeature);

			ObservableFeature mappedFeatureScore = new ObservableFeature();
			mappedFeatureScore.setIdentifier(STORE_MAPPING_SCORE);
			mappedFeatureScore.setDataType("decimal");
			mappedFeatureScore.setName(STORE_MAPPING_SCORE);
			features.add(mappedFeatureScore);

			ObservableFeature observationSetFeature = new ObservableFeature();
			observationSetFeature.setIdentifier(OBSERVATION_SET);
			observationSetFeature.setDataType("int");
			observationSetFeature.setName(OBSERVATION_SET);
			features.add(observationSetFeature);

			ObservableFeature mappedFeatureAbsoluteScore = new ObservableFeature();
			mappedFeatureAbsoluteScore.setIdentifier(STORE_MAPPING_ABSOLUTE_SCORE);
			mappedFeatureAbsoluteScore.setDataType("decimal");
			mappedFeatureAbsoluteScore.setName(STORE_MAPPING_ABSOLUTE_SCORE);
			features.add(mappedFeatureAbsoluteScore);

			ObservableFeature confirmMapping = new ObservableFeature();
			confirmMapping.setIdentifier(STORE_MAPPING_CONFIRM_MAPPING);
			confirmMapping.setDataType("bool");
			confirmMapping.setName("Mapping confirmed");
			features.add(confirmMapping);

			database.add(features);

			Protocol protocol = new Protocol();
			protocol.setIdentifier("store_mapping");
			protocol.setName("store_mapping");
			protocol.setFeatures_Identifier(Arrays.asList(STORE_MAPPING_FEATURE, STORE_MAPPING_MAPPED_FEATURE,
					STORE_MAPPING_SCORE, OBSERVATION_SET, STORE_MAPPING_ABSOLUTE_SCORE, STORE_MAPPING_CONFIRM_MAPPING));
			database.add(protocol);
		}

		for (Integer dataSetId : dataSetsToMatch)
		{
			String identifier = userName + "-" + selectedDataSet + "-" + dataSetId;
			boolean ifDataSetExists = database.find(DataSet.class,
					new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, identifier)).size() == 0;
			if (ifDataSetExists)
			{
				DataSet dataSet = new DataSet();
				dataSet.setIdentifier(identifier);
				dataSet.setName(identifier);
				dataSet.setProtocolUsed_Identifier(PROTOCOL_IDENTIFIER);
				dataSet.setDescription("");
				database.add(dataSet);
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

	public class BoostTermContainer
	{
		private final String parentNodePath;
		private boolean boost;
		private final LinkedHashSet<String> terms;

		public BoostTermContainer(String parentNodePath, LinkedHashSet<String> terms, boolean boost)
		{
			this.parentNodePath = parentNodePath;
			this.terms = terms;
			this.boost = boost;
		}

		public LinkedHashSet<String> getTerms()
		{
			return terms;
		}

		public void setBoost(boolean boost)
		{
			if (!this.boost) this.boost = boost;
		}

		public boolean isBoost()
		{
			return boost;
		}

		public String getParentNodePath()
		{
			return parentNodePath;
		}
	}

	public class OntologyTermContainer
	{
		private final String ontologyIRI;
		private final HashMap<String, String> alternativeDefinitions;
		private final Map<String, Boolean> allPaths;
		private final Set<String> selectedOntologyTerms;

		// private final Map<String, String>

		public OntologyTermContainer(String ontologyIRI)
		{
			this.ontologyIRI = ontologyIRI;
			this.alternativeDefinitions = new HashMap<String, String>();
			this.allPaths = new HashMap<String, Boolean>();
			this.selectedOntologyTerms = new HashSet<String>();

		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((ontologyIRI == null) ? 0 : ontologyIRI.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			OntologyTermContainer other = (OntologyTermContainer) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (ontologyIRI == null)
			{
				if (other.ontologyIRI != null) return false;
			}
			else if (!ontologyIRI.equals(other.ontologyIRI)) return false;
			return true;
		}

		private AsyncOntologyMatcher getOuterType()
		{
			return AsyncOntologyMatcher.this;
		}

		public String getOntologyIRI()
		{
			return ontologyIRI;
		}

		public Map<String, Boolean> getAllPaths()
		{
			return allPaths;
		}

		public HashMap<String, String> getAlternativeDefinitions()
		{
			return alternativeDefinitions;
		}

		public Set<String> getSelectedOntologyTerms()
		{
			return selectedOntologyTerms;
		}
	}
}