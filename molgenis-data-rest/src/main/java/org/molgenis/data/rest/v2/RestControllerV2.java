package org.molgenis.data.rest.v2;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.NameValidator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.Href;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.RepositoryCopier;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.transform;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.rest.v2.AttributeFilterToFetchConverter.createDefaultAttributeFetch;
import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;
import static org.molgenis.util.EntityUtils.getTypedValue;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@RequestMapping(BASE_URI)
class RestControllerV2
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerV2.class);

	static final int MAX_ENTITIES = 1000;

	public static final String BASE_URI = "/api/v2";

	private final DataService dataService;
	private final RestService restService;
	private final MolgenisPermissionService permissionService;
	private final PermissionSystemService permissionSystemService;
	private final LanguageService languageService;
	private final RepositoryCopier repoCopier;

	static UnknownEntityException createUnknownEntityException(String entityName)
	{
		return new UnknownEntityException("Operation failed. Unknown entity: '" + entityName + "'");
	}

	static MolgenisDataAccessException createNoReadPermissionOnEntityException(String entityName)
	{
		return new MolgenisDataAccessException("No read permission on entity " + entityName);
	}

	static MolgenisDataException createNoWriteCapabilitiesOnEntityException(String entityName)
	{
		return new MolgenisRepositoryCapabilitiesException("No write capabilities for entity " + entityName);
	}

	static DuplicateEntityException createDuplicateEntityException(String entityName)
	{
		return new DuplicateEntityException("Operation failed. Duplicate entity: '" + entityName + "'");
	}

	static UnknownAttributeException createUnknownAttributeException(String entityName, String attributeName)
	{
		return new UnknownAttributeException(
				"Operation failed. Unknown attribute: '" + attributeName + "', of entity: '" + entityName + "'");
	}

	static MolgenisDataAccessException createMolgenisDataAccessExceptionReadOnlyAttribute(String entityName,
			String attributeName)
	{
		return new MolgenisDataAccessException(
				"Operation failed. Attribute '" + attributeName + "' of entity '" + entityName + "' is readonly");
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

	@Autowired
	public RestControllerV2(DataService dataService, MolgenisPermissionService permissionService,
			RestService restService, LanguageService languageService, PermissionSystemService permissionSystemService,
			RepositoryCopier repoCopier)
	{
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
		this.restService = requireNonNull(restService);
		this.languageService = requireNonNull(languageService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.repoCopier = requireNonNull(repoCopier);
	}

	@Autowired
	@RequestMapping(value = "/version", method = GET)
	@ResponseBody
	public Map<String, String> getVersion(@Value("${molgenis.version:@null}") String molgenisVersion,
			@Value("${molgenis.build.date:@null}") String molgenisBuildDate)
	{
		if (molgenisVersion == null) throw new IllegalArgumentException("molgenisVersion is null");
		if (molgenisBuildDate == null) throw new IllegalArgumentException("molgenisBuildDate is null");
		molgenisBuildDate = molgenisBuildDate.equals("${maven.build.timestamp}") ?
				new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date())
						+ " by IntelliJ" : molgenisBuildDate;

		Map<String, String> result = new HashMap<>();
		result.put("molgenisVersion", molgenisVersion);
		result.put("buildDate", molgenisBuildDate);

		return result;
	}

	/**
	 * Retrieve an entity instance by id, optionally specify which attributes to include in the response.
	 *
	 * @param entityName
	 * @param untypedId
	 * @param attributeFilter
	 * @return
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = GET)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId,
			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter)
	{
		return getEntityResponse(entityName, untypedId, attributeFilter);
	}

	/**
	 * Tunnel retrieveEntity through a POST request
	 *
	 * @param entityName
	 * @param untypedId
	 * @param attributeFilter
	 * @return
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = POST, params = "_method=GET")
	@ResponseBody
	public Map<String, Object> retrieveEntityPost(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId,
			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter)
	{
		return getEntityResponse(entityName, untypedId, attributeFilter);
	}

	private Map<String, Object> getEntityResponse(String entityName, String untypedId, AttributeFilter attributeFilter)
	{
		EntityType entityType = dataService.getEntityType(entityName);
		Object id = getTypedValue(untypedId, entityType.getIdAttribute());

		Fetch fetch = AttributeFilterToFetchConverter
				.convert(attributeFilter, entityType, languageService.getCurrentUserLanguageCode());

		Entity entity = dataService.findOneById(entityName, id, fetch);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " [" + untypedId + "] not found");
		}

		return createEntityResponse(entity, fetch, true);
	}

	@RequestMapping(value = "/{entityName}/{id:.+}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void deleteEntity(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId)
	{
		EntityType entityType = dataService.getEntityType(entityName);
		Object id = getTypedValue(untypedId, entityType.getIdAttribute());

		if (ATTRIBUTE_META_DATA.equals(entityName))
		{
			dataService.getMeta().deleteAttributeById(id);
		}
		else
		{
			dataService.deleteById(entityName, id);
		}
	}

	/**
	 * Retrieve an entity collection, optionally specify which attributes to include in the response.
	 *
	 * @param entityName
	 * @param request
	 * @param httpRequest
	 * @return
	 */
	@RequestMapping(value = "/{entityName}", method = GET)
	@ResponseBody
	public EntityCollectionResponseV2 retrieveEntityCollection(@PathVariable("entityName") String entityName,
			@Valid EntityCollectionRequestV2 request, HttpServletRequest httpRequest)
	{
		return createEntityCollectionResponse(entityName, request, httpRequest);
	}

	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET")
	@ResponseBody
	public EntityCollectionResponseV2 retrieveEntityCollectionPost(@PathVariable("entityName") String entityName,
			@Valid EntityCollectionRequestV2 request, HttpServletRequest httpRequest)
	{
		return createEntityCollectionResponse(entityName, request, httpRequest);
	}

	/**
	 * Retrieve attribute meta data
	 *
	 * @param entityName
	 * @param attributeName
	 * @return
	 */
	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeResponseV2 retrieveEntityAttributeMeta(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName)
	{
		return createAttributeResponse(entityName, attributeName);
	}

	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeResponseV2 retrieveEntityAttributeMetaPost(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName)
	{
		return createAttributeResponse(entityName, attributeName);
	}

	/**
	 * Try to create multiple entities in one transaction. If one fails all fails.
	 *
	 * @param entityName name of the entity where the entities are going to be added.
	 * @param request    EntityCollectionCreateRequestV2
	 * @param response   HttpServletResponse
	 * @return EntityCollectionCreateResponseBodyV2
	 * @throws Exception
	 */
	@RequestMapping(value = "/{entityName}", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionBatchCreateResponseBodyV2 createEntities(@PathVariable("entityName") String entityName,
			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception
	{
		final EntityType meta = dataService.getEntityType(entityName);
		if (meta == null)
		{
			throw createUnknownEntityException(entityName);
		}

		try
		{
			final List<Entity> entities = request.getEntities().stream().map(e -> this.restService.toEntity(meta, e))
					.collect(Collectors.toList());
			final EntityCollectionBatchCreateResponseBodyV2 responseBody = new EntityCollectionBatchCreateResponseBodyV2();
			final List<String> ids = new ArrayList<String>();

			// Add all entities
			if (ATTRIBUTE_META_DATA.equals(entityName))
			{
				this.dataService.getMeta().addAttributes(entityName, entities.stream().map(a -> (Attribute) a));
			}
			else
			{
				this.dataService.add(entityName, entities.stream());
			}

			entities.forEach(entity ->
			{
				restService.updateMappedByEntities(entity);

				String id = entity.getIdValue().toString();
				ids.add(id.toString());
				responseBody.getResources().add(new AutoValue_ResourcesResponseV2(
						Href.concatEntityHref(RestControllerV2.BASE_URI, entityName, id)));
			});

			responseBody.setLocation(Href.concatEntityCollectionHref(RestControllerV2.BASE_URI, entityName,
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
	 * @param entityName name of the entity that will be copied.
	 * @param request    CopyEntityRequestV2
	 * @param response   HttpServletResponse
	 * @return String name of the new entity
	 * @throws Exception
	 */
	@RequestMapping(value = "copy/{entityName}", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public String copyEntity(@PathVariable("entityName") String entityName,
			@RequestBody @Valid CopyEntityRequestV2 request, HttpServletResponse response) throws Exception
	{
		// No repo
		if (!dataService.hasRepository(entityName)) throw createUnknownEntityException(entityName);

		Repository<Entity> repositoryToCopyFrom = dataService.getRepository(entityName);

		// Validate the new name
		NameValidator.validateName(request.getNewEntityName());

		// Check if the entity already exists
		String newFullName = EntityTypeUtils
				.buildFullName(repositoryToCopyFrom.getEntityType().getPackage(), request.getNewEntityName());
		if (dataService.hasRepository(newFullName)) throw createDuplicateEntityException(newFullName);

		// Permission
		boolean readPermission = permissionService
				.hasPermissionOnEntity(repositoryToCopyFrom.getName(), Permission.READ);
		if (!readPermission) throw createNoReadPermissionOnEntityException(entityName);

		// Capabilities
		boolean writableCapabilities = dataService.getCapabilities(repositoryToCopyFrom.getName())
				.contains(RepositoryCapability.WRITABLE);
		if (!writableCapabilities) throw createNoWriteCapabilitiesOnEntityException(entityName);

		// Copy
		this.copyRepositoryRunAsSystem(repositoryToCopyFrom, request.getNewEntityName(),
				repositoryToCopyFrom.getEntityType().getPackage(), request.getNewEntityName());

		// Retrieve new repo
		Repository<Entity> repository = dataService.getRepository(newFullName);
		permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
				Collections.singletonList(repository.getName()));

		response.addHeader("Location", Href.concatMetaEntityHrefV2(RestControllerV2.BASE_URI, repository.getName()));
		response.setStatus(HttpServletResponse.SC_CREATED);

		return repository.getName();
	}

	private void copyRepositoryRunAsSystem(Repository<Entity> repositoryToCopyFrom, String simpleName, Package pack,
			String label)
	{
		RunAsSystemProxy.runAsSystem(() -> repoCopier.copyRepository(repositoryToCopyFrom, simpleName, pack, label));
	}

	/**
	 * Try to update multiple entities in one transaction. If one fails all fails.
	 *
	 * @param entityName name of the entity where the entities are going to be added.
	 * @param request    EntityCollectionCreateRequestV2
	 * @param response   HttpServletResponse
	 * @throws Exception
	 */
	@RequestMapping(value = "/{entityName}", method = PUT)
	public synchronized void updateEntities(@PathVariable("entityName") String entityName,
			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception
	{
		final EntityType meta = dataService.getEntityType(entityName);
		if (meta == null)
		{
			throw createUnknownEntityException(entityName);
		}

		try
		{
			final Stream<Entity> entities = request.getEntities().stream().map(e -> this.restService.toEntity(meta, e));

			// update all entities
			this.dataService.update(entityName, entities);
			entities.forEach(entity -> restService
					.updateMappedByEntities(entity, dataService.findOneById(entityName, entity.getIdValue())));
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception e)
		{
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			throw e;
		}
	}

	/**
	 * @param entityName    The name of the entity to update
	 * @param attributeName The name of the attribute to update
	 * @param request       EntityCollectionBatchRequestV2
	 * @param response      HttpServletResponse
	 * @throws Exception
	 */
	@RequestMapping(value = "/{entityName}/{attributeName}", method = PUT)
	@ResponseStatus(OK)
	public synchronized void updateAttribute(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName,
			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception
	{
		final EntityType meta = dataService.getEntityType(entityName);
		if (meta == null)
		{
			throw createUnknownEntityException(entityName);
		}

		try
		{
			Attribute attr = meta.getAttribute(attributeName);
			if (attr == null)
			{
				throw createUnknownAttributeException(entityName, attributeName);
			}

			if (attr.isReadOnly())
			{
				throw createMolgenisDataAccessExceptionReadOnlyAttribute(entityName, attributeName);
			}

			final List<Entity> entities = request.getEntities().stream().filter(e -> e.size() == 2)
					.map(e -> this.restService.toEntity(meta, e)).collect(Collectors.toList());
			if (entities.size() != request.getEntities().size())
			{
				throw createMolgenisDataExceptionIdentifierAndValue();
			}

			final List<Entity> updatedEntities = new ArrayList<Entity>();
			int count = 0;
			for (Entity entity : entities)
			{
				Object id = checkForEntityId(entity, count);

				Entity originalEntity = dataService.findOneById(entityName, id);
				if (originalEntity == null)
				{
					throw createUnknownEntityExceptionNotValidId(id);
				}

				Object value = this.restService.toEntityValue(attr, entity.get(attributeName));
				originalEntity.set(attributeName, value);
				updatedEntities.add(originalEntity);
				count++;
			}

			// update all entities
			this.dataService.update(entityName, updatedEntities.stream());
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (Exception e)
		{
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			throw e;
		}
	}

	/**
	 * Get the i18n resource strings in the language of the current user
	 */
	@RequestMapping(value = "/i18n", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, String> getI18nStrings()
	{
		Map<String, String> translations = new HashMap<>();

		ResourceBundle bundle = languageService.getBundle();
		for (String key : bundle.keySet())
		{
			translations.put(key, bundle.getString(key));
		}

		return translations;
	}

	/**
	 * Get entity id and perform a check, throwing an MolgenisDataException when necessary
	 *
	 * @param entity
	 * @param count
	 * @return
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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	public
	@ResponseBody
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

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("Runtime exception occurred.", e);
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

	private AttributeResponseV2 createAttributeResponse(String entityName, String attributeName)
	{
		EntityType entity = dataService.getEntityType(entityName);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " not found");
		}

		Attribute attribute = entity.getAttribute(attributeName);
		if (attribute == null)
		{
			throw new RuntimeException("attribute : " + attributeName + " does not exist!");
		}

		return new AttributeResponseV2(entityName, entity, attribute, null, permissionService, dataService,
				languageService);
	}

	private EntityCollectionResponseV2 createEntityCollectionResponse(String entityName,
			EntityCollectionRequestV2 request, HttpServletRequest httpRequest)
	{
		EntityType meta = dataService.getEntityType(entityName);

		Query<Entity> q = request.getQ() != null ? request.getQ().createQuery(meta) : new QueryImpl<>();
		q.pageSize(request.getNum()).offset(request.getStart()).sort(request.getSort());
		Fetch fetch = AttributeFilterToFetchConverter
				.convert(request.getAttrs(), meta, languageService.getCurrentUserLanguageCode());
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
			AggregateResult aggs = dataService.aggregate(entityName, aggsQ);
			AttributeResponseV2 xAttrResponse =
					xAttr != null ? new AttributeResponseV2(entityName, meta, xAttr, fetch, permissionService,
							dataService, languageService) : null;
			AttributeResponseV2 yAttrResponse =
					yAttr != null ? new AttributeResponseV2(entityName, meta, yAttr, fetch, permissionService,
							dataService, languageService) : null;
			return new EntityAggregatesResponse(aggs, xAttrResponse, yAttrResponse, BASE_URI + '/' + entityName);
		}
		else
		{
			Long count = dataService.count(entityName, q);
			Iterable<Entity> it;
			if (count > 0)
			{
				it = () -> dataService.findAll(entityName, q).iterator();
			}
			else
			{
				it = Collections.emptyList();
			}
			EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

			List<Map<String, Object>> entities = new ArrayList<>();
			for (Entity entity : it)
			{
				Map<String, Object> responseData = new LinkedHashMap<String, Object>();
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

			return new EntityCollectionResponseV2(pager, entities, fetch, BASE_URI + '/' + entityName, meta,
					permissionService, dataService, languageService, prevHref, nextHref);
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
		Map<String, Object> responseData = new LinkedHashMap<String, Object>();
		if (includeMetaData)
		{
			createEntityTypeResponse(entity.getEntityType(), fetch, responseData);
		}
		createEntityValuesResponse(entity, fetch, responseData);
		return responseData;
	}

	private void createEntityTypeResponse(EntityType entityType, Fetch fetch, Map<String, Object> responseData)
	{
		responseData.put("_meta",
				new EntityTypeResponseV2(entityType, fetch, permissionService, dataService, languageService));
	}

	private void createEntityValuesResponse(Entity entity, Fetch fetch, Map<String, Object> responseData)
	{
		Iterable<Attribute> attrs = entity.getEntityType().getAtomicAttributes();
		createEntityValuesResponseRec(entity, attrs, fetch, responseData);
	}

	private void createEntityValuesResponseRec(Entity entity, Iterable<Attribute> attrs, Fetch fetch,
			Map<String, Object> responseData)
	{
		responseData
				.put("_href", Href.concatEntityHref(BASE_URI, entity.getEntityType().getName(), entity.getIdValue()));
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
											languageService.getCurrentUserLanguageCode());
							;
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
							refEntityResponses = new ArrayList<Map<String, Object>>();
							Fetch refAttrFetch =
									fetch != null ? fetch.getFetch(attrName) : createDefaultAttributeFetch(attr,
											languageService.getCurrentUserLanguageCode());
							;
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
						Date dateValue = entity.getUtilDate(attrName);
						String dateValueStr = dateValue != null ? getDateFormat().format(dateValue) : null;
						responseData.put(attrName, dateValueStr);
						break;
					case DATE_TIME:
						Date dateTimeValue = entity.getUtilDate(attrName);
						String dateTimeValueStr =
								dateTimeValue != null ? getDateTimeFormat().format(dateTimeValue) : null;
						responseData.put(attrName, dateTimeValueStr);
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
						throw new RuntimeException("Unknown data type [" + dataType + "]");
				}
			}
		}
	}
}
