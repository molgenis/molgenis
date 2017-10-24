package org.molgenis.dataexplorer.negotiator;

import org.molgenis.data.*;
import org.molgenis.data.i18n.LanguageService;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.dataexplorer.negotiator.NegotiatorController.URI;

@Controller
@RequestMapping(URI)
public class NegotiatorController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(NegotiatorController.class);
	private static final String ID = "directory";
	@SuppressWarnings("WeakerAccess")
	static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final RestTemplate restTemplate;
	private final PermissionService permissions;
	private final DataService dataService;
	private final QueryRsqlConverter rsqlQueryConverter;
	private final LanguageService languageService;

	public NegotiatorController(RestTemplate restTemplate, PermissionService permissions, DataService dataService,
			QueryRsqlConverter rsqlQueryConverter, LanguageService languageService)
	{
		super(URI);
		this.restTemplate = requireNonNull(restTemplate);
		this.permissions = requireNonNull(permissions);
		this.dataService = requireNonNull(dataService);
		this.rsqlQueryConverter = requireNonNull(rsqlQueryConverter);
		this.languageService = requireNonNull(languageService);
	}

	@RunAsSystem
	public boolean showDirectoryButton(String entityTypeId)
	{
		NegotiatorEntityConfig settings = getNegotiatorEntityConfig(entityTypeId);
		return settings != null && permissions.hasPermissionOnPlugin(ID, Permission.READ);
	}

	@PostMapping("/export")
	@ResponseBody
	public ExportResponse exportToNegotiator(@RequestBody NegotiatorRequest request)
	{
		ResourceBundle i18n = languageService.getBundle();

		String redirectUrl = "";
		String warning = "";
		boolean success = true;

		NegotiatorEntityConfig entityConfig = getNegotiatorEntityConfig(request.getEntityId());

		if (entityConfig != null)
		{
			NegotiatorConfig config = entityConfig.getNegotiatorConfig();
			LOG.info("NegotiatorRequest\n\n%s\n\nreceived, sending request", request);

			List<Entity> collectionEntities = getCollectionEntities(request, entityConfig);
			List<String> disabledCollections = getDisabledCollections(collectionEntities, entityConfig);

			if (disabledCollections.isEmpty())
			{
				List<Collection> collections = collectionEntities.stream()
																 .map(entity -> Collection.create(entity.get(
																		 entityConfig.getEntity(
																				 NegotiatorEntityConfigMeta.COLLECTION_ID,
																				 Attribute.class).getName()).toString(),
																		 entity.get(entityConfig.getEntity(
																				 NegotiatorEntityConfigMeta.BIOBANK_ID,
																				 Attribute.class).getName())
																			   .toString()))
																 .collect(toList());

				if (!collections.isEmpty())
				{
					HttpEntity<NegotiatorQuery> queryHttpEntity = getNegotiatorQueryHttpEntity(request, config,
							collections);
					postQueryToNegotiator(config, queryHttpEntity);
					redirectUrl = postQueryToNegotiator(config, queryHttpEntity);
				}
				else
				{
					success = false;
					warning = i18n.getString("dataexplorer_directory_no_rows");
				}
			}
			else
			{
				success = false;
				warning = String.format(i18n.getString("dataexplorer_directory_disabled"),
						String.join(",", disabledCollections));
			}
		}
		else
		{
			throw new MolgenisDataException(i18n.getString("dataexplorer_directory_no_config"));
		}
		return ExportResponse.create(success, warning, redirectUrl);
	}

	private List<String> getDisabledCollections(List<Entity> collectionEntities, NegotiatorEntityConfig config)
	{
		List<String> disabledLabels = new ArrayList<>();
		for (Entity collection : collectionEntities)
		{
			Attribute enabledAttribute = config.getEntity(NegotiatorEntityConfigMeta.ENABLED_ATTR, Attribute.class);
			if (enabledAttribute != null)
			{
				String enabledAttributeName = enabledAttribute.getName();
				Object value = collection.get(enabledAttributeName);
				Boolean enabled = value != null ? Boolean.valueOf(value.toString()) : false;
				if (!enabled) disabledLabels.add(collection.getLabelValue().toString());
			}
		}
		return disabledLabels;
	}

	private String postQueryToNegotiator(NegotiatorConfig config, HttpEntity<NegotiatorQuery> queryHttpEntity)
	{
		try
		{
			LOG.trace("NEGOTIATOR_URL: [{}]", config.getNegotiatorURL());
			String redirectURL = restTemplate.postForLocation(config.getNegotiatorURL(), queryHttpEntity)
											 .toASCIIString();
			LOG.trace("Redirecting to %s", redirectURL);
			return redirectURL;
		}
		catch (RestClientException e)
		{
			LOG.error("Posting to the negotiator went wrong: ", e);
			throw e;
		}
	}

	private List<Entity> getCollectionEntities(NegotiatorRequest request, NegotiatorEntityConfig entityConfig)
	{
		EntityType selectedEntityType = dataService.getEntityType(request.getEntityId());
		Query<Entity> molgenisQuery = rsqlQueryConverter.convert(request.getRsql()).createQuery(selectedEntityType);
		Fetch fetch = new Fetch().field(
				entityConfig.getEntity(NegotiatorEntityConfigMeta.COLLECTION_ID, Attribute.class).getName())
								 .field(entityConfig.getEntity(NegotiatorEntityConfigMeta.BIOBANK_ID, Attribute.class)
													.getName());
		molgenisQuery.fetch(fetch);
		return dataService.findAll(selectedEntityType.getId(), molgenisQuery).collect(toList());
	}

	private HttpEntity<NegotiatorQuery> getNegotiatorQueryHttpEntity(NegotiatorRequest request, NegotiatorConfig config,
			List<Collection> collections)
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
		Query<NegotiatorEntityConfig> query = new QueryImpl<NegotiatorEntityConfig>().eq(
				NegotiatorEntityConfigMeta.ENTITY, entityId);
		return dataService.findOne(NegotiatorEntityConfigMeta.NEGOTIATORENTITYCONFIG, query,
				NegotiatorEntityConfig.class);
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
