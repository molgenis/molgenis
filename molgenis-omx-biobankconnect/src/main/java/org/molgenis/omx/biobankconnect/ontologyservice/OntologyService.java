package org.molgenis.omx.biobankconnect.ontologyservice;

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
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.tartarus.snowball.ext.PorterStemmer;

public class OntologyService
{
	private final SearchService searchService;
	private static final String COMBINED_SCORE = "combinedScore";
	private static final String FUZZY_MATCH_SIMILARITY = "~0.8";
	private static final String SCORE = "score";
	private static final String NON_WORD_SEPARATOR = "[^a-zA-Z0-9]";
	private static final int MAX_NUMBER_MATCHES = 100;
	private static final PorterStemmer stemmer = new PorterStemmer();
	public static final Character DEFAULT_SEPARATOR = '|';

	@Autowired
	public OntologyService(SearchService searchService)
	{
		if (searchService == null) throw new IllegalArgumentException("SearchService is null");
		this.searchService = searchService;
	}

	public Hit getOntologyByIri(String ontologyIri)
	{
		QueryImpl q = new QueryImpl();
		q.pageSize(Integer.MAX_VALUE);
		q.addRule(new QueryRule(OntologyIndexRepository.ENTITY_TYPE, Operator.EQUALS,
				OntologyIndexRepository.TYPE_ONTOLOGY));
		q.addRule(new QueryRule(Operator.AND));
		q.addRule(new QueryRule(OntologyIndexRepository.ONTOLOGY_IRI, Operator.EQUALS, ontologyIri));
		SearchRequest searchRequest = new SearchRequest(AsyncOntologyIndexer.createOntologyDocumentType(ontologyIri),
				q, null);
		List<Hit> searchHits = searchService.search(searchRequest).getSearchHits();
		if (searchHits.size() > 0) return searchHits.get(0);
		return new Hit(null, AsyncOntologyIndexer.createOntologyDocumentType(ontologyIri),
				Collections.<String, Object> emptyMap());
	}

