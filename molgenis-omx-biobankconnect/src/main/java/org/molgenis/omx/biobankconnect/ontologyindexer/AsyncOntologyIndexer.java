package org.molgenis.omx.biobankconnect.ontologyindexer;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.omx.biobankconnect.utils.OntologyTable;
import org.molgenis.omx.biobankconnect.utils.OntologyTermTable;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
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
	private Database database;
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

	@Async
	public void index(String ontologyName, File ontologyFile)
	{
		isCorrectOntology = true;
		runningIndexProcesses.incrementAndGet();

		try
		{
			OntologyLoader model = new OntologyLoader(ontologyName, ontologyFile);
			ontologyUri = model.getOntologyIRI() == null ? StringUtils.EMPTY : model.getOntologyIRI();
			searchService.indexTupleTable("ontology-" + ontologyUri, new OntologyTable(model, database));
			searchService.indexTupleTable("ontologyTerm-" + ontologyUri, new OntologyTermTable(model, database));
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

	public void removeOntology(String ontologyURI)
	{
		try
		{
			List<Ontology> ontologies = database.find(Ontology.class, new QueryRule(Ontology.IDENTIFIER,
					Operator.EQUALS, ontologyURI));
			if (ontologies.size() > 0)
			{
				for (Ontology ontology : ontologies)
				{
					List<OntologyTerm> ontologyTerms = database.find(OntologyTerm.class, new QueryRule(
							OntologyTerm.ONTOLOGY_IDENTIFIER, Operator.EQUALS, ontology.getIdentifier()));

					if (ontologyTerms.size() > 0) database.remove(ontologyTerms);
				}
				database.remove(ontologies);
			}
		}
		catch (DatabaseException e)
		{
			new RuntimeException(e);
		}
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