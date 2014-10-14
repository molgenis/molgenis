package org.molgenis.ontology.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semantic.Ontology;
import org.molgenis.data.semantic.OntologyService;
import org.molgenis.data.semantic.OntologyServiceResult;
import org.molgenis.data.semantic.OntologyTerm;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.tree.OntologyEntity;
import org.molgenis.ontology.tree.OntologyTermEntity;
import org.molgenis.ontology.utils.NGramMatchingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.tartarus.snowball.ext.PorterStemmer;

public class OntologyServiceImpl implements OntologyService
{
	private final PorterStemmer stemmer = new PorterStemmer();
	private static final List<String> ELASTICSEARCH_RESERVED_WORDS = Arrays.asList("or", "and", "if");
	private static final String COMBINED_SCORE = "combinedScore";
	private static final String FUZZY_MATCH_SIMILARITY = "~0.8";
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final int MAX_NUMBER_MATCHES = 100;
	public static final Character DEFAULT_SEPARATOR = ';';
	public static final String DEFAULT_MATCHING_NAME_FIELD = "name";
	public static final String DEFAULT_MATCHING_SYNONYM_FIELD = "synonym";
	private static final String MAX_SCORE_FIELD = "maxScoreField";

	private final SearchService searchService;
	private final DataService dataService;

	@Autowired
	public OntologyServiceImpl(SearchService searchService, DataService dataService)
	{
		if (searchService == null) throw new IllegalArgumentException("SearchService is null");
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.searchService = searchService;
		this.dataService = dataService;
	}

	@Override
	public Iterable<Ontology> getAllOntologies()
	{
		Query query = new QueryImpl().eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
				.pageSize(Integer.MAX_VALUE);
		EntityMetaData entityMetaData = dataService.getEntityMetaData(OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO);
		List<Ontology> parseEntityToOntology = parseEntityToOntology(entityMetaData,
				searchService.search(query, entityMetaData));
		return parseEntityToOntology;
	}

	@Override
	public Ontology getOntology(String ontologyIri)
	{
		Query query = new QueryImpl().eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
				.and().eq(OntologyIndexRepository.ONTOLOGY_IRI, ontologyIri).pageSize(Integer.MAX_VALUE);
		EntityMetaData entityMetaData = dataService.getEntityMetaData(OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO);
		List<Ontology> parseEntityToOntology = parseEntityToOntology(entityMetaData,
				searchService.search(query, entityMetaData));
		return parseEntityToOntology.size() > 0 ? parseEntityToOntology.get(0) : null;
	}