	public Hit findOntologyTerm(String ontologyIri, String ontologyTermIri, String nodePath)
	{
		Query q = new QueryImpl().eq(OntologyTermIndexRepository.NODE_PATH, nodePath).and()
				.eq(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, ontologyTermIri).pageSize(5000);
		String documentType = AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri);
		SearchResult result = searchService.search(new SearchRequest(documentType, q, null));
		for (Hit hit : result.getSearchHits())
		{
			if (hit.getColumnValueMap().get(OntologyTermIndexRepository.NODE_PATH).toString().equals(nodePath)) return hit;
		}
		return new Hit(null, documentType, Collections.<String, Object> emptyMap());
	}

	public List<Hit> getChildren(String ontologyIri, String parentOntologyTermIri, String parentNodePath)
	{
		Query q = new QueryImpl().eq(OntologyTermIndexRepository.PARENT_NODE_PATH, parentNodePath).and()
				.eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, parentOntologyTermIri).pageSize(5000);
		String documentType = AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri);
		List<Hit> listOfHits = new ArrayList<Hit>();
		Set<String> processedOntologyTerms = new HashSet<String>();
		for (Hit hit : searchService.search(new SearchRequest(documentType, q, null)).getSearchHits())
		{
			String ontologyTermIri = hit.getColumnValueMap().get(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI)
					.toString();
			if (!processedOntologyTerms.contains(ontologyTermIri))
			{
				listOfHits.add(hit);
				processedOntologyTerms.add(ontologyTermIri);
			}
		}
		return listOfHits;
	}

	public List<Hit> getRootOntologyTerms(String ontologyIri)
	{
		SearchRequest searchRequest = new SearchRequest(
				AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri), new QueryImpl().pageSize(
						Integer.MAX_VALUE).eq(OntologyTermIndexRepository.ROOT, true), null);
		List<Hit> listOfHits = new ArrayList<Hit>();
		Set<String> processedOntologyTerms = new HashSet<String>();
		for (Hit hit : searchService.search(searchRequest))
		{
			String ontologyTermIri = hit.getColumnValueMap().get(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI)
					.toString();
			if (!processedOntologyTerms.contains(ontologyTermIri))
			{
				listOfHits.add(hit);
				processedOntologyTerms.add(ontologyTermIri);
			}
		}
		return listOfHits;
	}

	public List<Ontology> getAllOntologies()
	{
		List<Ontology> ontologies = new ArrayList<Ontology>();
		QueryImpl q = new QueryImpl();
		q.pageSize(Integer.MAX_VALUE);
		q.addRule(new QueryRule(OntologyIndexRepository.ENTITY_TYPE, Operator.EQUALS,
				OntologyIndexRepository.TYPE_ONTOLOGY));
		SearchRequest searchRequest = new SearchRequest(null, q, null);
		for (Hit hit : searchService.search(searchRequest).getSearchHits())
		{
			Ontology ontology = new Ontology();
			ontology.setIdentifier(hit.getColumnValueMap().get(OntologyIndexRepository.ONTOLOGY_IRI).toString());
			ontology.setOntologyURI(hit.getColumnValueMap().get(OntologyIndexRepository.ONTOLOGY_IRI).toString());
			ontology.setName(hit.getColumnValueMap().get(OntologyIndexRepository.ONTOLOGY_NAME).toString());
			ontologies.add(ontology);
		}
		return ontologies;
	}

	public SearchResult search(String ontologyIri, String queryString)
	{
		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(queryString.toLowerCase().trim()
				.split(NON_WORD_SEPARATOR)));
		uniqueTerms.removeAll(NGramMatchingModel.STOPWORDSLIST);
		List<QueryRule> rules = new ArrayList<QueryRule>();
		for (String term : uniqueTerms)
		{
			if (!StringUtils.isEmpty(term) && !term.matches(OntologyTermQueryRepository.MULTI_WHITESPACES))
			{
				stemmer.setCurrent(term.replaceAll(OntologyTermQueryRepository.ILLEGAL_CHARACTERS_PATTERN,
						StringUtils.EMPTY));
				stemmer.stem();
				rules.add(new QueryRule(OntologyTermIndexRepository.SYNONYMS, Operator.EQUALS, stemmer.getCurrent()
						+ FUZZY_MATCH_SIMILARITY));
			}
		}
		QueryRule finalQuery = new QueryRule(rules);
		finalQuery.setOperator(Operator.SHOULD);
		SearchRequest request = new SearchRequest(AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyIri),
				new QueryImpl(finalQuery).pageSize(MAX_NUMBER_MATCHES), null);
		Iterator<Hit> iterator = searchService.search(request).getSearchHits().iterator();

		List<ComparableHit> comparableHits = new ArrayList<ComparableHit>();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			String ontologySynonym = hit.getColumnValueMap().get(OntologyTermIndexRepository.SYNONYMS).toString();
			BigDecimal luceneScore = new BigDecimal(hit.getColumnValueMap().get(SCORE).toString());
			BigDecimal ngramScore = new BigDecimal(NGramMatchingModel.stringMatching(
					StringUtils.join(uniqueTerms, OntologyTermQueryRepository.ILLEGAL_CHARACTERS_REPLACEMENT),
					ontologySynonym));
			comparableHits.add(new ComparableHit(hit, luceneScore.multiply(ngramScore)));
		}
		Collections.sort(comparableHits);
		return convertResults(comparableHits);
	}

	private SearchResult convertResults(List<ComparableHit> comparableHits)
	{
		List<Hit> hits = new ArrayList<Hit>();
		Set<String> uniqueIdentifiers = new HashSet<String>();
		for (ComparableHit comparableHit : comparableHits)
		{
			Hit hit = comparableHit.getHit();
			String identifier = hit.getColumnValueMap().get(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI).toString();
			if (uniqueIdentifiers.contains(identifier)) continue;
			uniqueIdentifiers.add(identifier);
			Map<String, Object> columnValueMap = new HashMap<String, Object>();
			columnValueMap.putAll(hit.getColumnValueMap());
			columnValueMap.put(COMBINED_SCORE, comparableHit.getSimilarityScore().doubleValue());
			Hit copyHit = new Hit(hit.getId(), hit.getDocumentType(), columnValueMap);
			hits.add(copyHit);
		}
		return new SearchResult(hits.size(), hits);
	}

	class ComparableHit implements Comparable<ComparableHit>
	{
		private final Hit hit;
		private final BigDecimal similarityScore;

		public ComparableHit(Hit hit, BigDecimal similarityScore)
		{
			this.hit = hit;
			this.similarityScore = similarityScore;
		}

		private BigDecimal getSimilarityScore()
		{
			return similarityScore;
		}

		public Hit getHit()
		{
			return hit;
		}

		@Override
		public int compareTo(ComparableHit other)
		{
			return similarityScore.compareTo(other.getSimilarityScore()) * (-1);
		}
	}
}