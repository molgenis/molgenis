package org.molgenis.omx.harmonization.controllers;

import static org.molgenis.omx.harmonization.controllers.OntologyAnnotatorController.URI;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.harmonization.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.observ.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(URI)
public class OntologyAnnotatorController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "ontologyannotator";

	@Autowired
	private OntologyAnnotator ontologyAnnotator;

	@Autowired
	private OntologyMatcher ontologyMatcher;

	@Autowired
	private Database database;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	public OntologyAnnotatorController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, Model model) throws Exception
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		if (selectedDataSetId != null) model.addAttribute("selectedDataSet", selectedDataSetId);
		model.addAttribute("dataSets", dataSets);
		model.addAttribute("isRunning", ontologyAnnotator.isRunning());

		return "OntologyAnnotatorPlugin";
	}

	@RequestMapping(value = "/annotate", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String annotate(@RequestParam(value = "selectedDataSet", required = true)
	String selectedDataSetId, Model model) throws DatabaseException
	{
		ontologyAnnotator.annotate(Integer.parseInt(selectedDataSetId));
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		model.addAttribute("selectedDataSet", selectedDataSetId);
		model.addAttribute("dataSets", dataSets);
		model.addAttribute("isRunning", ontologyAnnotator.isRunning());
		model.addAttribute("message", "Please refresh the page to see result!");

		return "OntologyAnnotatorPlugin";
	}
}