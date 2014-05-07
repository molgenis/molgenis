package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.DataSetsIndexerController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

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
public class DataSetsIndexerController extends MolgenisPluginController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "dataindexer";

	public DataSetsIndexerController()
	{
		super(URI);
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private DataSetsIndexer dataSetsIndexer;

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		// add data sets to model
		Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		Iterable<Protocol> protocols = dataService.findAll(Protocol.ENTITY_NAME,
				new QueryImpl().eq(Protocol.ROOT, true), Protocol.class);

		model.addAttribute("dataSets", Lists.newArrayList(dataSets));
		model.addAttribute("protocols", Lists.newArrayList(protocols));
		return "view-datasetsindexer";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/index", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public DataSetIndexResponse index(@RequestBody
	DataSetIndexRequest request) throws UnsupportedEncodingException
	{

		if (dataSetsIndexer.isIndexingRunning())
		{
			return new DataSetIndexResponse(false, "Indexer is already running. Please wait until finished.");
		}

		List<String> dataSetIds = request.getSelectedDataSets();
		if ((dataSetIds == null) || dataSetIds.isEmpty())
		{
			return new DataSetIndexResponse(false, "Please select a dataset");
		}

		// Convert the strings to integer
		List<Object> ids = new ArrayList<Object>();
		for (String dataSetId : dataSetIds)
		{
			if (StringUtils.isNumeric(dataSetId))
			{
				ids.add(Integer.parseInt(dataSetId));
			}
		}
		if ("dataSet".equals(request.getEntity()))
		{
			dataSetsIndexer.indexDataSets(ids);
		}
		else if ("protocol".equals(request.getEntity()))
		{
			dataSetsIndexer.indexProtocols(ids);
		}
		return new DataSetIndexResponse(true, "");
	}

	static class DataSetIndexRequest
	{
		private List<String> selectedDataSets;
		private String entity;

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

		public String getEntity()
		{
			return entity;
		}

		public void setEntity(String entity)
		{
			this.entity = entity;
		}
	}

	static class DataSetIndexResponse
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