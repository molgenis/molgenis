package org.molgenis.data.rest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.ui.data.rsql.MolgenisRSQL;
import org.molgenis.core.ui.data.support.Href;
import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.data.support.DefaultEntityCollection;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.token.UnknownTokenException;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.token.TokenParam;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.data.util.EntityUtils.getTypedValue;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENABLED;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENFORCED;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Rest endpoint for the DataService
 * <p>
 * Query, create, update and delete entities.
 * <p>
 * If a repository isn't capable of doing the requested operation an error is thrown.
 * <p>
 * Response is json.
 *
 * @author erwin
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping(BASE_URI)
public class RestController
{
	private static final Logger LOG = LoggerFactory.getLogger(RestController.class);

	static final String BASE_URI = "/api/v1";
	private static final Pattern PATTERN_EXPANDS = Pattern.compile("([^\\[^\\]]+)(?:\\[(.+)\\])?");

	private final AuthenticationSettings authenticationSettings;
	private final DataService dataService;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final UserPermissionEvaluator permissionService;
	private final UserAccountService userAccountService;
	private final MolgenisRSQL molgenisRSQL;
	private final RestService restService;

	public RestController(AuthenticationSettings authenticationSettings, DataService dataService,
			TokenService tokenService, AuthenticationManager authenticationManager,
			UserPermissionEvaluator permissionService, UserAccountService userAccountService, MolgenisRSQL molgenisRSQL,
			RestService restService)
	{
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.dataService = requireNonNull(dataService);
		this.tokenService = requireNonNull(tokenService);
		this.authenticationManager = requireNonNull(authenticationManager);
		this.userAccountService = requireNonNull(userAccountService);
		this.permissionService = requireNonNull(permissionService);
		this.molgenisRSQL = requireNonNull(molgenisRSQL);
		this.restService = requireNonNull(restService);
	}

	/**
	 * Checks if an entity exists.
	 */
	@GetMapping(value = "/{entityTypeId}/exist", produces = APPLICATION_JSON_VALUE)
	public boolean entityExists(@PathVariable("entityTypeId") String entityTypeId)
	{
		try
		{
			dataService.getRepository(entityTypeId);
			return true;
		}
		catch (UnknownEntityTypeException e)
		{
			return false;
		}
	}

	/**
	 * Gets the metadata for an entity
	 * <p>
	 * Example url: /api/v1/person/meta
	 *
	 * @return EntityType
	 */
	@GetMapping(value = "/{entityTypeId}/meta", produces = APPLICATION_JSON_VALUE)
	public EntityTypeResponse retrieveEntityType(@PathVariable("entityTypeId") String entityTypeId,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
		return new EntityTypeResponse(meta, attributeSet, attributeExpandSet, permissionService, dataService);
	}

