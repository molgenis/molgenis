package org.molgenis.ontology.sorta.service.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.bean.OntologyTermHitEntity;
import org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData.COMBINED_SCORE;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitMetaData.SCORE;

public class SortaServiceImpl implements SortaService
{
	private static final Set<String> ELASTICSEARCH_RESERVED_WORDS = Sets.newHashSet("or", "and", "if");
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final String ILLEGAL_CHARACTERS_PATTERN = "[^a-zA-Z0-9 ]";
	private static final String FUZZY_MATCH_SIMILARITY = "~0.8";
	private static final String SINGLE_WHITESPACE = " ";
	private static final int MAX_NUMBER_MATCHES = 50;
	private static final int NUMBER_NGRAM_MATCHES = 10;

	// Global fields that are used by other classes
	public static final String SIGNIFICANT_VALUE = "Significant";
	public static final Character DEFAULT_SEPARATOR = ';';
	public static final String DEFAULT_MATCHING_NAME_FIELD = "Name";
	public static final String DEFAULT_MATCHING_SYNONYM_PREFIX_FIELD = "Synonym";
	public static final String DEFAULT_MATCHING_IDENTIFIER = "Identifier";

	private final DataService dataService;
	private final InformationContentService informationContentService;
	private final OntologyTermHitMetaData ontologyTermHitMetaData;
	private final OntologyTermSynonymFactory ontologyTermSynonymFactory;

	@Autowired
	public SortaServiceImpl(DataService dataService, InformationContentService informationContentService,
			OntologyTermHitMetaData ontologyTermHitMetaData, OntologyTermSynonymFactory ontologyTermSynonymFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.informationContentService = requireNonNull(informationContentService);
		this.ontologyTermHitMetaData = requireNonNull(ontologyTermHitMetaData);
		this.ontologyTermSynonymFactory = requireNonNull(ontologyTermSynonymFactory);
	}

	@Override
	public Iterable<Entity> getAllOntologyEntities()
	{
		Stream<Entity> findAll = dataService.findAll(ONTOLOGY);
		return findAll.collect(Collectors.toList());
	}

