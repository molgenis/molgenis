package org.molgenis.dataexplorer.controller;

import com.google.gson.Gson;
import freemarker.core.ParseException;
import org.molgenis.data.*;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.GenomicDataSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.directory.DirectorySettings;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData.ANNOTATION_JOB_EXECUTION;
import static org.molgenis.dataexplorer.controller.DataExplorerController.*;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.Permission.WRITE;
import static org.molgenis.util.EntityUtils.getTypedValue;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller class for the data explorer.
 */
@Controller
@RequestMapping(URI)
@SessionAttributes({ ATTR_GALAXY_URL, ATTR_GALAXY_API_KEY })
public class DataExplorerController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DataExplorerController.class);

	public static final String ID = "dataexplorer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	static final String ATTR_GALAXY_URL = "galaxyUrl";
	static final String ATTR_GALAXY_API_KEY = "galaxyApiKey";
	public static final String MOD_ANNOTATORS = "annotators";
	public static final String MOD_ENTITIESREPORT = "entitiesreport";
	public static final String MOD_DATA = "data";

	@Autowired
	private DataExplorerSettings dataExplorerSettings;

	@Autowired
	private GenomicDataSettings genomicDataSettings;

	@Autowired
	private DirectorySettings directorySettings;

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

	@Autowired
	private LanguageService languageService;

	@Autowired
	private AttributeFactory attrMetaFactory;

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
		List<EntityType> entitiesMeta = dataService.getMeta().getEntityTypes()
				.filter(entityType -> !entityType.isAbstract()).collect(toList());
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

		// Directory specific check if the configured collection entity equals to the currently selected entity
		model.addAttribute("showDirectoryButton", showDirectoryButton(selectedEntityName));

		return "view-dataexplorer";
	}

	@RequestMapping(value = "/module/{moduleId}", method = GET)
	public String getModule(@PathVariable("moduleId") String moduleId, @RequestParam("entity") String entityName,
			Model model)
	{
		if (moduleId.equals(MOD_DATA))
		{
			model.addAttribute("genomicDataSettings", genomicDataSettings);
			model.addAttribute("genomeEntities", getGenomeBrowserEntities());
			model.addAttribute("showDirectoryButton", showDirectoryButton(entityName));
		}
		else if (moduleId.equals(MOD_ENTITIESREPORT))
		{
			model.addAttribute("datasetRepository", dataService.getRepository(entityName));
			model.addAttribute("viewName", dataExplorerSettings.getEntityReport(entityName));
		}
		else if (moduleId.equals(MOD_ANNOTATORS))
		{
			// throw exception rather than disable the tab, users can act on the message. Hiding the tab is less
			// self-explanatory
			if (!molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITEMETA))
			{
				throw new MolgenisDataAccessException(
						"No " + Permission.WRITEMETA + " permission on entity [" + entityName
								+ "], this permission is necessary run the annotators.");
			}
			Entity annotationRun = dataService.findOne(ANNOTATION_JOB_EXECUTION,
					new QueryImpl<Entity>().eq(AnnotationJobExecutionMetaData.TARGET_NAME, entityName)
							.sort(new Sort(JobExecutionMetaData.START_DATE, Sort.Direction.DESC)));
			model.addAttribute("annotationRun", annotationRun);
			model.addAttribute("entityName", entityName);
		}

		return "view-dataexplorer-mod-" + moduleId; // TODO bad request in case of invalid module id
	}

	@RequestMapping(value = "/copy", method = GET)
	@ResponseBody
	public boolean showCopy(@RequestParam("entity") String entityName)
	{
		boolean showCopy = molgenisPermissionService.hasPermissionOnEntity(entityName, READ) && dataService
				.getCapabilities(entityName).contains(RepositoryCapability.WRITABLE);
		return showCopy;
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
		boolean modReports = dataExplorerSettings.getModReports();

		if (modAggregates)
		{
			modAggregates = dataService.getCapabilities(entityName).contains(RepositoryCapability.AGGREGATEABLE);
		}

		// set data explorer permission
		Permission pluginPermission = null;
		if (molgenisPermissionService.hasPermissionOnEntity(entityName, WRITE)) pluginPermission = WRITE;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, READ)) pluginPermission = READ;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.COUNT))
			pluginPermission = Permission.COUNT;

		ModulesConfigResponse modulesConfig = new ModulesConfigResponse();
		ResourceBundle i18n = languageService.getBundle();
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
					if (modAnnotators && pluginPermission == WRITE)
					{
						modulesConfig.add(new ModuleConfig("annotators", "Annotators", "annotator-icon.png"));
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
	 * TODO Improve performance by rewriting to query that returns all genomic entities instead of retrieving all entities and determining which one is genomic
	 * Get readable genome entities
	 *
	 * @return
	 */
	private Map<String, String> getGenomeBrowserEntities()
	{
		Map<String, String> genomeEntities = new HashMap<>();
		dataService.getMeta().getEntityTypes().filter(this::isGenomeBrowserEntity).forEach(entityType ->
		{
			boolean canRead = molgenisPermissionService.hasPermissionOnEntity(entityType.getName(), READ);
			boolean canWrite = molgenisPermissionService.hasPermissionOnEntity(entityType.getName(), WRITE);
			if (canRead || canWrite)
			{
				genomeEntities.put(entityType.getName(), entityType.getLabel());
			}
		});
		return genomeEntities;
	}

	private boolean isGenomeBrowserEntity(EntityType entityType)
	{
		Attribute attributeStartPosition = genomicDataSettings
				.getAttributeMetadataForAttributeNameArray(GenomicDataSettings.Meta.ATTRS_POS, entityType);
		Attribute attributeChromosome = genomicDataSettings
				.getAttributeMetadataForAttributeNameArray(GenomicDataSettings.Meta.ATTRS_CHROM, entityType);
		return attributeStartPosition != null && attributeChromosome != null;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("dataRequest") String dataRequestStr, HttpServletResponse response)
			throws IOException
	{
		DataExplorerDownloadHandler download = new DataExplorerDownloadHandler(dataService, attrMetaFactory);

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
				fileName = dataRequest.getEntityName() + '_' + new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss")
						.format(new Date()) + ".csv";
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				outputStream = response.getOutputStream();
				download.writeToCsv(dataRequest, outputStream, ',');
				break;
			case DOWNLOAD_TYPE_XLSX:
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				fileName = dataRequest.getEntityName() + '_' + new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss")
						.format(new Date()) + ".xlsx";
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

		DataExplorerDownloadHandler download = new DataExplorerDownloadHandler(dataService, attrMetaFactory);

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

	/**
	 * Builds a model containing one entity and returns the entityReport ftl view
	 *
	 * @param entityName
	 * @param entityId
	 * @param model
	 * @return entity report view
	 * @throws Exception if an entity name or id is not found
	 * @author mdehaan, fkelpin
	 */
	@RequestMapping(value = "/details", method = RequestMethod.POST)
	public String viewEntityDetails(@RequestParam(value = "entityName") String entityName,
			@RequestParam(value = "entityId") String entityId, Model model) throws Exception
	{
		EntityType entityType = dataService.getEntityType(entityName);
		Object id = getTypedValue(entityId, entityType.getIdAttribute());

		model.addAttribute("entity", dataService.getRepository(entityName).findOneById(id));
		model.addAttribute("entityType", entityType);
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

	private boolean showDirectoryButton(String selectedEntityName)
	{
		final EntityType collectionEntityType = directorySettings.getCollectionEntityType();
		//TODO: change to getFullyQualifiedName once identifier PR is accepted
		return collectionEntityType != null && collectionEntityType.getName().equals(selectedEntityName);
	}
}
