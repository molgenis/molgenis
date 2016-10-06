package org.molgenis.data.semanticsearch.explain.service.impl;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermQueryExpansion;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermQueryExpansionSolution;
import org.molgenis.data.semanticsearch.explain.criteria.MatchingCriterion;
import org.molgenis.data.semanticsearch.explain.criteria.impl.StrictMatchingCriterion;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.utils.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.*;
import static org.molgenis.ontology.utils.NGramDistanceAlgorithm.stringMatching;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;

public class ExplainMappingServiceImpl implements ExplainMappingService
{
	private final OntologyService ontologyService;
	private final TagGroupGenerator tagGroupGenerator;
	private final Joiner termJoiner = Joiner.on(' ');

	final static MatchingCriterion STRICT_MATCHING_CRITERION = new StrictMatchingCriterion();

	private final static ExplainedMatchCandidate<String> EMPTY_EXPLAINATION = ExplainedMatchCandidate
			.create(StringUtils.EMPTY);

	private static final Logger LOG = LoggerFactory.getLogger(ExplainMappingServiceImpl.class);

	@Autowired
	public ExplainMappingServiceImpl(OntologyService ontologyService, TagGroupGenerator tagGroupGenerator)
	{
		this.ontologyService = requireNonNull(ontologyService);
		this.tagGroupGenerator = requireNonNull(tagGroupGenerator);
	}

	@Override
	public ExplainedMatchCandidate<String> explainMapping(SearchParam searchParam, String matchedResult)
	{
		List<OntologyTermQueryExpansion> ontologyTermQueryExpansions = searchParam.getTagGroups().stream()
				.map(hit -> new OntologyTermQueryExpansion(hit.getOntologyTerms(), ontologyService)).collect(toList());

		Set<String> lexicalQueries = searchParam.getLexicalQueries();

		Set<String> matchedSourceWords = splitRemoveStopWords(matchedResult);

		List<OntologyTerm> ontologyTermScope = ontologyTermQueryExpansions.stream()
				.map(OntologyTermQueryExpansion::getOntologyTerms).flatMap(List::stream).collect(toList());

		LOG.debug("OntologyTerms {}", ontologyTermScope);

		List<OntologyTerm> relevantOntologyTerms = ontologyService
				.findOntologyTerms(ontologyService.getAllOntologiesIds(), matchedSourceWords, ontologyTermScope.size(),
						ontologyTermScope);

		List<TagGroup> tagGroups = tagGroupGenerator
				.applyTagMatchingCriterion(relevantOntologyTerms, matchedSourceWords, STRICT_MATCHING_CRITERION);

		List<TagGroup> matchedSourceTagGroups = tagGroupGenerator.combineTagGroups(matchedSourceWords, tagGroups);

		LOG.debug("Candidates: {}", matchedSourceTagGroups);

		OntologyTermQueryExpansionSolution queryExpansionSolution;
		if (matchedSourceTagGroups.isEmpty())
		{
			queryExpansionSolution = OntologyTermQueryExpansionSolution.create(Collections.emptyMap(), false);
		}
		else
		{
			queryExpansionSolution = ontologyTermQueryExpansions.stream()
					.map(expansion -> expansion.getQueryExpansionSolution(matchedSourceTagGroups.get(0))).sorted()
					.findFirst().orElse(null);
		}

		Optional<Hit<ExplainedMatchCandidate<String>>> max = stream(lexicalQueries.spliterator(), false)
				.map(lexicalQuery -> computeScoreForMatchedSource(queryExpansionSolution, lexicalQuery, matchedResult))
				.max(naturalOrder());

		if (max.isPresent())
		{
			return max.get().getResult();
		}

		return EMPTY_EXPLAINATION;
	}

