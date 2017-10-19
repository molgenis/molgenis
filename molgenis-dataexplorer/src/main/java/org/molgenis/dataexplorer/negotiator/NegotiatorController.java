package org.molgenis.dataexplorer.negotiator;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.convert.QueryRsqlConverter;
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
	private final QueryRsqlConverter rsqlQueryConverter;

	public NegotiatorController(RestTemplate restTemplate, PermissionService permissions, DataService dataService,
			QueryRsqlConverter rsqlQueryConverter)
	{
		super(URI);
		this.restTemplate = requireNonNull(restTemplate);
		this.permissions = requireNonNull(permissions);
		this.dataService = requireNonNull(dataService);
		this.rsqlQueryConverter = requireNonNull(rsqlQueryConverter);
	}

	@RunAsSystem
	public boolean showDirectoryButton(String selectedEntityName)
	{
		NegotiatorEntityConfig settings = getNegotiatorEntityConfig(selectedEntityName);
		return settings != null && permissions.hasPermissionOnPlugin(ID, Permission.READ) && settings.getBoolean(
				NegotiatorEntityConfigMeta.ENABLED);
	}

	@RunAsSystem
	public boolean isNegotiatorEnabled(String entityTypeId)
	{
		NegotiatorEntityConfig settings = getNegotiatorEntityConfig(entityTypeId);
		return settings == null || settings.getBoolean(NegotiatorEntityConfigMeta.ENABLED);

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

			List<Collection> collections = getCollections(request, entityConfig);

			HttpEntity<NegotiatorQuery> queryHttpEntity = getNegotiatorQueryHttpEntity(request, config, collections);

			return postQueryToNegotiator(config, queryHttpEntity);
		}
		else
		{
			throw new MolgenisDataException("No negotiator configuration found for the selected entity");
		}
	}

	private String postQueryToNegotiator(NegotiatorConfig config, HttpEntity<NegotiatorQuery> queryHttpEntity)
	{
		try
		{
			LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", config.getNegotiatorURL());
			String redirectURL = restTemplate.postForLocation(config.getNegotiatorURL(), queryHttpEntity)
											 .toASCIIString();
			LOG.trace("Redirecting to " + redirectURL);
			return redirectURL;
		}
		catch (Exception e)
		{
			LOG.error("Posting to the directory went wrong: ", e);
			throw e;
		}
	}

	private List<Collection> getCollections(@RequestBody NegotiatorRequest request, NegotiatorEntityConfig entityConfig)
	{
		EntityType selectedEntityType = dataService.getEntityType(request.getEntityId());
		Query<Entity> molgenisQuery = rsqlQueryConverter.convert(request.getRsql()).createQuery(selectedEntityType);
		Fetch fetch = new Fetch().field(
				entityConfig.getEntity(NegotiatorEntityConfigMeta.COLLECTION_ID, Attribute.class).getName())
								 .field(entityConfig.getEntity(NegotiatorEntityConfigMeta.BIOBANK_ID, Attribute.class)
													.getName());
		molgenisQuery.fetch(fetch);
		List<Collection> collections = dataService.findAll(selectedEntityType.getId(), molgenisQuery)
												  .map(entity -> Collection.create(entity.get(entityConfig.getEntity(
														  NegotiatorEntityConfigMeta.COLLECTION_ID, Attribute.class)
																										  .getName())
																						 .toString(), entity.get(
														  entityConfig.getEntity(NegotiatorEntityConfigMeta.BIOBANK_ID,
																  Attribute.class).getName()).toString()))
												  .collect(toList());
		if (collections.size() == 0)
		{
			throw new EmptyCollectionSelectionException("Please make sure your filters result in at least 1 row");
		}
		return collections;
	}

	private HttpEntity<NegotiatorQuery> getNegotiatorQueryHttpEntity(@RequestBody NegotiatorRequest request,
			NegotiatorConfig config, List<Collection> collections)
	{
		NegotiatorQuery query = NegotiatorQuery.create(request.getURL(), collections, request.getHumanReadable(),
				request.getnToken());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String username = config.getUsername();
		String password = config.getPassword();
		headers.set("Authorization", generateBase64Authentication(username, password));

		return new HttpEntity<>(query, headers);
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
