package org.molgenis.omx.biobankconnect.ontologyindexer;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyIndexer implements OntologyIndexer, InitializingBean
{
	@Autowired
	private DataService dataService;
	private SearchService searchService;
	private String ontologyUri = null;
	private boolean isCorrectOntology = true;
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

	@Override
	public boolean isIndexingRunning()
	{
		return (runningIndexProcesses.get() > 0);
	}

	@Override
	@Async
	@RunAsSystem
	public void index(String ontologyName, File ontologyFile)
	{
		isCorrectOntology = true;
		runningIndexProcesses.incrementAndGet();

		try
		{
			OntologyLoader model = new OntologyLoader(ontologyName, ontologyFile);
			ontologyUri = model.getOntologyIRI() == null ? StringUtils.EMPTY : model.getOntologyIRI();
			searchService.indexRepository(new OntologyRepository(model, "ontology-" + ontologyUri));
			searchService.indexRepository(new OntologyTermRepository(model, "ontologyTerm-" + ontologyUri));
		}
		catch (OWLOntologyCreationException e)
		{
			isCorrectOntology = false;
			logger.error("Exception imported file is not a valid ontology", e);
		}
		finally
		{
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

		searchService.deleteDocumentsByType("ontology-" + ontologyURI);
		searchService.deleteDocumentsByType("ontologyTerm-" + ontologyURI);
	}

	@Override
	public String getOntologyUri()
	{
		return ontologyUri;
	}

	@Override
	public boolean isCorrectOntology()
	{
		return this.isCorrectOntology;
	}
}