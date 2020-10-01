package org.molgenis.dataexplorer.negotiator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMetadata.ENABLED_EXPRESSION;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMetadata;
import org.molgenis.dataexplorer.negotiator.exception.MissingLocationException;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.web.PluginController;
import org.molgenis.web.rsql.QueryRsqlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping(NegotiatorController.URI)
public class NegotiatorController extends PluginController {
  private static final Logger LOG = LoggerFactory.getLogger(NegotiatorController.class);
  public static final String ID = "directory";
  static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  private final RestTemplate restTemplate;
  private final UserPermissionEvaluator permissions;
  private final DataService dataService;
  private final QueryRsqlConverter rsqlQueryConverter;
  private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
  private final MessageSource messageSource;

  public NegotiatorController(
      RestTemplate restTemplate,
      UserPermissionEvaluator permissions,
      DataService dataService,
      QueryRsqlConverter rsqlQueryConverter,
      JsMagmaScriptEvaluator jsMagmaScriptEvaluator,
      MessageSource messageSource) {
    super(URI);
    this.restTemplate = requireNonNull(restTemplate);
    this.permissions = requireNonNull(permissions);
    this.dataService = requireNonNull(dataService);
    this.rsqlQueryConverter = requireNonNull(rsqlQueryConverter);
    this.jsMagmaScriptEvaluator = requireNonNull(jsMagmaScriptEvaluator);
    this.messageSource = requireNonNull(messageSource);
  }

  @RunAsSystem
  public boolean showDirectoryButton(String entityTypeId) {
    Optional<NegotiatorEntityConfig> settings = getNegotiatorEntityConfig(entityTypeId);
    return settings.isPresent()
        && permissions.hasPermission(new PluginIdentity(ID), PluginPermission.VIEW_PLUGIN);
  }

  /**
   * Validates a {@link NegotiatorRequest}.
   *
   * <p>Used by the data explorer to verify the request. Since the data explorer does not allow
   * filtering on more than one repository, this endpoint does *NOT* support providing biobankRsql.
   *
   * <ul>
   *   <li>Checks if disabled collections are selected.
   *   <li>Checks if at least one collection is left after filtering out the disabled ones.
   * </ul>
   *
   * @param request the request to validate
   * @return ExportValidationResponse
   * @throws MolgenisDataException if the negotiator is not configured for this entity type
   */
  @PostMapping("/validate")
  @ResponseBody
  public ExportValidationResponse validateNegotiatorExport(@RequestBody NegotiatorRequest request) {
    if (request.getBiobankRsql() != null || request.getBiobankId() != null) {
      throw new IllegalArgumentException("Cannot verify requests with biobank filters.");
    }
    boolean isValidRequest = true;
    String message = "";
    List<String> enabledCollectionsLabels;
    List<String> disabledCollectionLabels;

    Optional<NegotiatorEntityConfig> entityConfigOptional =
        getNegotiatorEntityConfig(request.getEntityId());
    if (entityConfigOptional.isPresent()) {
      NegotiatorEntityConfig entityConfig = entityConfigOptional.get();

      LOG.info("Validating negotiator request\n\n{}", request);

      List<Entity> collectionEntities = getCollectionEntities(request);
      List<Entity> disabledCollections = getDisabledCollections(collectionEntities, entityConfig);

      Function<Entity, String> getLabel = entity -> entity.getLabelValue().toString();
      disabledCollectionLabels = disabledCollections.stream().map(getLabel).collect(toList());
      enabledCollectionsLabels =
          collectionEntities.stream()
              .filter(e -> !disabledCollections.contains(e))
              .map(getLabel)
              .collect(toList());

      if (!disabledCollections.isEmpty()) {
        message =
            messageSource.getMessage(
                "dataexplorer_directory_disabled",
                new Object[] {disabledCollections.size(), collectionEntities.size()},
                getLocale());
      }

      if (collectionEntities.isEmpty()
          || (collectionEntities.size() == disabledCollections.size())) {
        isValidRequest = false;
        message =
            messageSource.getMessage(
                "dataexplorer_directory_no_rows", new Object[] {}, getLocale());
      }
    } else {
      throw new MolgenisDataException(
          messageSource.getMessage(
              "dataexplorer_directory_no_config", new Object[] {}, getLocale()));
    }
    return ExportValidationResponse.create(
        isValidRequest, message, enabledCollectionsLabels, disabledCollectionLabels);
  }

