package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.File;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class HarmonizationIndexerPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private HarmonizationModel model;

	public HarmonizationIndexerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
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

	public HarmonizationModel getMyModel()
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
			catch (OWLOntologyCreationException e)
			{
				model.setErrorMessage("Failed to load ontology");
				e.printStackTrace();
			}
			catch (TableException e)
			{
				model.setErrorMessage("Failed to create index");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void reload(Database db)
	{
		this.model = new HarmonizationModel();
		this.model.setOntologyUri(getRunningIndex());
	}

	private HarmonizationIndexer getHarmonizationIndexer()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(HarmonizationIndexer.class);
	}

	public String getRunningIndex()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(HarmonizationIndexer.class).getOntologyUri();
	}
}