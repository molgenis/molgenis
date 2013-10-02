package org.molgenis.omx.harmonization.ontologyindexer;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.omx.harmonization.tupleTables.OntologyTable;
import org.molgenis.omx.harmonization.tupleTables.OntologyTermTable;
import org.molgenis.omx.harmonization.utils.OntologyLoader;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyIndexer implements OntologyIndexer, InitializingBean
{
	@Autowired
	@Qualifier("unsecuredDatabase")
	private Database unsecuredDatabase;
	private SearchService searchService;
	private String ontologyUri = null;
	private boolean isCorrectOntology = true;

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

	@Async
	public void index(String ontologyName, File ontologyFile)
	{
		isCorrectOntology = true;
		runningIndexProcesses.incrementAndGet();

		try
		{
			OntologyLoader model = new OntologyLoader(ontologyName, ontologyFile);
			ontologyUri = model.getOntologyIRI() == null ? StringUtils.EMPTY : model.getOntologyIRI();
			searchService.indexTupleTable("ontology-" + ontologyUri, new OntologyTable(model, unsecuredDatabase));
			searchService.indexTupleTable("ontologyTerm-" + ontologyUri,
					new OntologyTermTable(model, unsecuredDatabase));
		}
		catch (OWLOntologyCreationException e)
		{
			isCorrectOntology = false;
			e.printStackTrace();
		}
		finally
		{
			runningIndexProcesses.decrementAndGet();
			ontologyUri = null;
		}
	}

	public void removeOntology(String ontologyURI)
	{
		searchService.deleteDocumentsByType("ontology-" + ontologyURI);
		searchService.deleteDocumentsByType("ontologyTerm-" + ontologyURI);
	}

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