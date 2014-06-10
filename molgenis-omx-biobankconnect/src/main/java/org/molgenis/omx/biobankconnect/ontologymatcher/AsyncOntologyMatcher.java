package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.omx.biobankconnect.algorithm.ApplyAlgorithms;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.biobankconnect.utils.StoreMappingRepository;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.search.Hit;
import org.molgenis.search.MultiSearchRequest;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tartarus.snowball.ext.PorterStemmer;

public class AsyncOntologyMatcher implements OntologyMatcher, InitializingBean
{
	private static final Logger logger = Logger.getLogger(AsyncOntologyMatcher.class);
	public static final String PROTOCOL_IDENTIFIER = "store_mapping";
	public static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	public static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	public static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	public static final String STORE_MAPPING_SCORE = "store_mapping_score";
	public static final String STORE_MAPPING_ALGORITHM_SCRIPT = "store_mapping_algorithm_script";
	private static final String CATALOGUE_PREFIX = "protocolTree-";
	private static final String FEATURE_CATEGORY = "featureCategory-";
	private static final String FIELD_DESCRIPTION_STOPWORDS = "descriptionStopwords";
	private static final String FIELD_BOOST_ONTOLOGYTERM = "boostOntologyTerms";
	private static final String ONTOLOGY_IRI = "ontologyIRI";
	private static final String ONTOLOGY_LABEL = "ontologyLabel";
	private static final String OBSERVATION_SET = "observationsetid";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGY_TERM = "ontologyTerm";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	private static final String NODE_PATH = "nodePath";
	private static final String ENTITY_ID = "id";
	private static final String ENTITY_TYPE = "type";
	private static final AtomicInteger runningProcesses = new AtomicInteger();

	@Autowired
	private DataService dataService;

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

