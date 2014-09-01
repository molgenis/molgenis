package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.ATTR_GALAXY_API_KEY;
import static org.molgenis.dataexplorer.controller.DataExplorerController.ATTR_GALAXY_URL;
import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.*;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.GenomeConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest.ColNames;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExportException;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExportRequest;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExporter;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.core.FreemarkerTemplate;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
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
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class for the data explorer.
 */
@Controller
@RequestMapping(URI)
@SessionAttributes(
{ ATTR_GALAXY_URL, ATTR_GALAXY_API_KEY })
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
	public static final String KEY_GALAXY_ENABLED = "plugin.dataexplorer.galaxy.enabled";
	public static final String KEY_GALAXY_URL = "plugin.dataexplorer.galaxy.url";
	public static final String KEY_SHOW_WIZARD_ONINIT = "plugin.dataexplorer.wizard.oninit";
	public static final String KEY_HIDE_SELECT = "plugin.dataexplorer.select.hide";
	public static final String KEY_HEADER_ABBREVIATE = "plugin.dataexplorer.header.abbreviate";
	public static final String KEY_HIDE_SEARCH_BOX = "plugin.dataexplorer.hide.searchbox";
	public static final String KEY_HIDE_ITEM_SELECTION = "plugin.dataexplorer.hide.itmeselection";

	private static final boolean DEFAULT_VAL_MOD_AGGREGATES = true;
	private static final boolean DEFAULT_VAL_MOD_ANNOTATORS = false;
	private static final boolean DEFAULT_VAL_MOD_CHARTS = true;
	private static final boolean DEFAULT_VAL_MOD_DATA = true;
	private static final boolean DEFAULT_VAL_MOD_DISEASEMATCHER = false;
	private static final boolean DEFAULT_VAL_GALAXY_ENABLED = false;
	public static final boolean DEFAULT_VAL_SHOW_WIZARD_ONINIT = false;
	public static final String DEFAULT_VAL_HEADER_ABBREVIATE = "180";
	public static final String DEFAULT_AGGREGATES_NORESULTS_MESSAGE = "No results found";

	static final String ATTR_GALAXY_URL = "galaxyUrl";
	static final String ATTR_GALAXY_API_KEY = "galaxyApiKey";

	public static final String INITLOCATION = "genomebrowser.init.initLocation";
	public static final String COORDSYSTEM = "genomebrowser.init.coordSystem";
	public static final String CHAINS = "genomebrowser.init.chains";
	public static final String SOURCES = "genomebrowser.init.sources";
	public static final String BROWSERLINKS = "genomebrowser.init.browserLinks";
	public static final String AGGREGATES_NORESULTS_MESSAGE = "plugin.dataexplorer.mod.aggregates.noresults";

	public static final String KEY_DATAEXPLORER_EDITABLE = "plugin.dataexplorer.editable";
	public static final String KEY_DATAEXPLORER_ROW_CLICKABLE = "plugin.dataexplorer.rowClickable";

	private static final boolean DEFAULT_VAL_DATAEXPLORER_EDITABLE = false;
	private static final boolean DEFAULT_VAL_DATAEXPLORER_ROW_CLICKABLE = false;
	private static final boolean DEFAULT_VAL_KEY_HIDE_SELECT = true;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private GenomeConfig genomeConfig;

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
			@RequestParam(value = "searchTerm", required = false) String searchTerm, Model model) throws Exception
	{
		boolean entityExists = false;
		boolean hasEntityPermission = false;
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
			hasEntityPermission = molgenisPermissionService.hasPermissionOnEntity(selectedEntityName, Permission.COUNT);

		}

		if (entityExists && hasEntityPermission)
		{
			if (molgenisSettings.getBooleanProperty(KEY_HIDE_SELECT, DEFAULT_VAL_KEY_HIDE_SELECT)) model.addAttribute(
					"hideDatasetSelect", true);
		}
		else
		{
			if (selectedEntityName != null)
			{
                StringBuilder message = new StringBuilder("Entity does not exist or you do not have permission on this entity");
                if(!SecurityUtils.currentUserIsAuthenticated()){
                      message.append(", log in to view more entities");
                }
                model.addAttribute("warningMessage",
						message.toString());
			}
			Iterator<EntityMetaData> entitiesIterator = entitiesMeta.iterator();
			if (entitiesIterator.hasNext())
			{
				selectedEntityName = entitiesIterator.next().getName();
			}
		}
		model.addAttribute("selectedEntityName", selectedEntityName);
		model.addAttribute("searchTerm", searchTerm);
		model.addAttribute("hideSearchBox", molgenisSettings.getBooleanProperty(KEY_HIDE_SEARCH_BOX, false));
		model.addAttribute("hideDataItemSelect", molgenisSettings.getBooleanProperty(KEY_HIDE_ITEM_SELECTION, false));

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

			model.addAttribute("genomebrowser_start_list",
					molgenisSettings.getProperty(GenomeConfig.GENOMEBROWSER_START, "POS"));
			model.addAttribute("genomebrowser_chrom_list",
					molgenisSettings.getProperty(GenomeConfig.GENOMEBROWSER_CHROM, "CHROM"));
			model.addAttribute("genomebrowser_id_list",
					molgenisSettings.getProperty(GenomeConfig.GENOMEBROWSER_ID, "ID"));
			model.addAttribute("genomebrowser_desc_list",
					molgenisSettings.getProperty(GenomeConfig.GENOMEBROWSER_DESCRIPTION, "INFO"));
			model.addAttribute("genomebrowser_patient_list",
					molgenisSettings.getProperty(GenomeConfig.GENOMEBROWSER_PATIENT_ID, "patient_id"));

			model.addAttribute("tableEditable", isTableEditable());
			model.addAttribute("galaxyEnabled",
					molgenisSettings.getBooleanProperty(KEY_GALAXY_ENABLED, DEFAULT_VAL_GALAXY_ENABLED));
			String galaxyUrl = molgenisSettings.getProperty(KEY_GALAXY_URL);
			model.addAttribute("rowClickable", isRowClickable());
			if (galaxyUrl != null) model.addAttribute(ATTR_GALAXY_URL, galaxyUrl);
		}
		else if (moduleId.equals("diseasematcher"))
		{
			model.addAttribute("tableEditable", isTableEditable());
			model.addAttribute("rowClickable", isRowClickable());
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
		boolean modDiseasematcher = molgenisSettings.getBooleanProperty(KEY_MOD_DISEASEMATCHER,
				DEFAULT_VAL_MOD_DISEASEMATCHER);

		if (modAggregates)
		{
			// Check if the repository is aggregateable
			modAggregates = dataService.getRepositoryByEntityName(entityName) instanceof Aggregateable;
		}

		// set data explorer permission
		Permission pluginPermission = null;
		if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE)) pluginPermission = Permission.WRITE;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ)) pluginPermission = Permission.READ;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.COUNT)) pluginPermission = Permission.COUNT;

		ModulesConfigResponse modulesConfig = new ModulesConfigResponse();

		Locale locale = new Locale("molgenis", "molgenis");
		ResourceBundle i18n = ResourceBundle.getBundle("i18n", locale);
		String aggregatesTitle = i18n.getString("dataexplorer_aggregates_title");

		if (pluginPermission != null)
		{
			switch (pluginPermission)
			{
				case COUNT:
					if (modAggregates)
					{
						modulesConfig.add(new ModuleConfig("aggregates", aggregatesTitle, "grid-icon.png"));
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
						modulesConfig.add(new ModuleConfig("aggregates", aggregatesTitle, "aggregate-icon.png"));
					}
					if (modCharts)
					{
						modulesConfig.add(new ModuleConfig("charts", "Charts", "chart-icon.png"));
					}
					if (modAnnotators)
					{
						modulesConfig.add(new ModuleConfig("annotators", "Annotators", "annotator-icon.png"));
					}
					if (modDiseasematcher)
					{
						modulesConfig.add(new ModuleConfig("diseasematcher", "Disease Matcher",
								"diseasematcher-icon.png"));
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
		AttributeMetaData attributeStartPosition = genomeConfig.getAttributeMetadataForAttributeNameArray(
				GenomeConfig.GENOMEBROWSER_START, entityMetaData);
		AttributeMetaData attributeChromosome = genomeConfig.getAttributeMetadataForAttributeNameArray(
				GenomeConfig.GENOMEBROWSER_CHROM, entityMetaData);
		return attributeStartPosition != null && attributeChromosome != null;
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
		String fileName = entityName + '_' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";

		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
		writeDataRequestCsv(dataRequest, response.getOutputStream(), ',');
	}

	@RequestMapping(value = "/galaxy/export", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void exportToGalaxy(@Valid @RequestBody GalaxyDataExportRequest galaxyDataExportRequest, Model model)
			throws IOException
	{
		boolean galaxyEnabled = molgenisSettings.getBooleanProperty(KEY_GALAXY_ENABLED, DEFAULT_VAL_GALAXY_ENABLED);
		if (!galaxyEnabled) throw new MolgenisDataAccessException("Galaxy export disabled");

		String galaxyUrl = galaxyDataExportRequest.getGalaxyUrl();
		String galaxyApiKey = galaxyDataExportRequest.getGalaxyApiKey();
		GalaxyDataExporter galaxyDataSetExporter = new GalaxyDataExporter(galaxyUrl, galaxyApiKey);

		DataRequest dataRequest = galaxyDataExportRequest.getDataRequest();

		File csvFile = File.createTempFile("galaxydata_" + System.currentTimeMillis(), ".tsv");
		try
		{
			writeDataRequestCsv(dataRequest, new FileOutputStream(csvFile), '\t');
			galaxyDataSetExporter.export(dataRequest.getEntityName(), csvFile);
		}
		finally
		{
			csvFile.delete();
		}

		// store url and api key in session for subsequent galaxy export requests
		model.addAttribute(ATTR_GALAXY_URL, galaxyUrl);
		model.addAttribute(ATTR_GALAXY_API_KEY, galaxyApiKey);
	}

	private void writeDataRequestCsv(DataRequest dataRequest, OutputStream outputStream, char separator)
			throws IOException
	{
		CsvWriter csvWriter = new CsvWriter(outputStream, separator);
		try
		{
			String entityName = dataRequest.getEntityName();
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			final Set<String> attributeNames = new HashSet<String>(dataRequest.getAttributeNames());
			Iterable<AttributeMetaData> attributes = Iterables.filter(entityMetaData.getAtomicAttributes(),
					new Predicate<AttributeMetaData>()
					{
						@Override
						public boolean apply(AttributeMetaData attributeMetaData)
						{
							return attributeNames.contains(attributeMetaData.getName());
						}
					});

			if (dataRequest.getColNames() == ColNames.ATTRIBUTE_LABELS)
			{
				csvWriter.writeAttributes(attributes);
			}
			else if (dataRequest.getColNames() == ColNames.ATTRIBUTE_NAMES)
			{
				csvWriter.writeAttributeNames(Iterables.transform(attributes, new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				}));
			}

			QueryImpl query = dataRequest.getQuery();
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

	/**
	 * Builds a model containing one entity and returns the entityReport ftl view
	 * 
	 * @author mdehaan, fkelpin
	 * @param entityName
	 * @param entityId
	 * @param model
	 * @return entity report view
	 * @throws Exception
	 *             if an entity name or id is not found
	 */
	@RequestMapping(value = "/details", method = RequestMethod.POST)
	public String viewEntityDetails(@RequestParam(value = "entityName") String entityName,
			@RequestParam(value = "entityId") String entityId, Model model) throws Exception
	{
		model.addAttribute("entity", dataService.getQueryableRepository(entityName).findOne(entityId));
		model.addAttribute("entityMetadata", dataService.getEntityMetaData(entityName));
		model.addAttribute("viewName", getViewName(entityName));
		return "view-entityreport";
	}

	private String getViewName(String entityName)
	{
		final String specificViewname = "view-entityreport-specific-" + entityName;
		if (viewExists(specificViewname))
		{
			return specificViewname;
		}
		if (viewExists("view-entityreport-generic"))
		{
			return "view-entityreport-generic";
		}
		return "view-entityreport-generic-default";
	}

	private boolean viewExists(String viewName)
	{
		Queryable templateRepository = dataService.getQueryableRepository(FreemarkerTemplate.ENTITY_NAME);
		return templateRepository.count(new QueryImpl().eq("Name", viewName + ".ftl")) > 0;
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, String> getSettings(@RequestParam(required = false) String keyStartsWith)
	{
		if (keyStartsWith == null)
		{
			keyStartsWith = "";
		}
		return molgenisSettings.getProperties("plugin.dataexplorer" + keyStartsWith);
	}

	@ExceptionHandler(GalaxyDataExportException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleGalaxyDataExportException(GalaxyDataExportException e)
	{
		logger.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
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

	private boolean isTableEditable()
	{
		return molgenisSettings.getBooleanProperty(KEY_DATAEXPLORER_EDITABLE, DEFAULT_VAL_DATAEXPLORER_EDITABLE)
				&& molgenisPermissionService.hasPermissionOnPlugin(ID, Permission.READ);
	}

	private boolean isRowClickable()
	{
		return molgenisSettings.getBooleanProperty(KEY_DATAEXPLORER_ROW_CLICKABLE,
				DEFAULT_VAL_DATAEXPLORER_ROW_CLICKABLE);
	}
}
