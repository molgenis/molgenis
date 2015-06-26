package org.molgenis.ontology.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.beans.Ontology;
import org.molgenis.ontology.beans.OntologyImpl;
import org.molgenis.ontology.beans.OntologyServiceResult;
import org.molgenis.ontology.beans.OntologyTerm;
import org.molgenis.ontology.beans.OntologyTermImpl;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.utils.OntologyServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

public class OntologyServiceImpl implements OntologyService
{
	private static final List<String> ELASTICSEARCH_RESERVED_WORDS = Arrays.asList("or", "and", "if");
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final String ILLEGAL_CHARACTERS_PATTERN = "[^a-zA-Z0-9 ]";
	private static final String FUZZY_MATCH_SIMILARITY = "~0.8";
	private static final String SINGLE_WHITESPACE = " ";
	private static final int MAX_NUMBER_MATCHES = 500;

	// Global fields that are used by other classes
	public static final String SIGNIFICANT_VALUE = "Significant";
	public static final Character DEFAULT_SEPARATOR = ';';
	public static final String DEFAULT_MATCHING_NAME_FIELD = "Name";
	public static final String DEFAULT_MATCHING_SYNONYM_FIELD = "Synonym";
	public static final String DEFAULT_MATCHING_IDENTIFIER = "Identifier";
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";

	private final PorterStemmer stemmer = new PorterStemmer();
	private final DataService dataService;
	private final SearchService searchService;
	private final InformationContentService informationContentService;

	@Autowired
	public OntologyServiceImpl(DataService dataService, SearchService searchService,
			InformationContentService informationContentService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (searchService == null) throw new IllegalArgumentException("SearchService is null");
		if (informationContentService == null) throw new IllegalArgumentException("InformationContentService is null");
		this.dataService = dataService;
		this.searchService = searchService;
		this.informationContentService = informationContentService;
	}

	@Override
	public Iterable<Ontology> getAllOntologies()
	{
		// TODO
		return null;
	}

	@Override
	public Iterable<Entity> getAllOntologyEntities()
	{
		return dataService.findAll(OntologyMetaData.ENTITY_NAME);
	}

