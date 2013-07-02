package org.molgenis.omx.ontologyIndexer.plugin;

import java.io.File;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;

public class OntologyIndexerPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private OntologyIndexerModel model;

	public OntologyIndexerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.model = new OntologyIndexerModel();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + OntologyIndexerPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder header = new StringBuilder();
		header.append("<link rel=\"stylesheet\" href=\"/css/bootstrap-fileupload.min.css\" type=\"text/css\">")
				.append("<link rel=\"stylesheet\" href=\"/css/ontology-indexer.css\" type=\"text/css\">")
				.append("<script type=\"text/javascript\" src=\"/js/bootstrap-fileupload.min.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"/js/ontology-indexer.js\"></script>");
		return header.toString();
	}

	@Override
	public String getViewName()
	{
		return "OntologyIndexerPlugin";
	}

	public OntologyIndexerModel getMyModel()
	{
		return model;
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if ("indexOntology".equals(request.getAction()))
		{
			try
			{
				File file = request.getFile("uploadedOntology");
				getHarmonizationIndexer().index(file);
			}
			catch (TableException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void reload(Database db)
	{
		this.model.setOntologyUri(getOntologyUri());
		this.model.setStartProcess(isIndexingRunning());
		this.model.setCorrectOntology(isCorrectOntology());
	}

	private OntologyIndexer getHarmonizationIndexer()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(OntologyIndexer.class);
	}

	public boolean isIndexingRunning()
	{
		return getHarmonizationIndexer().isIndexingRunning();
	}

	public boolean isCorrectOntology()
	{
		return getHarmonizationIndexer().isCorrectOntology();
	}

	public String getOntologyUri()
	{
		return getHarmonizationIndexer().getOntologyUri();
	}
}