	/**
	 * This functions computes the similarity score for the matched candidate using either ontologyterms and the target
	 * label. Whichever gives higher similarity score gets selected as the final explanation
	 *
	 * @param queryExpansionSolution
	 * @param targetQueryTerm
	 * @param match
	 * @return
	 */
	Hit<ExplainedMatchCandidate<String>> computeScoreForMatchedSource(
			OntologyTermQueryExpansionSolution queryExpansionSolution, String targetQueryTerm, String match)
	{
		// Explain the match using ontology terms
		List<ExplainedQueryString> explainedUsingOntologyTerms = new ArrayList<>();

		for (Entry<OntologyTerm, OntologyTerm> entry : queryExpansionSolution.getMatchOntologyTerms()
				.entrySet())
		{
			OntologyTerm targetOntologyTerm = entry.getKey();
			OntologyTerm sourceOntologyTerm = entry.getValue();

			String bestMatchingSynonym = findBestMatchingSynonym(match, sourceOntologyTerm);
			String joinedMatchedWords = termJoiner.join(findMatchedWords(match, bestMatchingSynonym));
			float score = (float) (Math.round(stringMatching(joinedMatchedWords, match) * 10) / 10f);
			explainedUsingOntologyTerms.add(ExplainedQueryString
					.create(joinedMatchedWords, bestMatchingSynonym, targetOntologyTerm.getLabel(), score));
		}

		// Explain the match using the target label
		List<ExplainedQueryString> explainedUsingTargetLabel = new ArrayList<>();

		float score = (float) (Math.round(stringMatching(targetQueryTerm, match) * 10) / 10f);
		explainedUsingTargetLabel.add(ExplainedQueryString
				.create(termJoiner.join(findMatchedWords(targetQueryTerm, match)), targetQueryTerm, StringUtils.EMPTY,
						score));

		// Choose to be explained whether by ontology terms or target label depending one which one produces a higher
		// similarity score
		Hit<ExplainedMatchCandidate<String>> explainedCandidateUsingOntologyTerms = createExplainedCandidate(match,
				explainedUsingOntologyTerms, queryExpansionSolution.isHighQuality());
		Hit<ExplainedMatchCandidate<String>> explainedCandidateUsingTargetLabel = createExplainedCandidate(match,
				explainedUsingTargetLabel, false);

		return explainedCandidateUsingOntologyTerms.getScore() >= explainedCandidateUsingTargetLabel
				.getScore() ? explainedCandidateUsingOntologyTerms : explainedCandidateUsingTargetLabel;
	}

	private Hit<ExplainedMatchCandidate<String>> createExplainedCandidate(String match,
			List<ExplainedQueryString> explainedUsingOntologyTerms, boolean isHighQuality)
	{
		String combinedQuery = termJoiner.join(splitIntoUniqueTerms(
				explainedUsingOntologyTerms.stream().map(ExplainedQueryString::getQueryString)
						.collect(Collectors.joining(" "))));

		float score = (float) (Math.round(stringMatching(combinedQuery, match) * 10) / 10f);

		return Hit.create(ExplainedMatchCandidate.create(match, explainedUsingOntologyTerms, isHighQuality), score);
	}

	/**
	 * find matched words between two {@link String}s
	 *
	 * @param string1
	 * @param string2
	 * @return
	 */
	private Set<String> findMatchedWords(String string1, String string2)
	{
		Set<String> intersectedWords = new LinkedHashSet<>();
		Set<String> stemmedWordsFromString2 = splitAndStem(string2);
		for (String wordFromString1 : SemanticSearchServiceUtils.splitIntoUniqueTerms(string1))
		{
			String stemmedSourceWord = Stemmer.stem(wordFromString1);
			if (stemmedWordsFromString2.contains(stemmedSourceWord))
			{
				intersectedWords.add(wordFromString1);
			}
		}
		return intersectedWords;
	}

	/**
	 * Get a list of matched Synonym from {@link OntologyTerm}s for the given target query
	 *
	 * @param ontologyTerms
	 * @param targetQueryTerm
	 * @return
	 */
	private String findBestMatchingSynonym(String attributeLabel, OntologyTerm ontologyTerm)
	{
		Hit<String> hit = getLowerCaseTerms(ontologyTerm).stream()
				.map(synonym -> Hit.create(synonym, (float) stringMatching(attributeLabel, synonym)))
				.sorted(Comparator.reverseOrder()).findFirst().get();

		return hit.getResult();
	}
}