package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
	private static final String OBSERVATION_SET = "observationsetid";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGY_TERM = "ontologyTerm";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	private static final String NODE_PATH = "nodePath";
	private static final String ENTITY_ID = "id";
	private static final String ENTITY_TYPE = "type";
	private static final String PATTERN_MATCH = "[^a-zA-Z0-9 ]";
	private static final String COMMON_SEPERATOR = ",";
	private static final String ALTERNATIVE_DEFINITION_SEPERATOR = "&&&";
	private static final int DEFAULT_RETRIEVAL_DOCUMENTS_SIZE = 50;
	private static final String MULTIPLE_NUMBERS_PATTERN = "[0-9]+";
	private static final String NODEPATH_SEPARATOR = "\\.";

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
	@RunAsSystem
	@Transactional
	public void match(String userName, Integer selectedDataSetId, List<Integer> dataSetIdsToMatch, Integer featureId)
	{
		createMappingStore(userName, selectedDataSetId, dataSetIdsToMatch);
	}

	/**
	 * This method is to generate a list of candidate mappings for the chosen
	 * feature by using ElasticSearch based on the information from ontology
	 * terms
	 */
	@Override
	@Transactional
	public SearchResult generateMapping(String userName, Integer featureId, Integer targetDataSetId,
			Integer sourceDataSetId)
	{
		// TODO : we might want to know how the mapping is produced, is that
		// based on the description of features or categories?

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
				List<String> boostedOntologyTermUris = Arrays.asList(columnValueMap.get(FIELD_BOOST_ONTOLOGYTERM)
						.toString().split(COMMON_SEPERATOR));
				String description = StringUtils.isEmpty(feature.getDescription()) ? feature.getName().replaceAll(
						PATTERN_MATCH, OntologyTermQueryRepository.SINGLE_WHITESPACE) : feature.getDescription()
						.replaceAll(PATTERN_MATCH, OntologyTermQueryRepository.SINGLE_WHITESPACE);

				List<String> ontologyTermUris = Lists.transform(feature.getDefinitions(),
						new Function<OntologyTerm, String>()
						{
							@Override
							public String apply(OntologyTerm ontologyTerm)
							{
								return ontologyTerm.getTermAccession();
							}
						});

				// Collect the information from ontology terms and create
				// ElasticSearch queries
				Collection<OntologyTermContainer> ontologyTermContainers = collectOntologyTermInfo(ontologyTermUris,
						boostedOntologyTermUris);
				List<QueryRule> subQueryRules = createQueryRules(description, ontologyTermContainers, stemmer);
				List<QueryRule> subQueryRules_2 = getAlternativeOTs(description, ontologyTermContainers, stemmer);
				List<QueryRule> subQueryRules_3 = getExistingMappings(feature, stemmer,
						createMappingDataSetIdentifier(userName, targetDataSetId, sourceDataSetId));

				QueryRule finalQueryRule = new QueryRule(new ArrayList<QueryRule>());
				// Add original description of data items to the query
				finalQueryRule.getNestedRules().add(
						new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS, removeStopWords(description)));
				finalQueryRule.getNestedRules().add(
						new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS, removeStopWords(description)));
				finalQueryRule.setOperator(Operator.DIS_MAX);
				finalQueryRule.getNestedRules().addAll(subQueryRules);
				finalQueryRule.getNestedRules().addAll(subQueryRules_2);
				finalQueryRule.getNestedRules().addAll(subQueryRules_3);
				return searchDisMaxQuery(sourceDataSet.getProtocolUsed().getId().toString(), new QueryImpl(
						finalQueryRule));
			}
		}
		return new SearchResult(0, Collections.<Hit> emptyList());
	}

	/**
	 * This method is to collect existing mappings for the same desired data
	 * element from other datasets and use them for query expansion as well
	 * 
	 * @param description
	 * @param desiredDataElement
	 * @param stemmer
	 * @param dataSetIdentifier
	 * @return
	 */
	private List<QueryRule> getExistingMappings(ObservableFeature desiredDataElement, PorterStemmer stemmer,
			String dataSetIdentifier)
	{
		List<QueryRule> queryRules = new ArrayList<QueryRule>();

		SearchResult searchResult = searchService.search(new SearchRequest(null, new QueryImpl().eq(
				STORE_MAPPING_FEATURE, desiredDataElement.getId()).pageSize(Integer.MAX_VALUE), null));

		// Collect all the mapped features and put their IDs in a list
		List<Object> mappedFeatureIds = new ArrayList<Object>();
		for (Hit hit : searchResult.getSearchHits())
		{
			// Only collect the mappings that are not from the dataset in which
			// the mapping will be found
			if (!hit.getDocumentType().equals(dataSetIdentifier))
			{
				Object entityID = hit.getColumnValueMap().get(STORE_MAPPING_MAPPED_FEATURE);
				if (entityID != null)
				{
					String mappedIds = entityID.toString();
					// Because a list of IDs is stored as string in the
					// index/database such as [1,2], therefore an empty check is
					// needed here. If the value is an empty list like '[]', the
					// minimal length is 2
					if (mappedIds.length() > 2)
					{
						for (String id : mappedIds.substring(1, mappedIds.length() - 1).split(COMMON_SEPERATOR))
						{
							mappedFeatureIds.add(id.trim());
						}
					}
				}
			}
		}
		// If there are mappings already
		if (mappedFeatureIds.size() > 0)
		{
			Iterable<ObservableFeature> mappedFeatures = dataService.findAll(ObservableFeature.ENTITY_NAME,
					mappedFeatureIds, ObservableFeature.class);

			for (ObservableFeature feature : mappedFeatures)
			{
				String description = feature.getDescription();
				if (!StringUtils.isEmpty(description))
				{
					description = StringUtils.join(
							stemMembers(
									Arrays.asList(removeStopWords(description).split(
											OntologyTermQueryRepository.MULTI_WHITESPACES)), stemmer),
							OntologyTermQueryRepository.SINGLE_WHITESPACE);
					queryRules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS, description));
					queryRules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS, description));
				}
			}
		}

		return queryRules;
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
		query.pageSize(Integer.MAX_VALUE);
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

	private SearchResult searchDisMaxQuery(String protocolId, Query q)
	{
		SearchResult result = null;
		try
		{
			q.pageSize(DEFAULT_RETRIEVAL_DOCUMENTS_SIZE);
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

	/**
	 * This method is to collect ontology term information from the index, which
	 * will be used in composing queries next
	 * 
	 * @param ontologyTermUris
	 * @param boostedOntologyTerms
	 * @return
	 */
	private Collection<OntologyTermContainer> collectOntologyTermInfo(List<String> ontologyTermUris,
			List<String> boostedOntologyTerms)
	{
		if (ontologyTermUris == null || ontologyTermUris.size() == 0) return Collections.emptyList();

		Map<String, OntologyTermContainer> totalHits = new HashMap<String, OntologyTermContainer>();

		Query query = new QueryImpl().pageSize(Integer.MAX_VALUE);
		for (String ontologyTermUri : ontologyTermUris)
		{
			if (query.getRules().size() > 0)
			{
				query.or();
			}
			query.eq(ONTOLOGY_TERM_IRI, ontologyTermUri);
		}

		SearchResult result = searchService.search(new SearchRequest(null, query, null));
		for (Hit hit : result.getSearchHits())
		{
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			String ontologyIRI = columnValueMap.get(ONTOLOGY_IRI).toString();
			String ontologyTermUri = columnValueMap.get(ONTOLOGY_TERM_IRI).toString();
			String ontologyTermNameSynonym = columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString();
			String ontologyTermName = columnValueMap.get(ONTOLOGY_TERM).toString();
			boolean boost = boostedOntologyTerms.contains(ontologyTermUri);

			if (ontologyTermName.equalsIgnoreCase(ontologyTermNameSynonym))
			{
				String nodePath = columnValueMap.get(NODE_PATH).toString();
				if (!totalHits.containsKey(ontologyIRI))
				{
					totalHits.put(ontologyIRI, new OntologyTermContainer(ontologyIRI));
				}
				String alternativeDefinitions = columnValueMap.get(ALTERNATIVE_DEFINITION) == null ? StringUtils.EMPTY : columnValueMap
						.get(ALTERNATIVE_DEFINITION).toString();
				totalHits.get(ontologyIRI).getAllPaths().put(nodePath, boost);
				totalHits.get(ontologyIRI).getSelectedOntologyTerms().add(hit.getId());
				totalHits.get(ontologyIRI).getAlternativeDefinitions().put(nodePath, alternativeDefinitions);
			}
		}
		return totalHits.values();
	}

	/**
	 * The method is to create ElasticSearch queries with Molgenis queryRule by
	 * using ontology term information
	 * 
	 * @param description
	 * @param ontologyTermContainers
	 * @param stemmer
	 * @return
	 */
	private List<QueryRule> createQueryRules(String description,
			Collection<OntologyTermContainer> ontologyTermContainers, PorterStemmer stemmer)
	{
		// Default position is -1, which means the ontologyterm cannot be
		// located anywhere inside the description
		Integer locationNotFound = -1;

		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		List<QueryRule> shouldQueryRules = new ArrayList<QueryRule>();
		List<String> uniqueTokens = stemMembers(
				Arrays.asList(description.split(OntologyTermQueryRepository.MULTI_WHITESPACES)), stemmer);
		for (OntologyTermContainer ontologyTermContainer : ontologyTermContainers)
		{
			Set<String> existingQueryStrings = new HashSet<String>();
			for (Entry<String, Boolean> entry : ontologyTermContainer.getAllPaths().entrySet())
			{
				String currentNodePath = entry.getKey();
				int parentNodeLevel = currentNodePath.split(NODEPATH_SEPARATOR).length;
				Query query = new QueryImpl().eq(NODE_PATH, entry.getKey()).pageSize(Integer.MAX_VALUE);
				SearchResult result = searchService.search(new SearchRequest(AsyncOntologyIndexer
						.createOntologyTermDocumentType(ontologyTermContainer.getOntologyIRI()), query, null));

				Pattern pattern = Pattern.compile(MULTIPLE_NUMBERS_PATTERN);
				Matcher matcher = null;

				// Assume the position is -1, which represents the ontology term
				// could not be located anywhere inside the description
				int finalIndexPosition = locationNotFound;

				// Create list of queryRules to hold values for all retrieved
				// ontology term synonyms
				List<QueryRule> subQueryRules = new ArrayList<QueryRule>();
				QueryRule disJunctQuery = new QueryRule(subQueryRules);
				disJunctQuery.setOperator(Operator.DIS_MAX);

				// Retrieve all the ontology terms that are 'under' specific
				// node (all descendants)
				for (Hit hit : result.getSearchHits())
				{
					Map<String, Object> columnValueMap = hit.getColumnValueMap();
					String nodePath = columnValueMap.get(NODE_PATH).toString();

					// ElasticSearch might pick up more hits than needed,
					// therefore a check on the nodePath is necessary
					if (nodePath.startsWith(currentNodePath))
					{
						String ontologyTermSynonym = columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString().trim()
								.toLowerCase();

						// Only process new ontologyTermSynonym
						if (!existingQueryStrings.contains(ontologyTermSynonym))
						{
							// Remember the synonyms that have been added to the
							// query already
							existingQueryStrings.add(ontologyTermSynonym);

							// Keep looking for the potential location of the
							// ontology term synonym until it is found
							if (finalIndexPosition == locationNotFound)
							{
								finalIndexPosition = locateTermInDescription(uniqueTokens, ontologyTermSynonym, stemmer);
							}

							// If the node is different from currentNode, that
							// means the node is subclass of currentNode.
							// Depending on number of levels down, we assign
							// different weights
							if (!nodePath.equals(currentNodePath))
							{
								matcher = pattern.matcher(ontologyTermSynonym);

								if (!matcher.find() && !StringUtils.isEmpty(ontologyTermSynonym))
								{
									int levelDown = nodePath.split(NODEPATH_SEPARATOR).length - parentNodeLevel;
									double boostedNumber = Math.pow(0.5, levelDown);

									StringBuilder boostedSynonym = new StringBuilder();
									for (String eachToken : ontologyTermSynonym
											.split(OntologyTermQueryRepository.MULTI_WHITESPACES))
									{
										if (eachToken.length() != 0) boostedSynonym
												.append(OntologyTermQueryRepository.SINGLE_WHITESPACE);
										boostedSynonym.append(eachToken).append('^').append(boostedNumber);
									}
									ontologyTermSynonym = boostedSynonym.toString();
								}
							}

							// Add the non-empty one of the ontology term
							// synonyms to the term collection
							if (!StringUtils.isEmpty(ontologyTermSynonym))
							{
								subQueryRules.add(new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS,
										ontologyTermSynonym));
								subQueryRules.add(new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS,
										ontologyTermSynonym));
							}
						}
					}
				}
				// The ontology term is not located inside the description,
				// therefore create Disjunction Max query
				if (finalIndexPosition == locationNotFound)
				{
					if (disJunctQuery.getNestedRules().size() > 0) queryRules.add(disJunctQuery);
				}
				else
				{
					if (disJunctQuery.getNestedRules().size() > 0) shouldQueryRules.add(disJunctQuery);
				}
			}
		}

		// Process should queryRules
		if (shouldQueryRules.size() > 0)
		{
			// If there are multiple rules in the shouldQueryRules list, create
			// a Should QueryRule to hold the list
			if (shouldQueryRules.size() != 1)
			{
				QueryRule combinedQuery = new QueryRule(shouldQueryRules);
				combinedQuery.setOperator(Operator.SHOULD);
				queryRules.add(combinedQuery);
			}
			// Otherwise simply add one query to the disJunctionQuery list
			else
			{
				queryRules.add(shouldQueryRules.get(0));
			}
		}
		return queryRules;
	}

	private List<QueryRule> getAlternativeOTs(String description,
			Collection<OntologyTermContainer> ontologyTermContainers, PorterStemmer stemmer)
	{
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		for (OntologyTermContainer ontologyTermContainer : ontologyTermContainers)
		{
			for (Entry<String, String> entry : ontologyTermContainer.getAlternativeDefinitions().entrySet())
			{
				String alternativeDefinitions = entry.getValue();
				if (!StringUtils.isEmpty(alternativeDefinitions))
				{
					String nodePath = entry.getKey();
					boolean isBoosted = ontologyTermContainer.getAllPaths().get(nodePath);
					for (String definition : alternativeDefinitions.split(ALTERNATIVE_DEFINITION_SEPERATOR))
					{
						List<String> ontologyTermUris = Arrays.asList(definition.split(COMMON_SEPERATOR));
						queryRules.addAll(createQueryRules(
								description,
								collectOntologyTermInfo(ontologyTermUris,
										isBoosted ? ontologyTermUris : Collections.<String> emptyList()), stemmer));
					}
				}
			}
		}
		return queryRules;
	}

	private String removeStopWords(String originalTerm)
	{
		Set<String> tokens = new LinkedHashSet<String>(Arrays.asList(originalTerm.trim().toLowerCase()
				.split(OntologyTermQueryRepository.MULTI_WHITESPACES)));
		tokens.removeAll(NGramMatchingModel.STOPWORDSLIST);
		return StringUtils.join(tokens.toArray(), OntologyTermQueryRepository.SINGLE_WHITESPACE);
	}

	private Integer locateTermInDescription(List<String> uniqueSets, String ontologyTermSynonym, PorterStemmer stemmer)
	{
		int finalIndex = -1;
		List<String> termsFromDescription = stemMembers(
				Arrays.asList(ontologyTermSynonym.split(OntologyTermQueryRepository.MULTI_WHITESPACES)), stemmer);
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
		query.pageSize(Integer.MAX_VALUE);
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
					searchService.updateDocumentById(mappingDataSetIdentifier, hit.getId(),
							constructIndexUpdateScript(STORE_MAPPING_ALGORITHM_SCRIPT, algorithmScript));
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
					searchService.updateDocumentById(mappingDataSetIdentifier, hit.getId(),
							constructIndexUpdateScript(STORE_MAPPING_MAPPED_FEATURE, convertToFeatureIds.toString()));
				}
			}
		}
		return result.getTotalHitCount() > 0;
	}

	/**
	 * A help function to create ElasticSerach update script syntax
	 * 
	 * @param field
	 * @param newValue
	 * @return
	 */
	private String constructIndexUpdateScript(String field, String newValue)
	{
		StringBuilder updateScriptSyntax = new StringBuilder();
		updateScriptSyntax.append(field).append('=').append("\"").append(newValue).append("\"");
		return updateScriptSyntax.toString();
	}

	/**
	 * A helper function to extract featureName from the algorithm script and
	 * convert them to database IDs
	 * 
	 * @param request
	 * @return
	 */
	private String convertToFeatureIds(OntologyMatcherRequest request)
	{
		List<Integer> featureIds = new ArrayList<Integer>();
		if (request.getMappedFeatureIds() != null && request.getMappedFeatureIds().size() > 0)
		{
			return request.getMappedFeatureIds().toString();
		}

		if (!StringUtils.isEmpty(request.getAlgorithmScript()))
		{
			List<String> selectedFeatureNames = new ArrayList<String>();
			for (String standardFeatureName : ApplyAlgorithms.extractFeatureName(request.getAlgorithmScript()))
			{
				selectedFeatureNames.add(standardFeatureName);
			}

			if (selectedFeatureNames.size() > 0)
			{
				Iterable<ObservableFeature> iterators = dataService.findAll(ObservableFeature.ENTITY_NAME,
						new QueryImpl().in(ObservableFeature.NAME, selectedFeatureNames), ObservableFeature.class);
				for (ObservableFeature feature : iterators)
				{
					featureIds.add(feature.getId());
				}
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
}