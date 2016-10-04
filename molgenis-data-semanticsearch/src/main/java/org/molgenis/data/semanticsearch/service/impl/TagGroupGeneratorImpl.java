package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.collectLowerCaseTerms;
import static org.molgenis.ontology.utils.NGramDistanceAlgorithm.STOPWORDSLIST;
import static org.molgenis.ontology.utils.NGramDistanceAlgorithm.stringMatching;
import static org.molgenis.ontology.utils.Stemmer.cleanStemPhrase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.spell.StringDistance;
import org.elasticsearch.common.base.Joiner;
import org.molgenis.data.semanticsearch.explain.criteria.MatchingCriterion;
import org.molgenis.data.semanticsearch.explain.criteria.impl.StrictMatchingCriterion;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.semanticsearch.utils.TagGroupComparator;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.model.SemanticType;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.utils.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class TagGroupGeneratorImpl implements TagGroupGenerator
{
	private static final Logger LOG = LoggerFactory.getLogger(TagGroupGeneratorImpl.class);

	private final OntologyService ontologyService;

	public final static int MAX_NUM_TAGS = 3000;
	public final static MatchingCriterion STRICT_MATCHING_CRITERION = new StrictMatchingCriterion();
	private final static String ILLEGAL_CHARS_REGEX = "[^\\p{L}'a-zA-Z0-9\\.~]+";
	private Joiner termJoiner = Joiner.on(' ');

	@Autowired
	public TagGroupGeneratorImpl(OntologyService ontologyService)
	{
		this.ontologyService = requireNonNull(ontologyService);
	}

	@Override
	public List<TagGroup> generateTagGroups(String queryString, List<String> ontologyIds)
	{
		List<SemanticType> globalKeyConcepts = ontologyService.getAllSemanticTypes().stream()
				.filter(SemanticType::isGlobalKeyConcept).collect(Collectors.toList());

		return generateTagGroups(queryString, ontologyIds, globalKeyConcepts);
	}

	@Override
	public List<TagGroup> generateTagGroups(String queryString, List<String> ontologyIds,
			List<SemanticType> globalKeyConcepts)
	{
		Set<String> queryWords = removeIllegalCharactersAndStopWords(queryString);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("findTagGroups({},{},{})", ontologyIds, queryWords, MAX_NUM_TAGS);
		}

		List<OntologyTermImpl> relevantOntologyTerms = ontologyService.findOntologyTerms(ontologyIds, queryWords,
				MAX_NUM_TAGS);

		List<TagGroup> candidateTagGroups = applyTagMatchingCriterion(relevantOntologyTerms, queryWords,
				STRICT_MATCHING_CRITERION).stream().filter(tagGroup -> tagGroupKeyConcept(globalKeyConcepts, tagGroup))
						.collect(toList());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Candidates: {}", candidateTagGroups);
		}

		List<TagGroup> tagGroups = combineTagGroups(queryWords, candidateTagGroups);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("OntologyTermHit: {}", tagGroups);
		}

		return tagGroups;
	}

	/**
	 * Finds the best combinations of @{link OntologyTerm}s based on the given search terms
	 * 
	 * @param queryWords
	 * @param relevantOntologyTerms
	 * @return
	 */
	@Override
	public List<TagGroup> combineTagGroups(Set<String> queryWords, List<TagGroup> relevantTagGroups)
	{
		relevantTagGroups.sort(new TagGroupComparator());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Hits: {}", relevantTagGroups);
		}

		List<TagGroup> combinedTagGroups = new ArrayList<>();

		// 1. Create a list of ontology term candidates with the best matching synonym known
		// 2. Loop through the list of candidates and collect all the possible candidates (all best combinations of
		// ontology terms)
		// 3. Compute a list of possible ontology terms.
		for (TagGroup targetGroup : Lists.newArrayList(relevantTagGroups))
		{
			Multimap<String, TagGroup> ontologyTermGroups = LinkedHashMultimap.create();
			ontologyTermGroups.put(targetGroup.getMatchedWords(), targetGroup);

			for (TagGroup tagGroup : relevantTagGroups)
			{
				if (targetGroup.equals(tagGroup)) continue;

				if (ontologyTermGroups.containsKey(tagGroup.getMatchedWords()))
				{
					ontologyTermGroups.put(tagGroup.getMatchedWords(), tagGroup);
				}
				else
				{
					String previousJoinedTerm = termJoiner.join(ontologyTermGroups.keys().elementSet());
					Set<String> previousJoinedMatchedWords = removeIllegalCharactersAndStopWords(previousJoinedTerm);
					Set<String> currentMatchedWords = removeIllegalCharactersAndStopWords(tagGroup.getMatchedWords());

					// The next TagGroup words should not be present in the previous involved TagGroups
					String joinedTerm = termJoiner.join(Sets.union(previousJoinedMatchedWords, currentMatchedWords));
					float joinedScore = round(distanceFrom(joinedTerm, queryWords));
					float previousScore = round(distanceFrom(previousJoinedTerm, queryWords));

					if (joinedScore > previousScore)
					{
						ontologyTermGroups.put(tagGroup.getMatchedWords(), tagGroup);
					}
				}
			}

			String joinedSynonym = termJoiner.join(ontologyTermGroups.keySet());
			float newScore = round(distanceFrom(joinedSynonym, queryWords));
			List<TagGroup> newTagGroups = createTagGroups(ontologyTermGroups).stream()
					.map(list -> TagGroup.create(list, joinedSynonym, newScore)).collect(toList());

			combinedTagGroups.addAll(newTagGroups);

			relevantTagGroups.removeAll(ontologyTermGroups.values());
		}

		if (combinedTagGroups.size() > 0)
		{
			float maxScore = (float) combinedTagGroups.stream().map(TagGroup::getScore).mapToDouble(Float::doubleValue)
					.max().getAsDouble() * 0.8f;
			combinedTagGroups = combinedTagGroups.stream().sorted(reverseOrder())
					.filter(tagGroup -> tagGroup.getScore() >= maxScore).limit(20).collect(toList());
			if (LOG.isDebugEnabled())
			{
				LOG.debug("result: {}", combinedTagGroups);
			}
			return combinedTagGroups;
		}

		return emptyList();
	}

	@Override
	public List<TagGroup> applyTagMatchingCriterion(List<OntologyTermImpl> relevantOntologyTerms,
			Set<String> queryWords, MatchingCriterion matchingCriterion)
	{
		if (relevantOntologyTerms.size() > 0)
		{
			Set<String> stemmedSearchTerms = queryWords.stream().map(Stemmer::stem).collect(toSet());

			List<TagGroup> orderedIndividualOntologyTermHits = relevantOntologyTerms.stream()
					.filter(ontologyTerm -> matchingCriterion.apply(stemmedSearchTerms, ontologyTerm))
					.map(ontologyTerm -> createTagGroup(stemmedSearchTerms, ontologyTerm))
					.sorted(new TagGroupComparator()).collect(toList());

			// Remove the low ranking ontologyterms that are the parents of the high ranking ontologyterms
			List<TagGroup> copyOfOntologyTermHits = Lists.newArrayList(orderedIndividualOntologyTermHits);
			for (int i = orderedIndividualOntologyTermHits.size() - 1; i > 1; i--)
			{
				OntologyTermImpl lowRankingOntologyTerm = orderedIndividualOntologyTermHits.get(i).getOntologyTerms()
						.get(0);

				for (int j = i - 1; j > 0; j--)
				{
					OntologyTermImpl highRankingOntologyTerm = orderedIndividualOntologyTermHits.get(j)
							.getOntologyTerms().get(0);

					if (ontologyService.isDescendant(highRankingOntologyTerm, lowRankingOntologyTerm))
					{
						copyOfOntologyTermHits.remove(orderedIndividualOntologyTermHits.get(i));
					}
				}
			}

			return copyOfOntologyTermHits;
		}
		return emptyList();
	}

	private TagGroup createTagGroup(Set<String> stemmedSearchTerms, OntologyTermImpl ontologyTerm)
	{
		Hit<String> bestMatchingSynonym = bestMatchingSynonym(ontologyTerm, stemmedSearchTerms);
		return TagGroup.create(ontologyTerm, cleanStemPhrase(bestMatchingSynonym.getResult()),
				bestMatchingSynonym.getScore());
	}

	List<List<OntologyTermImpl>> createTagGroups(Multimap<String, TagGroup> candidates)
	{
		List<List<OntologyTermImpl>> ontologyTermGroups = new ArrayList<>();

		for (Collection<TagGroup> values : candidates.asMap().values())
		{
			List<OntologyTermImpl> atomicOntologyTermGroup = values.stream()
					.flatMap(tagGroup -> tagGroup.getOntologyTerms().stream()).collect(toList());

			if (ontologyTermGroups.isEmpty())
			{
				ontologyTermGroups.addAll(atomicOntologyTermGroup.stream().map(Lists::newArrayList).collect(toList()));
			}
			else
			{
				List<List<OntologyTermImpl>> tempOntologyTermGroups = new ArrayList<>();

				for (OntologyTermImpl ontologyTerm : atomicOntologyTermGroup)
				{
					List<List<OntologyTermImpl>> copyOfOntologyTermGroups = ontologyTermGroups.stream()
							.map(list -> Lists.newArrayList(list)).collect(Collectors.toList());

					copyOfOntologyTermGroups.forEach(list -> list.add(ontologyTerm));

					tempOntologyTermGroups.addAll(copyOfOntologyTermGroups);
				}

				ontologyTermGroups = Lists.newArrayList(tempOntologyTermGroups);
			}
		}

		return ontologyTermGroups;
	}

	/**
	 * Computes the best matching synonym which is closest to a set of search terms.<br/>
	 * Will stem the {@link OntologyTermImpl} 's synonyms and the search terms, and then compute the maximum
	 * {@link StringDistance} between them. 0 means disjunct, 1 means identical
	 * 
	 * @param ontologyTerm
	 *            the {@link OntologyTermImpl}
	 * @param queryWords
	 *            the search terms
	 * @return the maximum {@link StringDistance} between the ontologyterm and the search terms
	 */
	Hit<String> bestMatchingSynonym(OntologyTermImpl ontologyTerm, Set<String> queryWords)
	{
		List<Hit<String>> collect = collectLowerCaseTerms(ontologyTerm).stream()
				.map(term -> termJoiner.join(removeIllegalCharactersAndStopWords(term)))
				.filter(term -> queryWords.containsAll(Stemmer.splitAndStem(term)))
				.map(synonym -> Hit.create(synonym, distanceFrom(synonym, queryWords))).sorted(reverseOrder())
				.collect(toList());

		return collect.get(0);
	}

	float distanceFrom(String term, Set<String> searchTerms)
	{
		String s1 = Stemmer.stemAndJoin(removeIllegalCharactersAndStopWords(term));
		String s2 = Stemmer.stemAndJoin(searchTerms);
		float distance = (float) stringMatching(s1, s2) / 100;
		LOG.debug("Similarity between: {} and {} is {}", s1, s2, distance);
		return distance;
	}

	Set<String> removeIllegalCharactersAndStopWords(String description)
	{
		return newLinkedHashSet(stream(description.split(ILLEGAL_CHARS_REGEX)).map(StringUtils::lowerCase)
				.filter(word -> !STOPWORDSLIST.contains(word)).filter(StringUtils::isNotBlank).collect(toList()));
	}

	float round(float score)
	{
		return Math.round(score * 100000) / 100000.0f;
	}

	private boolean tagGroupKeyConcept(List<SemanticType> keyConcepts, TagGroup tagGroup)
	{
		if (keyConcepts.isEmpty()) return true;
		return tagGroup.getOntologyTerms().stream().flatMap(ot -> ot.getSemanticTypes().stream())
				.allMatch(keyConcepts::contains);
	}
}