	@Override
	public Iterable<OntologyTerm> findOntologyTerms(String queryTerm, String ontologyIri)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OntologyTerm getOntologyTerm(String ontologyTermIri, String ontologyIri)
	{
		Ontology ontology = getOntology(ontologyIri);
		if (ontology != null)
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService
					.getEntityMetaData(getEntityName(ontologyIri));
			Iterable<Entity> listOfOntologyTerms = searchService.search(
					new QueryImpl()
							.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM)
							.and().eq(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI, ontologyTermIri),
					entityMetaDataIndexedOntologyTerm);
			List<OntologyTerm> parseEntityToOntologyTerm = parseEntityToOntologyTerm(entityMetaDataIndexedOntologyTerm,
					listOfOntologyTerms);
			return parseEntityToOntologyTerm.size() > 0 ? parseEntityToOntologyTerm.get(0) : null;
		}
		return null;
	}

	@Override
	public Iterable<OntologyTerm> getAllOntologyTerms(String ontologyIri)
	{
		Ontology ontology = getOntology(ontologyIri);
		if (ontology != null)
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService
					.getEntityMetaData(getEntityName(ontologyIri));
			Iterable<Entity> listOfOntologyTerms = searchService.search(new QueryImpl().eq(
					OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM),
					entityMetaDataIndexedOntologyTerm);
			return parseEntityToOntologyTerm(entityMetaDataIndexedOntologyTerm, listOfOntologyTerms);
		}
		return Collections.emptyList();
	}

	private String getEntityName(String ontologyIri)
	{
		return getOntology(ontologyIri).getLabel();
	}

	@Override
	public Iterable<OntologyTerm> getRootOntologyTerms(String ontologyIri)
	{
		EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getEntityName(ontologyIri));
		Iterable<Entity> listOfOntologyTerms = searchService.search(
				new QueryImpl()
						.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM)
						.and().eq(OntologyTermIndexRepository.ROOT, true), entityMetaDataIndexedOntologyTerm);
		return parseEntityToOntologyTerm(entityMetaDataIndexedOntologyTerm, listOfOntologyTerms);
	}

	@Override
	public Iterable<OntologyTerm> getChildOntologyTerms(String ontologyIri, String ontologyTermIri)
	{
		Ontology ontology = getOntology(ontologyIri);
		if (ontology != null)
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService
					.getEntityMetaData(getEntityName(ontologyIri));
			Query query = new QueryImpl()
					.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM).and()
					.eq(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, ontologyTermIri);
			Iterable<Entity> listOfOntologyTerms = searchService.search(query, entityMetaDataIndexedOntologyTerm);

			if (Iterables.size(listOfOntologyTerms) > 0)
			{
				Iterator<Entity> iterator = listOfOntologyTerms.iterator();
				Entity entity = iterator.next();
				String currentNodePath = entity.getString(OntologyTermQueryRepository.NODE_PATH);
				Iterable<Entity> childEntities = getChildren(entityMetaDataIndexedOntologyTerm, ontologyTermIri,
						currentNodePath);
				return parseEntityToOntologyTerm(entityMetaDataIndexedOntologyTerm, childEntities);
			}
		}
		return Collections.emptyList();
	}

	private Iterable<Entity> getChildren(EntityMetaData entityMetaData, String parentOntologyTermIri,
			String parentNodePath)
	{
		Query query = new QueryImpl()
				.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM).and()
				.eq(OntologyTermIndexRepository.PARENT_NODE_PATH, parentNodePath).and()
				.eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, parentOntologyTermIri)
				.pageSize(Integer.MAX_VALUE);
		return searchService.search(query, entityMetaData);
	}

	public OntologyServiceResult searchEntity(String ontologyIri, Entity inputEntity)
	{
		List<QueryRule> allQueryRules = new ArrayList<QueryRule>();
		List<QueryRule> rulesForOntologyTermFields = new ArrayList<QueryRule>();
		for (String attributeName : inputEntity.getAttributeNames())
		{
			if (!StringUtils.isEmpty(inputEntity.getString(attributeName)))
			{
				// The attribute name is either equal to 'Name' or starts
				// with string 'Synonym'
				if (DEFAULT_MATCHING_NAME_FIELD.equals(attributeName.toLowerCase())
						|| attributeName.toLowerCase().startsWith(DEFAULT_MATCHING_SYNONYM_FIELD))
				{
					String medicalStemProxy = medicalStemProxy(inputEntity.getString(attributeName));
					if (!StringUtils.isEmpty(medicalStemProxy))
					{
						rulesForOntologyTermFields.add(new QueryRule(OntologyTermIndexRepository.SYNONYMS,
								Operator.EQUALS, medicalStemProxy));
					}
				}
				else if (inputEntity.get(attributeName) != null
						&& !StringUtils.isEmpty(inputEntity.get(attributeName).toString()))
				{
					allQueryRules.add(new QueryRule(attributeName, Operator.EQUALS, inputEntity.get(attributeName)));
				}
			}
		}

		QueryRule nestedQueryRule = new QueryRule(rulesForOntologyTermFields);
		nestedQueryRule.setOperator(Operator.DIS_MAX);
		allQueryRules.add(nestedQueryRule);

		QueryRule finalQueryRule = new QueryRule(allQueryRules);
		finalQueryRule.setOperator(Operator.DIS_MAX);

		List<ComparableEntity> comparableEntities = new ArrayList<ComparableEntity>();
		Map<String, Object> inputData = new HashMap<String, Object>();
		String maxScoreField = null;
		int count = 0;
		EntityMetaData entityMetaData = dataService.getEntityMetaData(getEntityName(ontologyIri));
		for (Entity entity : searchService.search(new QueryImpl(finalQueryRule).pageSize(MAX_NUMBER_MATCHES),
				entityMetaData))
		{
			BigDecimal maxNgramScore = new BigDecimal(0);
			for (String inputAttrName : inputEntity.getAttributeNames())
			{
				if (!StringUtils.isEmpty(inputEntity.getString(inputAttrName)))
				{
					if (DEFAULT_MATCHING_NAME_FIELD.equals(inputAttrName.toLowerCase())
							|| inputAttrName.toLowerCase().startsWith(DEFAULT_MATCHING_SYNONYM_FIELD))
					{
						BigDecimal ngramScore = new BigDecimal(NGramMatchingModel.stringMatching(
								inputEntity.getString(inputAttrName),
								entity.getString(OntologyTermIndexRepository.SYNONYMS)));
						if (maxNgramScore.doubleValue() < ngramScore.doubleValue())
						{
							maxNgramScore = ngramScore;
							maxScoreField = inputAttrName;
						}
						if (count == 0) inputData.put(inputAttrName, inputEntity.getString(inputAttrName));
					}
					else
					{
						for (String attributeName : entity.getAttributeNames())
						{
							// Check if indexed ontology term contains such
							// external database reference
							if (attributeName.equalsIgnoreCase(inputAttrName))
							{
								if (!StringUtils.isEmpty(entity.getString(attributeName)))
								{
									for (Object databaseId : (List<?>) entity.get(attributeName))
									{
										BigDecimal ngramScore = new BigDecimal(NGramMatchingModel.stringMatching(
												inputEntity.getString(attributeName), databaseId.toString()));
										if (maxNgramScore.doubleValue() < ngramScore.doubleValue())
										{
											maxNgramScore = ngramScore;
											maxScoreField = attributeName;
										}
									}
									if (count == 0) inputData.put(attributeName, inputEntity.getString(attributeName));
								}
							}
						}
					}
				}
			}
			count++;
			comparableEntities.add(new ComparableEntity(entity, maxNgramScore, maxScoreField));

		}
		Collections.sort(comparableEntities);
		return convertResults(inputData, comparableEntities, entityMetaData);
	}

	public OntologyServiceResult search(String ontologyUrl, String queryString)
	{
		return null;
	}

	/**
	 * A helper function to parse generic entities to ontology entities
	 * 
	 * @param entityMetaData
	 * @param entities
	 * @return a list of Typed Ontology entities
	 */
	private List<Ontology> parseEntityToOntology(EntityMetaData entityMetaData, Iterable<Entity> entities)
	{
		List<Ontology> ontologies = new ArrayList<Ontology>();
		for (Entity entity : entities)
		{
			ontologies.add(new OntologyEntity(entity, entityMetaData, this, searchService, dataService));
		}
		return ontologies;
	}

	/**
	 * A helper function to parse generic entities to ontologyTerm entities
	 * 
	 * @param entityMetaData
	 * @param entities
	 * @return a list of Typed OntologyTerm entities
	 */
	private List<OntologyTerm> parseEntityToOntologyTerm(EntityMetaData entityMetaData, Iterable<Entity> entities)
	{
		List<OntologyTerm> ontologyTerms = new ArrayList<OntologyTerm>();
		for (Entity entity : entities)
		{
			ontologyTerms.add(new OntologyTermEntity(entity, entityMetaData, searchService, dataService, this));
		}
		return ontologyTerms;
	}

	/**
	 * This method is to stem the orignal queryString and then create fuzzy
	 * match query.
	 * 
	 * @param queryString
	 * @return a fuzzymatch query for elasticsearch
	 */
	private String medicalStemProxy(String queryString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(queryString.toLowerCase().trim()
				.split(NON_WORD_SEPARATOR)));
		uniqueTerms.removeAll(NGramMatchingModel.STOPWORDSLIST);
		for (String term : uniqueTerms)
		{
			if (!StringUtils.isEmpty(term) && !term.matches(OntologyTermQueryRepository.MULTI_WHITESPACES)
					&& !(ELASTICSEARCH_RESERVED_WORDS.contains(term)))
			{
				stemmer.setCurrent(term.replaceAll(OntologyTermQueryRepository.ILLEGAL_CHARACTERS_PATTERN,
						StringUtils.EMPTY));
				stemmer.stem();
				stringBuilder.append(stemmer.getCurrent()).append(FUZZY_MATCH_SIMILARITY)
						.append(OntologyTermQueryRepository.SINGLE_WHITESPACE);
			}
		}
		return stringBuilder.toString().trim();
	}

	private OntologyServiceResult convertResults(Map<String, Object> inputData,
			List<ComparableEntity> comparableEntities, EntityMetaData entityMetaData)
	{
		List<Entity> entities = new ArrayList<Entity>();
		Set<String> uniqueIdentifiers = new HashSet<String>();
		for (ComparableEntity comparableHit : comparableEntities)
		{
			Entity entity = comparableHit.getEntity();
			String identifier = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
			if (!uniqueIdentifiers.contains(identifier))
			{
				uniqueIdentifiers.add(identifier);
				MapEntity copyEntity = new MapEntity();
				for (String attributeName : entity.getAttributeNames())
				{
					copyEntity.set(attributeName, entity.get(attributeName));
				}
				copyEntity.set(COMBINED_SCORE, comparableHit.getSimilarityScore().doubleValue());
				copyEntity.set(MAX_SCORE_FIELD, comparableHit.getMaxScoreField());
				entities.add(copyEntity);
			}
		}
		return new OntologyServiceResult(inputData, parseEntityToOntologyTerm(entityMetaData, entities),
				entities.size());
	}
}