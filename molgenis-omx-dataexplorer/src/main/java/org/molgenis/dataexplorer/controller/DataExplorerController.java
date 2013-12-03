package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.entityexplorer.controller.EntityExplorerController;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.ProtocolUtils;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.ValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
public class DataExplorerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(DataExplorerController.class);

	public static final String ID = "dataexplorer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final int DOWNLOAD_SEARCH_LIMIT = 1000;

	private static final String DEFAULT_KEY_TABLE_TYPE = "MultiObservationSetTable.js";
	private static final String KEY_TABLE_TYPE = "dataexplorer.resultstable.js";
	private static final String KEY_APP_HREF_CSS = "app.href.css";

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private SearchService searchService;

	public DataExplorerController()
	{
		super(URI);
	}

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "dataset", required = false)
	String selectedDataSetIdentifier, Model model) throws Exception
	{
		// set entityExplorer URL for link to EntityExplorer for x/mrefs, but only if the user has permission to see the
		// plugin
		if (molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.READ)
				|| molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.WRITE))
		{
			model.addAttribute("entityExplorerUrl", EntityExplorerController.ID);
		}

		List<DataSet> dataSets = Lists.<DataSet>newArrayList(dataService.<DataSet>findAll(DataSet.ENTITY_NAME));

		model.addAttribute("dataSets", dataSets);

		if (dataSets != null && !dataSets.isEmpty())
		{
			// determine selected data set and add to model
			DataSet selectedDataSet = null;
			if (selectedDataSetIdentifier != null)
			{
				for (DataSet dataSet : dataSets)
				{
					if (dataSet.getIdentifier().equals(selectedDataSetIdentifier))
					{
						selectedDataSet = dataSet;
						break;
					}
				}

				if (selectedDataSet == null) throw new IllegalArgumentException(selectedDataSetIdentifier
						+ " is not a valid data set identifier");
			}
			else
			{
				// select first data set by default
				selectedDataSet = dataSets.iterator().next();
			}
			model.addAttribute("selectedDataSet", selectedDataSet);
		}

		String resultsTableJavascriptFile = molgenisSettings.getProperty(KEY_TABLE_TYPE, DEFAULT_KEY_TABLE_TYPE);
		model.addAttribute("resultsTableJavascriptFile", resultsTableJavascriptFile);

		String appHrefCss = molgenisSettings.getProperty(KEY_APP_HREF_CSS);
		if (appHrefCss != null) model.addAttribute(KEY_APP_HREF_CSS.replaceAll("\\.", "_"), appHrefCss);

		return "view-dataexplorer";
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("searchRequest")
	String searchRequest, HttpServletResponse response) throws IOException, DatabaseException, TableException
	{
		searchRequest = URLDecoder.decode(searchRequest, "UTF-8");
		logger.info("Download request: [" + searchRequest + "]");

		SearchRequest request = new GsonHttpMessageConverter().getGson().fromJson(searchRequest, SearchRequest.class);

		request.getQueryRules().add(
				new org.molgenis.framework.db.QueryRule(org.molgenis.framework.db.QueryRule.Operator.LIMIT,
						DOWNLOAD_SEARCH_LIMIT));
		org.molgenis.framework.db.QueryRule offsetRule = new org.molgenis.framework.db.QueryRule(
				org.molgenis.framework.db.QueryRule.Operator.OFFSET, 0);
		request.getQueryRules().add(offsetRule);

		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		TupleWriter tupleWriter = null;
		try
		{
			tupleWriter = new CsvWriter(response.getWriter());

			// The fieldsToReturn contain identifiers, we need the names
			tupleWriter.write(getFeatureNames(request.getFieldsToReturn()));
			int count = 0;
			SearchResult searchResult;

			do
			{
				offsetRule.setValue(count);
				searchResult = searchService.search(request);

				for (Hit hit : searchResult.getSearchHits())
				{
					List<Object> values = new ArrayList<Object>();
					for (String field : request.getFieldsToReturn())
					{
						values.add(hit.getColumnValueMap().get(field));
					}

					tupleWriter.write(new ValueTuple(values));
				}

				count += searchResult.getSearchHits().size();
			}
			while (count < searchResult.getTotalHitCount());
		}
		finally
		{
			IOUtils.closeQuietly(tupleWriter);
		}
	}

	@RequestMapping(value = "/aggregate", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public AggregateResponse aggregate(@RequestBody
	AggregateRequest request)
	{

		Map<String, Integer> hashCounts = new HashMap<String, Integer>();

		try
		{
			if (request.getDataType().equals("categorical"))
			{
				ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, request.getFeatureId());
				if (feature != null)
				{
					Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
							new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature));

					for (Category category : categories)
					{
						hashCounts.put(category.getName(), 0);
					}
				}
			}
			else if (request.getDataType().equals("bool"))
			{
				hashCounts.put("true", 0);
				hashCounts.put("false", 0);
			}
			else
			{
				throw new RuntimeException("Illegal datatype");
			}

			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, request.getFeatureId());
			SearchResult searchResult = searchService.search(request.getSearchRequest());

			for (Hit hit : searchResult.getSearchHits())
			{
				Map<String, Object> columnValueMap = hit.getColumnValueMap();
				if (columnValueMap.containsKey(feature.getIdentifier()))
				{
					String categoryValue = columnValueMap.get(feature.getIdentifier()).toString();
					if (hashCounts.containsKey(categoryValue))
					{
						Integer countPerCategoricalValue = hashCounts.get(categoryValue);
						hashCounts.put(categoryValue, ++countPerCategoricalValue);
					}
				}
			}

		}
		catch (MolgenisDataException e)
		{
			logger.info(e);

		}
		return new AggregateResponse(hashCounts);

	}

	@RequestMapping(value = "/filterdialog", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public String filterwizard(@RequestBody
	@Valid
	@NotNull
	FilterWizardRequest request, Model model)
	{
		String dataSetIdentifier = request.getDataSetIdentifier();
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier));
		List<Protocol> listOfallProtocols = ProtocolUtils.getProtocolDescendants(dataSet.getProtocolUsed(), true);

		model.addAttribute("listOfallProtocols", listOfallProtocols);
		model.addAttribute("identifier", dataSetIdentifier);
		return "view-filter-dialog";
	}

	private Tuple getFeatureNames(List<String> identifiers) throws DatabaseException
	{

		Iterable<ObservableFeature> features = dataService.findAll(ObservableFeature.ENTITY_NAME,
				new QueryImpl().in(ObservableFeature.IDENTIFIER, identifiers));

		// Keep order the same
		Map<String, String> nameByIdentifier = new HashMap<String, String>();
		for (ObservableFeature feature : features)
		{
			nameByIdentifier.put(feature.getIdentifier(), feature.getName());
		}

		List<String> names = new ArrayList<String>();
		for (String identifier : identifiers)
		{
			names.add(nameByIdentifier.get(identifier));
		}

		return new ValueTuple(names);
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}
}
