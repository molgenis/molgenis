package org.molgenis.ontology.search;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.MultiSearchRequest;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.semantic.SemanticSearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.ontology.index.AsyncOntologyIndexer;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.utils.NGramMatchingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	private static final Logger logger = Logger.getLogger(SemanticSearchServiceImpl.class);
	public static final String PROTOCOL_IDENTIFIER = "store_mapping";
	public static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	public static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	public static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	public static final String STORE_MAPPING_SCORE = "store_mapping_score";
	public static final String STORE_MAPPING_ALGORITHM_SCRIPT = "store_mapping_algorithm_script";
	public static final String COMMON_SEPERATOR = ",";
	public static final String ALTERNATIVE_DEFINITION_SEPERATOR = "&&&";
	private static final String CATALOGUE_PREFIX = "protocolTree-";
	private static final String FEATURE_CATEGORY = "featureCategory-";
	private static final String FIELD_DESCRIPTION_STOPWORDS = "descriptionStopwords";
	private static final String FIELD_BOOST_ONTOLOGYTERM = "boostOntologyTerms";
	private static final String ONTOLOGY_IRI = "ontologyIRI";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGY_TERM = "ontologyTerm";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	private static final String NODE_PATH = "nodePath";
	private static final String ENTITY_ID = "id";
	private static final String ENTITY_TYPE = "type";
	private static final String PATTERN_MATCH = "[^a-zA-Z0-9 ]";
	private static final int DEFAULT_RETRIEVAL_DOCUMENTS_SIZE = 50;
	private static final String MULTIPLE_NUMBERS_PATTERN = "[0-9]+";
	private static final String NODEPATH_SEPARATOR = "\\.";
	private static final double DEFAULT_BOOST = 10;

	private final DataService dataService;
	private final SearchService searchService;

	@Autowired
	public SemanticSearchServiceImpl(DataService dataService, SearchService searchService)
	{
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Override
	public Iterable<AttributeMetaData> findAttributes(Package p, AttributeMetaData attributeMetaData)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method is to generate a list of candidate mappings for the chosen
	 * feature by using ElasticSearch based on the information from ontology
	 * terms
	 */
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
				QueryRule subQueryRules_0 = createQueryRulesForDescription(description, stemmer);
				List<QueryRule> subQueryRules_1 = createQueryRules(description, ontologyTermContainers, stemmer);
				List<QueryRule> subQueryRules_2 = getAlternativeOTs(description, ontologyTermContainers, stemmer);
				List<QueryRule> subQueryRules_3 = getExistingMappings(feature, stemmer,
						createMappingDataSetIdentifier(userName, targetDataSetId, sourceDataSetId));

				QueryRule finalQueryRule = new QueryRule(new ArrayList<QueryRule>());
				// Add original description of data items to the query
				finalQueryRule.setOperator(Operator.DIS_MAX);
				finalQueryRule.getNestedRules().add(subQueryRules_0);
				finalQueryRule.getNestedRules().addAll(subQueryRules_1);
				finalQueryRule.getNestedRules().addAll(subQueryRules_2);
				finalQueryRule.getNestedRules().addAll(subQueryRules_3);
				return searchDisMaxQuery(sourceDataSet.getProtocolUsed().getId().toString(), new QueryImpl(
						finalQueryRule));
			}
		}
		return new SearchResult(0, Collections.<Hit> emptyList());
	}

	private QueryRule createQueryRulesForDescription(String description, PorterStemmer stemmer)
	{
		QueryRule queryRule = new QueryRule(new ArrayList<QueryRule>());
		queryRule.getNestedRules().add(
				new QueryRule(FIELD_DESCRIPTION_STOPWORDS, Operator.EQUALS, removeStopWords(description)));
		queryRule.getNestedRules().add(
				new QueryRule(ObservableFeature.DESCRIPTION, Operator.EQUALS, removeStopWords(description)));
		queryRule.setOperator(Operator.DIS_MAX);
		return queryRule;
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

		QueryRule finalQueryRule = new QueryRule(queryRules);
		finalQueryRule.setOperator(Operator.DIS_MAX);
		return queryRules.size() > 0 ? Arrays.<QueryRule> asList(finalQueryRule) : Collections.<QueryRule> emptyList();
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
				// prevent the duplicated ontology terms from being added to the
				// list (because one ontology term could appear mulitple times
				// within one ontology)
				if (!totalHits.get(ontologyIRI).getSelectedOntologyTerms().contains(ontologyTermName))
				{
					String alternativeDefinitions = columnValueMap.get(ALTERNATIVE_DEFINITION) == null ? StringUtils.EMPTY : columnValueMap
							.get(ALTERNATIVE_DEFINITION).toString();
					totalHits.get(ontologyIRI).getAllPaths().put(nodePath, boost);
					totalHits.get(ontologyIRI).getSelectedOntologyTerms().add(ontologyTermName);
					totalHits.get(ontologyIRI).getAlternativeDefinitions().put(nodePath, alternativeDefinitions);
				}
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

		List<QueryRule> disjuncQueryRules = new ArrayList<QueryRule>();
		Map<Integer, List<QueryRule>> shouldQueryRules = new HashMap<Integer, List<QueryRule>>();
		List<String> uniqueTokens = stemMembers(
				Arrays.asList(description.split(OntologyTermQueryRepository.MULTI_WHITESPACES)), stemmer);
		for (OntologyTermContainer ontologyTermContainer : ontologyTermContainers)
		{
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
				disJunctQuery.setValue(entry.getValue() ? DEFAULT_BOOST : null);
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
								ontologyTermSynonym = createQueryWithBoost(ontologyTermSynonym, boostedNumber);
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
				// The ontology term is not located inside the description,
				// therefore create Disjunction Max query
				if (finalIndexPosition == locationNotFound)
				{
					disjuncQueryRules.add(disJunctQuery);
				}
				else
				{
					if (!shouldQueryRules.containsKey(finalIndexPosition))
					{
						shouldQueryRules.put(finalIndexPosition, new ArrayList<QueryRule>());
					}
					shouldQueryRules.get(finalIndexPosition).add(disJunctQuery);
				}

			}
		}

		// Process should queryRules
		QueryRule combinedQuery = new QueryRule(new ArrayList<QueryRule>());
		combinedQuery.setOperator(Operator.SHOULD);
		for (List<QueryRule> rules : shouldQueryRules.values())
		{
			if (rules.size() == 1)
			{
				combinedQuery.getNestedRules().addAll(rules);
			}
			else
			{
				QueryRule disJuncQuery = new QueryRule(rules);
				disJuncQuery.setOperator(Operator.DIS_MAX);
				combinedQuery.getNestedRules().add(disJuncQuery);
			}
		}

		if (combinedQuery.getNestedRules().size() > 0) disjuncQueryRules
				.add(combinedQuery.getNestedRules().size() == 1 ? combinedQuery.getNestedRules().get(0) : combinedQuery);

		return disjuncQueryRules;
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
					for (String alternativeDefinition : alternativeDefinitions.split(ALTERNATIVE_DEFINITION_SEPERATOR))
					{
						List<QueryRule> subQueryRules = new ArrayList<QueryRule>();
						for (String ontologyTermUri : Arrays.asList(alternativeDefinition.split(COMMON_SEPERATOR)))
						{
							List<QueryRule> rules = createQueryRules(
									description,
									collectOntologyTermInfo(
											Arrays.asList(ontologyTermUri),
											isBoosted ? Arrays.asList(ontologyTermUri) : Collections
													.<String> emptyList()), stemmer);
							subQueryRules.addAll(rules);

						}
						if (subQueryRules.size() > 1)
						{
							QueryRule shouldQueryRule = new QueryRule(subQueryRules);
							shouldQueryRule.setOperator(Operator.SHOULD);
							queryRules.add(shouldQueryRule);
						}
						else
						{
							queryRules.addAll(subQueryRules);
						}
					}
				}
			}
		}
		return queryRules;
	}

	private String createQueryWithBoost(String ontologyTermSynonym, double boostedNumber)
	{
		StringBuilder boostedSynonym = new StringBuilder();
		for (String eachToken : ontologyTermSynonym.split(OntologyTermQueryRepository.MULTI_WHITESPACES))
		{
			if (boostedSynonym.length() != 0) boostedSynonym.append(OntologyTermQueryRepository.SINGLE_WHITESPACE);
			boostedSynonym.append(eachToken).append('^').append(boostedNumber);
		}
		ontologyTermSynonym = boostedSynonym.toString();
		return boostedSynonym.toString();
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

		private SemanticSearchServiceImpl getOuterType()
		{
			return SemanticSearchServiceImpl.this;
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