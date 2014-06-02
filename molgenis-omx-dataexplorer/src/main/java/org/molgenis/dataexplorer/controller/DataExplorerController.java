package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.GenomeConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
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

	public static final String KEY_MOD_AGGREGATES = "plugin.dataexplorer.mod.aggregates";
	public static final String KEY_MOD_ANNOTATORS = "plugin.dataexplorer.mod.annotators";
	public static final String KEY_MOD_CHARTS = "plugin.dataexplorer.mod.charts";
	public static final String KEY_MOD_DATA = "plugin.dataexplorer.mod.data";
	public static final String KEY_MOD_DISEASEMATCHER = "plugin.dataexplorer.mod.diseasematcher";
	private static final boolean DEFAULT_VAL_MOD_AGGREGATES = true;
	private static final boolean DEFAULT_VAL_MOD_ANNOTATORS = true;
	private static final boolean DEFAULT_VAL_MOD_CHARTS = true;
	private static final boolean DEFAULT_VAL_MOD_DATA = true;
	private static final boolean DEFAULT_VAL_MOD_DISEASEMATCHER = false;

	public static final String INITLOCATION = "genomebrowser.init.initLocation";
	public static final String COORDSYSTEM = "genomebrowser.init.coordSystem";
	public static final String CHAINS = "genomebrowser.init.chains";
	public static final String SOURCES = "genomebrowser.init.sources";
	public static final String BROWSERLINKS = "genomebrowser.init.browserLinks";
	public static final String WIZARD_TITLE = "plugin.dataexplorer.wizard.title";

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private MolgenisSettings molgenisSettings;

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
	public String init(@RequestParam(value = "dataset", required = false) String selectedEntityName,
			@RequestParam(value = "wizard", required = false) Boolean wizard, Model model) throws Exception
	{
		boolean entityExists = false;
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
		if (selectedEntityName != null)
		{
			entityExists = dataService.hasRepository(selectedEntityName);
		}

		if (entityExists)
		{
			model.addAttribute("hideDatasetSelect", true);
		}
		else
		{
			Iterator<EntityMetaData> entitiesIterator = entitiesMeta.iterator();
			if (entitiesIterator.hasNext())
			{
				selectedEntityName = entitiesIterator.next().getName();
			}
		}
		model.addAttribute("selectedEntityName", selectedEntityName);
		model.addAttribute(
				WIZARD_TITLE,
				molgenisSettings.getProperty(WIZARD_TITLE) == null ? "Filter Wizard" : molgenisSettings
						.getProperty(WIZARD_TITLE));
		model.addAttribute("wizard", (wizard != null) && wizard.booleanValue());

		boolean modDiseaseMatcher = molgenisSettings.getBooleanProperty(KEY_MOD_DISEASEMATCHER,
				DEFAULT_VAL_MOD_DISEASEMATCHER);
		model.addAttribute("modDiseaseMatcher", modDiseaseMatcher);

		return "view-dataexplorer";
	}

	@RequestMapping(value = "/module/{moduleId}", method = GET)
	public String getModule(@PathVariable("moduleId") String moduleId, Model model)
	{
		if (moduleId.equals("data"))
		{
			// Init genome browser
			model.addAttribute("genomeEntities", getGenomeBrowserEntities());

			model.addAttribute("initLocation", molgenisSettings.getProperty(INITLOCATION));
			model.addAttribute("coordSystem", molgenisSettings.getProperty(COORDSYSTEM));
			model.addAttribute("chains", molgenisSettings.getProperty(CHAINS));
			model.addAttribute("sources", molgenisSettings.getProperty(SOURCES));
			model.addAttribute("browserLinks", molgenisSettings.getProperty(BROWSERLINKS));
		}
		return "view-dataexplorer-mod-" + moduleId; // TODO bad request in case of invalid module id
	}

	/**
	 * Returns modules configuration for this entity based on current user permissions.
	 * 
	 * @param entityName
	 * @return
	 */
	@RequestMapping(value = "/modules", method = GET)
	@ResponseBody
	public ModulesConfigResponse getModules(@RequestParam("entity") String entityName)
	{
		// get data explorer settings
		boolean modCharts = molgenisSettings.getBooleanProperty(KEY_MOD_CHARTS, DEFAULT_VAL_MOD_CHARTS);
		boolean modData = molgenisSettings.getBooleanProperty(KEY_MOD_DATA, DEFAULT_VAL_MOD_DATA);
		boolean modAggregates = molgenisSettings.getBooleanProperty(KEY_MOD_AGGREGATES, DEFAULT_VAL_MOD_AGGREGATES);
		boolean modAnnotators = molgenisSettings.getBooleanProperty(KEY_MOD_ANNOTATORS, DEFAULT_VAL_MOD_ANNOTATORS);

		// set data explorer permission
		Permission pluginPermission = null;
		if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE)) pluginPermission = Permission.WRITE;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ)) pluginPermission = Permission.READ;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.COUNT)) pluginPermission = Permission.COUNT;

		ModulesConfigResponse modulesConfig = new ModulesConfigResponse();
		if (pluginPermission != null)
		{
			switch (pluginPermission)
			{
				case COUNT:
					if (modAggregates)
					{
						modulesConfig.add(new ModuleConfig("aggregates", "Aggregates", "grid-icon.png"));
					}
					break;
				case READ:
				case WRITE:
					if (modData)
					{
						modulesConfig.add(new ModuleConfig("data", "Data", "grid-icon.png"));
					}
					if (modAggregates)
					{
						modulesConfig.add(new ModuleConfig("aggregates", "Aggregates", "aggregate-icon.png"));
					}
					if (modCharts)
					{
						modulesConfig.add(new ModuleConfig("charts", "Charts", "chart-icon.png"));
					}
					if (modAnnotators)
					{
						modulesConfig.add(new ModuleConfig("annotators", "Annotators", "annotator-icon.png"));
					}

					break;
				default:
					throw new RuntimeException("unknown plugin permission: " + pluginPermission);
			}
		}
		return modulesConfig;
	}

	/**
	 * Get readable genome entities
	 * 
	 * @return
	 */
	private Map<String, String> getGenomeBrowserEntities()
	{
		Map<String, String> genomeEntities = new HashMap<String, String>();
		for (String entityName : dataService.getEntityNames())
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			if (isGenomeBrowserEntity(entityMetaData))
			{
				boolean canRead = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ);
				boolean canWrite = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
				if (canRead || canWrite)
				{
					genomeEntities.put(entityMetaData.getName(), entityMetaData.getLabel());
				}
			}
		}
		return genomeEntities;
	}

	private boolean isGenomeBrowserEntity(EntityMetaData entityMetaData)
	{
		AttributeMetaData attributeStartPosition = entityMetaData.getAttribute(GenomeConfig.GENOMEBROWSER_START_POSITION);
		AttributeMetaData attributeId = entityMetaData.getAttribute(GenomeConfig.GENOMEBROWSER_ID);
		AttributeMetaData attributeChromosome = entityMetaData.getAttribute(GenomeConfig.GENOMEBROWSER_CHROMOSOME);
		return attributeStartPosition != null && attributeId != null && attributeChromosome != null;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("dataRequest") String dataRequestStr, HttpServletResponse response)
			throws IOException
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

		QueryImpl query = dataRequest.getQuery();
		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

		CsvWriter csvWriter = new CsvWriter(response.getOutputStream());
		try
		{
			csvWriter.writeAttributeNames(Iterables.transform(
					Iterables.filter(entityMetaData.getAtomicAttributes(), new Predicate<AttributeMetaData>()
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
			csvWriter.add(dataService.findAll(entityName, query));
		}
		finally
		{
			csvWriter.close();
		}
	}

	@RequestMapping(value = "/aggregate", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public AggregateResult aggregate(@Valid @RequestBody AggregateRequest request)
	{
		String entityName = request.getEntityName();
		String xAttributeName = request.getXAxisAttributeName();
		String yAttributeName = request.getYAxisAttributeName();

		if (StringUtils.isBlank(xAttributeName) && StringUtils.isBlank(yAttributeName))
		{
			throw new InputValidationException("Missing aggregate attribute");
		}

		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);

		AttributeMetaData xAttributeMeta = null;
		if (StringUtils.isNotBlank(xAttributeName))
		{
			xAttributeMeta = entityMeta.getAttribute(xAttributeName);
			if (xAttributeMeta == null)
			{
				throw new InputValidationException("Unknown attribute '" + xAttributeName + "'");
			}

			if (!xAttributeMeta.isAggregateable())
			{
				throw new InputValidationException("Attribute '" + xAttributeName + "' is not aggregateable");
			}
		}

		AttributeMetaData yAttributeMeta = null;
		if (StringUtils.isNotBlank(yAttributeName))
		{

			yAttributeMeta = entityMeta.getAttribute(yAttributeName);
			if (yAttributeMeta == null)
			{
				throw new InputValidationException("Unknow attribute '" + yAttributeName + "'");
			}

			if (!yAttributeMeta.isAggregateable())
			{
				throw new InputValidationException("Attribute '" + yAttributeName + "' is not aggregateable");
			}
		}

		return dataService.aggregate(entityName, xAttributeMeta, yAttributeMeta, new QueryImpl(request.getQ()));
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
