package org.molgenis.omx.ontologyMatcher.plugin;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.Entity;

public class OntologyMatcherPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private OntologyMatcherModel model;
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	public OntologyMatcherPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.model = new OntologyMatcherModel();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + OntologyMatcherPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder header = new StringBuilder();
		header.append("<link rel=\"stylesheet\" href=\"/css/bootstrap-fileupload.min.css\" type=\"text/css\">")
				.append("<link rel=\"stylesheet\" href=\"/css/ontology-matcher.css\" type=\"text/css\">")
				.append("<script type=\"text/javascript\" src=\"/js/common-component.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"/js/bootstrap-fileupload.min.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"/js/ontology-matcher.js\"></script>");
		return header.toString();
	}

	@Override
	public String getViewName()
	{
		return "OntologyMatcherPlugin";
	}

	public OntologyMatcherModel getMyModel()
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
		}
	}

	@Override
	public void reload(Database db)
	{
		try
		{
			model.getDataSets().clear();

			for (DataSet dataSet : db.find(DataSet.class))
			{
				if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) model.getDataSets().add(dataSet);

			}
			if (model.getDataSets().size() > 0) model.setSelectedDataSet(model.getDataSets().get(0));
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}
}