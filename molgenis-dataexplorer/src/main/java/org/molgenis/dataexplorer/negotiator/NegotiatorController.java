package org.molgenis.dataexplorer.negotiator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.convert.QueryRsqlConverter;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMeta;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.script.core.exception.ScriptExecutionException;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.dataexplorer.negotiator.NegotiatorController.URI;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMeta.ENABLED_EXPRESSION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Controller
@RequestMapping(URI)
public class NegotiatorController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(NegotiatorController.class);
	private static final String ID = "directory";
	static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final RestTemplate restTemplate;
	private final PermissionService permissions;
	private final DataService dataService;
	private final QueryRsqlConverter rsqlQueryConverter;
	private final LanguageService languageService;
	private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	public NegotiatorController(RestTemplate restTemplate, PermissionService permissions, DataService dataService,
			QueryRsqlConverter rsqlQueryConverter, LanguageService languageService,
			JsMagmaScriptEvaluator jsMagmaScriptEvaluator)
	{
		super(URI);
		this.restTemplate = requireNonNull(restTemplate);
		this.permissions = requireNonNull(permissions);
		this.dataService = requireNonNull(dataService);
		this.rsqlQueryConverter = requireNonNull(rsqlQueryConverter);
		this.languageService = requireNonNull(languageService);
		this.jsMagmaScriptEvaluator = requireNonNull(jsMagmaScriptEvaluator);
	}

	@RunAsSystem
	public boolean showDirectoryButton(String entityTypeId)
	{
		NegotiatorEntityConfig settings = getNegotiatorEntityConfig(entityTypeId);
		return settings != null && permissions.hasPermissionOnPlugin(ID, Permission.READ);
	}

	@PostMapping("/validate")
	@ResponseBody
	public ExportValidationResponse validateNegotiatorExport(@RequestBody NegotiatorRequest request)
	{
		ResourceBundle i18n = languageService.getBundle();

		boolean isValidRequest = true;
		String message = "";

		NegotiatorEntityConfig entityConfig = getNegotiatorEntityConfig(request.getEntityId());
		if (null != entityConfig)
		{
			LOG.info("Validating negotiator request\n\n{}", request);

			List<Entity> collectionEntities = getCollectionEntities(request);
			List<String> disabledCollections = getDisabledCollections(collectionEntities, entityConfig);

			if (!disabledCollections.isEmpty())
			{
				message = String.format(i18n.getString("dataexplorer_directory_disabled"), disabledCollections.size(),
						String.join(", ", disabledCollections));
			}

			if (collectionEntities.isEmpty() || (collectionEntities.size() == disabledCollections.size()))
			{
				isValidRequest = false;
				message = i18n.getString("dataexplorer_directory_no_rows");
			}
		}
		else
		{
			throw new MolgenisDataException(i18n.getString("dataexplorer_directory_no_config"));
		}
		return ExportValidationResponse.create(isValidRequest, message);
	}

	@PostMapping("/export")
	@ResponseBody
	public String exportToNegotiator(@RequestBody NegotiatorRequest request)
	{
		LOG.info("Sending Negotiator request");

		NegotiatorEntityConfig entityConfig = getNegotiatorEntityConfig(request.getEntityId());
		NegotiatorConfig config = entityConfig.getNegotiatorConfig();
		String expression = config.getString(ENABLED_EXPRESSION);

		List<Collection> nonDisabledCollectionEntities = getCollectionEntities(request).stream()
																					   .filter(entity ->
																							   expression == null
																									   || evaluateExpressionOnEntity(
																									   expression,
																									   entity))
																					   .map(entity -> getEntityCollection(
																							   entityConfig, entity))
																					   .collect(toList());

		HttpEntity<NegotiatorQuery> queryHttpEntity = getNegotiatorQueryHttpEntity(request, config,
				nonDisabledCollectionEntities);

		return postQueryToNegotiator(config, queryHttpEntity);
	}

	private Collection getEntityCollection(NegotiatorEntityConfig entityConfig, Entity entity)
	{
		Attribute collectionAttr = entityConfig.getEntity(NegotiatorEntityConfigMeta.COLLECTION_ID, Attribute.class);

		Attribute biobankAttr = entityConfig.getEntity(NegotiatorEntityConfigMeta.BIOBANK_ID, Attribute.class);

		String biobankString = getStringValue(biobankAttr, entity);
		String collectionString = getStringValue(collectionAttr, entity);

		return Collection.create(collectionString, biobankString);
	}

	private String getStringValue(Attribute attribute, Entity entity)
	{
		String stringValue;
		Object value = entity.get(attribute.getName());

		if (EntityTypeUtils.isMultipleReferenceType(attribute))
		{
			throw new MolgenisDataException(
					String.format("The %s cannot be a mref of categorical mref", attribute.getName()));
		}

		//If the configured attr is an xref or categorical we asume the id value should be used
		if (EntityTypeUtils.isReferenceType(attribute))
		{
			stringValue = value != null ? ((Entity) value).getIdValue().toString() : "";
		}
		else
		{
			stringValue = value != null ? value.toString() : "";
		}
		return stringValue;
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

	private List<Entity> getCollectionEntities(NegotiatorRequest request)
	{
		EntityType selectedEntityType = dataService.getEntityType(request.getEntityId());
		Query<Entity> molgenisQuery = rsqlQueryConverter.convert(request.getRsql()).createQuery(selectedEntityType);
		return dataService.findAll(selectedEntityType.getId(), molgenisQuery).collect(toList());
	}

	private List<String> getDisabledCollections(List<Entity> entities, NegotiatorEntityConfig config)
	{
		String expression = config.getString(ENABLED_EXPRESSION);
		return entities.stream()
					   .filter(entity -> !evaluateExpressionOnEntity(expression, entity))
					   .map(entity -> entity.getLabelValue().toString())
					   .collect(Collectors.toList());
	}

	private boolean evaluateExpressionOnEntity(String expression, Entity entity)
	{
		if (expression == null)
		{
			return true;
		}
		else
		{
			Object value;
			try
			{
				value = jsMagmaScriptEvaluator.eval(expression, entity);
			}
			catch (ScriptExecutionException see)
			{
				return false;
			}
			return value != null ? Boolean.valueOf(value.toString()) : false;
		}
	}

	private HttpEntity<NegotiatorQuery> getNegotiatorQueryHttpEntity(NegotiatorRequest request, NegotiatorConfig config,
			List<Collection> collections)
	{
		NegotiatorQuery query = NegotiatorQuery.create(request.getURL(), collections, request.getHumanReadable(),
				request.getnToken());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
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
}
