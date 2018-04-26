package org.molgenis.dataexplorer.controller;

import com.google.gson.Gson;
import freemarker.core.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.*;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest.DownloadType;
import org.molgenis.dataexplorer.download.DataExplorerDownloadHandler;
import org.molgenis.dataexplorer.negotiator.NegotiatorController;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.genomebrowser.GenomeBrowserTrack;
import org.molgenis.genomebrowser.service.GenomeBrowserService;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.settings.AppSettings;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData.ANNOTATION_JOB_EXECUTION;
import static org.molgenis.data.util.EntityUtils.getTypedValue;
import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.molgenis.dataexplorer.controller.DataRequest.DownloadType.DOWNLOAD_TYPE_CSV;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.Permission.WRITE;

/**
 * Controller class for the data explorer.
 */
@Controller
@RequestMapping(URI)
public class DataExplorerController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DataExplorerController.class);

	public static final String ID = "dataexplorer";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public static final String MOD_ANNOTATORS = "annotators";
	public static final String MOD_ENTITIESREPORT = "entitiesreport";
	public static final String MOD_DATA = "data";
	public static final String NAVIGATOR = "navigator";

	@Autowired
	private DataExplorerSettings dataExplorerSettings;

	@Autowired
	private NegotiatorController directoryController;

	@Autowired
	private DataService dataService;

	@Autowired
	private UserPermissionEvaluator permissionService;

	@Autowired
	private FreeMarkerConfigurer freemarkerConfigurer;

	@Autowired
	private Gson gson;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private GenomeBrowserService genomeBrowserService;

	@Autowired
	private MenuReaderService menuReaderService;

	@Autowired
	private AppSettings appSettings;

	public DataExplorerController()
	{
		super(URI);
	}

	/**
	 * Show the explorer page
	 *
	 * @return the view name
	 */
	@GetMapping
	public String init(@RequestParam(value = "entity", required = false) String selectedEntityName,
			@RequestParam(value = "entityId", required = false) String selectedEntityId, Model model)
	{
		StringBuilder message = new StringBuilder("");

		final boolean currentUserIsSu = SecurityUtils.currentUserIsSu();

		Map<String, EntityType> entitiesMeta = dataService.getMeta()
														  .getEntityTypes()
														  .filter(entityType -> !entityType.isAbstract())
														  .filter(entityType -> currentUserIsSu
																  || !EntityTypeUtils.isSystemEntity(entityType))
														  .sorted(Comparator.comparing(EntityType::getLabel))
														  .collect(Collectors.toMap(EntityType::getId,
																  Function.identity(), (e1, e2) -> e2,
																  LinkedHashMap::new));

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
		model.addAttribute("isAdmin", currentUserIsSu);
		boolean navigatorAvailable = menuReaderService.getMenu().findMenuItemPath(NAVIGATOR) != null;
		model.addAttribute("showNavigatorLink", dataExplorerSettings.isShowNavigatorLink() && navigatorAvailable);

		model.addAttribute("hasTrackingId", null != appSettings.getGoogleAnalyticsTrackingId());
		model.addAttribute("hasMolgenisTrackingId", null != appSettings.getGoogleAnalyticsTrackingIdMolgenis());

		return "view-dataexplorer";
	}

	private void checkExistsAndPermission(@RequestParam(value = "entity", required = false) String selectedEntityName,
			StringBuilder message)
	{
		boolean entityExists = dataService.hasRepository(selectedEntityName);
		boolean hasEntityPermission = permissionService.hasPermission(new EntityTypeIdentity(selectedEntityName),
				EntityTypePermission.COUNT);

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

	@GetMapping("/module/{moduleId}")
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
				model.addAttribute("genomeTracks", genomeBrowserService.getTracksJson(entityTracks));
				//if multiple tracks are available we assume chrom and pos attribute are the same
				if (!entityTracks.isEmpty())
				{
					//FIXME: how to do this cleaner
					GenomeBrowserTrack track = entityTracks.entrySet().iterator().next().getValue();
					model.addAttribute("pos_attr", track.getGenomeBrowserAttrs().getPos());
					model.addAttribute("chrom_attr", track.getGenomeBrowserAttrs().getChrom());
				}
				model.addAttribute("showDirectoryButton", directoryController.showDirectoryButton(entityTypeId));
				model.addAttribute("NegotiatorEnabled", directoryController.showDirectoryButton(entityTypeId));
				break;
			case MOD_ENTITIESREPORT:
				//TODO: figure out if we need to know pos and chrom attrs here
				selectedEntityType = dataService.getMeta().getEntityTypeById(entityTypeId);
				entityTracks = genomeBrowserService.getGenomeBrowserTracks(selectedEntityType);
				model.addAttribute("genomeTracks", genomeBrowserService.getTracksJson(entityTracks));
				model.addAttribute("showDirectoryButton", directoryController.showDirectoryButton(entityTypeId));
				model.addAttribute("NegotiatorEnabled", directoryController.showDirectoryButton(entityTypeId));

				model.addAttribute("datasetRepository", dataService.getRepository(entityTypeId));
				model.addAttribute("viewName", dataExplorerSettings.getEntityReport(entityTypeId));
				break;
			case MOD_ANNOTATORS:
				// throw exception rather than disable the tab, users can act on the message. Hiding the tab is less
				// self-explanatory
				if (!permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
						EntityTypePermission.WRITEMETA))
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

	@GetMapping("/copy")
	@ResponseBody
	public boolean showCopy(@RequestParam("entity") String entityTypeId)
	{
		return permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ)
				&& dataService.getCapabilities(entityTypeId).contains(RepositoryCapability.WRITABLE);
	}

	/**
	 * Returns modules configuration for this entity based on current user permissions.
	 */
	@GetMapping("/modules")
	@ResponseBody
	public ModulesConfigResponse getModules(@RequestParam("entity") String entityTypeId)
	{
		boolean modAggregates = dataExplorerSettings.getModAggregates();
		boolean modAnnotators = dataExplorerSettings.getModAnnotators();
		boolean modData = dataExplorerSettings.getModData();
		boolean modReports = dataExplorerSettings.getModReports();

		if (modAggregates)
		{
			modAggregates = dataService.getCapabilities(entityTypeId).contains(RepositoryCapability.AGGREGATEABLE);
		}

		// set data explorer permission
		Permission pluginPermission = null;
		if (permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), EntityTypePermission.WRITE))
			pluginPermission = WRITE;
		else if (permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ))
			pluginPermission = READ;
		else if (permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), EntityTypePermission.COUNT))
			pluginPermission = Permission.COUNT;

		ModulesConfigResponse modulesConfig = new ModulesConfigResponse();
		String aggregatesTitle = messageSource.getMessage("dataexplorer_aggregates_title", new Object[] {},
				LocaleContextHolder.getLocale());

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
					throw new UnexpectedEnumException(pluginPermission);
			}
		}
		return modulesConfig;
	}

	@GetMapping("/navigatorLinks")
	@ResponseBody
	public List<NavigatorLink> getNavigatorLinks(@RequestParam("entity") String entityTypeId)
	{
		List<NavigatorLink> result = new LinkedList<>();
		EntityType entityType = dataService.getEntityType(entityTypeId);
		String navigatorPath = menuReaderService.getMenu().findMenuItemPath(NAVIGATOR);
		if (entityType != null)
		{
			Package pack = entityType.getPackage();
			getNavigatorLinks(result, pack, navigatorPath);

			//add root navigator link
			result.add(NavigatorLink.create(navigatorPath + "/", "glyphicon-home"));
			Collections.reverse(result);
		}
		return result;
	}

	private void getNavigatorLinks(List<NavigatorLink> result, Package pack, String navigatorPath)
	{
		if (pack != null)
		{
			String label = pack.getLabel();
			String href = navigatorPath + "/" + pack.getId();
			result.add(NavigatorLink.create(href, label));
			pack = pack.getParent();
			getNavigatorLinks(result, pack, navigatorPath);
		}
	}

	@PostMapping("/download")
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
			default:
				throw new UnexpectedEnumException(dataRequest.getDownloadType());
		}
	}

	public String getDownloadFilename(String entityTypeId, LocalDateTime localDateTime, DownloadType downloadType)
	{
		String timestamp = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss"));
		return String.format("%s_%s.%s", entityTypeId, timestamp, downloadType == DOWNLOAD_TYPE_CSV ? "csv" : "xlsx");
	}

	/**
	 * Builds a model containing one entity and returns the entityReport ftl view
	 *
	 * @return entity report view
	 * @throws Exception if an entity name or id is not found
	 */
	@PostMapping("/details")
	public String viewEntityDetails(@RequestParam(value = "entityTypeId") String entityTypeId,
			@RequestParam(value = "entityId") String entityId, Model model)
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
	 * @return standalone report view
	 * @throws Exception                   if an entity name or id is not found
	 * @throws MolgenisDataAccessException if an EntityType does not exist
	 */
	@GetMapping("/details/{entityTypeId}/{entityId}")
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
}
