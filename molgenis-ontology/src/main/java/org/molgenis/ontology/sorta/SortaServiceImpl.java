package org.molgenis.ontology.sorta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.repository.model.OntologyTerm;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.utils.NGramMatchingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

public class SortaServiceImpl implements SortaService
{
	private static final Set<String> ELASTICSEARCH_RESERVED_WORDS = Sets.newHashSet("or", "and", "if");
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final String ILLEGAL_CHARACTERS_PATTERN = "[^a-zA-Z0-9 ]";
	private static final String FUZZY_MATCH_SIMILARITY = "~0.8";
	private static final String SINGLE_WHITESPACE = " ";
	private static final int MAX_NUMBER_MATCHES = 100;

	// Global fields that are used by other classes
	public static final String SIGNIFICANT_VALUE = "Significant";
	public static final Character DEFAULT_SEPARATOR = ';';
	public static final String DEFAULT_MATCHING_NAME_FIELD = "Name";
	public static final String DEFAULT_MATCHING_SYNONYM_PREFIX_FIELD = "Synonym";
	public static final String DEFAULT_MATCHING_IDENTIFIER = "Identifier";
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";

	private final PorterStemmer stemmer = new PorterStemmer();
	private final DataService dataService;
	private final InformationContentService informationContentService;

	@Autowired
	public SortaServiceImpl(DataService dataService, InformationContentService informationContentService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (informationContentService == null) throw new IllegalArgumentException("InformationContentService is null");
		this.dataService = dataService;
		this.informationContentService = informationContentService;
	}

	@Override
	public Iterable<Entity> getAllOntologyEntities()
	{
		return dataService.findAll(OntologyMetaData.ENTITY_NAME);
	}

	@Override
	public Entity getOntologyEntity(String ontologyIri)
	{
		return dataService.findOne(OntologyMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri));
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
	public Iterable<OntologyTerm> findOntologyTerms(String ontologyIri, Entity inputEntity)
	{
		Iterable<Entity> ontologyTermEntities = findOntologyTermEntities(ontologyIri, inputEntity);
		return Iterables.transform(ontologyTermEntities, SortaServiceImpl::transformOntologyTermEntity);
	}

	@Override
	public Iterable<OntologyTerm> findOntologyTerms(String ontologyIri, String queryString)
	{
		Iterable<Entity> ontologyTermEntities = findOntologyTermEntities(ontologyIri, queryString);
		return Iterables.transform(ontologyTermEntities, SortaServiceImpl::transformOntologyTermEntity);
	}

