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
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.OntologyTerm;
import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.beans.OntologyEntity;
import org.molgenis.ontology.beans.OntologyImpl;
import org.molgenis.ontology.beans.OntologyServiceResultImpl;
import org.molgenis.ontology.beans.OntologyTermImpl;
import org.molgenis.ontology.beans.OntologyTermTransformer;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.utils.NGramMatchingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.collect.Iterables;

public class OntologyServiceImpl implements OntologyService
{
	private final PorterStemmer stemmer = new PorterStemmer();
	private static final List<String> ELASTICSEARCH_RESERVED_WORDS = Arrays.asList("or", "and", "if");
	private static final String FUZZY_MATCH_SIMILARITY = "~0.8";
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final int MAX_NUMBER_MATCHES = 500;
	public static final String SCORE = "Score";
	public static final Character DEFAULT_SEPARATOR = ';';
	public static final String COMMOM_SEPARATOR = ",";
	public static final String DEFAULT_MATCHING_NAME_FIELD = "name";
	public static final String DEFAULT_MATCHING_SYNONYM_FIELD = "synonym";
	public static final String MAX_SCORE_FIELD = "maxScoreField";
	public static final String ALLOWED_IDENTIFIER = "Identifier";

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
	public Iterable<OntologyTerm> findOntologyTerms(String queryTerm, String ontologyIri)
	{
		return null;
	}

	@Override
	public Iterable<Entity> getAllOntologyEntities()
	{
		Query query = new QueryImpl().eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
				.pageSize(Integer.MAX_VALUE);
		EntityMetaData entityMetaData = dataService.getEntityMetaData(OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO);
		return searchService.search(query, entityMetaData);
	}

	@Override
	public Iterable<Ontology> getAllOntologies()
	{
		return parseEntityToOntology(getAllOntologyEntities());
	}

	@Override
	public Entity getOntologyEntity(String ontologyIri)
	{
		Query query = new QueryImpl().eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
				.and().eq(OntologyIndexRepository.ONTOLOGY_IRI, ontologyIri).pageSize(Integer.MAX_VALUE);
		EntityMetaData entityMetaData = dataService.getEntityMetaData(OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO);
		for (Entity entity : searchService.search(query, entityMetaData))
		{
			return new OntologyEntity(entity, entityMetaData, dataService, searchService, this);
		}
		return null;
	}

