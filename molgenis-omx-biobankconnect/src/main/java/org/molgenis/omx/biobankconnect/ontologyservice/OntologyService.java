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
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
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
	private static final String SCORE = "score";
	private static final PorterStemmer stemmer = new PorterStemmer();

	@Autowired
	public OntologyService(SearchService searchService)
	{
		if (searchService == null) throw new IllegalArgumentException("SearchService is null");
		this.searchService = searchService;
	}

	public Hit getOntologyByUrl(String ontologyUrl)
	{
		QueryImpl q = new QueryImpl();
		q.pageSize(1000);
		q.addRule(new QueryRule(OntologyRepository.ENTITY_TYPE, Operator.EQUALS, OntologyRepository.TYPE_ONTOLOGY));
		q.addRule(new QueryRule(Operator.AND));
		q.addRule(new QueryRule(OntologyRepository.ONTOLOGY_URL, Operator.EQUALS, ontologyUrl));
		SearchRequest searchRequest = new SearchRequest(createOntologyDocumentType(ontologyUrl), q, null);
		List<Hit> searchHits = searchService.search(searchRequest).getSearchHits();
		if (searchHits.size() > 0) return searchHits.get(0);
		return new Hit(null, createOntologyDocumentType(ontologyUrl), Collections.<String, Object> emptyMap());
	}

	public Hit findOntologyTerm(String ontologyUrl, String ontologyTermUrl, String nodePath)
	{
		Query q = new QueryImpl().eq(OntologyTermRepository.NODE_PATH, nodePath).and()
				.eq(OntologyTermRepository.ONTOLOGY_TERM_IRI, ontologyTermUrl).pageSize(5000);
		String documentType = createOntologyTermDocumentType(ontologyUrl);
		SearchResult result = searchService.search(new SearchRequest(documentType, q, null));
		for (Hit hit : result.getSearchHits())
		{
			if (hit.getColumnValueMap().get(OntologyTermRepository.NODE_PATH).toString().equals(nodePath)) return hit;
		}
		return new Hit(null, documentType, Collections.<String, Object> emptyMap());
	}

	public List<Hit> getChildren(String ontologyUrl, String parentOntologyTermUrl, String parentNodePath)
	{
		Query q = new QueryImpl().eq(OntologyTermRepository.PARENT_NODE_PATH, parentNodePath).and()
				.eq(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, parentOntologyTermUrl).pageSize(5000);
		String documentType = createOntologyTermDocumentType(ontologyUrl);
		List<Hit> listOfHits = new ArrayList<Hit>();
		Set<String> processedOntologyTerms = new HashSet<String>();
		for (Hit hit : searchService.search(new SearchRequest(documentType, q, null)).getSearchHits())
		{
			String ontologyTermUrl = hit.getColumnValueMap().get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString();
			if (!processedOntologyTerms.contains(ontologyTermUrl))
			{
				listOfHits.add(hit);
				processedOntologyTerms.add(ontologyTermUrl);
			}
		}
		return listOfHits;
	}

	public List<Hit> getRootOntologyTerms(String ontologyUrl)
	{
		SearchRequest searchRequest = new SearchRequest(OntologyService.createOntologyTermDocumentType(ontologyUrl),
				new QueryImpl().pageSize(10000).eq(OntologyTermRepository.ROOT, true), null);
		List<Hit> listOfHits = new ArrayList<Hit>();
		Set<String> processedOntologyTerms = new HashSet<String>();
		for (Hit hit : searchService.search(searchRequest))
		{
			String ontologyTermUrl = hit.getColumnValueMap().get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString();
			if (!processedOntologyTerms.contains(ontologyTermUrl))
			{
				listOfHits.add(hit);
				processedOntologyTerms.add(ontologyTermUrl);
			}
		}
		return listOfHits;
	}

	public List<Ontology> getAllOntologies()
	{
		List<Ontology> ontologies = new ArrayList<Ontology>();
		QueryImpl q = new QueryImpl();
		q.pageSize(1000);
		q.addRule(new QueryRule(OntologyRepository.ENTITY_TYPE, Operator.EQUALS, OntologyRepository.TYPE_ONTOLOGY));
		SearchRequest searchRequest = new SearchRequest(null, q, null);
		for (Hit hit : searchService.search(searchRequest).getSearchHits())
		{
			Ontology ontology = new Ontology();
			ontology.setIdentifier(hit.getColumnValueMap().get(OntologyRepository.ONTOLOGY_URL).toString());
			ontology.setOntologyURI(hit.getColumnValueMap().get(OntologyRepository.ONTOLOGY_URL).toString());
			ontology.setName(hit.getColumnValueMap().get(OntologyRepository.ONTOLOGY_LABEL).toString());
			ontologies.add(ontology);
		}
		return ontologies;
	}

	public SearchResult search(String ontologyUrl, String queryString)
	{
		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(queryString.toLowerCase().trim()
				.split("[^a-zA-Z0-9]")));
		uniqueTerms.removeAll(NGramMatchingModel.STOPWORDSLIST);
		List<QueryRule> rules = new ArrayList<QueryRule>();
		for (String term : uniqueTerms)
		{
			if (!term.isEmpty() && !term.matches(" +"))
			{
				stemmer.setCurrent(term.replaceAll("[^(a-zA-Z0-9 )]", StringUtils.EMPTY));
				stemmer.stem();
				rules.add(new QueryRule(OntologyTermRepository.SYNONYMS, Operator.EQUALS, stemmer.getCurrent() + "~0.8"));
			}
		}
		QueryRule finalQuery = new QueryRule(rules);
		finalQuery.setOperator(Operator.SHOULD);
		SearchRequest request = new SearchRequest(createOntologyTermDocumentType(ontologyUrl),
				new QueryImpl(finalQuery).pageSize(100), null);
		Iterator<Hit> iterator = searchService.search(request).getSearchHits().iterator();

		List<ComparableHit> comparableHits = new ArrayList<ComparableHit>();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			String ontologySynonym = hit.getColumnValueMap().get(OntologyTermRepository.SYNONYMS).toString();
			BigDecimal luceneScore = new BigDecimal(hit.getColumnValueMap().get(SCORE).toString());
			BigDecimal ngramScore = new BigDecimal(NGramMatchingModel.stringMatching(
					StringUtils.join(uniqueTerms, " "), ontologySynonym));
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
			String identifier = hit.getColumnValueMap().get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString();
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

	public static String createOntologyDocumentType(String ontologyUrl)
	{
		return ontologyUrl == null ? null : "ontology-" + ontologyUrl;
	}

	public static String createOntologyTermDocumentType(String ontologyUrl)
	{
		return ontologyUrl == null ? null : "ontologyTerm-" + ontologyUrl;
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