package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.StringDistance;
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

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	public static final int MAX_NUM_TAGS = 100;

	private static final Logger LOG = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private MetaDataService metaDataService;

	public static final Set<String> STOP_WORDS;

	private static StringDistance stringDistance = new NGramDistance(2);

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
	public Map<AttributeMetaData, List<Hit<OntologyTerm>>> findTags(String entity, List<String> ontologyIds)
	{
		Map<AttributeMetaData, List<Hit<OntologyTerm>>> result = new LinkedHashMap<AttributeMetaData, List<Hit<OntologyTerm>>>();
		EntityMetaData emd = metaDataService.getEntityMetaData(entity);
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			result.put(amd, findTags(amd, ontologyIds));
		}
		return result;
	}

	@Override
	public List<Hit<OntologyTerm>> findTags(AttributeMetaData attribute, List<String> ontologyIds)
	{
		String description = attribute.getDescription() == null ? attribute.getLabel() : attribute.getDescription();
		Set<String> searchTerms = splitIntoTerms(description);
		LOG.info("findOntologyTerms({},{},{})", ontologyIds, searchTerms, MAX_NUM_TAGS);
		List<OntologyTerm> candidates = ontologyService.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS);
		LOG.info("Candidates: {}", candidates);
		Stream<Hit<OntologyTerm>> hits = candidates.stream().map(o -> Hit.create(o, distanceFrom(o, searchTerms)))
				.sorted();
		LOG.info("Hits: {}", hits);
		// TODO: now find the best combination through chao magick
		return hits.collect(Collectors.toList());
	}

	/**
	 * Computes the minimum distance from an ontology term and a set of search terms.<br/>
	 * Will stem the {@link OntologyTerm} 's synonyms and the search terms, and then compute the maximum
	 * {@link StringDistance} between them. 0 means disjunct, 1 means identical
	 * 
	 * @param o
	 *            the {@link OntologyTerm}
	 * @param searchTerms
	 *            the search terms
	 * @return the maximum {@link StringDistance} between the ontologyterm and the search terms
	 */
	public float distanceFrom(OntologyTerm o, Set<String> searchTerms)
	{
		Stemmer stemmer = new Stemmer();
		Optional<Hit<String>> bestSynonym = o.getSynonyms().stream()
				.map(synonym -> Hit.<String> create(synonym, distanceFrom(synonym, searchTerms, stemmer)))
				.max(Comparator.naturalOrder());
		return bestSynonym.get().getScore();
	}

	private float distanceFrom(String synonym, Set<String> searchTerms, Stemmer stemmer)
	{
		return stringDistance.getDistance(stemmer.stemAndJoin(splitIntoTerms(synonym)),
				stemmer.stemAndJoin(searchTerms));
	}

	private Set<String> splitIntoTerms(String description)
	{
		return stream(description.split("[^\\p{IsAlphabetic}]+")).map(String::toLowerCase)
				.filter(w -> !STOP_WORDS.contains(w)).filter(w -> !StringUtils.isEmpty(w)).collect(Collectors.toSet());
	}
}