	@Override
	@Transactional
	public SearchResult generateMapping(String userName, Integer featureId, Integer targetDataSetId,
			Integer sourceDataSetId)
	{
		PorterStemmer stemmer = new PorterStemmer();
		DataSet sourceDataSet = dataService.findOne(DataSet.ENTITY_NAME, sourceDataSetId, DataSet.class);
		DataSet targetDataSet = dataService.findOne(DataSet.ENTITY_NAME, targetDataSetId, DataSet.class);
		SearchResult mappedFeatures = findMappingsByAnnotation(featureId, sourceDataSet);
		if (mappedFeatures.getTotalHitCount() > 0) return mappedFeatures;

		SearchResult result = retrieveFeatureFromIndex(targetDataSet.getProtocolUsed().getId(),
				Arrays.<Integer> asList(featureId));
		List<Hit> searchHits = result.getSearchHits();
		if (searchHits.size() > 0)
		{
			Hit hit = searchHits.get(0);
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId,
					ObservableFeature.class);
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
					Map<String, OntologyTermContainer> ontologyTermContainers = collectOntologyTermInfo(definitions,
							boostedOntologyTermUris);
					rules.addAll(makeQueryForOntologyTerms(createQueryRules(description, ontologyTermContainers,
							stemmer)));
					for (Map<Integer, List<BoostTermContainer>> alternativeDefinition : addAlternativeDefinition(
							ontologyTermContainers, stemmer))
					{
						QueryRule queryRule = new QueryRule(makeQueryForOntologyTerms(alternativeDefinition));
						queryRule.setOperator(Operator.DIS_MAX);
						queryRule.setValue(0.6);
						rules.add(queryRule);
					}
				}
				else
				{
					rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.SEARCH, description));
				}

				QueryRule finalQueryRule = new QueryRule(rules);
				finalQueryRule.setOperator(Operator.DIS_MAX);

				QueryImpl finalQuery = new QueryImpl();
				finalQuery.addRule(finalQueryRule);
				return searchDisMaxQuery(sourceDataSet.getProtocolUsed().getId().toString(), finalQuery);
			}
		}
		stemmer = null;
		return new SearchResult(0, Collections.<Hit> emptyList());
	}

	/**
	 * Compare whether or not the ontology annotation of feature of interest and
	 * candidate feature are the same
	 * 
	 * @param featureId
	 * @param sourceDataSetId
	 * @return
	 */
	private SearchResult findMappingsByAnnotation(Integer featureId, DataSet sourceDataSet)
	{
		ObservableFeature featureOfInterest = dataService.findOne(ObservableFeature.ENTITY_NAME, featureId,
				ObservableFeature.class);

		List<Object> candidateFeatureIds = new ArrayList<>();
		for (Hit hit : retrieveFeatureFromIndex(sourceDataSet.getProtocolUsed().getId(), null))
		{
			candidateFeatureIds.add(hit.getColumnValueMap().get(ENTITY_ID));
		}

		List<Integer> featureOfInterestIds = new ArrayList<Integer>();
		if (featureOfInterest.getDefinitions().size() != 0)
		{

			Iterable<ObservableFeature> iterableObserableFeatures = dataService.findAll(
					ObservableFeature.ENTITY_NAME,
					new QueryImpl().in(ObservableFeature.DEFINITIONS, featureOfInterest.getDefinitions()).and()
							.in(ObservableFeature.ID, candidateFeatureIds), ObservableFeature.class);

			// Compare ontology annotations between feature of interest and
			// candidate feature by checking on the size of annotations
			for (ObservableFeature observableFeature : iterableObserableFeatures)
			{
				List<OntologyTerm> definitions = observableFeature.getDefinitions();
				List<OntologyTerm> definitions1 = featureOfInterest.getDefinitions();
				if (definitions.size() == definitions1.size())
				{
					definitions.removeAll(definitions1);
					if (definitions.size() == 0) featureOfInterestIds.add(observableFeature.getId());
				}
			}
		}
		return (featureOfInterestIds.size() == 0) ? new SearchResult(0, Collections.<Hit> emptyList()) : retrieveFeatureFromIndex(
				sourceDataSet.getProtocolUsed().getId(), featureOfInterestIds);
	}

	/**
	 * Retrieve feature information from Index based on given featureIds list.
	 * If featureIds is empty, all of features are retrieved for particular
	 * dataset
	 * 
	 * @param protocolId
	 * @param featureIds
	 * @return
	 */
	private SearchResult retrieveFeatureFromIndex(Integer protocolId, Iterable<Integer> featureIds)
	{
		QueryImpl query = new QueryImpl();
		query.pageSize(100000);
		if (featureIds != null && Iterables.size(featureIds) > 0)
		{
			for (Integer featureId : featureIds)
			{
				if (query.getRules().size() > 0) query.addRule(new QueryRule(Operator.OR));
				query.addRule(new QueryRule(ENTITY_ID, Operator.EQUALS, featureId));
			}
		}
		else
		{
			query.addRule(new QueryRule(ENTITY_TYPE, Operator.EQUALS, ObservableFeature.class.getSimpleName()
					.toLowerCase()));
		}
		return searchService.search(new SearchRequest(CATALOGUE_PREFIX + protocolId, query, null));
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
						rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS, term.trim()));
						rules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS, term.trim()));
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
								rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS, term.trim()));
								rules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS, term.trim()));
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

							rules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS, boostedSynonym
									.toString()));
							rules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS, boostedSynonym
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

	private SearchResult searchDisMaxQuery(String protocolId, Query q)
	{
		SearchResult result = null;
		try
		{
			q.pageSize(50);
			MultiSearchRequest request = new MultiSearchRequest(Arrays.asList(CATALOGUE_PREFIX + protocolId,
					FEATURE_CATEGORY + protocolId), q, null);
			result = searchService.multiSearch(request);
		}
		catch (Exception e)
		{
			result = new SearchResult(e.getMessage());
			logger.error("Exception failed to search the request " + result, e);
		}
		return result;
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

		QueryImpl query = new QueryImpl();
		query.pageSize(10000);
		for (QueryRule rule : rules)
		{
			query.addRule(rule);
		}

		SearchRequest request = new SearchRequest(null, query, null);
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
			Map<String, OntologyTermContainer> ontologyTermContainers, PorterStemmer stemmer)
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

							QueryImpl q = new QueryImpl();
							q.pageSize(10000);
							for (QueryRule rule : rules)
							{
								q.addRule(rule);
							}

							SearchRequest request = new SearchRequest(null, q, null);
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
							positions.add(createQueryRules(StringUtils.join(ontologyTerms.toArray(), ' '), totalHits,
									stemmer));
						}
					}
				}
			}
		}
		return positions;
	}

	private Map<Integer, List<BoostTermContainer>> createQueryRules(String description,
			Map<String, OntologyTermContainer> totalHits, PorterStemmer stemmer)
	{
		Map<Integer, List<BoostTermContainer>> position = new HashMap<Integer, List<BoostTermContainer>>();
		List<String> uniqueTokens = stemMembers(Arrays.asList(description.split(" +")), stemmer);

		for (OntologyTermContainer ontologyTermContainer : totalHits.values())
		{
			Set<String> existingQueryStrings = new HashSet<String>();
			for (Entry<String, Boolean> entry : ontologyTermContainer.getAllPaths().entrySet())
			{
				String documentType = "ontologyTerm-" + ontologyTermContainer.getOntologyIRI();
				String parentNodePath = entry.getKey();
				int parentNodeLevel = parentNodePath.split("\\.").length;
				Boolean boost = entry.getValue();

				Query query = new QueryImpl().eq(NODE_PATH, entry.getKey()).pageSize(5000);
				SearchResult result = searchService.search(new SearchRequest(documentType, query, null));
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
									ontologyTermSynonym, stemmer);
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
										uniqueTokens, ontologyTermSynonym, stemmer);

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

	private Integer locateTermInDescription(List<String> uniqueSets, String ontologyTermSynonym, PorterStemmer stemmer)
	{
		int finalIndex = -1;
		List<String> termsFromDescription = stemMembers(Arrays.asList(ontologyTermSynonym.split(" +")), stemmer);
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

	private List<String> stemMembers(List<String> originalList, PorterStemmer stemmer)
	{
		List<String> newList = new ArrayList<String>();
		for (String eachTerm : originalList)
		{
			eachTerm = eachTerm.toLowerCase().trim();
			if (!NGramMatchingModel.STOPWORDSLIST.contains(eachTerm))
			{
				try
				{
					stemmer.setCurrent(eachTerm);
					stemmer.stem();
					eachTerm = stemmer.getCurrent().toLowerCase();
				}
				catch (RuntimeException e)
				{
					logger.error("Could not stem word : " + eachTerm, e);
				}
				newList.add(eachTerm);
			}
		}
		return newList;
	}

	private void createMappingStore(String userName, Integer selectedDataSet, List<Integer> dataSetsToMatch)
	{
		ObservableFeature f = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, STORE_MAPPING_FEATURE), ObservableFeature.class);

		if (f == null)
		{
			List<ObservableFeature> features = new ArrayList<ObservableFeature>();

			ObservableFeature feature = new ObservableFeature();
			feature.setIdentifier(STORE_MAPPING_FEATURE);
			feature.setDataType(MolgenisFieldTypes.FieldTypeEnum.INT.toString().toLowerCase());
			feature.setName("Features");
			features.add(feature);

			ObservableFeature mappedFeature = new ObservableFeature();
			mappedFeature.setIdentifier(STORE_MAPPING_MAPPED_FEATURE);
			mappedFeature.setDataType(MolgenisFieldTypes.FieldTypeEnum.STRING.toString().toLowerCase());
			mappedFeature.setName("Mapped features");
			features.add(mappedFeature);

			ObservableFeature mappedFeatureScore = new ObservableFeature();
			mappedFeatureScore.setIdentifier(STORE_MAPPING_SCORE);
			mappedFeatureScore.setDataType(MolgenisFieldTypes.FieldTypeEnum.DECIMAL.toString().toLowerCase());
			mappedFeatureScore.setName(STORE_MAPPING_SCORE);
			features.add(mappedFeatureScore);

			ObservableFeature observationSetFeature = new ObservableFeature();
			observationSetFeature.setIdentifier(OBSERVATION_SET);
			observationSetFeature.setDataType(MolgenisFieldTypes.FieldTypeEnum.INT.toString().toLowerCase());
			observationSetFeature.setName(OBSERVATION_SET);
			features.add(observationSetFeature);

			ObservableFeature algorithmScriptFeature = new ObservableFeature();
			algorithmScriptFeature.setIdentifier(STORE_MAPPING_ALGORITHM_SCRIPT);
			algorithmScriptFeature.setDataType(MolgenisFieldTypes.FieldTypeEnum.STRING.toString().toLowerCase());
			algorithmScriptFeature.setName(STORE_MAPPING_ALGORITHM_SCRIPT);
			features.add(algorithmScriptFeature);

			ObservableFeature confirmMapping = new ObservableFeature();
			confirmMapping.setIdentifier(STORE_MAPPING_CONFIRM_MAPPING);
			confirmMapping.setDataType(MolgenisFieldTypes.FieldTypeEnum.BOOL.toString().toLowerCase());
			confirmMapping.setName("Mapping confirmed");
			features.add(confirmMapping);

			dataService.add(ObservableFeature.ENTITY_NAME, features);

			Protocol protocol = new Protocol();
			protocol.setIdentifier(PROTOCOL_IDENTIFIER);
			protocol.setName(PROTOCOL_IDENTIFIER);
			protocol.setFeatures(features);
			dataService.add(Protocol.ENTITY_NAME, protocol);

			dataService.getCrudRepository(ObservableFeature.ENTITY_NAME).flush();
		}

		for (Integer dataSetId : dataSetsToMatch)
		{
			String identifier = createMappingDataSetIdentifier(userName, selectedDataSet, dataSetId);
			DataSet existing = dataService.findOne(DataSet.ENTITY_NAME,
					new QueryImpl().eq(DataSet.IDENTIFIER, identifier), DataSet.class);

			if (existing == null)
			{
				DataSet dataSet = new DataSet();
				dataSet.setIdentifier(identifier);
				dataSet.setName(identifier);

				Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME,
						new QueryImpl().eq(Protocol.IDENTIFIER, PROTOCOL_IDENTIFIER), Protocol.class);
				dataSet.setProtocolUsed(protocol);
				dataService.add(DataSet.ENTITY_NAME, dataSet);
				dataService.getCrudRepository(DataSet.ENTITY_NAME).flush();
				dataService.addRepository(new OmxRepository(dataService, searchService, identifier,
						new DefaultEntityValidator(dataService, new EntityAttributesValidator())));
			}
		}
		dataService.getCrudRepository(Protocol.ENTITY_NAME).flush();
	}

	@Override
	@RunAsSystem
	public boolean checkExistingMappings(String dataSetIdentifier, DataService dataService)
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);
		if (dataSet == null)
		{
			throw new MolgenisDataException("Unknown DataSet [" + dataSetIdentifier + "]");
		}

		Iterable<ObservationSet> listOfObservationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);

		return Iterables.size(listOfObservationSets) > 0;
	}

	@Override
	@RunAsSystem
	@Transactional
	public Map<String, String> updateScript(String userName, OntologyMatcherRequest request)
	{
		Map<String, String> updateResult = new HashMap<String, String>();
		// check if the dataset for mappings has been created
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		Integer targetDataSetId = request.getTargetDataSetId();
		createMappingStore(userName, targetDataSetId, selectedDataSetIds);

		boolean toUpdate = false;
		for (Integer selectedDataSetId : selectedDataSetIds)
		{
			// check if the mapping that needs to be updated has been created
			String mappingDataSetIdentifier = createMappingDataSetIdentifier(userName, targetDataSetId,
					selectedDataSetId);
			// update the existing mappings
			toUpdate = updateExistingMapping(mappingDataSetIdentifier, request);
			if (!toUpdate) addNewMappingToDatabase(mappingDataSetIdentifier, request);
		}

		updateResult.put("message",
				toUpdate ? "the script has been updated!" : "the script has been added to the database!");
		return updateResult;
	}

	private void addNewMappingToDatabase(String mappingDataSetIdentifier, OntologyMatcherRequest request)
	{
		List<ObservedValue> listOfNewObservedValues = new ArrayList<ObservedValue>();

		DataSet storingMappingDataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, mappingDataSetIdentifier), DataSet.class);
		ObservationSet observationSet = new ObservationSet();
		observationSet.setIdentifier(mappingDataSetIdentifier + "-" + request.getFeatureId());
		observationSet.setPartOfDataSet(storingMappingDataSet);
		dataService.add(ObservationSet.ENTITY_NAME, observationSet);

		IntValue xrefForFeature = new IntValue();
		xrefForFeature.setValue(request.getFeatureId());
		dataService.add(IntValue.ENTITY_NAME, xrefForFeature);

		ObservedValue valueForFeature = new ObservedValue();
		valueForFeature.setObservationSet(observationSet);
		ObservableFeature smf = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, STORE_MAPPING_FEATURE), ObservableFeature.class);
		valueForFeature.setFeature(smf);
		valueForFeature.setValue(xrefForFeature);
		listOfNewObservedValues.add(valueForFeature);

		StringValue algorithmScriptValue = new StringValue();
		algorithmScriptValue.setValue(request.getAlgorithmScript() == null ? StringUtils.EMPTY : request
				.getAlgorithmScript());
		dataService.add(StringValue.ENTITY_NAME, algorithmScriptValue);

		ObservedValue algorithmScriptObservedValue = new ObservedValue();
		algorithmScriptObservedValue.setObservationSet(observationSet);
		ObservableFeature algorithmScriptFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, STORE_MAPPING_ALGORITHM_SCRIPT),
				ObservableFeature.class);

		algorithmScriptObservedValue.setFeature(algorithmScriptFeature);
		algorithmScriptObservedValue.setValue(algorithmScriptValue);
		listOfNewObservedValues.add(algorithmScriptObservedValue);

		IntValue observationSetIntValue = new IntValue();
		observationSetIntValue.setValue(observationSet.getId());
		dataService.add(IntValue.ENTITY_NAME, observationSetIntValue);

		ObservedValue valueForObservationSet = new ObservedValue();
		ObservableFeature observationSetFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.IDENTIFIER, OBSERVATION_SET), ObservableFeature.class);
		valueForObservationSet.setFeature(observationSetFeature);
		valueForObservationSet.setObservationSet(observationSet);
		valueForObservationSet.setValue(observationSetIntValue);
		listOfNewObservedValues.add(valueForObservationSet);

		// Extract feature names out of algorithm script and store ids of the
		// mapped features
		ObservedValue valueForMappedFeatures = new ObservedValue();
		ObservableFeature SMMF = dataService
				.findOne(ObservableFeature.ENTITY_NAME,
						new QueryImpl().eq(ObservableFeature.IDENTIFIER, STORE_MAPPING_MAPPED_FEATURE),
						ObservableFeature.class);
		StringValue mappedFeatureValues = new StringValue();
		mappedFeatureValues.setValue(convertToFeatureIds(request));
		dataService.add(StringValue.ENTITY_NAME, mappedFeatureValues);
		valueForMappedFeatures.setFeature(SMMF);
		valueForMappedFeatures.setValue(mappedFeatureValues);
		valueForMappedFeatures.setObservationSet(observationSet);
		listOfNewObservedValues.add(valueForMappedFeatures);

		// Add observedValues to database and index those values
		dataService.add(ObservedValue.ENTITY_NAME, listOfNewObservedValues);
		dataService.getCrudRepository(ObservedValue.ENTITY_NAME).flush();
		searchService.updateRepositoryIndex(new StoreMappingRepository(storingMappingDataSet, listOfNewObservedValues,
				dataService));
	}

	private boolean updateExistingMapping(String mappingDataSetIdentifier, OntologyMatcherRequest request)
	{
		QueryImpl query = new QueryImpl();
		query.pageSize(100000);
		query.addRule(new QueryRule(STORE_MAPPING_FEATURE, Operator.EQUALS, request.getFeatureId()));
		SearchResult result = searchService.search(new SearchRequest(mappingDataSetIdentifier, query, null));

		if (result.getTotalHitCount() > 0)
		{
			Hit hit = result.getSearchHits().get(0);
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			ObservationSet observationSet = dataService.findOne(ObservationSet.ENTITY_NAME,
					Integer.parseInt(columnValueMap.get(OBSERVATION_SET).toString()), ObservationSet.class);
			if (observationSet == null) return false;

			// Check if the new script is same as old script
			Object existingScript = columnValueMap.get(STORE_MAPPING_ALGORITHM_SCRIPT);
			String algorithmScript = request.getAlgorithmScript();
			if (algorithmScript != null && !existingScript.toString().trim().equalsIgnoreCase(algorithmScript.trim()))
			{
				ObservableFeature storeMappingAlgorithmScriptFeature = dataService.findOne(
						ObservableFeature.ENTITY_NAME,
						new QueryImpl().eq(ObservableFeature.IDENTIFIER, STORE_MAPPING_ALGORITHM_SCRIPT),
						ObservableFeature.class);

				ObservedValue algorithmScriptObservedValue = dataService.findOne(
						ObservedValue.ENTITY_NAME,
						new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet).and()
								.eq(ObservedValue.FEATURE, storeMappingAlgorithmScriptFeature), ObservedValue.class);

				if (algorithmScriptObservedValue != null
						&& algorithmScriptObservedValue.getValue() instanceof StringValue)
				{
					// Update algorithm script in database
					StringValue algorithmScriptValue = (StringValue) algorithmScriptObservedValue.getValue();
					algorithmScriptValue.setValue(algorithmScript);
					dataService.update(StringValue.ENTITY_NAME, algorithmScriptValue);
					dataService.getCrudRepository(StringValue.ENTITY_NAME).flush();
					// Update algorithm script in index
					StringBuilder updateScriptBuilder = new StringBuilder();
					updateScriptBuilder.append(STORE_MAPPING_ALGORITHM_SCRIPT).append('=').append("\"")
							.append(algorithmScript).append("\"");
					searchService.updateDocumentById(mappingDataSetIdentifier, hit.getId(),
							updateScriptBuilder.toString());
				}
			}

			Object existingMappedFeature = columnValueMap.get(STORE_MAPPING_MAPPED_FEATURE);
			String convertToFeatureIds = convertToFeatureIds(request);
			if (!existingMappedFeature.toString().equalsIgnoreCase(convertToFeatureIds))
			{
				ObservableFeature SMMF = dataService.findOne(ObservableFeature.ENTITY_NAME,
						new QueryImpl().eq(ObservableFeature.IDENTIFIER, STORE_MAPPING_MAPPED_FEATURE),
						ObservableFeature.class);
				ObservedValue mappedFeaturesObservedValue = dataService.findOne(
						ObservedValue.ENTITY_NAME,
						new QueryImpl().eq(ObservedValue.OBSERVATIONSET, observationSet).and()
								.eq(ObservedValue.FEATURE, SMMF), ObservedValue.class);
				if (mappedFeaturesObservedValue != null
						&& mappedFeaturesObservedValue.getValue() instanceof StringValue)
				{
					// Update mappedFeatures in database
					StringValue mappedFeaturesValue = (StringValue) mappedFeaturesObservedValue.getValue();
					mappedFeaturesValue.setValue(convertToFeatureIds);
					dataService.update(StringValue.ENTITY_NAME, mappedFeaturesValue);
					dataService.getCrudRepository(StringValue.ENTITY_NAME).flush();
					// Update mappedFeatures in index
					StringBuilder updateMappedFeaturesBuilder = new StringBuilder();
					updateMappedFeaturesBuilder.append(STORE_MAPPING_MAPPED_FEATURE).append('=').append("\"")
							.append(convertToFeatureIds.toString()).append("\"");
					searchService.updateDocumentById(mappingDataSetIdentifier, hit.getId(),
							updateMappedFeaturesBuilder.toString());
				}
			}
		}
		return result.getTotalHitCount() > 0;
	}

	private String convertToFeatureIds(OntologyMatcherRequest request)
	{
		List<Integer> featureIds = new ArrayList<Integer>();
		if (request.getMappedFeatureIds() != null && request.getMappedFeatureIds().size() > 0)
		{
			return request.getMappedFeatureIds().toString();
		}
		if (request.getAlgorithmScript() != null && !request.getAlgorithmScript().isEmpty())
		{
			List<String> selectedFeatureNames = new ArrayList<String>();
			for (String standardFeatureName : ApplyAlgorithms.extractFeatureName(request.getAlgorithmScript()))
			{
				selectedFeatureNames.add(standardFeatureName);
			}

			Iterable<ObservableFeature> iterators = dataService.findAll(ObservableFeature.ENTITY_NAME,
					new QueryImpl().in(ObservableFeature.NAME, selectedFeatureNames), ObservableFeature.class);
			for (ObservableFeature feature : iterators)
			{
				featureIds.add(feature.getId());
			}
		}
		return featureIds.size() == 0 ? StringUtils.EMPTY : featureIds.toString();
	}

	public static String createMappingDataSetIdentifier(String userName, Integer targetDataSetId,
			Integer sourceDataSetId)
	{
		StringBuilder dataSetIdentifier = new StringBuilder();
		dataSetIdentifier.append(userName).append('-').append(targetDataSetId).append('-').append(sourceDataSetId);
		return dataSetIdentifier.toString();
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

	@Override
	@RunAsSystem
	@Transactional
	public void match(String userName, Integer selectedDataSetId, List<Integer> dataSetIdsToMatch, Integer featureId)
	{
		createMappingStore(userName, selectedDataSetId, dataSetIdsToMatch);
	}
}