	@Override
	public Entity getOntologyEntity(String ontologyIri)
	{
		return dataService.findOne(ONTOLOGY, new QueryImpl<>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri));
	}

	@Override
	public Entity getOntologyTermEntity(String ontologyTermIri, String ontologyIri)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		if (ontologyEntity != null)
		{
			return dataService.findOne(ONTOLOGY_TERM,
					new QueryImpl<>().eq(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ontologyTermIri)
									 .and()
									 .eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity));
		}
		return null;
	}

	@Override
	public Iterable<Entity> findOntologyTermEntities(String ontologyIri, Entity inputEntity)
	{
		Entity ontologyEntity = getOntologyEntity(ontologyIri);
		if (ontologyEntity == null)
			throw new IllegalArgumentException("Ontology IRI " + ontologyIri + " does not exist in the database!");

		// a list to store most relevant entities
		List<Entity> relevantEntities = new ArrayList<>();
		// query rules for ontology anntations, e.g. OMIM:124343
		List<QueryRule> rulesForOtherFields = new ArrayList<>();
		// query rules for ontology name and synonyms, e.g. name = proptosis, sysnonym = protruding eye
		List<QueryRule> rulesForOntologyTermFields = new ArrayList<>();

		List<QueryRule> rulesForOntologyTermFieldsNGram = new ArrayList<>();

		for (String attributeName : inputEntity.getAttributeNames())
		{
			if (StringUtils.isNotEmpty(inputEntity.getString(attributeName)) && !attributeName.equalsIgnoreCase(
					DEFAULT_MATCHING_IDENTIFIER))
			{
				// The attribute name is either equal to 'Name' or starts with string 'Synonym'
				if (isAttrNameValidForLexicalMatch(attributeName))
				{
					String stemmedQueryString = stemQuery(inputEntity.getString(attributeName));
					if (StringUtils.isNotEmpty(stemmedQueryString))
					{
						rulesForOntologyTermFields.add(
								new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH,
										fuzzyMatchQuerySyntax(stemmedQueryString)));

						rulesForOntologyTermFieldsNGram.add(
								new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH_NGRAM,
										stemmedQueryString));
					}
				}
				else
				{
					QueryRule queryAnnotationName = new QueryRule(OntologyTermDynamicAnnotationMetaData.NAME, EQUALS,
							attributeName);
					QueryRule queryAnnotationValue = new QueryRule(OntologyTermDynamicAnnotationMetaData.VALUE, EQUALS,
							inputEntity.getString(attributeName));

					// ((name=OMIM Operator.AND value=124325) Operator.OR (name=HPO Operator.AND value=hp12435))
					if (rulesForOtherFields.size() > 0) rulesForOtherFields.add(new QueryRule(OR));
					rulesForOtherFields.add(new QueryRule(
							Arrays.asList(queryAnnotationName, new QueryRule(AND), queryAnnotationValue)));
				}
			}
		}

		// Find the ontology terms that have the same annotations as the input ontology annotations
		if (rulesForOtherFields.size() > 0)
		{
			annotationMatchOntologyTerms(inputEntity, ontologyEntity, relevantEntities, rulesForOtherFields);
		}

		// Find the ontology terms based on the lexical similarities
		if (rulesForOntologyTermFields.size() > 0)
		{
			int pageSize = MAX_NUMBER_MATCHES - relevantEntities.size();
			lexicalMatchOntologyTerms(ontologyIri, inputEntity, ontologyEntity, pageSize, rulesForOntologyTermFields,
					relevantEntities);
		}

		if (rulesForOntologyTermFieldsNGram.size() > 0)
		{
			lexicalMatchOntologyTerms(ontologyIri, inputEntity, ontologyEntity, NUMBER_NGRAM_MATCHES,
					rulesForOntologyTermFieldsNGram, relevantEntities);
		}

		relevantEntities.sort((entity_1, entity_2) -> entity_2.getDouble(COMBINED_SCORE)
															  .compareTo(entity_1.getDouble(COMBINED_SCORE)));

		return relevantEntities;
	}

	private void annotationMatchOntologyTerms(Entity inputEntity, Entity ontologyEntity, List<Entity> relevantEntities,
			List<QueryRule> rulesForOtherFields)
	{
		List<Entity> ontologyTermAnnotationEntities = dataService.findAll(ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				new QueryImpl<>(rulesForOtherFields).pageSize(Integer.MAX_VALUE)).collect(Collectors.toList());

		if (ontologyTermAnnotationEntities.size() > 0)
		{
			List<QueryRule> rules = Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, EQUALS, ontologyEntity),
					new QueryRule(AND), new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, IN,
							ontologyTermAnnotationEntities));

			Stream<Entity> ontologyTermEntities = dataService.findAll(ONTOLOGY_TERM, new QueryImpl<>(rules).pageSize(Integer.MAX_VALUE));

			List<Entity> relevantOntologyTermEntities = ontologyTermEntities.map(
					ontologyTermEntity -> calculateNGromOTAnnotations(inputEntity, ontologyTermEntity))
																			.collect(Collectors.toList());

			relevantEntities.addAll(relevantOntologyTermEntities);
		}
	}

	private void lexicalMatchOntologyTerms(String ontologyIri, Entity inputEntity, Entity ontologyEntity, int pageSize,
			List<QueryRule> rulesForOntologyTermFields, List<Entity> relevantEntities)
	{
		QueryRule disMaxQueryRule = new QueryRule(rulesForOntologyTermFields);
		disMaxQueryRule.setOperator(DIS_MAX);

		List<QueryRule> finalQueryRules = Arrays.asList(
				new QueryRule(OntologyTermMetaData.ONTOLOGY, EQUALS, ontologyEntity), new QueryRule(AND),
				disMaxQueryRule);

		Stream<Entity> lexicalMatchedOntologyTermEntities = dataService.findAll(ONTOLOGY_TERM,
				new QueryImpl<>(finalQueryRules).pageSize(pageSize))
																	   .map(ontologyTerm -> addLexicalScoreToMatchedEntity(
																			   inputEntity, ontologyTerm, ontologyIri));

		lexicalMatchedOntologyTermEntities.forEach(matchedEntity ->
		{
			if (!relevantEntities.contains(matchedEntity))
			{
				relevantEntities.add(matchedEntity);
			}
		});
	}

	Entity addLexicalScoreToMatchedEntity(Entity inputEntity, Entity ontologyTerm, String ontologyIri)
	{
		double maxNgramScore = 0;
		double maxNgramIDFScore = 0;
		for (String inputAttrName : inputEntity.getAttributeNames())
		{
			String queryString = inputEntity.getString(inputAttrName);
			if (StringUtils.isNotEmpty(queryString) && isAttrNameValidForLexicalMatch(inputAttrName))
			{
				Entity topMatchedSynonymEntity = findSynonymWithHighestNgramScore(ontologyIri, queryString,
						ontologyTerm);
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
		OntologyTermHitEntity mapEntity = new OntologyTermHitEntity(ontologyTerm, ontologyTermHitMetaData);
		mapEntity.set(SCORE, maxNgramScore);
		mapEntity.set(COMBINED_SCORE, maxNgramIDFScore);
		return mapEntity;
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
		OntologyTermHitEntity mapEntity = new OntologyTermHitEntity(ontologyTermEntity, ontologyTermHitMetaData);
		for (Entity annotationEntity : ontologyTermEntity.getEntities(
				OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION))
		{
			String annotationName = annotationEntity.getString(OntologyTermDynamicAnnotationMetaData.NAME);
			String annotationValue = annotationEntity.getString(OntologyTermDynamicAnnotationMetaData.VALUE);
			for (String attributeName : inputEntity.getAttributeNames())
			{
				if (StringUtils.isNotEmpty(inputEntity.getString(attributeName)) && StringUtils.equalsIgnoreCase(
						attributeName, annotationName) && StringUtils.equalsIgnoreCase(
						inputEntity.getString(attributeName), annotationValue))
				{
					mapEntity.set(SCORE, 100d);
					mapEntity.set(COMBINED_SCORE, 100d);
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
	private Entity findSynonymWithHighestNgramScore(String ontologyIri, String queryString, Entity ontologyTermEntity)
	{
		Iterable<Entity> entities = ontologyTermEntity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM);
		if (Iterables.size(entities) > 0)
		{
			String cleanedQueryString = removeIllegalCharWithSingleWhiteSpace(queryString);

			// Calculate the Ngram silmiarity score for all the synonyms and sort them in descending order
			List<Entity> synonymEntities = FluentIterable.from(entities).transform(ontologyTermSynonymEntity ->
			{
				Entity mapEntity = ontologyTermSynonymFactory.create();
				mapEntity.set(ontologyTermSynonymEntity);
				String ontologyTermSynonym = removeIllegalCharWithSingleWhiteSpace(
						ontologyTermSynonymEntity.getString(
								OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR));
				mapEntity.set(SCORE,
						NGramDistanceAlgorithm.stringMatching(cleanedQueryString, ontologyTermSynonym));
				return mapEntity;
			}).toSortedList((entity_1, entity_2) -> entity_2.getDouble(SCORE).compareTo(entity_1.getDouble(SCORE)));

			Entity firstMatchedSynonymEntity = Iterables.getFirst(synonymEntities, ontologyTermSynonymFactory.create());
			double topNgramScore = firstMatchedSynonymEntity.getDouble(SCORE);
			String topMatchedSynonym = firstMatchedSynonymEntity.getString(
					OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR);

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
				String nextMatchedSynonym = nextMatchedSynonymEntity.getString(
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR);

				StringBuilder tempCombinedSynonym = new StringBuilder();
				tempCombinedSynonym.append(topMatchedSynonym).append(SINGLE_WHITESPACE).append(nextMatchedSynonym);

				double newScore = NGramDistanceAlgorithm.stringMatching(cleanedQueryString,
						removeIllegalCharWithSingleWhiteSpace(tempCombinedSynonym.toString()));

				if (newScore > topNgramScore)
				{
					topNgramScore = newScore;
					topMatchedSynonym = tempCombinedSynonym.toString();
				}
			}

			firstMatchedSynonymEntity.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR, topMatchedSynonym);
			firstMatchedSynonymEntity.set(SCORE, topNgramScore);
			firstMatchedSynonymEntity.set(COMBINED_SCORE, topNgramScore);

			// The similarity scores are adjusted based on the inverse document frequency of the words.
			// The idea is that all the words from query string are weighted (important words occur fewer times across
			// all ontology terms than common words), the final score should be compensated for according to the word
			// // weight.
			Map<String, Double> weightedWordSimilarity = informationContentService.redistributedNGramScore(
					cleanedQueryString, ontologyIri);

			Set<String> synonymStemmedWords = informationContentService.createStemmedWordSet(topMatchedSynonym);

			Set<String> createStemmedWordSet = informationContentService.createStemmedWordSet(cleanedQueryString);

			createStemmedWordSet.stream()
								.filter(originalWord -> Iterables.contains(synonymStemmedWords, originalWord)
										&& weightedWordSimilarity.containsKey(originalWord))
								.forEach(word -> firstMatchedSynonymEntity.set(COMBINED_SCORE,
										(firstMatchedSynonymEntity.getDouble(COMBINED_SCORE)
												+ weightedWordSimilarity.get(word))));

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

	private String stemQuery(String queryString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> uniqueTerms = Sets.newHashSet(queryString.toLowerCase().trim().split(NON_WORD_SEPARATOR));
		uniqueTerms.removeAll(NGramDistanceAlgorithm.STOPWORDSLIST);
		for (String word : uniqueTerms)
		{
			if (StringUtils.isNotEmpty(word.trim()) && !(ELASTICSEARCH_RESERVED_WORDS.contains(word)))
			{
				String afterStem = Stemmer.stem(removeIllegalCharWithEmptyString(word));
				if (StringUtils.isNotEmpty(afterStem))
				{
					stringBuilder.append(afterStem).append(SINGLE_WHITESPACE);
				}
			}
		}
		return stringBuilder.toString().trim();
	}

	private String fuzzyMatchQuerySyntax(String queryString)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (String word : queryString.split(SINGLE_WHITESPACE))
		{
			stringBuilder.append(word).append(FUZZY_MATCH_SIMILARITY).append(SINGLE_WHITESPACE);
		}
		return stringBuilder.toString().trim();
	}

	private static String removeIllegalCharWithSingleWhiteSpace(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE);
	}

	private static String removeIllegalCharWithEmptyString(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, StringUtils.EMPTY);
	}

	private boolean isAttrNameValidForLexicalMatch(String attr)
	{
		return StringUtils.equalsIgnoreCase(attr, DEFAULT_MATCHING_NAME_FIELD) || StringUtils.containsIgnoreCase(attr,
				DEFAULT_MATCHING_SYNONYM_PREFIX_FIELD);
	}
}