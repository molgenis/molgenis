package org.molgenis.omx.biobankconnect.ontologyindexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.biobankconnect.ontologytree.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyIndexer implements OntologyIndexer, InitializingBean
{
	@Autowired
	private DataService dataService;
	@Autowired
	private MolgenisSettings molgenisSettings;
	private SearchService searchService;
	private String ontologyUri = null;
	private boolean isCorrectOntology = true;
	private static final String SYNONYM_FIELDS = "plugin.ontology.synonym.field";
	private static final Logger logger = Logger.getLogger(AsyncOntologyIndexer.class);

	private final AtomicInteger runningIndexProcesses = new AtomicInteger();

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
	}

	public boolean isIndexingRunning()
	{
		return (runningIndexProcesses.get() > 0);
	}

	@Override
	@Async
	@RunAsSystem
	public void index(OntologyLoader model)
	{
		isCorrectOntology = true;
		runningIndexProcesses.incrementAndGet();

		try
		{
			String property = molgenisSettings.getProperty(SYNONYM_FIELDS);
			if (!StringUtils.isBlank(property)) model.addSynonymsProperties(new HashSet<String>(Arrays.asList(property
					.split(","))));
			ontologyUri = model.getOntologyIRI() == null ? StringUtils.EMPTY : model.getOntologyIRI();
			searchService.indexRepository(new OntologyRepository(model, "ontology-" + ontologyUri));
			searchService.indexRepository(new OntologyTermRepository(model, "ontologyTerm-" + ontologyUri));
		}
		catch (Exception e)
		{
			isCorrectOntology = false;
			logger.error("Exception imported file is not a valid ontology", e);
		}
		finally
		{
			String ontologyName = model.getOntologyName();
			if (!dataService.hasRepository(ontologyName))
			{
				Hit hit = searchService
						.search(new SearchRequest("ontology-" + ontologyUri, new QueryImpl().eq(
								OntologyRepository.ENTITY_TYPE, OntologyRepository.TYPE_ONTOLOGY), null))
						.getSearchHits().get(0);
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				String ontologyTermEntityName = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_LABEL) ? columnValueMap
						.get(OntologyRepository.ONTOLOGY_LABEL).toString() : OntologyTermIndexRepository.DEFAULT_ONTOLOGY_TERM_REPO;
				String ontologyUrl = columnValueMap.containsKey(OntologyTermRepository.ONTOLOGY_LABEL) ? columnValueMap
						.get(OntologyRepository.ONTOLOGY_URL).toString() : OntologyTermIndexRepository.DEFAULT_ONTOLOGY_TERM_REPO;
				dataService.addRepository(new OntologyTermIndexRepository(ontologyTermEntityName, ontologyUrl,
						searchService));
			}
			runningIndexProcesses.decrementAndGet();
			ontologyUri = null;
		}
	}

	@Override
	@RunAsSystem
	public void removeOntology(String ontologyURI)
	{
		Iterable<Ontology> ontologies = dataService.findAll(Ontology.ENTITY_NAME,
				new QueryImpl().eq(Ontology.IDENTIFIER, ontologyURI), Ontology.class);

		if (Iterables.size(ontologies) > 0)
		{
			for (Ontology ontology : ontologies)
			{
				Iterable<OntologyTerm> ontologyTerms = dataService.findAll(OntologyTerm.ENTITY_NAME,
						new QueryImpl().eq(OntologyTerm.ONTOLOGY, ontology), OntologyTerm.class);

				if (Iterables.size(ontologyTerms) > 0) dataService.delete(OntologyTerm.ENTITY_NAME, ontologyTerms);
			}
			dataService.delete(Ontology.ENTITY_NAME, ontologies);
		}

		SearchRequest request = new SearchRequest("ontology-" + ontologyURI, new QueryImpl().eq(
				OntologyRepository.ONTOLOGY_URL, ontologyURI), null);
		SearchResult result = searchService.search(request);
		if (result.getTotalHitCount() > 0)
		{
			Hit hit = result.getSearchHits().get(0);
			String ontologyEntityName = hit.getColumnValueMap().get(OntologyRepository.ONTOLOGY_LABEL).toString();
			if (dataService.hasRepository(ontologyEntityName))
			{
				dataService.removeRepository(ontologyEntityName);
			}
		}

		searchService.deleteDocumentsByType("ontology-" + ontologyURI);
		searchService.deleteDocumentsByType("ontologyTerm-" + ontologyURI);
	}

	public String getOntologyUri()
	{
		return ontologyUri;
	}

	public boolean isCorrectOntology()
	{
		return this.isCorrectOntology;
	}
}