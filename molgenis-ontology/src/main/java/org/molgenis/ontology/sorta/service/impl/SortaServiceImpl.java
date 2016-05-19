package org.molgenis.ontology.sorta.service.impl;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH_NGRAM;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm.STOPWORDSLIST;
import static org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm.stringMatching;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.OntologyTermAnnotation;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.bean.SortaHit;
import org.molgenis.ontology.sorta.bean.SortaInput;
import org.molgenis.ontology.sorta.service.SortaService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import static java.util.Objects.requireNonNull;

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
	private final OntologyService ontologyService;
	private final InformationContentService informationContentService;

	@Autowired
	public SortaServiceImpl(DataService dataService, OntologyService ontologyService,
			InformationContentService informationContentService)
	{
		this.dataService = requireNonNull(dataService);
		this.ontologyService = requireNonNull(ontologyService);
		this.informationContentService = requireNonNull(informationContentService);
	}

	@Override
	public List<SortaHit> findOntologyTermEntities(String ontologyUrl, String queryString)
	{
		Entity entity = new MapEntity(of(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD, queryString));
		return findOntologyTermEntities(ontologyUrl, entity);
	}

	@Override
	public List<SortaHit> findOntologyTermEntities(String ontologyIri, Entity inputEntity)
	{
		Ontology ontology = ontologyService.getOntology(ontologyIri);
		if (ontology == null)
			throw new IllegalArgumentException("Ontology IRI " + ontologyIri + " does not exist in the database!");

		SortaInput sortaInput = new SortaInput(inputEntity);

		Set<SortaHit> uniequeSortaHits = new LinkedHashSet<>();

		// query rules for ontology anntations, e.g. OMIM:124343
		List<QueryRule> rulesForOtherFields = new ArrayList<>();
		// query rules for ontology name and synonyms, e.g. name = proptosis, sysnonym = protruding eye
		List<QueryRule> rulesForOntologyTermFields = new ArrayList<>();

		List<QueryRule> rulesForOntologyTermFieldsNGram = new ArrayList<>();

		for (String attributeName : sortaInput.getLexicalMatchAttributes())
		{
			String attributeValue = sortaInput.getValue(attributeName);
			if (isNotBlank(attributeValue))
			{
				String stemmedValue = stemQuery(attributeValue);
				rulesForOntologyTermFields
						.add(new QueryRule(ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH, fuzzyMatchQuerySyntax(stemmedValue)));
				rulesForOntologyTermFieldsNGram
						.add(new QueryRule(ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH_NGRAM, stemmedValue));
			}
		}

		for (String attributeName : sortaInput.getAnnotationMatchAttributes())
		{
			String attributeValue = sortaInput.getValue(attributeName);
			if (isNotBlank(attributeValue))
			{
				QueryRule queryAnnotationName = new QueryRule(OntologyTermDynamicAnnotationMetaData.NAME, EQUALS,
						attributeName);
				QueryRule queryAnnotationValue = new QueryRule(OntologyTermDynamicAnnotationMetaData.VALUE, EQUALS,
						attributeValue);

				// ((name=OMIM Operator.AND value=124325) Operator.OR (name=HPO Operator.AND value=hp12435))
				if (rulesForOtherFields.size() > 0) rulesForOtherFields.add(new QueryRule(OR));
				rulesForOtherFields
						.add(new QueryRule(asList(queryAnnotationName, new QueryRule(AND), queryAnnotationValue)));
			}
		}

		// Find the ontology terms that have the same annotations as the input ontology annotations
		if (rulesForOtherFields.size() > 0)
		{
			uniequeSortaHits.addAll(annotationMatchOntologyTerms(sortaInput, ontology, rulesForOtherFields));
		}

		// Find the ontology terms based on the lexical similarities
		if (rulesForOntologyTermFields.size() > 0)
		{
			int pageSize = MAX_NUMBER_MATCHES - uniequeSortaHits.size();
			uniequeSortaHits
					.addAll(lexicalMatchOntologyTerms(sortaInput, ontology, pageSize, rulesForOntologyTermFields));
		}

		if (rulesForOntologyTermFieldsNGram.size() > 0)
		{
			uniequeSortaHits.addAll(lexicalMatchOntologyTerms(sortaInput, ontology, NUMBER_NGRAM_MATCHES,
					rulesForOntologyTermFieldsNGram));
		}

		List<SortaHit> sortHits = Lists.newArrayList(uniequeSortaHits);

		Collections.sort(sortHits);

		return sortHits;
	}

	private List<SortaHit> annotationMatchOntologyTerms(SortaInput sortaInput, Ontology ontology,
			List<QueryRule> rulesForOtherFields)
	{
		List<Entity> ontologyTermAnnotationEntities = dataService
				.findAll(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
						new QueryImpl(rulesForOtherFields).pageSize(MAX_VALUE))
				.collect(toList());

		if (ontologyTermAnnotationEntities.size() > 0)
		{
			List<QueryRule> rules = Arrays.asList(new QueryRule(ONTOLOGY, EQUALS, ontology.getId()), new QueryRule(AND),
					new QueryRule(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, IN, ontologyTermAnnotationEntities));

			Stream<Entity> ontologyTermEntities = dataService.findAll(OntologyTermMetaData.ENTITY_NAME,
					new QueryImpl(rules).pageSize(Integer.MAX_VALUE));

			List<SortaHit> collect = ontologyTermEntities.map(SortaServiceImpl::toOntologyTerm)
					.map(ontologyTerm -> calculateNGromOTAnnotations(sortaInput, ontologyTerm))
					.filter(hit -> hit != null).collect(Collectors.toList());

			return collect;
		}

		return emptyList();
	}

	private List<SortaHit> lexicalMatchOntologyTerms(SortaInput sortaInput, Ontology ontology, int pageSize,
			List<QueryRule> rulesForOntologyTermFields)
	{
		QueryRule disMaxQueryRule = new QueryRule(rulesForOntologyTermFields);
		disMaxQueryRule.setOperator(DIS_MAX);

		List<QueryRule> finalQueryRules = asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, EQUALS, ontology.getId()),
				new QueryRule(AND), disMaxQueryRule);

		List<SortaHit> collect = dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(finalQueryRules).pageSize(pageSize))
				.map(SortaServiceImpl::toOntologyTerm)
				.map(ontologyTerm -> computeLexicalSimilarity(sortaInput, ontologyTerm, ontology)).collect(toList());

		return collect;
	}

	SortaHit computeLexicalSimilarity(SortaInput sortaInput, OntologyTerm ontologyTerm, Ontology ontology)
	{
		SortaHit topMatchedSynonymEntity = null;

		for (String attributeName : sortaInput.getLexicalMatchAttributes())
		{
			String attributeValue = sortaInput.getValue(attributeName);

			if (StringUtils.isNotBlank(attributeValue))
			{
				SortaHit sortaHit = findSynonymWithHighestNgramScore(attributeValue, ontology, ontologyTerm);
				if (sortaHit != null)
				{
					if (topMatchedSynonymEntity == null
							|| topMatchedSynonymEntity.getWeightedScore() < sortaHit.getWeightedScore())
					{
						topMatchedSynonymEntity = sortaHit;
					}
				}
			}
		}

		return topMatchedSynonymEntity;
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
	private SortaHit calculateNGromOTAnnotations(SortaInput sortaInput, OntologyTerm ontologyTerm)
	{
		for (String attributeName : sortaInput.getAnnotationMatchAttributes())
		{
			for (OntologyTermAnnotation annotation : ontologyTerm.getAnnotations())
			{
				String annotationName = annotation.getName();
				String annotationValue = annotation.getValue();
				if (attributeName.equalsIgnoreCase(annotationName)
						&& sortaInput.getValue(attributeName).equalsIgnoreCase(annotationValue))
				{
					return SortaHit.create(ontologyTerm, 100, 100);
				}
			}
		}

		return null;
	}

	/**
	 * A helper function to calculate the best NGram score from a list ontologyTerm synonyms
	 * 
	 * @param queryString
	 * @param ontologyTermEntity
	 * @return
	 */
	private SortaHit findSynonymWithHighestNgramScore(String queryString, Ontology ontology, OntologyTerm ontologyTerm)
	{
		List<String> ontologyTermSynonyms = ontologyTerm.getSynonyms();

		// Calculate the Ngram silmiarity score for all the synonyms and sort them in descending order
		List<Hit<String>> sortedHits = ontologyTermSynonyms.stream()
				.map(synonym -> calculateLexicalSimilarity(queryString, synonym)).sorted(Ordering.natural().reverse())
				.collect(toList());

		if (sortedHits.size() > 0)
		{
			float topNgramScore = sortedHits.get(0).getScore();
			String topMatchedSynonym = sortedHits.get(0).getResult();

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
			// if(score(a+b, query) > score(a)) combine else move to next synonym

			for (Hit<String> hit : Iterables.skip(sortedHits, 1))
			{
				String combinedSynonym = topMatchedSynonym + SINGLE_WHITESPACE + hit.getResult();
				Hit<String> combinedHit = calculateLexicalSimilarity(queryString, combinedSynonym);

				if (combinedHit.getScore() > topNgramScore)
				{
					topNgramScore = combinedHit.getScore();
					topMatchedSynonym = combinedSynonym;
				}
			}

			// The similarity scores are adjusted based on the inverse document frequency of the words.
			// The idea is that all the words from query string are weighted (important words occur fewer times across
			// all ontology terms than common words), the final score should be compensated for according to the word
			// // weight.
			String cleanedQueryString = removeIllegalCharWithSingleWhiteSpace(queryString);
			Map<String, Double> weightedWordSimilarity = informationContentService
					.redistributedNGramScore(cleanedQueryString, ontology.getIRI());

			Set<String> synonymStemmedWords = informationContentService.createStemmedWordSet(topMatchedSynonym);
			Set<String> createStemmedWordSet = informationContentService.createStemmedWordSet(cleanedQueryString);

			double calibratedScore = createStemmedWordSet.stream()
					.filter(originalWord -> synonymStemmedWords.contains(originalWord)
							&& weightedWordSimilarity.containsKey(originalWord))
					.map(word -> weightedWordSimilarity.get(word)).mapToDouble(Double::doubleValue).sum();

			return SortaHit.create(ontologyTerm, topNgramScore * 100, topNgramScore * 100 + calibratedScore);
		}

		return null;
	}

	private Hit<String> calculateLexicalSimilarity(String queryString, String matchedTerm)
	{
		double score = stringMatching(removeIllegalCharWithSingleWhiteSpace(queryString),
				removeIllegalCharWithSingleWhiteSpace(matchedTerm));
		return Hit.create(matchedTerm, (float) score / 100);
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
		List<String> collect = Stream.of(queryString.toLowerCase().trim().split(NON_WORD_SEPARATOR))
				.filter(w -> !STOPWORDSLIST.contains(w) && !ELASTICSEARCH_RESERVED_WORDS.contains(w)).map(Stemmer::stem)
				.filter(StringUtils::isNotBlank).collect(toList());
		return join(Sets.newLinkedHashSet(collect), SINGLE_WHITESPACE);
	}

	private String fuzzyMatchQuerySyntax(String queryString)
	{
		List<String> collect = Stream.of(queryString.split(SINGLE_WHITESPACE)).map(w -> w + FUZZY_MATCH_SIMILARITY)
				.collect(Collectors.toList());
		return join(collect, SINGLE_WHITESPACE);
	}

	public String removeIllegalCharWithSingleWhiteSpace(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE);
	}

	public String removeIllegalCharWithEmptyString(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, StringUtils.EMPTY);
	}

	private static OntologyTerm toOntologyTerm(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}
		// Collect synonyms if there are any
		List<String> synonyms = new ArrayList<>();
		Iterable<Entity> ontologyTermSynonymEntities = entity.getEntities(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM);
		if (ontologyTermSynonymEntities != null)
		{
			ontologyTermSynonymEntities.forEach(synonymEntity -> synonyms
					.add(synonymEntity.getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM)));
		}
		if (!synonyms.contains(entity.getString(ONTOLOGY_TERM_NAME)))
		{
			synonyms.add(entity.getString(ONTOLOGY_TERM_NAME));
		}

		// Collect annotations if there are any
		List<OntologyTermAnnotation> annotations = new ArrayList<>();
		Iterable<Entity> ontologyTermAnnotationEntities = entity.getEntities(ONTOLOGY_TERM_DYNAMIC_ANNOTATION);
		if (ontologyTermAnnotationEntities != null)
		{
			for (Entity annotationEntity : ontologyTermAnnotationEntities)
			{
				String annotationName = annotationEntity.getString(OntologyTermDynamicAnnotationMetaData.NAME);
				String annotationValue = annotationEntity.getString(OntologyTermDynamicAnnotationMetaData.VALUE);
				annotations.add(OntologyTermAnnotation.create(annotationName, annotationValue));
			}
		}
		return OntologyTerm.create(entity.getString(ONTOLOGY_TERM_IRI), entity.getString(ONTOLOGY_TERM_NAME), null,
				synonyms, annotations);
	}
}