	@Override
	public Ontology getOntology(String ontologyIri)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		return ontologyEntity != null ? new OntologyImpl(ontologyEntity) : null;
	}

	@Override
	public Entity getOntologyTermEntity(String ontologyTermIri, String ontologyIri)
	{
		EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getEntityName(ontologyIri));
		Iterable<Entity> listOfOntologyTerms = searchService.search(
				new QueryImpl()
						.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM)
						.and().eq(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI, ontologyTermIri),
				entityMetaDataIndexedOntologyTerm);
		for (Entity entity : listOfOntologyTerms)
		{
			return new OntologyEntity(entity, entityMetaDataIndexedOntologyTerm, dataService, searchService, this);
		}
		return null;
	}

	@Override
	public OntologyTerm getOntologyTerm(String ontologyTermIri, String ontologyIri)
	{
		Entity ontologyTermEntity = getOntologyTermEntity(ontologyTermIri, ontologyIri);
		return ontologyTermEntity != null ? new OntologyTermImpl(ontologyTermEntity, this) : null;
	}

	@Override
	public Iterable<Entity> getAllOntologyTermEntities(String ontologyIri)
	{
		EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getEntityName(ontologyIri));
		Iterable<Entity> listOfOntologyTerms = searchService.search(new QueryImpl().eq(
				OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM),
				entityMetaDataIndexedOntologyTerm);
		return Iterables.transform(listOfOntologyTerms, new OntologyTermTransformer(entityMetaDataIndexedOntologyTerm,
				searchService));
	}

	@Override
	public Iterable<OntologyTerm> getAllOntologyTerms(String ontologyIri)
	{
		return parseEntityToOntologyTerm(getAllOntologyTermEntities(ontologyIri));
	}

	@Override
	public Iterable<Entity> getRootOntologyTermEntities(String ontologyIri)
	{
		EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getEntityName(ontologyIri));
		Iterable<Entity> entities = searchService.search(
				new QueryImpl()
						.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM)
						.and().eq(OntologyTermIndexRepository.ROOT, true), entityMetaDataIndexedOntologyTerm);
		return Iterables.transform(entities, new OntologyTermTransformer(entityMetaDataIndexedOntologyTerm,
				searchService));
	}

	@Override
	public Iterable<OntologyTerm> getRootOntologyTerms(String ontologyIri)
	{
		return parseEntityToOntologyTerm(getRootOntologyTermEntities(ontologyIri));
	}

	@Override
	public Iterable<Entity> getChildOntologyTermEntities(String ontologyIri, String ontologyTermIri)
	{
		EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getEntityName(ontologyIri));
		Query query = new QueryImpl()
				.eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM).and()
				.eq(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, ontologyTermIri);
		Iterable<Entity> entities = searchService.search(query, entityMetaDataIndexedOntologyTerm);

		for (Entity entity : entities)
		{
			String currentNodePath = entity.getString(OntologyTermQueryRepository.NODE_PATH);
			return Iterables.transform(
					getChildren(entityMetaDataIndexedOntologyTerm, ontologyTermIri, currentNodePath),
					new OntologyTermTransformer(entityMetaDataIndexedOntologyTerm, searchService));
		}
		return Collections.emptyList();
	}

	@Override
	public Iterable<OntologyTerm> getChildOntologyTerms(String ontologyIri, String ontologyTermIri)
	{
		return parseEntityToOntologyTerm(getChildOntologyTermEntities(ontologyIri, ontologyTermIri));
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
			if (!StringUtils.isEmpty(inputEntity.getString(attributeName))
					&& !attributeName.equalsIgnoreCase(ALLOWED_IDENTIFIER))
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
						BigDecimal ngramScore = matchOntologyTerm(inputEntity.getString(inputAttrName), entity);
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
								String listOfDatabaseIds = entity.getString(attributeName);
								if (!StringUtils.isEmpty(listOfDatabaseIds) && listOfDatabaseIds.length() > 2)
								{
									for (String databaseId : listOfDatabaseIds.substring(1,
											listOfDatabaseIds.length() - 1).split(COMMOM_SEPARATOR))
									{
										if (databaseId.trim().equalsIgnoreCase(
												inputEntity.getString(inputAttrName).trim()))
										{
											maxNgramScore = new BigDecimal(100);
											maxScoreField = attributeName;
										}
									}
									if (count == 0) inputData.put(inputAttrName, inputEntity.getString(inputAttrName));
								}
							}
						}
					}
				}
			}
			count++;
			comparableEntities.add(new ComparableEntity(entity, maxNgramScore, maxScoreField));
		}
		return convertResults(comparableEntities, inputData);
	}

	public OntologyServiceResult search(String ontologyUrl, String queryString)
	{
		return null;
	}

	/**
	 * This method is to stem the orignal queryString and then create fuzzy match query.
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
				String afterStem = stemmer.getCurrent();
				if (!StringUtils.isEmpty(afterStem))
				{
					stringBuilder.append(afterStem).append(FUZZY_MATCH_SIMILARITY)
							.append(OntologyTermQueryRepository.SINGLE_WHITESPACE);
				}
			}
		}
		return stringBuilder.toString().trim();
	}

	private OntologyServiceResult convertResults(List<ComparableEntity> comparableEntities,
			Map<String, Object> inputData)
	{
		Collections.sort(comparableEntities);
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
				copyEntity.set(SCORE, comparableHit.getSimilarityScore().doubleValue());
				copyEntity.set(MAX_SCORE_FIELD, comparableHit.getMaxScoreField());
				entities.add(copyEntity);
			}
		}
		return new OntologyServiceResultImpl(inputData, entities, comparableEntities.size());
	}

	private BigDecimal matchOntologyTerm(String queryString, Entity entity)
	{
		String ontologyTermSynonym = entity.getString(OntologyTermIndexRepository.SYNONYMS);
		String ontologyTerm = entity.getString(OntologyTermIndexRepository.ONTOLOGY_TERM);

		BigDecimal ngramScore = null;
		if (!ontologyTerm.equalsIgnoreCase(ontologyTermSynonym))
		{
			double score_1 = NGramMatchingModel.stringMatching(queryString, ontologyTerm);
			double score_2 = NGramMatchingModel.stringMatching(queryString, ontologyTermSynonym);

			ngramScore = new BigDecimal(score_1 > score_2 ? score_1 : score_2);
		}
		else
		{
			ngramScore = new BigDecimal(NGramMatchingModel.stringMatching(queryString, ontologyTermSynonym));
		}

		return ngramScore;
	}

	/**
	 * A helper function to parse generic entities to ontology entities
	 * 
	 * @param entities
	 * @return a list of Typed Ontology entities
	 */
	public Iterable<Ontology> parseEntityToOntology(Iterable<Entity> entities)
	{
		final Iterator<Entity> iterator = entities.iterator();
		return new Iterable<Ontology>()
		{
			@Override
			public Iterator<Ontology> iterator()
			{
				return new Iterator<Ontology>()
				{
					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public Ontology next()
					{
						return new OntologyImpl(iterator.next());
					}

					@Override
					public void remove()
					{
					}
				};
			}
		};
	}

	/**
	 * A helper function to parse generic entities to ontologyTerm entities
	 * 
	 * @param entityMetaData
	 * @param entities
	 * @return a list of Typed OntologyTerm entities
	 */
	public Iterable<OntologyTerm> parseEntityToOntologyTerm(Iterable<Entity> entities)
	{
		final OntologyService ontologyService = this;
		final Iterator<Entity> iterator = entities.iterator();
		return new Iterable<OntologyTerm>()
		{
			@Override
			public Iterator<OntologyTerm> iterator()
			{
				return new Iterator<OntologyTerm>()
				{
					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public OntologyTerm next()
					{
						return new OntologyTermImpl(iterator.next(), ontologyService);
					}

					@Override
					public void remove()
					{

					}
				};
			}
		};
	}

	private String getEntityName(String ontologyIri)
	{
		return getOntology(ontologyIri).getLabel();
	}
}