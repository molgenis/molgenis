package org.molgenis.dataexplorer.negotiator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMeta;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.dataexplorer.negotiator.NegotiatorController.URI;

@Controller
@RequestMapping(URI)
public class NegotiatorController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(NegotiatorController.class);
	public static final String ID = "directory";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final RestTemplate restTemplate;
	private final PermissionService permissions;
	private final DataService dataService;
	private final MolgenisRSQL molgenisRSQL;

	public NegotiatorController(RestTemplate restTemplate, PermissionService permissions, DataService dataService,
			MolgenisRSQL molgenisRSQL)
	{
		super(URI);
		this.restTemplate = requireNonNull(restTemplate);
		this.permissions = requireNonNull(permissions);
		this.dataService = requireNonNull(dataService);
		this.molgenisRSQL = requireNonNull(molgenisRSQL);
	}

	@RunAsSystem
	public boolean showDirectoryButton(String selectedEntityName)
	{
		NegotiatorEntityConfig settings = getNegotiatorEntityConfig(selectedEntityName);
		return settings != null && permissions.hasPermissionOnPlugin(ID, Permission.READ) && settings.getBoolean(
				NegotiatorEntityConfigMeta.ENABLED);
	}

	@PostMapping("/export")
	@ResponseBody
	public String exportToNegotiator(@RequestBody NegotiatorRequest request) throws Exception
	{
		NegotiatorEntityConfig entityConfig = getNegotiatorEntityConfig(request.getEntityId());
		if (entityConfig != null)
		{
			NegotiatorConfig config = entityConfig.getNegotiatorConfig();
			LOG.info("NegotiatorRequest\n\n" + request + "\n\nreceived, sending request");

			String username = config.getUsername();
			String password = config.getPassword();

			EntityType selectedEntityType = dataService.getEntityType(request.getEntityId());
			Query<Entity> molgenisQuery = molgenisRSQL.createQuery(request.getRsql(), selectedEntityType);

			List<Collection> collections = dataService.findAll(selectedEntityType.getId(), molgenisQuery)
													  .map(entity -> Collection.create(
															  entity.get("collectionID").toString(),
															  entity.get("biobankID").toString()))
													  .collect(toList());
			if (collections.size() == 0)
			{
				throw new EmptyCollectionSelectionException("Please make sure your filters result in at least 1 row");
			}
			NegotiatorQuery query = NegotiatorQuery.create(request.getURL(), collections, request.getHumanReadable(),
					request.getnToken());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", generateBase64Authentication(username, password));

			HttpEntity<NegotiatorQuery> entity = new HttpEntity<>(query, headers);

			try
			{
				LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", config.getNegotiatorURL());
				String redirectURL = restTemplate.postForLocation(config.getNegotiatorURL(), entity).toASCIIString();
				LOG.trace("Redirecting to " + redirectURL);
				return redirectURL;
			}
			catch (Exception e)
			{
				LOG.error("Posting to the directory went wrong: ", e);
				throw e;
			}
		}
		else
		{
			throw new MolgenisDataException("No negotiator configuration found for the selected entity");
		}
	}

	private NegotiatorEntityConfig getNegotiatorEntityConfig(String entityId)
	{
		Query query = new QueryImpl<NegotiatorEntityConfig>().eq(NegotiatorEntityConfigMeta.ENTITY, entityId);
		NegotiatorEntityConfig config = dataService.findOne(NegotiatorEntityConfigMeta.NEGOTIATORENTITYCONFIG, query,
				NegotiatorEntityConfig.class);
		return config;
	}

	/**
	 * Generate base64 authentication based on username and password.
	 *
	 * @return Authentication header value.
	 */
	private static String generateBase64Authentication(String username, String password)
	{
		requireNonNull(username, password);
		String userPass = username + ":" + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(UTF_8));
		return "Basic " + userPassBase64;
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
