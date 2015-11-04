package org.molgenis.dataexplorer.controller;

import static org.molgenis.dataexplorer.controller.DataExplorerController.ATTR_GALAXY_API_KEY;
import static org.molgenis.dataexplorer.controller.DataExplorerController.ATTR_GALAXY_URL;
import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.GenomicDataSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.download.DataExplorerDownloadHandler;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExportException;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExportRequest;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExporter;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;

import freemarker.core.ParseException;

/**
 * Controller class for the data explorer.
 */
@Controller
@RequestMapping(URI)
@SessionAttributes(
{ ATTR_GALAXY_URL, ATTR_GALAXY_API_KEY })
public class DataExplorerController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DataExplorerController.class);

	public static final String ID = "dataexplorer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	static final String ATTR_GALAXY_URL = "galaxyUrl";
	static final String ATTR_GALAXY_API_KEY = "galaxyApiKey";

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private DataExplorerSettings dataExplorerSettings;

	@Autowired
	private GenomicDataSettings genomicDataSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private FreeMarkerConfigurer freemarkerConfigurer;

	@Autowired
	MenuManagerService menuManager;

	@Autowired
	private Gson gson;

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
	public String init(@RequestParam(value = "entity", required = false) String selectedEntityName, Model model)
			throws Exception
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

		if (!(entityExists && hasEntityPermission))
		{
			if (selectedEntityName != null)
			{
				StringBuilder message = new StringBuilder(
						"Entity does not exist or you do not have permission on this entity");
				if (!SecurityUtils.currentUserIsAuthenticated())
				{
					message.append(", log in to view more entities");
				}
				else
				{
					message.append(", please specify the fully qualified entity name");
				}
				model.addAttribute("warningMessage", message.toString());
			}
		}
		model.addAttribute("selectedEntityName", selectedEntityName);
		model.addAttribute("isAdmin", SecurityUtils.currentUserIsSu());

		return "view-dataexplorer";
	}

	@RequestMapping(value = "/module/{moduleId}", method = GET)
	public String getModule(@PathVariable("moduleId") String moduleId, @RequestParam("entity") String entityName,
			Model model)
	{
		if (moduleId.equals("data"))
		{
			model.addAttribute("genomicDataSettings", genomicDataSettings);
			model.addAttribute("genomeEntities", getGenomeBrowserEntities());
		}
		else if (moduleId.equals("diseasematcher"))
		{
			// TODO replace disease matcher table with react table and remove attrs from model
			model.addAttribute("tableEditable", false);
			model.addAttribute("rowClickable", false);
		}
		else if (moduleId.equals("entitiesreport"))
		{
			model.addAttribute("datasetRepository", dataService.getRepository(entityName));
			model.addAttribute("viewName", dataExplorerSettings.getEntityReport(entityName));
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
		boolean modAggregates = dataExplorerSettings.getModAggregates();
		boolean modAnnotators = dataExplorerSettings.getModAnnotators();
		boolean modCharts = dataExplorerSettings.getModCharts();
		boolean modData = dataExplorerSettings.getModData();
		boolean modDiseaseMatcher = dataExplorerSettings.getModDiseaseMatcher();
		boolean modReports = dataExplorerSettings.getModReports();

		if (modAggregates)
		{
			modAggregates = dataService.getCapabilities(entityName).contains(RepositoryCapability.AGGREGATEABLE);
		}

		// set data explorer permission
		Permission pluginPermission = null;
		if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE))
			pluginPermission = Permission.WRITE;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ))
			pluginPermission = Permission.READ;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.COUNT))
			pluginPermission = Permission.COUNT;

		ModulesConfigResponse modulesConfig = new ModulesConfigResponse();

		String i18nLocale = appSettings.getLanguageCode();
		Locale locale = new Locale(i18nLocale, i18nLocale);
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
					if (modAnnotators && pluginPermission == Permission.WRITE)
					{
						modulesConfig.add(new ModuleConfig("annotators", "Annotators", "annotator-icon.png"));
					}
					if (modDiseaseMatcher)
					{
						modulesConfig
								.add(new ModuleConfig("diseasematcher", "Disease Matcher", "diseasematcher-icon.png"));
					}
					if (modReports)
					{
						String modEntitiesReportName = dataExplorerSettings.getEntityReport(entityName);
						if (modEntitiesReportName != null)
						{
							modulesConfig
									.add(new ModuleConfig("entitiesreport", modEntitiesReportName, "report-icon.png"));
						}
					}
					break;
				case NONE:
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
		AttributeMetaData attributeStartPosition = genomicDataSettings
				.getAttributeMetadataForAttributeNameArray(GenomicDataSettings.Meta.ATTRS_POS, entityMetaData);
		AttributeMetaData attributeChromosome = genomicDataSettings
				.getAttributeMetadataForAttributeNameArray(GenomicDataSettings.Meta.ATTRS_CHROM, entityMetaData);
		return attributeStartPosition != null && attributeChromosome != null;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("dataRequest") String dataRequestStr, HttpServletResponse response)
			throws IOException
	{
		DataExplorerDownloadHandler download = new DataExplorerDownloadHandler(dataService);

		// Workaround because binding with @RequestBody is not possible:
		// http://stackoverflow.com/a/9970672
		dataRequestStr = URLDecoder.decode(dataRequestStr, "UTF-8");
		LOG.info("Download request: [" + dataRequestStr + "]");
		DataRequest dataRequest = gson.fromJson(dataRequestStr, DataRequest.class);

		String fileName = "";
		ServletOutputStream outputStream = null;

		switch (dataRequest.getDownloadType())
		{
			case DOWNLOAD_TYPE_CSV:
				response.setContentType("text/csv");
				fileName = dataRequest.getEntityName() + '_'
						+ new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss").format(new Date()) + ".csv";
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				outputStream = response.getOutputStream();
				download.writeToCsv(dataRequest, outputStream, ',');
				break;
			case DOWNLOAD_TYPE_XLSX:
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				fileName = dataRequest.getEntityName() + '_'
						+ new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss").format(new Date()) + ".xlsx";
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				outputStream = response.getOutputStream();
				download.writeToExcel(dataRequest, outputStream);
				break;
		}
	}

	@RequestMapping(value = "/galaxy/export", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void exportToGalaxy(@Valid @RequestBody GalaxyDataExportRequest galaxyDataExportRequest, Model model)
			throws IOException
	{
		boolean galaxyEnabled = dataExplorerSettings.getGalaxyExport();
		if (!galaxyEnabled) throw new MolgenisDataAccessException("Galaxy export disabled");

		DataExplorerDownloadHandler download = new DataExplorerDownloadHandler(dataService);

		String galaxyUrl = galaxyDataExportRequest.getGalaxyUrl();
		String galaxyApiKey = galaxyDataExportRequest.getGalaxyApiKey();
		GalaxyDataExporter galaxyDataSetExporter = new GalaxyDataExporter(galaxyUrl, galaxyApiKey);

		DataRequest dataRequest = galaxyDataExportRequest.getDataRequest();

		File csvFile = File.createTempFile("galaxydata_" + System.currentTimeMillis(), ".tsv");
		try
		{
			download.writeToCsv(dataRequest, new FileOutputStream(csvFile), '\t', true);
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

	@RequestMapping(value = "/aggregate", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public AggregateResult aggregate(@Valid @RequestBody AggregateRequest request)
	{
		String entityName = request.getEntityName();
		String xAttributeName = request.getXAxisAttributeName();
		String yAttributeName = request.getYAxisAttributeName();
		String distinctAttributeName = getDistinctAttributeName(request);

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
		AttributeMetaData distinctAttributeMeta = null;
		if (StringUtils.isNotBlank(distinctAttributeName))
		{
			distinctAttributeMeta = entityMeta.getAttribute(distinctAttributeName);
			if (distinctAttributeName == null)
			{
				throw new InputValidationException("Unknow attribute '" + distinctAttributeName + "'");
			}
		}

		AggregateQueryImpl aggregateQuery = new AggregateQueryImpl().attrX(xAttributeMeta).attrY(yAttributeMeta)
				.attrDistinct(distinctAttributeMeta).query(new QueryImpl(request.getQ()));
		return dataService.aggregate(entityName, aggregateQuery);
	}

	/**
	 * Retrieves the distinct attribute from the request, overriding it if the runtime property is set.
	 * 
	 * @param request
	 *            the {@link AggregateRequest}
	 * @return the name of the distinct attribute
	 */
	private String getDistinctAttributeName(AggregateRequest request)
	{
		String distinctAttributeName = request.getDistinctAttributeName();

		// check if an override for attr name exists
		String overrideDistinctAttributeName;

		Map<String, String> distinctAttrOverrides = dataExplorerSettings.getAggregatesDistinctOverrides();
		if (distinctAttrOverrides != null)
		{
			overrideDistinctAttributeName = distinctAttrOverrides.get(request.getEntityName());
		}
		else
		{
			overrideDistinctAttributeName = null;
		}

		if (overrideDistinctAttributeName != null)
		{
			if (distinctAttributeName != null)
			{
				LOG.info("[mod-aggregate] Overriding distinct attribute from request! Request specifies "
						+ distinctAttributeName + ", data explorer setting specifies " + overrideDistinctAttributeName);
			}
			else
			{
				LOG.debug("[mod-aggregate] Using distinct attribute " + overrideDistinctAttributeName
						+ " from data explorer setting");
			}
			return overrideDistinctAttributeName;
		}
		return distinctAttributeName;
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
		model.addAttribute("entity", dataService.getRepository(entityName).findOne(entityId));
		model.addAttribute("entityMetadata", dataService.getEntityMetaData(entityName));
		model.addAttribute("viewName", getViewName(entityName));
		return "view-entityreport";
	}

	private String getViewName(String entityName)
	{
		// check if entity report is set for this entity
		String reportTemplate = dataExplorerSettings.getEntityReport(entityName);
		if (reportTemplate != null)
		{
			String specificViewname = "view-entityreport-specific-" + reportTemplate;
			if (viewExists(specificViewname))
			{
				return specificViewname;
			}
		}

		// if there are no RuntimeProperty mappings, execute existing behaviour
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
		try
		{
			return freemarkerConfigurer.getConfiguration().getTemplate(viewName + ".ftl") != null;
		}
		catch (ParseException e)
		{
			LOG.info("error parsing template: ", e);
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	@ExceptionHandler(GalaxyDataExportException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleGalaxyDataExportException(GalaxyDataExportException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}
