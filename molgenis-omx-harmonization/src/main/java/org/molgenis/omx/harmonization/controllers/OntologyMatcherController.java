package org.molgenis.omx.harmonization.controllers;

import static org.molgenis.omx.harmonization.controllers.OntologyMatcherController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.observ.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class OntologyMatcherController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "ontologymatcher";

	@Autowired
	private OntologyMatcher ontologyMatcher;

	@Autowired
	private Database database;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	public OntologyMatcherController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "sourceDataSet", required = false)
	String sourceDataSetId, Model model) throws Exception
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}

		if (sourceDataSetId != null) model.addAttribute("selectedSourceDataSetId", sourceDataSetId);
		model.addAttribute("isComplete", ontologyMatcher.isComplete());
		model.addAttribute("isRunning", ontologyMatcher.isRunning());
		model.addAttribute("percentage", ontologyMatcher.matchPercentage());
		model.addAttribute("dataSets", dataSets);
		ontologyMatcher.initCompleteState();

		return "OntologyMatcherPlugin";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/check", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyMatcherResponse check(@RequestBody
	OntologyMatcherRequest request) throws DatabaseException
	{
		Boolean isRunning = null;
		String message = null;

		Integer sourceDataSetId = request.getSourceDataSetId();
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();

		if (sourceDataSetId != null || selectedDataSetIds != null)
		{
			if (checkExistingMappings(sourceDataSetId, selectedDataSetIds))
			{
				isRunning = false;
				message = "Mappings already exist, do you want to overwrite them?";
			}
			else
			{
				match(request);
				isRunning = true;
				message = "Matching started running! Please click on 'Reset' button to check result!";
			}
		}
		OntologyMatcherResponse response = new OntologyMatcherResponse(isRunning, message);
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/match", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyMatcherResponse match(@RequestBody
	OntologyMatcherRequest request) throws DatabaseException
	{
		Integer sourceDataSetId = request.getSourceDataSetId();
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		ontologyMatcher.match(sourceDataSetId, selectedDataSetIds);
		OntologyMatcherResponse response = new OntologyMatcherResponse(true,
				"Matching started running! Please click refresh button to check the status of matching...");
		return response;
	}

	private boolean checkExistingMappings(Integer sourceDataSetId, List<Integer> selectedDataSetIds)
			throws DatabaseException
	{
		for (Integer dataSetId : selectedDataSetIds)
		{
			StringBuilder mappingIdentifer = new StringBuilder();
			mappingIdentifer.append(sourceDataSetId).append('-').append(dataSetId);
			if (ontologyMatcher.checkExistingMappings(mappingIdentifer.toString(), database)) return true;
		}
		return false;
	}

	class OntologyMatcherRequest
	{
		private final Integer sourceDataSetId;
		private final List<Integer> selectedDataSetIds;

		public OntologyMatcherRequest(Integer sourceDataSetId, List<Integer> selectedDataSetIds)
		{
			this.sourceDataSetId = sourceDataSetId;
			this.selectedDataSetIds = selectedDataSetIds;
		}

		public Integer getSourceDataSetId()
		{
			return sourceDataSetId;
		}

		public List<Integer> getSelectedDataSetIds()
		{
			return selectedDataSetIds;
		}
	}

	class OntologyMatcherResponse
	{
		private final Boolean isRunning;
		private final String message;

		public OntologyMatcherResponse(Boolean isRunning, String message)
		{
			this.isRunning = isRunning;
			this.message = message;
		}

		public Boolean getIsRunning()
		{
			return isRunning;
		}

		public String getMessage()
		{
			return message;
		}
	}
}