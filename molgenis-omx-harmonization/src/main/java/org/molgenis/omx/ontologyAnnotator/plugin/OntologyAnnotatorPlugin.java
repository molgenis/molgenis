package org.molgenis.omx.ontologyAnnotator.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.ontologyMatcher.lucene.OntologyMatcher;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OntologyAnnotatorPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private OntologyAnnotatorModel model;
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	public OntologyAnnotatorPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.model = new OntologyAnnotatorModel();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + OntologyAnnotatorPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder header = new StringBuilder();
		header.append("<link rel=\"stylesheet\" href=\"/css/bootstrap-fileupload.min.css\" type=\"text/css\">")
				.append("<link rel=\"stylesheet\" href=\"/css/ontology-annotator.css\" type=\"text/css\">")
				.append("<script type=\"text/javascript\" src=\"/js/common-component.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"/js/bootstrap-fileupload.min.js\"></script>")
				.append("<script type=\"text/javascript\" src=\"/js/ontology-annotator.js\"></script>");

		return header.toString();
	}

	@Override
	public String getViewName()
	{
		return "OntologyAnnotatorPlugin";
	}

	public OntologyAnnotatorModel getMyModel()
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
		else if ("ontologyMatch".equals(request.getAction()))
		{
			if (getLuceneMatcher().isRunning())
			{
				getLuceneMatcher().matchPercentage();
			}
			else
			{
				Integer selectedDataSetId = Integer.parseInt(request.getString("selectedDataSet"));
				Map<String, String> selectedCatalogues = new Gson().fromJson(
						request.getString("selectedStudiesToMatch"), new TypeToken<Map<String, String>>()
						{
						}.getType());
				System.out.println("The catalogue to match is : " + selectedCatalogues.keySet());
				System.out.println("The selected catalogue is : " + selectedDataSetId);

				List<Integer> dataSetsToMatch = new ArrayList<Integer>();

				for (String id : selectedCatalogues.keySet())
				{
					dataSetsToMatch.add(Integer.parseInt(id));
				}
				getLuceneMatcher().match(selectedDataSetId, dataSetsToMatch);
			}
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
			if (getLuceneMatcher().isRunning()) getLuceneMatcher().matchPercentage();
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	private OntologyMatcher getLuceneMatcher()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(OntologyMatcher.class);
	}

	private OntologyAnnotator getOntologyAnnotator()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(OntologyAnnotator.class);
	}
}