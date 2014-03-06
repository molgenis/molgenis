package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.entityexplorer.controller.EntityExplorerController;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.search.SearchService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "dataset", required = false)
	String selectedEntityName, @RequestParam(value = "wizard", required = false)
	Boolean wizard, Model model) throws Exception
	{
		// set entityExplorer URL for link to EntityExplorer for x/mrefs, but only if the user has permission to see the
		// plugin
		if (molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.READ)
				|| molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.WRITE))
		{
			model.addAttribute("entityExplorerUrl", EntityExplorerController.ID);
		}

		Iterable<EntityMetaData> entitiesMeta = Iterables.transform(dataService.getEntityNames(),
				new Function<String, EntityMetaData>()
				{
					@Override
					public EntityMetaData apply(String entityName)
					{
						return dataService.getEntityMetaData(entityName);
					}
				});
		model.addAttribute("entitiesMeta", entitiesMeta);

		if (selectedEntityName == null)
		{
			selectedEntityName = entitiesMeta.iterator().next().getName();
		}
		model.addAttribute("selectedEntityName", selectedEntityName);
		model.addAttribute("wizard", (wizard != null) && wizard.booleanValue());

		// Init genome browser
		model.addAttribute("genomeBrowserSets", getGenomeBrowserSetsToModel());

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

	private Map<String, String> getGenomeBrowserSetsToModel()
	{
		Map<String, String> genomeBrowserSets = new HashMap<String, String>();
		for (String entityName : dataService.getEntityNames())
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			AttributeMetaData attributeStartPosition = entityMetaData.getAttribute(MUTATION_START_POSITION);
			AttributeMetaData attributeId = entityMetaData.getAttribute(MUTATION_ID);
			AttributeMetaData attributeChromosome = entityMetaData.getAttribute(MUTATION_CHROMOSOME);
			if (attributeStartPosition != null && attributeId != null && attributeChromosome != null)
			{
				genomeBrowserSets.put(entityName, entityMetaData.getLabel());
			}
		}
		return genomeBrowserSets;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("dataRequest")
	String dataRequestStr, HttpServletResponse response) throws IOException
	{
		// Workaround because binding with @RequestBody is not possible:
		// http://stackoverflow.com/a/9970672
		dataRequestStr = URLDecoder.decode(dataRequestStr, "UTF-8");
		logger.info("Download request: [" + dataRequestStr + "]");
		DataRequest dataRequest = new GsonHttpMessageConverter().getGson().fromJson(dataRequestStr, DataRequest.class);

		String entityName = dataRequest.getEntityName();
		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		final Set<String> attributes = new HashSet<String>(dataRequest.getAttributeNames());
		String fileName = entityName + '_' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";

		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

		CsvWriter csvWriter = new CsvWriter(response.getOutputStream());
		try
		{
			csvWriter.writeAttributeNames(Iterables.transform(
					Iterables.filter(entityMetaData.getAttributes(), new Predicate<AttributeMetaData>()
					{
						@Override
						public boolean apply(AttributeMetaData attributeMetaData)
						{
							return attributes.contains(attributeMetaData.getName());
						}
					}), new Function<AttributeMetaData, String>()
					{
						@Override
						public String apply(AttributeMetaData attributeMetaData)
						{
							return attributeMetaData.getName();
						}
					}));
			csvWriter.add(dataService.findAll(entityName, dataRequest.getQuery()));
		}
		finally
		{
			csvWriter.close();
		}
	}

	@RequestMapping(value = "/aggregate", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public AggregateResponse aggregate(@Valid
	@RequestBody
	AggregateRequest request)
	{
		// TODO create utility class to extract info from entity/attribute uris
		String[] attributeUriTokens = request.getAttributeUri().split("/");
		String entityName = attributeUriTokens[3];
		String attributeName = attributeUriTokens[5];
		QueryImpl q = request.getQ() != null ? new QueryImpl(request.getQ()) : new QueryImpl();

		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);
		AttributeMetaData attributeMeta = entityMeta.getAttribute(attributeName);
		FieldTypeEnum dataType = attributeMeta.getDataType().getEnumType();
		if (dataType != FieldTypeEnum.BOOL && dataType != FieldTypeEnum.CATEGORICAL)
		{
			throw new RuntimeException("Unsupported data type " + dataType);
		}

		EntityMetaData refEntityMeta = attributeMeta.getRefEntity();
		String refAttributeName = refEntityMeta.getLabelAttribute().getName();
		Map<String, Integer> aggregateMap = new HashMap<String, Integer>();
		for (Entity entity : dataService.findAll(entityName, q))
		{
			String val;
			switch (dataType)
			{
				case BOOL:
					val = entity.getString(attributeName);
					break;
				case CATEGORICAL:
					Entity refEntity = (Entity) entity.get(attributeName);
					val = refEntity.getString(refAttributeName);
					break;
				default:
					throw new RuntimeException("Unsupported data type " + dataType);

			}

			Integer count = aggregateMap.get(val);
			if (count == null) aggregateMap.put(val, 1);
			else aggregateMap.put(val, count + 1);
		}
		return new AggregateResponse(aggregateMap);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
