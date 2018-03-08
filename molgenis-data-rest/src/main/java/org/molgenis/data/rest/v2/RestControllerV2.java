package org.molgenis.data.rest.v2;

import org.molgenis.core.ui.data.support.Href;
import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.i18n.LocalizationService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.RepositoryCopier;
import org.molgenis.data.validation.meta.NameValidator;
import org.molgenis.i18n.LanguageService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.rest.v2.AttributeFilterToFetchConverter.createDefaultAttributeFetch;
import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;
import static org.molgenis.data.util.EntityUtils.getTypedValue;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping(BASE_URI)
public class RestControllerV2
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerV2.class);

	static final int MAX_ENTITIES = 1000;

	public static final String BASE_URI = "/api/v2";
	public static final String TIME_PARAM_NAME = "_t";

	private final DataService dataService;
	private final RestService restService;
	private final UserPermissionEvaluator permissionService;
	private final PermissionSystemService permissionSystemService;
	private final RepositoryCopier repoCopier;
	private final LocalizationService localizationService;

	static UnknownEntityException createUnknownEntityException(String entityTypeId)
	{
		return new UnknownEntityException("Operation failed. Unknown entity: '" + entityTypeId + "'");
	}

	static MolgenisDataAccessException createNoReadPermissionOnEntityException(String entityTypeId)
	{
		return new MolgenisDataAccessException("No read permission on entity " + entityTypeId);
	}

	static MolgenisDataException createNoWriteCapabilitiesOnEntityException(String entityTypeId)
	{
		return new MolgenisRepositoryCapabilitiesException("No write capabilities for entity " + entityTypeId);
	}

	static DuplicateEntityException createDuplicateEntityException(String entityTypeId)
	{
		return new DuplicateEntityException("Operation failed. Duplicate entity: '" + entityTypeId + "'");
	}

	static MolgenisDataAccessException createMolgenisDataAccessExceptionReadOnlyAttribute(String entityTypeId,
			String attributeName)
	{
		return new MolgenisDataAccessException(
				"Operation failed. Attribute '" + attributeName + "' of entity '" + entityTypeId + "' is readonly");
	}

	static MolgenisDataException createMolgenisDataExceptionUnknownIdentifier(int count)
	{
		return new MolgenisDataException("Operation failed. Unknown identifier on index " + count);
	}

	static MolgenisDataException createMolgenisDataExceptionIdentifierAndValue()
	{
		return new MolgenisDataException("Operation failed. Entities must provide only an identifier and a value");
	}

	static UnknownEntityException createUnknownEntityExceptionNotValidId(Object id)
	{
		return new UnknownEntityException(
				"The entity you are trying to update [" + id.toString() + "] does not exist.");
	}

	public RestControllerV2(DataService dataService, UserPermissionEvaluator permissionService, RestService restService,
			LocalizationService localizationService, PermissionSystemService permissionSystemService,
			RepositoryCopier repoCopier)
	{
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
		this.restService = requireNonNull(restService);
		this.localizationService = requireNonNull(localizationService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.repoCopier = requireNonNull(repoCopier);
	}

	@Autowired
	@GetMapping("/version")
	public Map<String, String> getVersion(@Value("${molgenis.version:@null}") String molgenisVersion,
			@Value("${molgenis.build.date:@null}") String molgenisBuildDate)
	{
		if (molgenisVersion == null) throw new IllegalArgumentException("molgenisVersion is null");
		if (molgenisBuildDate == null) throw new IllegalArgumentException("molgenisBuildDate is null");
		if (molgenisBuildDate.equals("${maven.build.timestamp}"))
		{
			molgenisBuildDate = DateTimeFormatter.ofLocalizedDateTime(MEDIUM).format(now()) + " by IntelliJ";
		}
		Map<String, String> result = new HashMap<>();
		result.put("molgenisVersion", molgenisVersion);
		result.put("buildDate", molgenisBuildDate);

		return result;
	}

	/**
	 * Retrieve an entity instance by id, optionally specify which attributes to include in the response.
	 */
	@GetMapping("/{entityTypeId}/{id:.+}")
	public Map<String, Object> retrieveEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId,
			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter,
			@RequestParam(value = "includeCategories", defaultValue = "false") boolean includeCategories)
	{
		return getEntityResponse(entityTypeId, untypedId, attributeFilter, includeCategories);
	}

	/**
	 * Tunnel retrieveEntity through a POST request
	 */
	@PostMapping(value = "/{entityTypeId}/{id:.+}", params = "_method=GET")
	public Map<String, Object> retrieveEntityPost(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId,
			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter,
			@RequestParam(value = "includeCategories", defaultValue = "false") boolean includeCategories)
	{
		return getEntityResponse(entityTypeId, untypedId, attributeFilter, includeCategories);
	}

	private Map<String, Object> getEntityResponse(String entityTypeId, String untypedId,
			AttributeFilter attributeFilter, boolean includeCategories)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		Object id = getTypedValue(untypedId, entityType.getIdAttribute());

		Fetch fetch = AttributeFilterToFetchConverter.convert(attributeFilter, entityType,
				LanguageService.getCurrentUserLanguageCode());

		Entity entity = dataService.findOneById(entityTypeId, id, fetch);
		if (entity == null)
		{
			throw new UnknownEntityException(entityTypeId + " [" + untypedId + "] not found");
		}

		return createEntityResponse(entity, fetch, true, includeCategories);
	}

	@Transactional
	@DeleteMapping("/{entityTypeId:^(?!i18n).+}/{id:.+}")
	@ResponseStatus(NO_CONTENT)
	public void deleteEntity(@PathVariable("entityTypeId") String entityTypeId, @PathVariable("id") String untypedId)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		Object id = getTypedValue(untypedId, entityType.getIdAttribute());

		if (ATTRIBUTE_META_DATA.equals(entityTypeId))
		{
			dataService.getMeta().deleteAttributeById(id);
		}
		else
		{
			dataService.deleteById(entityTypeId, id);
		}
	}

	/**
	 * Delete multiple entities of the given entity type
	 */
	@DeleteMapping("/{entityTypeId}")
	@ResponseStatus(NO_CONTENT)
	public void deleteEntityCollection(@PathVariable("entityTypeId") String entityTypeId,
			@RequestBody @Valid EntityCollectionDeleteRequestV2 request)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		if (entityType.isAbstract())
		{
			throw new MolgenisDataException(
					format("Cannot delete entities because type [%s] is abstract.", entityTypeId));
		}
		Attribute idAttribute = entityType.getIdAttribute();
		Stream<Object> typedIds = request.getEntityIds().stream().map(entityId -> getTypedValue(entityId, idAttribute));
		dataService.deleteAll(entityTypeId, typedIds);
	}

	/**
	 * Retrieve an entity collection, optionally specify which attributes to include in the response.
	 */
	@GetMapping("/{entityTypeId}")
	public EntityCollectionResponseV2 retrieveEntityCollection(@PathVariable("entityTypeId") String entityTypeId,
			@Valid EntityCollectionRequestV2 request, HttpServletRequest httpRequest,
			@RequestParam(value = "includeCategories", defaultValue = "false") boolean includeCategories)
	{
		return createEntityCollectionResponse(entityTypeId, request, httpRequest, includeCategories);
	}

	@PostMapping(value = "/{entityTypeId}", params = "_method=GET")
	public EntityCollectionResponseV2 retrieveEntityCollectionPost(@PathVariable("entityTypeId") String entityTypeId,
			@Valid EntityCollectionRequestV2 request, HttpServletRequest httpRequest,
			@RequestParam(value = "includeCategories", defaultValue = "false") boolean includeCategories)
	{
		return createEntityCollectionResponse(entityTypeId, request, httpRequest, includeCategories);
	}

	/**
	 * Retrieve attribute meta data
	 */
	@GetMapping(value = "/{entityTypeId}/meta/{attributeName}", produces = APPLICATION_JSON_VALUE)
	public AttributeResponseV2 retrieveEntityAttributeMeta(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName)
	{
		return createAttributeResponse(entityTypeId, attributeName);
	}

	@PostMapping(value = "/{entityTypeId}/meta/{attributeName}", params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	public AttributeResponseV2 retrieveEntityAttributeMetaPost(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName)
	{
		return createAttributeResponse(entityTypeId, attributeName);
	}

	/**
	 * Try to create multiple entities in one transaction. If one fails all fails.
	 *
	 * @param entityTypeId name of the entity where the entities are going to be added.
	 * @param request      EntityCollectionCreateRequestV2
	 * @param response     HttpServletResponse
	 * @return EntityCollectionCreateResponseBodyV2
	 */
	@Transactional
	@PostMapping(value = "/{entityTypeId}", produces = APPLICATION_JSON_VALUE)
	public EntityCollectionBatchCreateResponseBodyV2 createEntities(@PathVariable("entityTypeId") String entityTypeId,
			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception
	{
		final EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw createUnknownEntityException(entityTypeId);
		}

		try
		{
			final List<Entity> entities = request.getEntities()
												 .stream()
												 .map(e -> this.restService.toEntity(meta, e))
												 .collect(toList());
			final EntityCollectionBatchCreateResponseBodyV2 responseBody = new EntityCollectionBatchCreateResponseBodyV2();
			final List<String> ids = new ArrayList<>();

			// Add all entities
			if (ATTRIBUTE_META_DATA.equals(entityTypeId))
			{
				entities.stream()
						.map(attribute -> (Attribute) attribute)
						.forEach(attribute -> dataService.getMeta().addAttribute(attribute));
			}
			else
			{
				this.dataService.add(entityTypeId, entities.stream());
			}

			entities.forEach(entity ->
			{
				restService.updateMappedByEntities(entity);

				String id = entity.getIdValue().toString();
				ids.add(id);
				responseBody.getResources()
							.add(new AutoValue_ResourcesResponseV2(
									Href.concatEntityHref(RestControllerV2.BASE_URI, entityTypeId, id)));
			});

			responseBody.setLocation(Href.concatEntityCollectionHref(RestControllerV2.BASE_URI, entityTypeId,
					meta.getIdAttribute().getName(), ids));

			response.setStatus(HttpServletResponse.SC_CREATED);
			return responseBody;
		}
		catch (Exception e)
		{
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			throw e;
		}
	}

	/**
	 * Copy an entity.
	 *
	 * @param entityTypeId name of the entity that will be copied.
	 * @param request      CopyEntityRequestV2
	 * @param response     HttpServletResponse
	 * @return String name of the new entity
	 */
	@Transactional
	@PostMapping(value = "copy/{entityTypeId}", produces = APPLICATION_JSON_VALUE)
	public String copyEntity(@PathVariable("entityTypeId") String entityTypeId,
			@RequestBody @Valid CopyEntityRequestV2 request, HttpServletResponse response) throws Exception
	{
		// No repo
		if (!dataService.hasRepository(entityTypeId)) throw createUnknownEntityException(entityTypeId);

		Repository<Entity> repositoryToCopyFrom = dataService.getRepository(entityTypeId);

		// Validate the new name
		NameValidator.validateEntityName(request.getNewEntityName());

		// Check if the entity already exists
		String newFullName = EntityTypeUtils.buildFullName(repositoryToCopyFrom.getEntityType().getPackage(),
				request.getNewEntityName());
		if (dataService.hasRepository(newFullName)) throw createDuplicateEntityException(newFullName);

		// Permission
		boolean readPermission = permissionService.hasPermission(new EntityTypeIdentity(repositoryToCopyFrom.getName()),
				EntityTypePermission.READ);
		if (!readPermission) throw createNoReadPermissionOnEntityException(entityTypeId);

		// Capabilities
		boolean writableCapabilities = dataService.getCapabilities(repositoryToCopyFrom.getName())
												  .contains(RepositoryCapability.WRITABLE);
		if (!writableCapabilities) throw createNoWriteCapabilitiesOnEntityException(entityTypeId);

		// Copy
		Repository<Entity> repository = this.copyRepositoryRunAsSystem(repositoryToCopyFrom, request.getNewEntityName(),
				repositoryToCopyFrom.getEntityType().getPackage(), request.getNewEntityName());

		// Retrieve new repo
		permissionSystemService.giveUserWriteMetaPermissions(repository.getEntityType());

		response.addHeader("Location", Href.concatMetaEntityHrefV2(RestControllerV2.BASE_URI, repository.getName()));
		response.setStatus(HttpServletResponse.SC_CREATED);

		return repository.getName();
	}

	private Repository<Entity> copyRepositoryRunAsSystem(Repository<Entity> repositoryToCopyFrom, String entityTypeId,
			Package pack, String label)
	{
		return runAsSystem(() -> repoCopier.copyRepository(repositoryToCopyFrom, entityTypeId, pack, label));
	}

	/**
	 * Try to update multiple entities in one transaction. If one fails all fails.
	 *
	 * @param entityTypeId name of the entity where the entities are going to be added.
	 * @param request      EntityCollectionCreateRequestV2
	 * @param response     HttpServletResponse
	 */
	@PutMapping("/{entityTypeId}")
	public synchronized void updateEntities(@PathVariable("entityTypeId") String entityTypeId,
			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception
	{
		final EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw createUnknownEntityException(entityTypeId);
		}

		try
		{
			List<Entity> entities = request.getEntities()
										   .stream()
										   .map(e -> this.restService.toEntity(meta, e))
										   .collect(toList());

			// update all entities
			this.dataService.update(entityTypeId, entities.stream());
			entities.forEach(entity -> restService.updateMappedByEntities(entity,
					dataService.findOneById(entityTypeId, entity.getIdValue())));
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception e)
		{
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			throw e;
		}
	}

	/**
	 * @param entityTypeId  The name of the entity to update
	 * @param attributeName The name of the attribute to update
	 * @param request       EntityCollectionBatchRequestV2
	 * @param response      HttpServletResponse
	 */
	@PutMapping("/{entityTypeId}/{attributeName}")
	@ResponseStatus(OK)
	public synchronized void updateAttribute(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName,
			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception
	{
		final EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw createUnknownEntityException(entityTypeId);
		}

		try
		{
			Attribute attr = meta.getAttribute(attributeName);
			if (attr == null)
			{
				throw new UnknownAttributeException(meta, attributeName);
			}

			if (attr.isReadOnly())
			{
				throw createMolgenisDataAccessExceptionReadOnlyAttribute(entityTypeId, attributeName);
			}

			final List<Entity> entities = request.getEntities()
												 .stream()
												 .filter(e -> e.size() == 2)
												 .map(e -> this.restService.toEntity(meta, e))
												 .collect(toList());
			if (entities.size() != request.getEntities().size())
			{
				throw createMolgenisDataExceptionIdentifierAndValue();
			}

			final List<Entity> updatedEntities = new ArrayList<>();
			int count = 0;
			for (Entity entity : entities)
			{
				Object id = checkForEntityId(entity, count);

				Entity originalEntity = dataService.findOneById(entityTypeId, id);
				if (originalEntity == null)
				{
					throw createUnknownEntityExceptionNotValidId(id);
				}

				Object value = this.restService.toEntityValue(attr, entity.get(attributeName), id);
				originalEntity.set(attributeName, value);
				updatedEntities.add(originalEntity);
				count++;
			}

			// update all entities
			this.dataService.update(entityTypeId, updatedEntities.stream());
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception e)
		{
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			throw e;
		}
	}

	/**
	 * Get all l10n resource strings in the language of the current user
	 */
	@GetMapping(value = "/i18n", produces = APPLICATION_JSON_VALUE)
	public Map<String, String> getL10nStrings()
	{
		Map<String, String> translations = new HashMap<>();

		ResourceBundle bundle = LanguageService.getBundle();
		for (String key : localizationService.getAllMessageIds())
		{
			translations.put(key, bundle.getString(key));
		}

		return translations;
	}

	/**
	 * Get the localization resource strings for a specific language and namespace.
	 * Will *not* provide fallback values if the specified language is not available.
	 */
	@GetMapping(value = "/i18n/{namespace}/{language}", produces = APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public Map<String, String> getL10nStrings(@PathVariable String namespace, @PathVariable String language)
	{
		return localizationService.getMessages(namespace, new Locale(language));
	}

	/**
	 * Get a properties file to put on your classpath.
	 */
	@GetMapping(value = "/i18n/{namespace}_{language}.properties", produces = TEXT_PLAIN_VALUE + ";charset=UTF-8 ")
	public String getL10nProperties(@PathVariable String namespace, @PathVariable String language) throws IOException
	{
		language = language.toLowerCase();
		Properties translations = new Properties();
		Locale locale = new Locale(language);
		translations.putAll(localizationService.getMessages(namespace, locale));
		StringWriter sw = new StringWriter();
		translations.store(sw, String.format("%s_%s.properties", namespace, language));
		return sw.toString();
	}

	/**
	 * Registers missing message IDs.
	 * Used by XHR backend of i18next.
	 * User needs permissions on the entity to add the values, otherwise they'll only be logged.
	 */
	@PostMapping("/i18n/{namespace}")
	@ResponseStatus(CREATED)
	public void registerMissingResourceStrings(@PathVariable String namespace, HttpServletRequest request)
	{
		Set<String> messageIDs = request.getParameterMap()
										.entrySet()
										.stream()
										.map(Map.Entry::getKey)
										.filter(id -> !id.equals(TIME_PARAM_NAME))
										.collect(toSet());
		localizationService.addMissingMessageIds(namespace, messageIDs);
	}

	@DeleteMapping("/i18n/{namespace}")
	@ResponseStatus(NO_CONTENT)
	public void deleteNamespace(@PathVariable String namespace)
	{
		localizationService.deleteNamespace(namespace);
	}

	/**
	 * Get entity id and perform a check, throwing an MolgenisDataException when necessary
	 */
	private static Object checkForEntityId(Entity entity, int count)
	{
		Object id = entity.getIdValue();
		if (null == id)
		{
			throw createMolgenisDataExceptionUnknownIdentifier(count);
		}
		return id;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	public @ResponseBody
	ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception)
	{
		LOG.debug("Invalid request body.", exception);
		return new ErrorMessageResponse(new ErrorMessage("Invalid request body."));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	public @ResponseBody
	ErrorMessageResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception)
	{
		LOG.info("Invalid method arguments.", exception);
		return new ErrorMessageResponse(transform(exception.getBindingResult().getFieldErrors(),
				error -> new ErrorMessage(error.getDefaultMessage())));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.info("Operation failed.", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataAccessException.class)
	@ResponseStatus(UNAUTHORIZED)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
		LOG.debug("Data access exception occurred.", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(ConversionFailedException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleConversionFailedException(ConversionFailedException e)
	{
		LOG.info("ConversionFailedException occurred", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	private AttributeResponseV2 createAttributeResponse(String entityTypeId, String attributeName)
	{
		EntityType entity = dataService.getEntityType(entityTypeId);
		if (entity == null)
		{
			throw new UnknownEntityException(entityTypeId + " not found");
		}

		Attribute attribute = entity.getAttribute(attributeName);
		if (attribute == null)
		{
			throw new RuntimeException("attribute : " + attributeName + " does not exist!");
		}

		return new AttributeResponseV2(entityTypeId, entity, attribute, null, permissionService, dataService);
	}

	private EntityCollectionResponseV2 createEntityCollectionResponse(String entityTypeId,
			EntityCollectionRequestV2 request, HttpServletRequest httpRequest, boolean includeCategories)
	{
		EntityType meta = dataService.getEntityType(entityTypeId);

		Query<Entity> q = request.getQ() != null ? request.getQ().createQuery(meta) : new QueryImpl<>();
		q.pageSize(request.getNum()).offset(request.getStart()).sort(request.getSort());
		Fetch fetch = AttributeFilterToFetchConverter.convert(request.getAttrs(), meta,
				LocaleContextHolder.getLocale().getLanguage());
		if (fetch != null)
		{
			q.fetch(fetch);
		}

		if (request.getAggs() != null)
		{
			// return aggregates for aggregate query
			AggregateQuery aggsQ = request.getAggs().createAggregateQuery(meta, q);
			Attribute xAttr = aggsQ.getAttributeX();
			Attribute yAttr = aggsQ.getAttributeY();
			if (xAttr == null && yAttr == null)
			{
				throw new MolgenisQueryException("Aggregate query is missing 'x' or 'y' attribute");
			}
			AggregateResult aggs = dataService.aggregate(entityTypeId, aggsQ);
			AttributeResponseV2 xAttrResponse =
					xAttr != null ? new AttributeResponseV2(entityTypeId, meta, xAttr, fetch, permissionService,
							dataService) : null;
			AttributeResponseV2 yAttrResponse =
					yAttr != null ? new AttributeResponseV2(entityTypeId, meta, yAttr, fetch, permissionService,
							dataService) : null;
			return new EntityAggregatesResponse(aggs, xAttrResponse, yAttrResponse, BASE_URI + '/' + entityTypeId);
		}
		else
		{
			Long count = dataService.count(entityTypeId, new QueryImpl<>(q).setOffset(0).setPageSize(0));
			Iterable<Entity> it;
			if (count > 0 && q.getPageSize() > 0)
			{
				it = () -> dataService.findAll(entityTypeId, q).iterator();
			}
			else
			{
				it = Collections.emptyList();
			}
			EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

			List<Map<String, Object>> entities = new ArrayList<>();
			for (Entity entity : it)
			{
				Map<String, Object> responseData = new LinkedHashMap<>();
				createEntityValuesResponse(entity, fetch, responseData);
				entities.add(responseData);
			}

			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getFullURL(httpRequest));

			String prevHref = null;
			if (pager.getPrevStart() != null)
			{
				builder.replaceQueryParam("start", pager.getPrevStart());
				prevHref = builder.build(false).toUriString();
			}

			String nextHref = null;
			if (pager.getNextStart() != null)
			{
				builder.replaceQueryParam("start", pager.getNextStart());
				nextHref = builder.build(false).toUriString();
			}

			return new EntityCollectionResponseV2(pager, entities, fetch, BASE_URI + '/' + entityTypeId, meta,
					permissionService, dataService, prevHref, nextHref, includeCategories);
		}
	}

	private String getFullURL(HttpServletRequest request)
	{
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null)
		{
			return requestURL.toString();
		}
		else
		{
			return requestURL.append('?').append(queryString).toString();
		}
	}

	private Map<String, Object> createEntityResponse(Entity entity, Fetch fetch, boolean includeMetaData)
	{
		return createEntityResponse(entity, fetch, includeMetaData, false);
	}

	private Map<String, Object> createEntityResponse(Entity entity, Fetch fetch, boolean includeMetaData, boolean includeCategories)
	{
		Map<String, Object> responseData = new LinkedHashMap<>();
		if (includeMetaData)
		{
			createEntityTypeResponse(entity.getEntityType(), fetch, responseData, includeCategories);
		}
		createEntityValuesResponse(entity, fetch, responseData);
		return responseData;
	}

	private void createEntityTypeResponse(EntityType entityType, Fetch fetch, Map<String, Object> responseData, boolean includeCategories)
	{
		responseData.put("_meta", new EntityTypeResponseV2(entityType, fetch, permissionService, dataService, includeCategories));
	}

	private void createEntityValuesResponse(Entity entity, Fetch fetch, Map<String, Object> responseData)
	{
		Iterable<Attribute> attrs = entity.getEntityType().getAtomicAttributes();
		createEntityValuesResponseRec(entity, attrs, fetch, responseData);
	}

	private void createEntityValuesResponseRec(Entity entity, Iterable<Attribute> attrs, Fetch fetch,
			Map<String, Object> responseData)
	{
		responseData.put("_href", Href.concatEntityHref(BASE_URI, entity.getEntityType().getId(), entity.getIdValue()));
		for (Attribute attr : attrs) // TODO performance use fetch instead of attrs
		{
			String attrName = attr.getName();
			if (fetch == null || fetch.hasField(attr))
			{
				AttributeType dataType = attr.getDataType();
				switch (dataType)
				{
					case BOOL:
						responseData.put(attrName, entity.getBoolean(attrName));
						break;
					case CATEGORICAL:
					case XREF:
					case FILE:
						Entity refEntity = entity.getEntity(attrName);
						Map<String, Object> refEntityResponse;
						if (refEntity != null)
						{
							Fetch refAttrFetch =
									fetch != null ? fetch.getFetch(attr) : createDefaultAttributeFetch(attr,
											LanguageService.getCurrentUserLanguageCode());
							refEntityResponse = createEntityResponse(refEntity, refAttrFetch, false);
						}
						else
						{
							refEntityResponse = null;
						}
						responseData.put(attrName, refEntityResponse);
						break;
					case CATEGORICAL_MREF:
					case MREF:
					case ONE_TO_MANY:
						Iterable<Entity> refEntities = entity.getEntities(attrName);
						List<Map<String, Object>> refEntityResponses;
						if (refEntities != null)
						{
							refEntityResponses = new ArrayList<>();
							Fetch refAttrFetch =
									fetch != null ? fetch.getFetch(attrName) : createDefaultAttributeFetch(attr,
											LanguageService.getCurrentUserLanguageCode());
							for (Entity refEntitiesEntity : refEntities)
							{
								refEntityResponses.add(createEntityResponse(refEntitiesEntity, refAttrFetch, false));
							}
						}
						else
						{
							refEntityResponses = null;
						}
						responseData.put(attrName, refEntityResponses);
						break;
					case COMPOUND:
						throw new RuntimeException("Invalid data type [" + dataType + "]");
					case DATE:
						LocalDate dateValue = entity.getLocalDate(attrName);
						responseData.put(attrName, dateValue != null ? dateValue.toString() : null);
						break;
					case DATE_TIME:
						Instant dateTimeValue = entity.getInstant(attrName);
						responseData.put(attrName, dateTimeValue != null ? dateTimeValue.toString() : null);
						break;
					case DECIMAL:
						responseData.put(attrName, entity.getDouble(attrName));
						break;
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case SCRIPT:
					case STRING:
					case TEXT:
						responseData.put(attrName, entity.getString(attrName));
						break;
					case INT:
						responseData.put(attrName, entity.getInt(attrName));
						break;
					case LONG:
						responseData.put(attrName, entity.getLong(attrName));
						break;
					default:
						throw new UnexpectedEnumException(dataType);
				}
			}
		}
	}
}
