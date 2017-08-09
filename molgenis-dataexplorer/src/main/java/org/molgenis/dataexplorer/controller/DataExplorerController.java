package org.molgenis.dataexplorer.controller;

import com.google.gson.Gson;
import freemarker.core.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.molgenis.data.*;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest.DownloadType;
import org.molgenis.dataexplorer.download.DataExplorerDownloadHandler;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExportException;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExportRequest;
import org.molgenis.dataexplorer.galaxy.GalaxyDataExporter;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.genomebrowser.GenomeBrowserTrack;
import org.molgenis.genomebrowser.service.GenomeBrowserService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.MolgenisPluginController;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData.ANNOTATION_JOB_EXECUTION;
import static org.molgenis.dataexplorer.controller.DataExplorerController.*;
import static org.molgenis.dataexplorer.controller.DataRequest.DownloadType.DOWNLOAD_TYPE_CSV;
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
	private DirectoryController directoryController;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private FreeMarkerConfigurer freemarkerConfigurer;

	@Autowired
	private Gson gson;

	@Autowired
	private LanguageService languageService;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private GenomeBrowserService genomeBrowserService;

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
	@RequestMapping(method = GET)
	public String init(@RequestParam(value = "entity", required = false) String selectedEntityName,
			@RequestParam(value = "entityId", required = false) String selectedEntityId, Model model) throws Exception
	{
		StringBuilder message = new StringBuilder("");

		Map<String, EntityType> entitiesMeta = dataService.getMeta()
														  .getEntityTypes()
														  .filter(entityType -> !entityType.isAbstract())
														  .collect(toMap(EntityType::getId, entityType -> entityType));

		model.addAttribute("entitiesMeta", entitiesMeta);
		if (selectedEntityId != null && selectedEntityName == null)
		{
			EntityType entityType = dataService.getMeta().getEntityType(selectedEntityId);
			if (entityType == null)
			{
				message.append("Entity does not exist or you do not have permission on this entity");
			}
			else
			{
				selectedEntityName = entityType.getId();
			}

			if (selectedEntityName != null)
			{
				checkExistsAndPermission(selectedEntityName, message);
			}
		}
		if (StringUtils.isNotEmpty(message.toString()))
		{
			model.addAttribute("warningMessage", message.toString());
		}
		model.addAttribute("selectedEntityName", selectedEntityName);
		model.addAttribute("isAdmin", SecurityUtils.currentUserIsSu());

		return "view-dataexplorer";
	}

	private void checkExistsAndPermission(@RequestParam(value = "entity", required = false) String selectedEntityName,
			StringBuilder message)
	{
		boolean entityExists = dataService.hasRepository(selectedEntityName);
		boolean hasEntityPermission = molgenisPermissionService.hasPermissionOnEntity(selectedEntityName,
				Permission.COUNT);

		if (!(entityExists && hasEntityPermission))
		{
			if (selectedEntityName != null)
			{
				message.append("Entity does not exist or you do not have permission on this entity");
				if (!SecurityUtils.currentUserIsAuthenticated())
				{
					message.append(", log in to view more entities");
				}
				else
				{
					message.append(", please specify the fully qualified entity name");
				}
			}
		}
	}

	@RequestMapping(value = "/module/{moduleId}", method = GET)
	public String getModule(@PathVariable("moduleId") String moduleId, @RequestParam("entity") String entityTypeId,
			Model model)
	{
		EntityType selectedEntityType;
		Map<String, GenomeBrowserTrack> entityTracks;
		switch (moduleId)
		{
			case MOD_DATA:
				selectedEntityType = dataService.getMeta().getEntityTypeById(entityTypeId);
				entityTracks = genomeBrowserService.getGenomeBrowserTracks(selectedEntityType);
				model.addAttribute("genomeTracks", getTracksJson(entityTracks));
				//if multiple tracks are available we assume chrom and pos attribute are the same
				if (!entityTracks.isEmpty())
				{
					//FIXME: how to do this cleaner
					GenomeBrowserTrack track = entityTracks.entrySet().iterator().next().getValue();
					model.addAttribute("pos_attr", track.getGenomeBrowserAttrs().getPos());
					model.addAttribute("chrom_attr", track.getGenomeBrowserAttrs().getChrom());
				}
				model.addAttribute("showDirectoryButton", directoryController.showDirectoryButton(entityTypeId));
				break;
			case MOD_ENTITIESREPORT:
				//TODO: figure out if we need to knwo pos and chrom attrs here
				selectedEntityType = dataService.getMeta().getEntityTypeById(entityTypeId);
				entityTracks = genomeBrowserService.getGenomeBrowserTracks(selectedEntityType);
				model.addAttribute("genomeTracks", getTracksJson(entityTracks));
				model.addAttribute("showDirectoryButton", directoryController.showDirectoryButton(entityTypeId));

				model.addAttribute("datasetRepository", dataService.getRepository(entityTypeId));
				model.addAttribute("viewName", dataExplorerSettings.getEntityReport(entityTypeId));
				break;
			case MOD_ANNOTATORS:
				// throw exception rather than disable the tab, users can act on the message. Hiding the tab is less
				// self-explanatory
				if (!molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.WRITEMETA))
				{
					throw new MolgenisDataAccessException(
							"No " + Permission.WRITEMETA + " permission on entity [" + entityTypeId
									+ "], this permission is necessary run the annotators.");
				}
				Entity annotationRun = dataService.findOne(ANNOTATION_JOB_EXECUTION,
						new QueryImpl<>().eq(AnnotationJobExecutionMetaData.TARGET_NAME, entityTypeId)
										 .sort(new Sort(JobExecutionMetaData.START_DATE, Sort.Direction.DESC)));
				model.addAttribute("annotationRun", annotationRun);
				model.addAttribute("entityTypeId", entityTypeId);
				break;
		}

		return "view-dataexplorer-mod-" + moduleId; // TODO bad request in case of invalid module id
	}

	@RequestMapping(value = "/copy", method = GET)
	@ResponseBody
	public boolean showCopy(@RequestParam("entity") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityTypeId, READ) && dataService.getCapabilities(
				entityTypeId).contains(RepositoryCapability.WRITABLE);
	}

	/**
	 * Returns modules configuration for this entity based on current user permissions.
	 *
	 * @param entityTypeId
	 * @return
	 */
	@RequestMapping(value = "/modules", method = GET)
	@ResponseBody
	public ModulesConfigResponse getModules(@RequestParam("entity") String entityTypeId)
	{
		boolean modAggregates = dataExplorerSettings.getModAggregates();
		boolean modAnnotators = dataExplorerSettings.getModAnnotators();
		boolean modCharts = dataExplorerSettings.getModCharts();
		boolean modData = dataExplorerSettings.getModData();
		boolean modReports = dataExplorerSettings.getModReports();

		if (modAggregates)
		{
			modAggregates = dataService.getCapabilities(entityTypeId).contains(RepositoryCapability.AGGREGATEABLE);
		}

		// set data explorer permission
		Permission pluginPermission = null;
		if (molgenisPermissionService.hasPermissionOnEntity(entityTypeId, WRITE)) pluginPermission = WRITE;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityTypeId, READ)) pluginPermission = READ;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.COUNT))
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
						String modEntitiesReportName = dataExplorerSettings.getEntityReport(entityTypeId);
						if (modEntitiesReportName != null)
						{
							modulesConfig.add(
									new ModuleConfig("entitiesreport", modEntitiesReportName, "report-icon.png"));
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
	private List<JSONObject> getTracksJson(Map<String, GenomeBrowserTrack> entityTracks)
	{
		Map<String, GenomeBrowserTrack> allTracks = new HashMap<>();
		allTracks.putAll(entityTracks);
		for (GenomeBrowserTrack track : entityTracks.values())
		{
			allTracks.putAll(genomeBrowserService.getReferenceTracks(track));
		}
		return allTracks.values()
						.stream().map(track -> track.toTrackJson())
						.collect(Collectors.toList());
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

		final String fileName = getDownloadFilename(dataRequest.getEntityName(), LocalDateTime.now(),
				dataRequest.getDownloadType());
		ServletOutputStream outputStream;

		switch (dataRequest.getDownloadType())
		{
			case DOWNLOAD_TYPE_CSV:
				response.setContentType("text/csv");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				outputStream = response.getOutputStream();
				download.writeToCsv(dataRequest, outputStream, ',');
				break;
			case DOWNLOAD_TYPE_XLSX:
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				outputStream = response.getOutputStream();
				download.writeToExcel(dataRequest, outputStream);
				break;
		}
	}

	public String getDownloadFilename(String entityTypeId, LocalDateTime localDateTime, DownloadType downloadType)
	{
		String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss"));
		return String.format("%s_%s.%s", entityTypeId, timestamp, downloadType == DOWNLOAD_TYPE_CSV ? "csv" : "xlsx");
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
	 * @param entityTypeId
	 * @param entityId
	 * @param model
	 * @return entity report view
	 * @throws Exception if an entity name or id is not found
	 */
	@RequestMapping(value = "/details", method = POST)
	public String viewEntityDetails(@RequestParam(value = "entityTypeId") String entityTypeId,
			@RequestParam(value = "entityId") String entityId, Model model) throws Exception
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		Object id = getTypedValue(entityId, entityType.getIdAttribute());

		model.addAttribute("entity", dataService.getRepository(entityTypeId).findOneById(id));
		model.addAttribute("entityType", entityType);
		model.addAttribute("viewName", getEntityReportViewName(entityTypeId));

		// Used to create a URL to a standalone report
		model.addAttribute("showStandaloneReportUrl", dataExplorerSettings.getModStandaloneReports());
		model.addAttribute("entityTypeId", entityTypeId);
		model.addAttribute("entityId", entityId);

		return "view-entityreport";
	}

	/**
	 * Builds a model containing one entity and returns standalone report ftl view
	 *
	 * @param entityTypeId
	 * @param entityId
	 * @param model
	 * @return standalone report view
	 * @throws Exception                   if an entity name or id is not found
	 * @throws MolgenisDataAccessException if an EntityType does not exist
	 */
	@RequestMapping(value = "/details/{entityTypeId}/{entityId}", method = GET)
	public String viewEntityDetailsById(@PathVariable(value = "entityTypeId") String entityTypeId,
			@PathVariable(value = "entityId") String entityId, Model model) throws Exception
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		if (entityType == null)
		{
			throw new MolgenisDataAccessException(
					"EntityType with id [" + entityTypeId + "] does not exist. Did you use the correct URL?");
		}
		Object id = getTypedValue(entityId, entityType.getIdAttribute());

		model.addAttribute("entity", dataService.getRepository(entityTypeId).findOneById(id));
		model.addAttribute("entityType", entityType);
		model.addAttribute("entityTypeId", entityTypeId);
		model.addAttribute("viewName", getStandaloneReportViewName(entityTypeId));

		return "view-standalone-report";
	}

	private String getEntityReportViewName(String entityTypeId)
	{
		// check if entity report is set for this entity
		String reportTemplate = dataExplorerSettings.getEntityReport(entityTypeId);
		if (reportTemplate != null)
		{
			String specificViewname = "view-entityreport-specific-" + reportTemplate;
			if (viewExists(specificViewname))
			{
				return specificViewname;
			}
		}

		// if there are no RuntimeProperty mappings, execute existing behaviour
		final String specificViewname = "view-entityreport-specific-" + entityTypeId;
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

	private String getStandaloneReportViewName(String entityTypeId)
	{
		final String specificStandaloneReportViewName = "view-standalone-report-specific-" + entityTypeId;
		if (viewExists(specificStandaloneReportViewName))
		{
			return specificStandaloneReportViewName;
		}
		return "view-standalone-report-default";
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
		return new ErrorMessageResponse(new ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}

}
