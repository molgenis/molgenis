package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.entityexplorer.controller.EntityExplorerController;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

//import org.molgenis.data.csv

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

	// Including excluding the charts module
	public static final boolean INCLUDE_CHARTS_MODULE = true;
	public static final String KEY_APP_INCLUDE_CHARTS = "app.dataexplorer.include.charts";
	private static final String MODEL_APP_INCLUDE_CHARTS = "app_dataexplorer_include_charts";

	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
	public static final String GENOMEBROWSERTABLE = "genomeBrowserTable";

	public static final String MUTATION_START_POSITION = "start_nucleotide";
	public static final String MUTATION_ID = "mutation_id";
	public static final String MUTATION_CHROMOSOME = "chromosome";

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
	 * TODO JJ
	 * 
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "dataset", required = false)
		String selectedEntityName, Model model) throws Exception
	{
		// set entityExplorer URL for link to EntityExplorer for x/mrefs, but only if the user has permission to see the
		// plugin
		if (molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.READ)
				|| molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.WRITE))
		{
			model.addAttribute("entityExplorerUrl", EntityExplorerController.ID);
		}

		Iterable<String> names = dataService.getEntityNames();
		List<String> entitiesNames = Lists.newArrayList(names.iterator());
		if (entitiesNames.isEmpty()) throw new IllegalArgumentException("Entities names are not found");
		model.addAttribute("entitiesNames", entitiesNames);

		if (selectedEntityName == null)
		{
			selectedEntityName = entitiesNames.get(0);
		}
		model.addAttribute("selectedEntityName", selectedEntityName);

		// Init genome browser
		this.addGenomeBrowserSetsToModel(model);

		String resultsTableJavascriptFile = molgenisSettings.getProperty(KEY_TABLE_TYPE, DEFAULT_KEY_TABLE_TYPE);
		model.addAttribute("resultsTableJavascriptFile", resultsTableJavascriptFile);

		String appHrefCss = molgenisSettings.getProperty(KEY_APP_HREF_CSS);
		if (appHrefCss != null) model.addAttribute(KEY_APP_HREF_CSS.replaceAll("\\.", "_"), appHrefCss);

		// including/excluding charts
		Boolean appIncludeCharts = molgenisSettings.getBooleanProperty(KEY_APP_INCLUDE_CHARTS, INCLUDE_CHARTS_MODULE);
		model.addAttribute(MODEL_APP_INCLUDE_CHARTS, appIncludeCharts);

		model.addAttribute(INITLOCATION, molgenisSettings.getProperty(INITLOCATION));
		model.addAttribute(COORDSYSTEM, molgenisSettings.getProperty(COORDSYSTEM));
		model.addAttribute(CHAINS, molgenisSettings.getProperty(CHAINS));
		model.addAttribute(SOURCES, molgenisSettings.getProperty(SOURCES));
		model.addAttribute(BROWSERLINKS, molgenisSettings.getProperty(BROWSERLINKS));
		model.addAttribute(SEARCHENDPOINT, molgenisSettings.getProperty(SEARCHENDPOINT));
		model.addAttribute(KARYOTYPEENDPOINT, molgenisSettings.getProperty(KARYOTYPEENDPOINT));
		model.addAttribute(GENOMEBROWSERTABLE, molgenisSettings.getProperty(GENOMEBROWSERTABLE));

		return "view-dataexplorer";
	}

	private void addGenomeBrowserSetsToModel(Model model)
	{
		Map<String, String> genomeBrowserSets = new HashMap<String, String>();
		List<DataSet> dataSets = Lists.newArrayList(dataService.findAll(DataSet.ENTITY_NAME,
				new QueryImpl().sort(new Sort(Direction.DESC, DataSet.STARTTIME)), DataSet.class));
		for (DataSet dataSet : dataSets)
		{
			if (isGenomeBrowserDataSet(dataSet))
			{
				genomeBrowserSets.put(dataSet.getIdentifier(), dataSet.getName());
			}
		}
		model.addAttribute("genomeBrowserSets", genomeBrowserSets);
	}

	private boolean isGenomeBrowserDataSet(DataSet selectedDataSet)
	{
		Collection<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(selectedDataSet.getProtocolUsed());
		return containsGenomeBrowserProtocol(protocols);
	}

	private boolean containsGenomeBrowserProtocol(Collection<Protocol> protocols)
	{
		boolean hasStartPosition = false;
		boolean hasChromosome = false;
		boolean hasId = false;
		boolean hasGenomeBrowserprotocol = false;
		boolean hasGenomeBrowserSubprotocol = false;

		for (Protocol protocol : protocols)
		{
			List<ObservableFeature> features = protocol.getFeatures();
			for (ObservableFeature feature : features)
			{
				if (feature.getIdentifier().equals(MUTATION_START_POSITION)) hasStartPosition = true;
				else if (feature.getIdentifier().equals(MUTATION_ID)) hasId = true;
				else if (feature.getIdentifier().equals(MUTATION_CHROMOSOME)) hasChromosome = true;
			}
			if (hasStartPosition && hasChromosome && hasId)
			{
				hasGenomeBrowserprotocol = true;
			}
			else
			{
				hasGenomeBrowserSubprotocol = containsGenomeBrowserProtocol(protocol.getSubprotocols());
			}
		}
		return hasGenomeBrowserprotocol || hasGenomeBrowserSubprotocol;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("searchRequest")
	String searchRequest, HttpServletResponse response) throws IOException
	{
		searchRequest = URLDecoder.decode(searchRequest, "UTF-8");
		logger.info("Download request: [" + searchRequest + "]");

		SearchRequest request = new GsonHttpMessageConverter().getGson().fromJson(searchRequest, SearchRequest.class);
		request.getQuery().pageSize(DOWNLOAD_SEARCH_LIMIT).offset(0);

		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		CsvWriter writer = null;
		try
		{
			writer = new CsvWriter(response.getWriter());

			// The fieldsToReturn contain identifiers, we need the names
			Map<String, String> nameByIdentifier = getFeatureNames(request.getFieldsToReturn());

			// Keep order
			List<String> names = new ArrayList<String>();
			for (String identifier : request.getFieldsToReturn())
			{
				String name = nameByIdentifier.get(identifier);
				if (name != null)
				{
					names.add(name);
				}
			}

			writer.writeAttributeNames(names);
			int count = 0;
			SearchResult searchResult;

			do
			{
				request.getQuery().offset(count);
				searchResult = searchService.search(request);

				for (Hit hit : searchResult.getSearchHits())
				{
					Entity entity = new MapEntity();
					for (String field : request.getFieldsToReturn())
					{
						entity.set(nameByIdentifier.get(field), hit.getColumnValueMap().get(field));
					}
					writer.add(entity);
				}

				count += searchResult.getSearchHits().size();
			}
			while (count < searchResult.getTotalHitCount());
		}
		finally
		{
			IOUtils.closeQuietly(writer);
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
				ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, request.getFeatureId(),
						ObservableFeature.class);
				if (feature != null)
				{
					Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
							new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature), Category.class);

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

			ObservableFeature feature = dataService.findOne(ObservableFeature.ENTITY_NAME, request.getFeatureId(),
					ObservableFeature.class);
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
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);
		List<Protocol> listOfallProtocols = ProtocolUtils.getProtocolDescendants(dataSet.getProtocolUsed(), true);

		model.addAttribute("listOfallProtocols", listOfallProtocols);
		model.addAttribute("identifier", dataSetIdentifier);
		return "view-filter-dialog";
	}

	// Get feature names by feature identifiers
	private Map<String, String> getFeatureNames(List<String> identifiers)
	{
		Iterable<ObservableFeature> features = dataService.findAll(ObservableFeature.ENTITY_NAME,
				new QueryImpl().in(ObservableFeature.IDENTIFIER, identifiers), ObservableFeature.class);

		Map<String, String> nameByIdentifier = new LinkedHashMap<String, String>();
		for (ObservableFeature feature : features)
		{
			nameByIdentifier.put(feature.getIdentifier(), feature.getName());
		}

		return nameByIdentifier;
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}
}