	@Override
	public Ontology getOntology(String ontologyIri)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		return ontologyEntity == null ? null : new OntologyImpl(ontologyEntity);
	}

	@Override
	public Entity getOntologyEntity(String ontologyIri)
	{
		return dataService.findOne(OntologyMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri));
	}

	@Override
	public Iterable<OntologyTerm> findOntologyTerms(String queryTerm, String ontologyIri)
	{
		return null;
	}

	@Override
	public OntologyTerm getOntologyTerm(String ontologyTermIri, String ontologyIri)
	{
		Entity ontologyTermEntity = getOntologyTermEntity(ontologyTermIri, ontologyIri);
		return ontologyTermEntity == null ? null : new OntologyTermImpl(ontologyTermEntity);
	}

	@Override
	public Entity getOntologyTermEntity(String ontologyTermIri, String ontologyIri)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		if (ontologyEntity != null)
		{
			return dataService.findOne(
					OntologyTermMetaData.ENTITY_NAME,
					new QueryImpl().eq(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ontologyTermIri).and()
							.eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity));
		}
		return null;
	}

	@Override
	public List<String> getOntologyTermSynonyms(String ontologyTermIri, String ontologyIri)
	{
		// TODO
		return null;
	}

	@Override
	public Iterable<OntologyTerm> getAllOntologyTerms(String ontologyIri)
	{
		// TODO
		return null;
	}

	@Override
	public Iterable<Entity> getAllOntologyTermEntities(String ontologyIri)
	{
		// TODO
		return null;
	}

	@Override
	public Iterable<OntologyTerm> getRootOntologyTerms(String ontologyIri)
	{
		// TODO
		return null;
	}

	@Override
	public Iterable<Entity> getRootOntologyTermEntities(String ontologyIri)
	{
		// TODO
		return null;
	}

	@Override
	public Iterable<OntologyTerm> getChildOntologyTerms(String ontologyIri, String ontologyTermIri)
	{
		return null;
	}

	@Override
	public Iterable<Entity> getChildOntologyTermEntities(String ontologyIri, String ontologyTermIri)
	{
		// TODO
		return null;
	}

	@Override
	public OntologyServiceResult searchEntity(String ontologyIri, Entity inputEntity)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		if (ontologyEntity == null) throw new IllegalArgumentException("Ontology IRI " + ontologyIri
				+ " does not exist in the database!");

		List<Entity> relevantEntities = new ArrayList<Entity>();

		List<QueryRule> rulesForOntologyTermFields = new ArrayList<QueryRule>();
		List<QueryRule> rulesForOtherFields = new ArrayList<QueryRule>();
		for (String attributeName : inputEntity.getAttributeNames())
		{
			if (StringUtils.isNotEmpty(inputEntity.getString(attributeName))
					&& !attributeName.equalsIgnoreCase(DEFAULT_MATCHING_IDENTIFIER))
			{
				// The attribute name is either equal to 'Name' or starts
				// with string 'Synonym'
				if (DEFAULT_MATCHING_NAME_FIELD.equalsIgnoreCase(attributeName)
						|| attributeName.toLowerCase().startsWith(DEFAULT_MATCHING_SYNONYM_FIELD.toLowerCase()))
				{
					String medicalStemProxy = fuzzyMatchQuerySyntax(inputEntity.getString(attributeName));
					if (StringUtils.isNotEmpty(medicalStemProxy))
					{
						rulesForOntologyTermFields.add(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
								Operator.FUZZY_MATCH, medicalStemProxy));
					}
				}
				else if (StringUtils.isNotEmpty(inputEntity.getString(attributeName)))
				{
					rulesForOtherFields.add(new QueryRule(attributeName, Operator.EQUALS, inputEntity
							.getString(attributeName)));
				}
			}
		}

		List<QueryRule> combinedRules = new ArrayList<QueryRule>();

		if (rulesForOntologyTermFields.size() > 0)
		{
			QueryRule disMaxQuery_1 = new QueryRule(rulesForOntologyTermFields);
			disMaxQuery_1.setOperator(Operator.DIS_MAX);
			combinedRules.add(disMaxQuery_1);
		}

		if (rulesForOtherFields.size() > 0)
		{
			QueryRule disMaxQuery_2 = new QueryRule(rulesForOtherFields);
			disMaxQuery_2.setOperator(Operator.DIS_MAX);
			combinedRules.add(disMaxQuery_2);
		}

		if (combinedRules.size() > 0)
		{
			QueryRule queryRule = new QueryRule(combinedRules);
			queryRule.setOperator(Operator.DIS_MAX);

			List<QueryRule> finalQueryRules = Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY,
					Operator.EQUALS, ontologyEntity), new QueryRule(Operator.AND), queryRule);

			EntityMetaData entityMetaData = dataService.getEntityMetaData(OntologyTermMetaData.ENTITY_NAME);
			for (Entity entity : searchService.search(new QueryImpl(finalQueryRules).pageSize(MAX_NUMBER_MATCHES),
					entityMetaData))
			{
				double maxNgramScore = 0;
				double maxNgramIDFScore = 0;
				for (String inputAttrName : inputEntity.getAttributeNames())
				{
					String queryString = inputEntity.getString(inputAttrName);
					if (StringUtils.isNotEmpty(queryString))
					{
						if (DEFAULT_MATCHING_NAME_FIELD.equalsIgnoreCase(inputAttrName)
								|| inputAttrName.toLowerCase().startsWith(DEFAULT_MATCHING_SYNONYM_FIELD.toLowerCase()))
						{
							Entity topMatchedSynonymEntity = calculateNGramOTSynonyms(ontologyIri, queryString, entity);
							if (maxNgramScore < topMatchedSynonymEntity.getDouble(SCORE))
							{
								maxNgramScore = topMatchedSynonymEntity.getDouble(SCORE);
							}
							if (maxNgramIDFScore < topMatchedSynonymEntity.getDouble(COMBINED_SCORE))
							{
								maxNgramIDFScore = topMatchedSynonymEntity.getDouble(COMBINED_SCORE);
							}
						}
						else
						{
							// TODO : implement the scenario where database annotations are used in matching
						}
					}
				}
				MapEntity mapEntity = new MapEntity();
				for (String attributeName : entity.getAttributeNames())
				{
					mapEntity.set(attributeName, entity.get(attributeName));
				}
				mapEntity.set(SCORE, maxNgramScore);
				mapEntity.set(COMBINED_SCORE, maxNgramIDFScore);
				relevantEntities.add(mapEntity);
			}
		}

		Collections.sort(relevantEntities, new Comparator<Entity>()
		{
			public int compare(Entity entity1, Entity entity2)
			{
				return entity2.getDouble(COMBINED_SCORE).compareTo(entity1.getDouble(COMBINED_SCORE));
			}
		});

		return new OntologyServiceResult(OntologyServiceUtil.getEntityAsMap(inputEntity), relevantEntities,
				relevantEntities.size());
	}

	@Override
	public OntologyServiceResult search(String ontologyUrl, String queryString)
	{
		Entity entity = new MapEntity();
		entity.set(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD, queryString);
		return searchEntity(ontologyUrl, entity);
	}

	/**
	 * A helper function to calculate the best NGram score from a list ontologyTerm synonyms
	 * 
	 * @param queryString
	 * @param entity
	 * @return
	 */
	private Entity calculateNGramOTSynonyms(String ontologyIri, String queryString, Entity entity)
	{
		Iterable<Entity> entities = entity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM);
		if (Iterables.size(entities) > 0)
		{
			List<MapEntity> synonymEntities = FluentIterable.from(entities).transform(new Function<Entity, MapEntity>()
			{
				public MapEntity apply(Entity input)
				{
					MapEntity mapEntity = new MapEntity();
					for (String attrName : input.getAttributeNames())
						mapEntity.set(attrName, input.get(attrName));

					String ontologyTermSynonym = removeIllegalCharWithSingleWhiteSpace(input
							.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM));
					double score_1 = NGramDistanceAlgorithm.stringMatching(queryString, ontologyTermSynonym);
					mapEntity.set(SCORE, score_1);

					return mapEntity;
				}

			}).toSortedList(new Comparator<MapEntity>()
			{
				public int compare(MapEntity o1, MapEntity o2)
				{
					return o2.getDouble(SCORE).compareTo(o1.getDouble(SCORE));
				}
			});

			Entity topMatchedSynonymEntity = synonymEntities.get(0);
			double ngramScore = topMatchedSynonymEntity.getDouble(SCORE);
			String topMatchedSynonym = topMatchedSynonymEntity
					.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);

			for (int j = 1; j < synonymEntities.size(); j++)
			{
				Entity nextMatchedSynonymEntity = synonymEntities.get(j);
				String nextMatchedSynonym = nextMatchedSynonymEntity
						.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);

				StringBuilder tempCombinedSynonym = new StringBuilder().append(topMatchedSynonym)
						.append(SINGLE_WHITESPACE).append(nextMatchedSynonym);

				double newScore = NGramDistanceAlgorithm.stringMatching(queryString.replaceAll(ILLEGAL_CHARACTERS_PATTERN,
						SINGLE_WHITESPACE),
						tempCombinedSynonym.toString().replaceAll(ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE));

				if (newScore > ngramScore)
				{
					ngramScore = newScore;
					topMatchedSynonym = tempCombinedSynonym.toString();
				}
			}

			topMatchedSynonymEntity.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, topMatchedSynonym);
			topMatchedSynonymEntity.set(SCORE, ngramScore);
			topMatchedSynonymEntity.set(COMBINED_SCORE, ngramScore);

			Map<String, Double> weightedWordSimilarity = informationContentService.redistributedNGramScore(queryString,
					ontologyIri);

			Set<String> synonymStemmedWordSet = informationContentService.createStemmedWordSet(topMatchedSynonym);

			for (String originalWord : informationContentService.createStemmedWordSet(queryString))
			{
				if (synonymStemmedWordSet.contains(originalWord) && weightedWordSimilarity.containsKey(originalWord))
				{
					topMatchedSynonymEntity.set(COMBINED_SCORE,
							(topMatchedSynonymEntity.getDouble(COMBINED_SCORE) + weightedWordSimilarity
									.get(originalWord)));
				}
			}
			return topMatchedSynonymEntity;
		}
		return null;
	}

	/**
	 * A helper function to produce fuzzy match query in elasticsearch
	 * 
	 * @param queryString
	 * @return
	 */
	private String fuzzyMatchQuerySyntax(String queryString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> uniqueTerms = Sets.newHashSet(queryString.toLowerCase().trim().split(NON_WORD_SEPARATOR));
		uniqueTerms.removeAll(NGramDistanceAlgorithm.STOPWORDSLIST);
		for (String term : uniqueTerms)
		{
			if (StringUtils.isNotEmpty(term.trim()) && !(ELASTICSEARCH_RESERVED_WORDS.contains(term)))
			{
				stemmer.setCurrent(removeIllegalCharWithEmptyString(term));
				stemmer.stem();
				String afterStem = stemmer.getCurrent();
				if (StringUtils.isNotEmpty(afterStem))
				{
					stringBuilder.append(afterStem).append(FUZZY_MATCH_SIMILARITY).append(SINGLE_WHITESPACE);
				}
			}
		}
		return stringBuilder.toString().trim();
	}

	public String removeIllegalCharWithSingleWhiteSpace(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE);
	}

	public String removeIllegalCharWithEmptyString(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, StringUtils.EMPTY);
	}
}