	/**
	 * A helper function to convert generic Entity to typed ontologyterm class
	 *
	 * @param ontologyTermEntity
	 * @return
	 */
	private static OntologyTerm transformOntologyTermEntity(Entity ontologyTermEntity)
	{
		String ontologyTermIri = ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI);
		String ontologyTermName = ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME);
		List<String> synonyms = FluentIterable
				.from(ontologyTermEntity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM))
				.transform(new Function<Entity, String>()
				{
					public String apply(Entity ontologyTermSynonymEntity)
					{
						return ontologyTermSynonymEntity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);
					}
				}).filter(new Predicate<String>()
				{
					public boolean apply(String synonym)
					{
						return !synonym.equalsIgnoreCase(ontologyTermName);
					}
				}).toList();

		return OntologyTerm.create(ontologyTermIri, ontologyTermName, StringUtils.EMPTY, synonyms);
	}

	@Override
	public Iterable<Entity> findOntologyTermEntities(String ontologyUrl, String queryString)
	{
		Entity entity = new MapEntity(Collections.singletonMap(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD,
				queryString));
		return findOntologyTermEntities(ontologyUrl, entity);
	}

	@Override
	public Iterable<Entity> findOntologyTermEntities(String ontologyIri, Entity inputEntity)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		if (ontologyEntity == null) throw new IllegalArgumentException("Ontology IRI " + ontologyIri
				+ " does not exist in the database!");

		// a list to store most relevant entities
		List<Entity> relevantEntities = new ArrayList<Entity>();
		// query rules for ontology anntations, e.g. OMIM:124343
		List<QueryRule> rulesForOtherFields = new ArrayList<QueryRule>();
		// query rules for ontology name and synonyms, e.g. name = proptosis, sysnonym = protruding eye
		List<QueryRule> rulesForOntologyTermFields = new ArrayList<QueryRule>();

		for (String attributeName : inputEntity.getAttributeNames())
		{
			if (StringUtils.isNotEmpty(inputEntity.getString(attributeName))
					&& !attributeName.equalsIgnoreCase(DEFAULT_MATCHING_IDENTIFIER))
			{
				// The attribute name is either equal to 'Name' or starts with string 'Synonym'
				if (isAttrNameValidForLexicalMatch(attributeName))
				{
					String medicalStemProxy = fuzzyMatchQuerySyntax(inputEntity.getString(attributeName));
					if (StringUtils.isNotEmpty(medicalStemProxy))
					{
						rulesForOntologyTermFields.add(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
								Operator.FUZZY_MATCH, medicalStemProxy));
					}
				}
				else
				{
					QueryRule queryAnnotationName = new QueryRule(OntologyTermDynamicAnnotationMetaData.NAME,
							Operator.EQUALS, attributeName);
					QueryRule queryAnnotationValue = new QueryRule(OntologyTermDynamicAnnotationMetaData.VALUE,
							Operator.EQUALS, inputEntity.getString(attributeName));

					// ((name=OMIM Operator.AND value=124325) Operator.OR (name=HPO Operator.AND value=hp12435))
					if (rulesForOtherFields.size() > 0) rulesForOtherFields.add(new QueryRule(Operator.OR));
					rulesForOtherFields.add(new QueryRule(Arrays.asList(queryAnnotationName,
							new QueryRule(Operator.AND), queryAnnotationValue)));
				}
			}
		}

		// Find the ontology terms that have the same annotations as the input ontology annotations
		if (rulesForOtherFields.size() > 0)
		{
			Iterable<Entity> ontologyTermAnnotationEntities = dataService.findAll(
					OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
					new QueryImpl(rulesForOtherFields).pageSize(Integer.MAX_VALUE));

			if (Iterables.size(ontologyTermAnnotationEntities) > 0)
			{
				List<QueryRule> rules = Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS,
						ontologyEntity), new QueryRule(Operator.AND), new QueryRule(
						OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, Operator.IN,
						ontologyTermAnnotationEntities));

				Iterable<Entity> ontologyTermEntities = dataService.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(rules).pageSize(Integer.MAX_VALUE));

				Iterable<Entity> relevantOntologyTermEntities = FluentIterable.from(ontologyTermEntities).transform(
						new Function<Entity, Entity>()
						{
							public Entity apply(Entity ontologyTermEntity)
							{
								return calculateNGromOTAnnotations(inputEntity, ontologyTermEntity);
							}
						});
				relevantEntities.addAll(ImmutableList.copyOf(relevantOntologyTermEntities));
			}
		}

		// Find the ontology terms based on the lexical similarities
		if (rulesForOntologyTermFields.size() > 0)
		{
			QueryRule disMaxQueryRule = new QueryRule(rulesForOntologyTermFields);
			disMaxQueryRule.setOperator(Operator.DIS_MAX);

			List<QueryRule> finalQueryRules = Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY,
					Operator.EQUALS, ontologyEntity), new QueryRule(Operator.AND), disMaxQueryRule);

			int pageSize = MAX_NUMBER_MATCHES - relevantEntities.size();

			Iterable<Entity> ontologyTermEntities = dataService.findAll(OntologyTermMetaData.ENTITY_NAME,
					new QueryImpl(finalQueryRules).pageSize(pageSize));

			Iterable<Entity> lexicalMatchedOntologyTermEntities = FluentIterable.from(ontologyTermEntities).transform(
					new Function<Entity, Entity>()
					{
						public Entity apply(Entity matchedOntologyTermEntity)
						{
							double maxNgramScore = 0;
							double maxNgramIDFScore = 0;
							for (String inputAttrName : inputEntity.getAttributeNames())
							{
								String queryString = inputEntity.getString(inputAttrName);

								if (StringUtils.isNotEmpty(queryString)
										&& isAttrNameValidForLexicalMatch(inputAttrName))
								{
									Entity topMatchedSynonymEntity = calculateNGramOTSynonyms(ontologyIri, queryString,
											matchedOntologyTermEntity);
									if (maxNgramScore < topMatchedSynonymEntity.getDouble(SCORE))
									{
										maxNgramScore = topMatchedSynonymEntity.getDouble(SCORE);
									}
									if (maxNgramIDFScore < topMatchedSynonymEntity.getDouble(COMBINED_SCORE))
									{
										maxNgramIDFScore = topMatchedSynonymEntity.getDouble(COMBINED_SCORE);
									}
								}
							}
							MapEntity mapEntity = new MapEntity(matchedOntologyTermEntity);
							mapEntity.set(SCORE, maxNgramScore);
							mapEntity.set(COMBINED_SCORE, maxNgramIDFScore);
							return mapEntity;
						}
					});

			relevantEntities.addAll(ImmutableList.copyOf(lexicalMatchedOntologyTermEntities));
		}

		Collections.sort(relevantEntities, new Comparator<Entity>()
		{
			public int compare(Entity entity_1, Entity entity_2)
			{
				return entity_2.getDouble(COMBINED_SCORE).compareTo(entity_1.getDouble(COMBINED_SCORE));
			}
		});

		return relevantEntities;
	}

	/**
	 * A helper function to check if the ontology term (OT) contains the ontology annotations provided in input. If the
	 * OT has the same annotation, the OT will be considered as a good match and the similarity scores 100 are allocated
	 * to the OT
	 * 
	 * @param inputEntity
	 * @param ontologyTermEntity
	 * @return
	 */
	private Entity calculateNGromOTAnnotations(Entity inputEntity, Entity ontologyTermEntity)
	{
		MapEntity mapEntity = new MapEntity(ontologyTermEntity);
		for (Entity annotationEntity : ontologyTermEntity
				.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION))
		{
			String annotationName = annotationEntity.getString(OntologyTermDynamicAnnotationMetaData.NAME);
			String annotationValue = annotationEntity.getString(OntologyTermDynamicAnnotationMetaData.VALUE);
			for (String attributeName : inputEntity.getAttributeNames())
			{
				if (StringUtils.isNotEmpty(inputEntity.getString(attributeName))
						&& StringUtils.equalsIgnoreCase(attributeName, annotationName)
						&& StringUtils.equalsIgnoreCase(inputEntity.getString(attributeName), annotationValue))
				{
					mapEntity.set(SCORE, 100);
					mapEntity.set(COMBINED_SCORE, 100);
					return mapEntity;
				}
			}
		}
		return mapEntity;
	}

	/**
	 * A helper function to calculate the best NGram score from a list ontologyTerm synonyms
	 * 
	 * @param queryString
	 * @param ontologyTermEntity
	 * @return
	 */
	private Entity calculateNGramOTSynonyms(String ontologyIri, String queryString, Entity ontologyTermEntity)
	{
		Iterable<Entity> entities = ontologyTermEntity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM);
		if (Iterables.size(entities) > 0)
		{
			String cleanedQueryString = removeIllegalCharWithSingleWhiteSpace(queryString);

			// Calculate the Ngram silmiarity score for all the synonyms and sort them in descending order
			List<MapEntity> synonymEntities = FluentIterable.from(entities).transform(new Function<Entity, MapEntity>()
			{
				public MapEntity apply(Entity ontologyTermSynonymEntity)
				{
					MapEntity mapEntity = new MapEntity(ontologyTermSynonymEntity);
					String ontologyTermSynonym = removeIllegalCharWithSingleWhiteSpace(ontologyTermSynonymEntity
							.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM));
					mapEntity.set(SCORE, NGramMatchingModel.stringMatching(cleanedQueryString, ontologyTermSynonym));
					return mapEntity;
				}

			}).toSortedList(new Comparator<MapEntity>()
			{
				public int compare(MapEntity entity_1, MapEntity entity_2)
				{
					return entity_2.getDouble(SCORE).compareTo(entity_1.getDouble(SCORE));
				}
			});

			MapEntity firstMatchedSynonymEntity = Iterables.getFirst(synonymEntities, new MapEntity());
			double topNgramScore = firstMatchedSynonymEntity.getDouble(SCORE);
			String topMatchedSynonym = firstMatchedSynonymEntity
					.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);

			// the algorithm to combine synonyms to re-calculate the similarity scores to deal with the case where the
			// input query string contains multiple words from different synonyms of the same ontology term. E.g.
			// query string "propotosis, protruding eyeball, Exophthalmos" contains three synonyms of OT (propotosis),
			// if it was matched to each of the synonyms, all the similarity score would be fairly low (25%), therefore
			// need to combine those synonyms to recalculate the similarity score.
			//
			// The idea of the algorithm is quite simple, we add up the current synonym (the most) and next synonym (the
			// second most), if the combined string yields a higher score, the synonyms will be combined together. The
			// same process is repeated until all the synonyms have been checked
			// A --> 30%
			// B --> 25%
			// C --> 20%
			//
			// if(score(a+b, query) > score(a)) combine
			// else move to next synonym
			for (Entity nextMatchedSynonymEntity : Iterables.skip(synonymEntities, 1))
			{
				String nextMatchedSynonym = nextMatchedSynonymEntity
						.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);

				StringBuilder tempCombinedSynonym = new StringBuilder();
				tempCombinedSynonym.append(topMatchedSynonym).append(SINGLE_WHITESPACE).append(nextMatchedSynonym);

				double newScore = NGramMatchingModel.stringMatching(cleanedQueryString,
						removeIllegalCharWithSingleWhiteSpace(tempCombinedSynonym.toString()));

				if (newScore > topNgramScore)
				{
					topNgramScore = newScore;
					topMatchedSynonym = tempCombinedSynonym.toString();
				}
			}

			firstMatchedSynonymEntity.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, topMatchedSynonym);
			firstMatchedSynonymEntity.set(SCORE, topNgramScore);
			firstMatchedSynonymEntity.set(COMBINED_SCORE, topNgramScore);

			// The similarity scores are adjusted based on the inverse document frequency of the words.
			// The idea is that all the words from query string are weighted (important words occur fewer times across
			// all ontology terms than common words), the final score should be compensated for according to the word
			// weight.
			Map<String, Double> weightedWordSimilarity = informationContentService.redistributedNGramScore(
					cleanedQueryString, ontologyIri);

			Set<String> synonymStemmedWords = informationContentService.createStemmedWordSet(topMatchedSynonym);

			Set<String> createStemmedWordSet = informationContentService.createStemmedWordSet(cleanedQueryString);

			createStemmedWordSet
					.stream()
					.filter(originalWord -> Iterables.contains(synonymStemmedWords, originalWord)
							&& weightedWordSimilarity.containsKey(originalWord))
					.forEach(
							word -> firstMatchedSynonymEntity.set(COMBINED_SCORE, (firstMatchedSynonymEntity
									.getDouble(COMBINED_SCORE) + weightedWordSimilarity.get(word))));
			return firstMatchedSynonymEntity;
		}

		return null;
	}

	/**
	 * A helper function to produce fuzzy match query with 80% similarity in elasticsearch because PorterStem does not
	 * work in some cases, e.g. the stemming results for placenta and placental are different, therefore would be missed
	 * by elasticsearch
	 * 
	 * @param queryString
	 * @return
	 */
	private String fuzzyMatchQuerySyntax(String queryString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> uniqueTerms = Sets.newHashSet(queryString.toLowerCase().trim().split(NON_WORD_SEPARATOR));
		uniqueTerms.removeAll(NGramMatchingModel.STOPWORDSLIST);
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

	private boolean isAttrNameValidForLexicalMatch(String attr)
	{
		return StringUtils.equalsIgnoreCase(attr, DEFAULT_MATCHING_NAME_FIELD)
				|| StringUtils.containsIgnoreCase(attr, DEFAULT_MATCHING_SYNONYM_PREFIX_FIELD);
	}
}