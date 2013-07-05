package org.molgenis.omx.harmonization.plugin;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;

public class HarmonizationPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private HarmonizationModel model;

	public HarmonizationPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.model = new HarmonizationModel();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + HarmonizationPlugin.class.getName().replace('.', '/') + ".ftl";
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
		return "HarmonizationPlugin";
	}

	public HarmonizationModel getMyModel()
	{
		return model;
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws DatabaseException
	{
		if ("annotateDataItems".equals(request.getAction()))
		{
			Integer selectedDataSetId = Integer.parseInt(request.getString("selectedDataSet"));
			model.setSelectedDataSet(db.findById(DataSet.class, selectedDataSetId));
			getOntologyAnnotator().annotate(selectedDataSetId);
		}
	}

	@Override
	public void reload(Database db)
	{
		try
		{
			model.getDataSets().clear();

			for (DataSet dataSet : db.find(DataSet.class))
				model.getDataSets().add(dataSet);

			if (model.getDataSets().size() > 0) model.setSelectedDataSet(model.getDataSets().get(0));
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	private OntologyAnnotator getOntologyAnnotator()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(OntologyAnnotator.class);
	}
}