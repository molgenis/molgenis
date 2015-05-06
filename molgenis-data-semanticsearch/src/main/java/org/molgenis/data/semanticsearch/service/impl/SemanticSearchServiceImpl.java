package org.molgenis.data.semanticsearch.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.StringDistance;
import org.elasticsearch.common.base.Joiner;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.semantic.ItemizedSearchResult;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	public static final int MAX_NUM_TAGS = 100;

	private static final Logger LOG = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private MetaDataService metaDataService;

	public static final Set<String> STOP_WORDS;

	private static final float CUTOFF = 0.4f;

	private static StringDistance stringDistance = new NGramDistance(2);

	private Splitter termSplitter = Splitter.onPattern("[^\\p{IsAlphabetic}]+");
	private Joiner termJoiner = Joiner.on(' ');

	static
	{
		STOP_WORDS = new HashSet<String>(Arrays.asList("a", "you", "about", "above", "after", "again", "against",
				"all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
				"being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did",
				"didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
				"further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
				"he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
				"i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
				"let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on",
				"once", "only", "or", "other", "ought", "our", "ours ", " ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
				"that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
				"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under",
				"until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't",
				"what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
				"why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves", "many", ")", "("));
	}

	@Override
	public Iterable<AttributeMetaData> findAttributes(Package p, AttributeMetaData attributeMetaData)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ItemizedSearchResult<java.lang.Package>> findPackages(String searchTerm)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<AttributeMetaData, Hit<OntologyTerm>> findTags(String entity, List<String> ontologyIds)
	{
		Map<AttributeMetaData, Hit<OntologyTerm>> result = new LinkedHashMap<AttributeMetaData, Hit<OntologyTerm>>();
		EntityMetaData emd = metaDataService.getEntityMetaData(entity);
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			Hit<OntologyTerm> tag = findTags(amd, ontologyIds);
			if (tag != null)
			{
				result.put(amd, tag);
			}
		}
		return result;
	}

	@Override
	public Hit<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds)
	{
		String description = attribute.getDescription() == null ? attribute.getLabel() : attribute.getDescription();
		Set<String> searchTerms = splitIntoTerms(description);
		LOG.debug("findOntologyTerms({},{},{})", ontologyIds, searchTerms, MAX_NUM_TAGS);
		List<OntologyTerm> candidates = ontologyService.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS);
		LOG.debug("Candidates: {}", candidates);
		List<Hit<OntologyTerm>> hits = candidates.stream()
				.map(o -> Hit.<OntologyTerm> create(o, bestMatchingSynonym(o, searchTerms).getScore()))
				.sorted(Ordering.natural().reverse()).collect(Collectors.toList());
		LOG.debug("Hits: {}", hits);

		Hit<OntologyTerm> result = null;
		String bestMatchingSynonym = null;
		Stemmer stemmer = new Stemmer();
		for (Hit<OntologyTerm> hit : hits)
		{
			String bestMatchingSynonymForHit = bestMatchingSynonym(hit.getResult(), searchTerms).getResult();
			if (result == null)
			{
				result = hit;
				bestMatchingSynonym = bestMatchingSynonymForHit;
			}
			else
			{
				// split first, then join!
				String joinedSynonyms = termJoiner.join(bestMatchingSynonym, bestMatchingSynonymForHit);
				Hit<OntologyTerm> joinedHit = Hit.create(OntologyTerm.and(result.getResult(), hit.getResult()),
						distanceFrom(joinedSynonyms, searchTerms, stemmer));
				if (joinedHit.compareTo(result) > 0)
				{
					result = joinedHit;
				}
			}
			LOG.debug("result: {}", result);
		}
		if (result != null && result.getScore() >= CUTOFF)
		{
			LOG.info("Tag {} with {}", attribute, result);
			return result;
		}
		return null;
	}

	/**
	 * Computes the best matching synonym which is closest to a set of search terms.<br/>
	 * Will stem the {@link OntologyTerm} 's synonyms and the search terms, and then compute the maximum
	 * {@link StringDistance} between them. 0 means disjunct, 1 means identical
	 * 
	 * @param o
	 *            the {@link OntologyTerm}
	 * @param searchTerms
	 *            the search terms
	 * @return the maximum {@link StringDistance} between the ontologyterm and the search terms
	 */
	public Hit<String> bestMatchingSynonym(OntologyTerm o, Set<String> searchTerms)
	{
		Stemmer stemmer = new Stemmer();
		Optional<Hit<String>> bestSynonym = o.getSynonyms().stream()
				.map(synonym -> Hit.<String> create(synonym, distanceFrom(synonym, searchTerms, stemmer)))
				.max(Comparator.naturalOrder());
		return bestSynonym.get();
	}

	private float distanceFrom(String synonym, Set<String> searchTerms, Stemmer stemmer)
	{
		String s1 = stemmer.stemAndJoin(splitIntoTerms(synonym));
		String s2 = stemmer.stemAndJoin(searchTerms);
		float distance = stringDistance.getDistance(s1, s2);
		LOG.debug("Similarity between: {} and {} is {}", s1, s2, distance);
		return distance;
	}

	private Set<String> splitIntoTerms(String description)
	{
		return FluentIterable.from(termSplitter.split(description)).transform(String::toLowerCase)
				.filter(w -> !STOP_WORDS.contains(w)).filter(w -> !StringUtils.isEmpty(w)).toSet();
	}
}
