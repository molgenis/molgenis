package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.DataSetsIndexerController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.search.DataSetsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller class for the data explorer.
 * 
 * The implementation javascript file for the resultstable is defined in a MolgenisSettings property named
 * 'dataexplorer.resultstable.js' possible values are '/js/SingleObservationSetTable.js' or
 * '/js/MultiObservationSetTable.js' with '/js/MultiObservationSetTable.js' as the default
 * 
 * @author erwin
 * 
 */
@Controller
@RequestMapping(URI)
public class DataSetsIndexerController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "dataindexer";

	public DataSetsIndexerController()
	{
		super(URI);
	}

	@Autowired
	private Database database;

	@Autowired
	private DataSetsIndexer dataSetsIndexer;

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		// add data sets to model
		List<DataSet> dataSets = database.find(DataSet.class);
		model.addAttribute("dataSets", dataSets);
		return "view-datasetsindexer";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/index", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public DataSetIndexResponse index(@RequestBody DataSetIndexRequest request) throws UnsupportedEncodingException,
			TableException
	{
		List<String> dataSetIds = request.getSelectedDataSets();
		if ((dataSetIds == null) || dataSetIds.isEmpty())
		{
			return new DataSetIndexResponse(false, "Please select a dataset");
		}

		if (dataSetsIndexer.isIndexingRunning())
		{
			return new DataSetIndexResponse(false, "Indexer is already running. Please wait until finished.");
		}

		// Convert the strings to integer
		List<Integer> ids = new ArrayList<Integer>();
		for (String dataSetId : dataSetIds)
		{
			if (StringUtils.isNumeric(dataSetId))
			{
				ids.add(Integer.parseInt(dataSetId));
			}
		}
		dataSetsIndexer.index(ids);

		return new DataSetIndexResponse(true, "Indexing started");
	}

	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated()
	{
		return "redirect:/";
	}

	class DataSetIndexRequest
	{
		private List<String> selectedDataSets;

		public DataSetIndexRequest(List<String> selectedDataSets)
		{
			this.selectedDataSets = selectedDataSets;
		}

		public List<String> getSelectedDataSets()
		{
			return selectedDataSets;
		}

		public void setSelectedDataSets(List<String> selectedDataSets)
		{
			this.selectedDataSets = selectedDataSets;
		}
	}

	class DataSetIndexResponse
	{
		private final boolean isRunning;
		private final String message;

		public DataSetIndexResponse(boolean isRunning, String message)
		{
			this.isRunning = isRunning;
			this.message = message;
		}

		public boolean isRunning()
		{
			return isRunning;
		}

		public String getMessage()
		{
			return message;
		}
	}
}