  /**
   * Sends a {@link NegotiatorQuery} to the negotiator.
   *
   * <p>Filters the repositories using the rsql in the request and sends the matching collections
   * with their biobanks to the negotiator.
   *
   * <p>Allows an extra biobankRsql expression to filter the biobanks as well. This allows you to
   * filter on xref/mref values in the biobank, which would otherwise be impossible unless you
   * increase the indexing depth on the collections to 2.
   *
   * <p>Determines negotiator information based on {@link NegotiatorEntityConfig} and {@link
   * NegotiatorConfig} stored in the database.
   *
   * @param request {@link NegotiatorRequest}
   * @return Location on the Negotiator where the negotiation can be started.
   */
  @PostMapping("/export")
  @ResponseBody
  public String exportToNegotiator(@RequestBody NegotiatorRequest request) {
    LOG.info("Sending Negotiator request");

    NegotiatorEntityConfig entityConfig =
        getNegotiatorEntityConfig(request.getEntityId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Negotiator entity config entity id is illegal"));

    NegotiatorConfig config = entityConfig.getNegotiatorConfig();
    String expression = config.getString(ENABLED_EXPRESSION);

    List<Collection> collectionEntities =
        getCollectionEntities(request).stream()
            .filter(entity -> expression == null || evaluateExpressionOnEntity(expression, entity))
            .map(entity -> getEntityCollection(entityConfig, entity))
            .collect(toList());

    // Filter biobanks if needed
    if (request.getBiobankRsql() != null) {
      requireNonNull(request.getBiobankId(), "Must provide biobankId if biobankRsql is provided");
      List<Object> biobankIds =
          getBiobankEntities(request).stream()
              .map(Entity::getIdValue)
              .map(Object::toString)
              .collect(toList());
      collectionEntities =
          collectionEntities.stream()
              .filter(it -> biobankIds.contains(it.getBiobankId()))
              .collect(toList());
    } else {
      requireNonNull(
          request.getRsql(), "Must provide at least one filter, either rsql or biobankRsql");
    }

    HttpEntity<NegotiatorQuery> queryHttpEntity =
        getNegotiatorQueryHttpEntity(request, config, collectionEntities);

    return postQueryToNegotiator(config, queryHttpEntity);
  }

  private Collection getEntityCollection(NegotiatorEntityConfig entityConfig, Entity entity) {
    Attribute collectionAttr =
        entityConfig.getEntity(NegotiatorEntityConfigMetadata.COLLECTION_ID, Attribute.class);

    Attribute biobankAttr =
        entityConfig.getEntity(NegotiatorEntityConfigMetadata.BIOBANK_ID, Attribute.class);

    String biobankString = getStringValue(biobankAttr, entity);
    String collectionString = getStringValue(collectionAttr, entity);

    return Collection.create(collectionString, biobankString);
  }

  private String getStringValue(Attribute attribute, Entity entity) {
    String stringValue;
    Object value = entity.get(attribute.getName());

    if (EntityTypeUtils.isMultipleReferenceType(attribute)) {
      throw new MolgenisDataException(
          String.format("The %s cannot be an mref or categorical mref", attribute.getName()));
    }

    // If the configured attr is an xref or categorical we assume the id value should be used
    if (EntityTypeUtils.isReferenceType(attribute)) {
      stringValue = value != null ? ((Entity) value).getIdValue().toString() : "";
    } else {
      stringValue = value != null ? value.toString() : "";
    }
    return stringValue;
  }

  private String postQueryToNegotiator(
      NegotiatorConfig config, HttpEntity<NegotiatorQuery> queryHttpEntity) {
    String negotiatorURL = config.getNegotiatorURL();
    try {
      LOG.trace("NEGOTIATOR_URL: [{}]", negotiatorURL);
      String redirectURL =
          Optional.ofNullable(restTemplate.postForLocation(negotiatorURL, queryHttpEntity))
              .map(java.net.URI::toString)
              .orElseThrow(() -> new MissingLocationException(negotiatorURL));
      LOG.trace("Redirecting to {}", redirectURL);
      return redirectURL;
    } catch (RestClientException e) {
      LOG.error("Posting to the negotiator went wrong: ", e);
      throw e;
    }
  }

  private List<Entity> getBiobankEntities(NegotiatorRequest request) {
    Repository<Entity> repository = dataService.getRepository(request.getBiobankId());
    Query<Entity> molgenisQuery =
        rsqlQueryConverter.convert(request.getBiobankRsql()).createQuery(repository);
    return molgenisQuery.findAll().collect(toList());
  }

  private List<Entity> getCollectionEntities(NegotiatorRequest request) {
    Repository<Entity> repository = dataService.getRepository(request.getEntityId());
    Query<Entity> molgenisQuery =
        Optional.ofNullable(request.getRsql())
            .map(rsqlQueryConverter::convert)
            .map(it -> it.createQuery(repository))
            .orElse(repository.query());
    return molgenisQuery.findAll().collect(toList());
  }

  private List<Entity> getDisabledCollections(
      List<Entity> entities, NegotiatorEntityConfig config) {
    String expression = config.getString(ENABLED_EXPRESSION);
    return entities.stream()
        .filter(entity -> !evaluateExpressionOnEntity(expression, entity))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("java:S1125") // boolean literal isn't redundant here
  private boolean evaluateExpressionOnEntity(String expression, Entity entity) {
    return expression == null
        ? true
        : Boolean.valueOf(jsMagmaScriptEvaluator.eval(expression, entity).toString());
  }

  private HttpEntity<NegotiatorQuery> getNegotiatorQueryHttpEntity(
      NegotiatorRequest request, NegotiatorConfig config, List<Collection> collections) {
    NegotiatorQuery query =
        NegotiatorQuery.create(
            request.getURL(), collections, request.getHumanReadable(), request.getnToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    String username = config.getUsername();
    String password = config.getPassword();
    headers.set("Authorization", generateBase64Authentication(username, password));

    return new HttpEntity<>(query, headers);
  }

  private Optional<NegotiatorEntityConfig> getNegotiatorEntityConfig(String entityId) {
    Query<NegotiatorEntityConfig> query =
        new QueryImpl<NegotiatorEntityConfig>().eq(NegotiatorEntityConfigMetadata.ENTITY, entityId);
    NegotiatorEntityConfig negotiatorEntityConfig =
        dataService.findOne(
            NegotiatorEntityConfigMetadata.NEGOTIATORENTITYCONFIG,
            query,
            NegotiatorEntityConfig.class);
    return Optional.ofNullable(negotiatorEntityConfig);
  }

  /**
   * Generate base64 authentication based on username and password.
   *
   * @return Authentication header value.
   */
  private static String generateBase64Authentication(String username, String password) {
    requireNonNull(username, password);
    String userPass = username + ":" + password;
    String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(UTF_8));
    return "Basic " + userPassBase64;
  }
}
