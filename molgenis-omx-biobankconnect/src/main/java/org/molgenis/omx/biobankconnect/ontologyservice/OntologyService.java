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
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.NGramMatchingModel;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyService
{
	@Autowired
	private SearchService searchService;

	private final static String ENTITY_TYPE = "entity_type";
	private static final String ONTOLOGY_IRI = "url";
	private final static String ONTOLOGY_LABEL = "ontologyLabel";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String ONTOLOGYTERM_IRI = "ontologyTermIRI";
	private static final String COMBINED_SCORE = "combinedScore";
	private static final String SCORE = "score";

	public List<Ontology> getAllOntologies()
	{
		List<Ontology> ontologies = new ArrayList<Ontology>();
		QueryImpl q = new QueryImpl();
		q.pageSize(1000);
		q.addRule(new QueryRule(ENTITY_TYPE, Operator.EQUALS, "indexedOntology"));
		SearchRequest searchRequest = new SearchRequest(null, q, null);
		for (Hit hit : searchService.search(searchRequest).getSearchHits())
		{
			Ontology ontology = new Ontology();
			ontology.setIdentifier(hit.getColumnValueMap().get(ONTOLOGY_IRI).toString());
			ontology.setName(hit.getColumnValueMap().get(ONTOLOGY_LABEL).toString());
			ontologies.add(ontology);
		}
		return ontologies;
	}

	public SearchResult searchById(String fieldId, String valueId)
	{
		QueryImpl q = new QueryImpl();
		q.pageSize(100);
		q.addRule(new QueryRule(ENTITY_TYPE, Operator.EQUALS, "ontologyTerm"));
		q.addRule(new QueryRule(Operator.AND));
		q.addRule(new QueryRule(fieldId, Operator.EQUALS, valueId));
		SearchRequest searchRequest = new SearchRequest(null, q, null);
		return searchService.search(searchRequest);
	}

	public SearchResult search(String ontologyUrl, String queryString)
	{
		Set<String> uniqueTerms = new HashSet<String>(Arrays.asList(queryString.toLowerCase().trim()
				.split("[^a-zA-Z0-9]")));
		uniqueTerms.removeAll(NGramMatchingModel.STOPWORDSLIST);
		QueryImpl q = new QueryImpl();
		q.pageSize(100);

		for (String term : uniqueTerms)
		{
			if (!term.isEmpty() && !term.matches(" +"))
			{
				if (q.getRules().size() > 0)
				{
					q.addRule(new QueryRule(Operator.OR));
				}
				term = term.replaceAll("[^(a-zA-Z0-9 )]", StringUtils.EMPTY);
				q.addRule(new QueryRule(ONTOLOGYTERM_SYNONYM, Operator.SEARCH, term));
			}
		}

		queryString = StringUtils.join(uniqueTerms, " +");
		SearchRequest request = new SearchRequest(createDocumentType(ontologyUrl), q, null);
		Iterator<Hit> iterator = searchService.search(request).getSearchHits().iterator();

		List<ComparableHit> comparableHits = new ArrayList<ComparableHit>();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			String ontologySynonym = hit.getColumnValueMap().get(ONTOLOGYTERM_SYNONYM).toString();
			BigDecimal luceneScore = new BigDecimal(hit.getColumnValueMap().get(SCORE).toString());
			BigDecimal ngramScore = new BigDecimal(NGramMatchingModel.stringMatching(queryString, ontologySynonym));
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
			String identifier = hit.getColumnValueMap().get(ONTOLOGYTERM_IRI).toString();
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

	private String createDocumentType(String ontologyUrl)
	{
		return "ontologyTerm-" + ontologyUrl;
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