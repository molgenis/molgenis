package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.File;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.harmonizationIndexer.controller.OntologyModel;
import org.molgenis.search.SearchService;
import org.molgenis.util.DatabaseUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncHarmonizationIndexer implements HarmonizationIndexer, InitializingBean
{
	private SearchService searchService;

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

	@Async
	public void index(File ontologyFile)
	{
		Database db = DatabaseUtil.createDatabase();

		try
		{
			OntologyModel model = new OntologyModel(ontologyFile);
			searchService.indexTupleTable("ontology", new OntologyTable(model, db));
			searchService.indexTupleTable("ontologyTerm", new OntologyTermTable(model, db));
		}
		catch (OWLOntologyCreationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
