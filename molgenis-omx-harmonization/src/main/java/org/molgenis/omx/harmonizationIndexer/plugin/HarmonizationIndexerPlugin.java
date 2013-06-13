package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.File;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.harmonizationIndexer.controller.OntologyModel;
import org.molgenis.search.SearchService;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class HarmonizationIndexerPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;
	private SearchService searchService;

	public HarmonizationIndexerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + HarmonizationIndexerPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder header = new StringBuilder();
		header.append("<link rel=\"stylesheet\" href=\"/css/bootstrap-fileupload.min.css\" type=\"text/css\">")
				.append("<link rel=\"stylesheet\" href=\"/css/harmonization-indexer.css\" type=\"text/css\">")
				.append("<script type=\"text/javascript\" src=\"/js/bootstrap-fileupload.min.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"/js/harmonization-indexer.js\"></script>");
		return header.toString();
	}

	@Override
	public String getViewName()
	{
		return "HarmonizationIndexerPlugin";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if ("indexOntology".equals(request.getAction()))
		{
			File file = request.getFile("uploadedOntology");
			System.out.println("----------------" + file.getAbsolutePath());
			indexOntology(file, db);
		}
	}

	@Async
	public void indexOntology(File ontologyFile, Database db)
	{
		try
		{
			OntologyModel model = new OntologyModel(ontologyFile);
			searchService.indexTupleTable("ontology", new OntologyTable(model, db));
			// searchService.indexTupleTable("ontologyTerm", new
			// OntologyTermTable(model, db));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void reload(Database db)
	{
		// TODO Auto-generated method stub

	}
}
