package org.molgenis.omx.harmonization.controllers;

import static org.molgenis.omx.harmonization.controllers.OntologyAnnotatorController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.ontologyAnnotator.plugin.OntologyAnnotator;
import org.molgenis.omx.ontologyMatcher.lucene.OntologyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

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
	public String init(Model model) throws Exception
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		model.addAttribute("dataSets", dataSets);

		return "OntologyAnnotatorPlugin";
	}

	@RequestMapping(value = "/annotate", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void annotate(@RequestBody
	OntologyAnnotatorRequest request)
	{
		ontologyAnnotator.annotate(request.getDataSetId());
	}

	@RequestMapping(value = "/match", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void match(@RequestBody
	OntologyAnnotatorRequest request) throws DatabaseException
	{
		System.out.println("The catalogue to match is : " + request.getSelectedDataSets());
		System.out.println("The selected catalogue is : " + request.getDataSetId());
		ontologyMatcher.match(request.getDataSetId(), request.getSelectedDataSets());
	}

	class OntologyAnnotatorRequest
	{
		private Integer dataSetId;
		private List<Integer> selectedDataSets;

		public OntologyAnnotatorRequest(Integer dataSetId, List<Integer> selectedDataSets)
		{
			this.dataSetId = dataSetId;
			this.selectedDataSets = selectedDataSets;
		}

		public Integer getDataSetId()
		{
			return dataSetId;
		}

		public List<Integer> getSelectedDataSets()
		{
			return selectedDataSets;
		}
	}
}