	/**
	 * Same as retrieveEntityType (GET) only tunneled through POST.
	 * <p>
	 * Example url: /api/v1/person/meta?_method=GET
	 *
	 * @return EntityType
	 */
	@PostMapping(value = "/{entityTypeId}/meta", params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	public EntityTypeResponse retrieveEntityTypePost(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody EntityTypeRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
		return new EntityTypeResponse(meta, attributesSet, attributeExpandSet, permissionService, dataService);
	}

	/**
	 * Example url: /api/v1/person/meta/emailaddresses
	 *
	 * @return EntityType
	 */
	@GetMapping(value = "/{entityTypeId}/meta/{attributeName}", produces = APPLICATION_JSON_VALUE)
	public AttributeResponse retrieveEntityAttributeMeta(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return getAttributePostInternal(entityTypeId, attributeName, attributeSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityAttributeMeta (GET) only tunneled through POST.
	 *
	 * @return EntityType
	 */
	@PostMapping(value = "/{entityTypeId}/meta/{attributeName}", params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	public AttributeResponse retrieveEntityAttributeMetaPost(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName, @Valid @RequestBody EntityTypeRequest request)
	{
		Set<String> attributeSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		return getAttributePostInternal(entityTypeId, attributeName, attributeSet, attributeExpandSet);
	}

	/**
	 * Get's an entity by it's id
	 * <p>
	 * Examples:
	 * <p>
	 * /api/v1/person/99 Retrieves a person with id 99
	 */
	@GetMapping(value = "/{entityTypeId}/{id:.+}", produces = APPLICATION_JSON_VALUE)
	public Map<String, Object> retrieveEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
		Object id = getTypedValue(untypedId, meta.getIdAttribute());
		Entity entity = dataService.findOneById(entityTypeId, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityTypeId + " " + untypedId + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntity (GET) only tunneled through POST.
	 */
	@PostMapping(value = "/{entityTypeId}/{id:.+}", params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId, @Valid @RequestBody EntityTypeRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		EntityType meta = dataService.getEntityType(entityTypeId);
		Object id = getTypedValue(untypedId, meta.getIdAttribute());
		Entity entity = dataService.findOneById(entityTypeId, id);

		if (entity == null)
		{
			throw new UnknownEntityException(entityTypeId + " " + untypedId + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * <p>
	 * Example:
	 * <p>
	 * /api/v1/person/99/address
	 */
	@GetMapping(value = "/{entityTypeId}/{id}/{refAttributeName}", produces = APPLICATION_JSON_VALUE)
	public Object retrieveEntityAttribute(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId, @PathVariable("refAttributeName") String refAttributeName,
			@Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return retrieveEntityAttributeInternal(entityTypeId, untypedId, refAttributeName, request, attributesSet,
				attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * <p>
	 * Example:
	 * <p>
	 * /api/v1/person/99/address
	 */
	@PostMapping(value = "/{entityTypeId}/{id}/{refAttributeName}", params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	public Object retrieveEntityAttributePost(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId, @PathVariable("refAttributeName") String refAttributeName,
			@Valid @RequestBody EntityCollectionRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request.getAttributes());
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request.getExpand());

		return retrieveEntityAttributeInternal(entityTypeId, untypedId, refAttributeName, request, attributesSet,
				attributeExpandSet);
	}

	/**
	 * Do a query
	 * <p>
	 * Returns json
	 */
	@GetMapping(value = "/{entityTypeId}", produces = APPLICATION_JSON_VALUE)
	public EntityCollectionResponse retrieveEntityCollection(@PathVariable("entityTypeId") String entityTypeId,
			@Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return retrieveEntityCollectionInternal(entityTypeId, request, attributesSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityCollection (GET) only tunneled through POST.
	 * <p>
	 * Example url: /api/v1/person?_method=GET
	 * <p>
	 * Returns json
	 */
	@PostMapping(value = "/{entityTypeId}", params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	public EntityCollectionResponse retrieveEntityCollectionPost(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody EntityCollectionRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request.getAttributes());
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request.getExpand());

		return retrieveEntityCollectionInternal(entityTypeId, request, attributesSet, attributeExpandSet);
	}

	/**
	 * Does a rsql/fiql query, returns the result as csv
	 * <p>
	 * Parameters:
	 * <p>
	 * q: the query
	 * <p>
	 * attributes: the attributes to return, if not specified returns all attributes
	 * <p>
	 * start: the index of the first row, default 0
	 * <p>
	 * num: the number of results to return, default 100, max 10000
	 * <p>
	 * <p>
	 * Example: /api/v1/csv/person?q=firstName==Piet&attributes=firstName,lastName&start=10&num=100
	 */
	@GetMapping(value = "/csv/{entityTypeId}", produces = "text/csv")
	@ResponseBody
	public EntityCollection retrieveEntityCollection(@PathVariable("entityTypeId") String entityTypeId,
			@RequestParam(value = "attributes", required = false) String[] attributes, HttpServletRequest req,
			HttpServletResponse resp) throws IOException
	{
		final Set<String> attributesSet = toAttributeSet(attributes);

		EntityType meta;
		Iterable<Entity> entities;
		try
		{
			meta = dataService.getEntityType(entityTypeId);
			Query<Entity> q = new QueryStringParser(meta, molgenisRSQL).parseQueryString(req.getParameterMap());

			String[] sortAttributeArray = req.getParameterMap().get("sortColumn");
			if (sortAttributeArray != null && sortAttributeArray.length == 1 && StringUtils.isNotEmpty(
					sortAttributeArray[0]))
			{
				String sortAttribute = sortAttributeArray[0];
				String sortOrderArray[] = req.getParameterMap().get("sortOrder");
				Sort.Direction order = Sort.Direction.ASC;

				if (sortOrderArray != null && sortOrderArray.length == 1 && StringUtils.isNotEmpty(sortOrderArray[0]))
				{
					String sortOrder = sortOrderArray[0];
					switch (sortOrder)
					{
						case "ASC":
							order = Sort.Direction.ASC;
							break;
						case "DESC":
							order = Sort.Direction.DESC;
							break;
						default:
							throw new RuntimeException("unknown sort order");
					}
				}
				q.sort().on(sortAttribute, order);
			}

			if (q.getPageSize() == 0)
			{
				q.pageSize(EntityCollectionRequest.DEFAULT_ROW_COUNT);
			}

			if (q.getPageSize() > EntityCollectionRequest.MAX_ROWS)
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Num exceeded the maximum of " + EntityCollectionRequest.MAX_ROWS + " rows");
				return null;
			}

			entities = () -> dataService.findAll(entityTypeId, q).iterator();
		}
		catch (ConversionFailedException | RSQLParserException | UnknownAttributeException | IllegalArgumentException | UnsupportedOperationException | UnknownEntityException e)
		{
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return null;
		}
		catch (MolgenisDataAccessException e)
		{
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		// Check attribute names
		Iterable<String> attributesIterable = Iterables.transform(meta.getAtomicAttributes(),
				attribute -> attribute.getName().toLowerCase());

		if (attributesSet != null)
		{
			SetView<String> diff = Sets.difference(attributesSet, Sets.newHashSet(attributesIterable));
			if (!diff.isEmpty())
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown attributes " + diff);
				return null;
			}
		}

		attributesIterable = Iterables.transform(meta.getAtomicAttributes(), Attribute::getName);

		if (attributesSet != null)
		{
			attributesIterable = Iterables.filter(attributesIterable,
					attribute -> attributesSet.contains(attribute.toLowerCase()));
		}

		return new DefaultEntityCollection(entities, attributesIterable);
	}

	/**
	 * Creates a new entity from a html form post.
	 */
	@Transactional
	@PostMapping(value = "/{entityTypeId}", headers = "Content-Type=application/x-www-form-urlencoded")
	public void createFromFormPost(@PathVariable("entityTypeId") String entityTypeId, HttpServletRequest request,
			HttpServletResponse response)
	{
		Map<String, Object> paramMap = new HashMap<>();
		for (String param : request.getParameterMap().keySet())
		{
			String[] values = request.getParameterValues(param);
			String value = values != null ? StringUtils.join(values, ',') : null;
			if (StringUtils.isNotBlank(value))
			{
				paramMap.put(param, value);
			}
		}

		createInternal(entityTypeId, paramMap, response);
	}

	/**
	 * Creates a new entity from a html form post.
	 */
	@Transactional
	@PostMapping(value = "/{entityTypeId}", headers = "Content-Type=multipart/form-data")
	public void createFromFormPostMultiPart(@PathVariable("entityTypeId") String entityTypeId,
			MultipartHttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> paramMap = new HashMap<>();
		for (String param : request.getParameterMap().keySet())
		{
			String[] values = request.getParameterValues(param);
			String value = values != null ? StringUtils.join(values, ',') : null;
			if (StringUtils.isNotBlank(value))
			{
				paramMap.put(param, value);
			}
		}

		// add files to param map
		for (Entry<String, List<MultipartFile>> entry : request.getMultiFileMap().entrySet())
		{
			String param = entry.getKey();
			List<MultipartFile> files = entry.getValue();
			if (files != null && files.size() > 1)
			{
				throw new IllegalArgumentException("Multiple file input not supported");
			}
			paramMap.put(param, files != null && !files.isEmpty() ? files.get(0) : null);
		}
		createInternal(entityTypeId, paramMap, response);
	}

	@Transactional
	@PostMapping("/{entityTypeId}")
	public void create(@PathVariable("entityTypeId") String entityTypeId, @RequestBody Map<String, Object> entityMap,
			HttpServletResponse response)
	{
		if (entityMap == null)
		{
			throw new UnknownEntityException("Missing entity in body");
		}

		createInternal(entityTypeId, entityMap, response);
	}

	/**
	 * Updates an entity using PUT
	 * <p>
	 * Example url: /api/v1/person/99
	 */
	@Transactional
	@PutMapping("/{entityTypeId}/{id}")
	@ResponseStatus(OK)
	public void update(@PathVariable("entityTypeId") String entityTypeId, @PathVariable("id") String untypedId,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityTypeId, untypedId, entityMap);
	}

	/**
	 * Updates an entity by tunneling PUT through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=PUT
	 */
	@Transactional
	@PostMapping(value = "/{entityTypeId}/{id}", params = "_method=PUT")
	@ResponseStatus(OK)
	public void updatePost(@PathVariable("entityTypeId") String entityTypeId, @PathVariable("id") String untypedId,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityTypeId, untypedId, entityMap);
	}

	@PutMapping(value = "/{entityTypeId}/{id}/{attributeName}")
	@ResponseStatus(OK)
	public void updateAttributePut(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName, @PathVariable("id") String untypedId,
			@RequestBody Object paramValue)
	{
		updateAttribute(entityTypeId, attributeName, untypedId, paramValue);
	}

	// TODO alternative for synchronization, for example by adding updatAttribute methods to the REST api
	@PostMapping(value = "/{entityTypeId}/{id}/{attributeName}", params = "_method=PUT")
	@ResponseStatus(OK)
	public synchronized void updateAttribute(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("attributeName") String attributeName, @PathVariable("id") String untypedId,
			@RequestBody Object paramValue)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		if (entityType == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}

		Object id = getTypedValue(untypedId, entityType.getIdAttribute());

		Entity entity = dataService.findOneById(entityTypeId, id);
		if (entity == null)
		{
			throw new UnknownEntityException("Entity of type " + entityTypeId + " with id " + id + " not found");
		}

		Attribute attr = entityType.getAttribute(attributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityType, attributeName);
		}

		if (attr.isReadOnly())
		{
			throw new MolgenisDataAccessException(
					"Attribute '" + attributeName + "' of entity '" + entityTypeId + "' is readonly");
		}

		Object value = this.restService.toEntityValue(attr, paramValue, id);
		entity.set(attributeName, value);
		dataService.update(entityTypeId, entity);
	}

	/**
	 * Updates an entity from a html form post.
	 * <p>
	 * Tunnels PUT through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=PUT
	 */
	@Transactional
	@PostMapping(value = "/{entityTypeId}/{id}", params = "_method=PUT", headers = "Content-Type=multipart/form-data")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPostMultiPart(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId, MultipartHttpServletRequest request)
	{
		Map<String, Object> paramMap = new HashMap<>();
		for (String param : request.getParameterMap().keySet())
		{
			String[] values = request.getParameterValues(param);
			String value = values != null ? StringUtils.join(values, ',') : null;
			paramMap.put(param, value);
		}

		// add files to param map
		for (Entry<String, List<MultipartFile>> entry : request.getMultiFileMap().entrySet())
		{
			String param = entry.getKey();
			List<MultipartFile> files = entry.getValue();
			if (files != null && files.size() > 1)
			{
				throw new IllegalArgumentException("Multiple file input not supported");
			}
			paramMap.put(param, files != null && !files.isEmpty() ? files.get(0) : null);
		}
		updateInternal(entityTypeId, untypedId, paramMap);
	}

	/**
	 * Updates an entity from a html form post.
	 * <p>
	 * Tunnels PUT through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=PUT
	 */
	@Transactional
	@PostMapping(value = "/{entityTypeId}/{id}", params = "_method=PUT", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPost(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("id") String untypedId, HttpServletRequest request)
	{
		Map<String, Object> paramMap = new HashMap<>();
		for (String param : request.getParameterMap().keySet())
		{
			String[] values = request.getParameterValues(param);
			String value = values != null ? StringUtils.join(values, ',') : null;
			paramMap.put(param, value);
		}

		updateInternal(entityTypeId, untypedId, paramMap);
	}

	/**
	 * Deletes an entity by it's id
	 */
	@Transactional
	@DeleteMapping("/{entityTypeId}/{id}")
	@ResponseStatus(NO_CONTENT)
	public void delete(@PathVariable("entityTypeId") String entityTypeId, @PathVariable("id") String untypedId)
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
	 * Deletes an entity by it's id but tunnels DELETE through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=DELETE
	 */
	@PostMapping(value = "/{entityTypeId}/{id}", params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deletePost(@PathVariable("entityTypeId") String entityTypeId, @PathVariable("id") String untypedId)
	{
		delete(entityTypeId, untypedId);
	}

	/**
	 * Deletes all entities for the given entity name
	 */
	@DeleteMapping("/{entityTypeId}")
	@ResponseStatus(NO_CONTENT)
	public void deleteAll(@PathVariable("entityTypeId") String entityTypeId)
	{
		dataService.deleteAll(entityTypeId);
	}

	/**
	 * Deletes all entities for the given entity name but tunnels DELETE through POST
	 */
	@PostMapping(value = "/{entityTypeId}", params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deleteAllPost(@PathVariable("entityTypeId") String entityTypeId)
	{
		dataService.deleteAll(entityTypeId);
	}

	/**
	 * Deletes all entities and entity meta data for the given entity name
	 */
	@DeleteMapping(value = "/{entityTypeId}/meta")
	@ResponseStatus(NO_CONTENT)
	public void deleteMeta(@PathVariable("entityTypeId") String entityTypeId)
	{
		deleteMetaInternal(entityTypeId);
	}

	/**
	 * Deletes all entities and entity meta data for the given entity name but tunnels DELETE through POST
	 */
	@PostMapping(value = "/{entityTypeId}/meta", params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deleteMetaPost(@PathVariable("entityTypeId") String entityTypeId)
	{
		deleteMetaInternal(entityTypeId);
	}

	private void deleteMetaInternal(String entityTypeId)
	{
		dataService.getMeta().deleteEntityType(entityTypeId);
	}

	/**
	 * Login to the api.
	 * <p>
	 * Returns a json object with a token on correct login else throws an AuthenticationException. Clients can use this
	 * token when calling the api.
	 * <p>
	 * Example:
	 * <p>
	 * Request: {username:admin,password:xxx}
	 * <p>
	 * Response: {token: b4fd94dc-eae6-4d9a-a1b7-dd4525f2f75d}
	 */
	@PostMapping(value = "/login", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public LoginResponse login(@Valid @RequestBody LoginRequest login, HttpServletRequest request)
	{
		if (login == null)
		{
			throw new HttpMessageNotReadableException("Missing login");
		}
		if (isUser2fa())
		{
			throw new BadCredentialsException(
					"Login using /api/v1/login is disabled, two factor authentication is enabled");
		}

		return runAsSystem(() ->
		{
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login.getUsername(),
					login.getPassword());

			authToken.setDetails(new WebAuthenticationDetails(request));

			// Authenticate the login
			Authentication authentication = authenticationManager.authenticate(authToken);
			if (!authentication.isAuthenticated())
			{
				throw new BadCredentialsException("Unknown username or password");
			}

			User user = dataService.findOne(USER,
					new QueryImpl<User>().eq(UserMetaData.USERNAME, authentication.getName()), User.class);

			if (user.isChangePassword())
			{
				throw new BadCredentialsException(
						"Unable to log in because a password reset is required. Sign in to the website to reset your password.");
			}

			// User authenticated, log the user in
			SecurityContextHolder.getContext().setAuthentication(authentication);

			// Generate a new token for the user
			String token = tokenService.generateAndStoreToken(authentication.getName(), "REST API login");
			return new LoginResponse(token, user.getUsername(), user.getFirstName(), user.getLastName());
		});
	}

	private boolean isUser2fa()
	{
		return authenticationSettings.getTwoFactorAuthentication() == ENFORCED || (
				authenticationSettings.getTwoFactorAuthentication() == ENABLED && userAccountService.getCurrentUser()
																									.isTwoFactorAuthentication());
	}

	@PostMapping("/logout")
	@ResponseStatus(OK)
	public void logout(@TokenParam(required = true) String token, HttpServletRequest request)
	{
		tokenService.removeToken(token);
		SecurityContextHolder.getContext().setAuthentication(null);

		if (request.getSession(false) != null)
		{
			request.getSession().invalidate();
		}
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	public ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownTokenException.class)
	@ResponseStatus(NOT_FOUND)
	public ErrorMessageResponse handleUnknownTokenException(UnknownTokenException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseStatus(NOT_FOUND)
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	public ErrorMessageResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
	{
		LOG.debug("", e);

		List<ErrorMessage> messages = Lists.newArrayList();
		for (ObjectError error : e.getBindingResult().getAllErrors())
		{
			messages.add(new ErrorMessage(error.getDefaultMessage()));
		}

		return new ErrorMessageResponse(messages);
	}

	@ExceptionHandler(MolgenisValidationException.class)
	@ResponseStatus(BAD_REQUEST)
	public ErrorMessageResponse handleMolgenisValidationException(MolgenisValidationException e)
	{
		LOG.info("", e);

		List<ErrorMessage> messages = Lists.newArrayList();
		for (ConstraintViolation violation : e.getViolations())
		{
			messages.add(new ErrorMessage(violation.getMessage()));
		}

		return new ErrorMessageResponse(messages);
	}

	@ExceptionHandler(ConversionException.class)
	@ResponseStatus(BAD_REQUEST)
	public ErrorMessageResponse handleConversionException(ConversionException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(BAD_REQUEST)
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(UNAUTHORIZED)
	public ErrorMessageResponse handleAuthenticationException(AuthenticationException e)
	{
		LOG.info("", e);
		// workaround for https://github.com/molgenis/molgenis/issues/4441
		String message = e.getMessage();
		String messagePrefix = "org.springframework.security.core.userdetails.UsernameNotFoundException: ";
		if (message.startsWith(messagePrefix))
		{
			message = message.substring(messagePrefix.length());
		}
		return new ErrorMessageResponse(new ErrorMessage(message));
	}

	@ExceptionHandler(MolgenisDataAccessException.class)
	@ResponseStatus(UNAUTHORIZED)
	public ErrorMessageResponse handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisReferencedEntityException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleMolgenisReferencingEntityException(MolgenisReferencedEntityException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	private void updateInternal(String entityTypeId, String untypedId, Map<String, Object> entityMap)
	{
		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta.getIdAttribute() == null)
		{
			throw new IllegalArgumentException(entityTypeId + " does not have an id attribute");
		}
		Object id = getTypedValue(untypedId, meta.getIdAttribute());

		Entity existing = dataService.findOneById(entityTypeId, id, new Fetch().field(meta.getIdAttribute().getName()));
		if (existing == null)
		{
			throw new UnknownEntityException("Entity of type " + entityTypeId + " with id " + id + " not found");
		}

		Entity entity = this.restService.toEntity(meta, entityMap);

		dataService.update(entityTypeId, entity);
		restService.updateMappedByEntities(entity, existing);
	}

	private void createInternal(String entityTypeId, Map<String, Object> entityMap, HttpServletResponse response)
	{
		EntityType entityType = dataService.getEntityType(entityTypeId);
		Entity entity = this.restService.toEntity(entityType, entityMap);

		if (ATTRIBUTE_META_DATA.equals(entityTypeId))
		{
			dataService.getMeta().addAttribute(new Attribute(entity));
		}
		else
		{
			dataService.add(entityTypeId, entity);
		}

		restService.updateMappedByEntities(entity);

		Object id = entity.getIdValue();
		if (id != null)
		{
			response.addHeader("Location", Href.concatEntityHref(RestController.BASE_URI, entityTypeId, id));
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
	}

	private AttributeResponse getAttributePostInternal(String entityTypeId, String attributeName,
			Set<String> attributeSet, Map<String, Set<String>> attributeExpandSet)
	{
		Attribute attribute = null;
		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta != null)
		{
			attribute = meta.getAttribute(attributeName);
		}
		if (attribute != null)
		{
			return new AttributeResponse(entityTypeId, meta, attribute, attributeSet, attributeExpandSet,
					permissionService, dataService);
		}
		else
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
	}

	private Object retrieveEntityAttributeInternal(String entityTypeId, String untypedId, String refAttributeName,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandSet)
	{
		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
		Object id = getTypedValue(untypedId, meta.getIdAttribute());

		// Check if the entity has an attribute with name refAttributeName
		Attribute attr = meta.getAttribute(refAttributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(meta, refAttributeName);
		}

		// Get the entity
		Entity entity = dataService.findOneById(entityTypeId, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityTypeId + " " + id + " not found");
		}

		String attrHref = Href.concatAttributeHref(RestController.BASE_URI, meta.getId(), entity.getIdValue(),
				refAttributeName);
		switch (attr.getDataType())
		{
			case COMPOUND:
				Map<String, Object> entityHasAttributeMap = new LinkedHashMap<>();
				entityHasAttributeMap.put("href", attrHref);
				@SuppressWarnings("unchecked")
				Iterable<Attribute> attributeParts = (Iterable<Attribute>) entity.get(refAttributeName);
				for (Attribute attribute : attributeParts)
				{
					String attrName = attribute.getName();
					entityHasAttributeMap.put(attrName, entity.get(attrName));
				}
				return entityHasAttributeMap;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				List<Entity> mrefEntities = new ArrayList<>();
				for (Entity e : entity.getEntities((attr.getName())))
					mrefEntities.add(e);
				int count = mrefEntities.size();
				int toIndex = request.getStart() + request.getNum();
				mrefEntities = mrefEntities.subList(request.getStart(), toIndex > count ? count : toIndex);

				List<Map<String, Object>> refEntityMaps = new ArrayList<>();
				for (Entity refEntity : mrefEntities)
				{
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, attr.getRefEntity(), attributesSet,
							attributeExpandSet);
					refEntityMaps.add(refEntityMap);
				}

				EntityPager pager = new EntityPager(request.getStart(), request.getNum(), (long) count, mrefEntities);
				return new EntityCollectionResponse(pager, refEntityMaps, attrHref, null, permissionService,
						dataService);
			case CATEGORICAL:
			case XREF:
				Map<String, Object> entityXrefAttributeMap = getEntityAsMap((Entity) entity.get(refAttributeName),
						attr.getRefEntity(), attributesSet, attributeExpandSet);
				entityXrefAttributeMap.put("href", attrHref);
				return entityXrefAttributeMap;
			default:
				Map<String, Object> entityAttributeMap = new LinkedHashMap<>();
				entityAttributeMap.put("href", attrHref);
				entityAttributeMap.put(refAttributeName, entity.get(refAttributeName));
				return entityAttributeMap;
		}
	}

	// Handles a Query
	@SuppressWarnings("deprecation")
	private EntityCollectionResponse retrieveEntityCollectionInternal(String entityTypeId,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandsSet)
	{
		EntityType meta = dataService.getEntityType(entityTypeId);
		if (meta == null)
		{
			throw new UnknownEntityTypeException(entityTypeId);
		}
		Repository<Entity> repository = dataService.getRepository(entityTypeId);

		// convert sort
		Sort sort;
		SortV1 sortV1 = request.getSort();
		if (sortV1 != null)
		{
			sort = new Sort();
			for (SortV1.OrderV1 orderV1 : sortV1)
			{
				sort.on(orderV1.getProperty(),
						orderV1.getDirection() == SortV1.DirectionV1.ASC ? Sort.Direction.ASC : Sort.Direction.DESC);
			}
		}
		else
		{
			sort = null;
		}

		List<QueryRule> queryRules = request.getQ() == null ? Collections.emptyList() : request.getQ();
		Query<Entity> q = new QueryImpl<>(queryRules).pageSize(request.getNum()).offset(request.getStart()).sort(sort);

		Iterable<Entity> it = () -> dataService.findAll(entityTypeId, q).iterator();
		Long count = repository.count(new QueryImpl<>(q).setOffset(0).setPageSize(0));
		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

		List<Map<String, Object>> entities = new ArrayList<>();
		for (Entity entity : it)
		{
			entities.add(getEntityAsMap(entity, meta, attributesSet, attributeExpandsSet));
		}

		return new EntityCollectionResponse(pager, entities, BASE_URI + "/" + entityTypeId, meta, permissionService,
				dataService);
	}

	// Transforms an entity to a Map so it can be transformed to json
	private Map<String, Object> getEntityAsMap(Entity entity, EntityType meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<>();
		entityMap.put("href", Href.concatEntityHref(RestController.BASE_URI, meta.getId(), entity.getIdValue()));

		for (Attribute attr : meta.getAtomicAttributes())
		{
			// filter fields
			if (attributesSet != null && !attributesSet.contains(attr.getName().toLowerCase())) continue;

			String attrName = attr.getName();
			AttributeType attrType = attr.getDataType();

			if (attrType == COMPOUND)
			{
				if (attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
					entityMap.put(attrName,
							new AttributeResponse(meta.getId(), meta, attr, subAttributesSet, null, permissionService,
									dataService));
				}
				else
				{
					entityMap.put(attrName, Collections.singletonMap("href",
							Href.concatAttributeHref(RestController.BASE_URI, meta.getId(), entity.getIdValue(),
									attrName)));
				}
			}
			else if (attrType == DATE)
			{
				LocalDate date = entity.getLocalDate(attrName);
				entityMap.put(attrName, date != null ? date.toString() : null);
			}
			else if (attrType == DATE_TIME)
			{
				Instant date = entity.getInstant(attrName);
				entityMap.put(attrName, date != null ? date.toString() : null);
			}
			else if (attrType != XREF && attrType != CATEGORICAL && attrType != MREF && attrType != CATEGORICAL_MREF
					&& attrType != ONE_TO_MANY && attrType != FILE)
			{
				entityMap.put(attrName, entity.get(attr.getName()));
			}
			else if ((attrType == XREF || attrType == CATEGORICAL || attrType == FILE) && attributeExpandsSet != null
					&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
			{
				Entity refEntity = entity.getEntity(attr.getName());
				if (refEntity != null)
				{
					Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
					EntityType refEntityType = dataService.getEntityType(attr.getRefEntity().getId());
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityType, subAttributesSet, null);
					entityMap.put(attrName, refEntityMap);
				}
			}
			else if ((attrType == MREF || attrType == CATEGORICAL_MREF || attrType == ONE_TO_MANY)
					&& attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
			{
				EntityType refEntityType = dataService.getEntityType(attr.getRefEntity().getId());
				Iterable<Entity> mrefEntities = entity.getEntities(attr.getName());

				Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
				List<Map<String, Object>> refEntityMaps = new ArrayList<>();
				for (Entity refEntity : mrefEntities)
				{
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityType, subAttributesSet, null);
					refEntityMaps.add(refEntityMap);
				}

				EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
						(long) refEntityMaps.size(), mrefEntities);

				EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps,
						Href.concatAttributeHref(RestController.BASE_URI, meta.getId(), entity.getIdValue(), attrName),
						null, permissionService, dataService);

				entityMap.put(attrName, ecr);
			}
			else if ((attrType == XREF && entity.get(attr.getName()) != null) || (attrType == CATEGORICAL
					&& entity.get(attr.getName()) != null) || (attrType == FILE && entity.get(attr.getName()) != null)
					|| attrType == MREF || attrType == CATEGORICAL_MREF || attrType == ONE_TO_MANY)
			{
				// Add href to ref field
				Map<String, String> ref = new LinkedHashMap<>();
				ref.put("href",
						Href.concatAttributeHref(RestController.BASE_URI, meta.getId(), entity.getIdValue(), attrName));
				entityMap.put(attrName, ref);
			}
		}

		return entityMap;
	}

	/**
	 * @return set of lower case attribute names
	 */
	private Set<String> toAttributeSet(String[] attributes)
	{
		return attributes != null && attributes.length > 0 ? Sets.newHashSet(
				Iterables.transform(Arrays.asList(attributes), String::toLowerCase)) : null;
	}

	/**
	 * expand is of form 'attr1', 'entity1[attr1]', 'entity1[attr1;attr2]'
	 *
	 * @return map from lower case expand names to a attribute set
	 */
	private Map<String, Set<String>> toExpandMap(String[] expands)
	{
		if (expands != null)
		{
			Map<String, Set<String>> expandMap = new HashMap<>();
			for (String expand : expands)
			{
				// validate
				Matcher matcher = PATTERN_EXPANDS.matcher(expand);
				if (!matcher.matches()) throw new MolgenisDataException("invalid expand value: " + expand);

				// for partial expands, create set
				expand = matcher.group(1);
				String attrsStr = matcher.group(2);
				Set<String> attrSet;
				if (attrsStr != null && !attrsStr.isEmpty())
				{
					attrSet = new HashSet<>();
					for (String attr : attrsStr.split(";"))
					{
						attrSet.add(attr.toLowerCase());
					}
				}
				else attrSet = null;

				expandMap.put(expand.toLowerCase(), attrSet);
			}
			return expandMap;
		}
		return null;
	}
}
