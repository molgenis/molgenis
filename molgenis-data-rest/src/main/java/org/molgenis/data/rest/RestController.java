package org.molgenis.data.rest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.*;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.DefaultEntityCollection;
import org.molgenis.data.support.Href;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.token.UnknownTokenException;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.molgenis.util.MolgenisDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.stereotype.Controller;
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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.molgenis.util.EntityUtils.getTypedValue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

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
@Controller
@RequestMapping(BASE_URI)
public class RestController
{
	private static final Logger LOG = LoggerFactory.getLogger(RestController.class);

	static final String BASE_URI = "/api/v1";
	private static final Pattern PATTERN_EXPANDS = Pattern.compile("([^\\[^\\]]+)(?:\\[(.+)\\])?");
	private final DataService dataService;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final MolgenisPermissionService molgenisPermissionService;
	private final MolgenisRSQL molgenisRSQL;
	private final RestService restService;
	private final LanguageService languageService;

	@Autowired
	public RestController(DataService dataService, TokenService tokenService,
			AuthenticationManager authenticationManager, MolgenisPermissionService molgenisPermissionService,
			MolgenisRSQL molgenisRSQL, RestService restService, LanguageService languageService)
	{
		this.dataService = requireNonNull(dataService);
		this.tokenService = requireNonNull(tokenService);
		this.authenticationManager = requireNonNull(authenticationManager);
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.molgenisRSQL = requireNonNull(molgenisRSQL);
		this.restService = requireNonNull(restService);
		this.languageService = requireNonNull(languageService);
	}

	/**
	 * Checks if an entity exists.
	 */
	@RequestMapping(value = "/{entityName}/exist", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public boolean entityExists(@PathVariable("entityName") String entityName)
	{
		try
		{
			dataService.getRepository(entityName);
			return true;
		}
		catch (UnknownEntityException e)
		{
			return false;
		}
	}

	/**
	 * Gets the metadata for an entity
	 * <p>
	 * Example url: /api/v1/person/meta
	 *
	 * @param entityName
	 * @return EntityType
	 */
	@RequestMapping(value = "/{entityName}/meta", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityTypeResponse retrieveEntityType(@PathVariable("entityName") String entityName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityType meta = dataService.getEntityType(entityName);
		return new EntityTypeResponse(meta, attributeSet, attributeExpandSet, molgenisPermissionService, dataService,
				languageService);
	}

	/**
	 * Same as retrieveEntityType (GET) only tunneled through POST.
	 * <p>
	 * Example url: /api/v1/person/meta?_method=GET
	 *
	 * @param entityName
	 * @return EntityType
	 */
	@RequestMapping(value = "/{entityName}/meta", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityTypeResponse retrieveEntityTypePost(@PathVariable("entityName") String entityName,
			@Valid @RequestBody EntityTypeRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		EntityType meta = dataService.getEntityType(entityName);
		return new EntityTypeResponse(meta, attributesSet, attributeExpandSet, molgenisPermissionService, dataService,
				languageService);
	}

	/**
	 * Example url: /api/v1/person/meta/emailaddresses
	 *
	 * @param entityName
	 * @return EntityType
	 */
	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeResponse retrieveEntityAttributeMeta(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return getAttributePostInternal(entityName, attributeName, attributeSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityAttributeMeta (GET) only tunneled through POST.
	 *
	 * @param entityName
	 * @return EntityType
	 */
	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeResponse retrieveEntityAttributeMetaPost(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName, @Valid @RequestBody EntityTypeRequest request)
	{
		Set<String> attributeSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		return getAttributePostInternal(entityName, attributeName, attributeSet, attributeExpandSet);
	}

	/**
	 * Get's an entity by it's id
	 * <p>
	 * Examples:
	 * <p>
	 * /api/v1/person/99 Retrieves a person with id 99
	 *
	 * @param entityName
	 * @param untypedId
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityType meta = dataService.getEntityType(entityName);
		if (meta == null)
		{
			throw new UnknownEntityException(entityName + " not found");
		}
		Object id = getTypedValue(untypedId, meta.getIdAttribute());
		Entity entity = dataService.findOneById(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + untypedId + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntity (GET) only tunneled through POST.
	 *
	 * @param entityName
	 * @param untypedId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId, @Valid @RequestBody EntityTypeRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		EntityType meta = dataService.getEntityType(entityName);
		Object id = getTypedValue(untypedId, meta.getIdAttribute());
		Entity entity = dataService.findOneById(entityName, id);

		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + untypedId + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * <p>
	 * Example:
	 * <p>
	 * /api/v1/person/99/address
	 *
	 * @param entityName
	 * @param untypedId
	 * @param refAttributeName
	 * @param request
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}/{refAttributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object retrieveEntityAttribute(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId, @PathVariable("refAttributeName") String refAttributeName,
			@Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return retrieveEntityAttributeInternal(entityName, untypedId, refAttributeName, request, attributesSet,
				attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * <p>
	 * Example:
	 * <p>
	 * /api/v1/person/99/address
	 *
	 * @param entityName
	 * @param untypedId
	 * @param refAttributeName
	 * @param request
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}/{refAttributeName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object retrieveEntityAttributePost(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId, @PathVariable("refAttributeName") String refAttributeName,
			@Valid @RequestBody EntityCollectionRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request.getAttributes());
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request.getExpand());

		return retrieveEntityAttributeInternal(entityName, untypedId, refAttributeName, request, attributesSet,
				attributeExpandSet);
	}

	/**
	 * Do a query
	 * <p>
	 * Returns json
	 *
	 * @param entityName
	 * @param request
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollection(@PathVariable("entityName") String entityName,
			@Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return retrieveEntityCollectionInternal(entityName, request, attributesSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityCollection (GET) only tunneled through POST.
	 * <p>
	 * Example url: /api/v1/person?_method=GET
	 * <p>
	 * Returns json
	 *
	 * @param request
	 * @param entityName
	 * @return
	 */
	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollectionPost(@PathVariable("entityName") String entityName,
			@Valid @RequestBody EntityCollectionRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request.getAttributes());
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request.getExpand());

		return retrieveEntityCollectionInternal(entityName, request, attributesSet, attributeExpandSet);
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
	 *
	 * @param entityName
	 * @param attributes
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/csv/{entityName}", method = GET, produces = "text/csv")
	@ResponseBody
	public EntityCollection retrieveEntityCollection(@PathVariable("entityName") String entityName,
			@RequestParam(value = "attributes", required = false) String[] attributes, HttpServletRequest req,
			HttpServletResponse resp) throws IOException
	{
		final Set<String> attributesSet = toAttributeSet(attributes);

		EntityType meta;
		Iterable<Entity> entities;
		try
		{
			meta = dataService.getEntityType(entityName);
			Query<Entity> q = new QueryStringParser(meta, molgenisRSQL).parseQueryString(req.getParameterMap());

			String[] sortAttributeArray = req.getParameterMap().get("sortColumn");
			if (sortAttributeArray != null && sortAttributeArray.length == 1 && StringUtils
					.isNotEmpty(sortAttributeArray[0]))
			{
				String sortAttribute = sortAttributeArray[0];
				String sortOrderArray[] = req.getParameterMap().get("sortOrder");
				Sort.Direction order = Sort.Direction.ASC;

				if (sortOrderArray != null && sortOrderArray.length == 1 && StringUtils.isNotEmpty(sortOrderArray[0]))
				{
					String sortOrder = sortOrderArray[0];
					if (sortOrder.equals("ASC"))
					{
						order = Sort.Direction.ASC;
					}
					else if (sortOrder.equals("DESC"))
					{
						order = Sort.Direction.DESC;
					}
					else
					{
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

			entities = () -> dataService.findAll(entityName, q).iterator();
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
		Iterable<String> attributesIterable = Iterables
				.transform(meta.getAtomicAttributes(), attribute -> attribute.getName().toLowerCase());

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
			attributesIterable = Iterables
					.filter(attributesIterable, attribute -> attributesSet.contains(attribute.toLowerCase()));
		}

		return new DefaultEntityCollection(entities, attributesIterable);
	}

	/**
	 * Creates a new entity from a html form post.
	 *
	 * @param entityName
	 * @param request
	 * @param response
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = POST, headers = "Content-Type=application/x-www-form-urlencoded")
	public void createFromFormPost(@PathVariable("entityName") String entityName, HttpServletRequest request,
			HttpServletResponse response)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			String[] values = request.getParameterValues(param);
			String value = values != null ? StringUtils.join(values, ',') : null;
			if (StringUtils.isNotBlank(value))
			{
				paramMap.put(param, value);
			}
		}

		createInternal(entityName, paramMap, response);
	}

	/**
	 * Creates a new entity from a html form post.
	 *
	 * @param entityName
	 * @param request
	 * @param response
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = POST, headers = "Content-Type=multipart/form-data")
	public void createFromFormPostMultiPart(@PathVariable("entityName") String entityName,
			MultipartHttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
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
		createInternal(entityName, paramMap, response);
	}

	@RequestMapping(value = "/{entityName}", method = POST)
	public void create(@PathVariable("entityName") String entityName, @RequestBody Map<String, Object> entityMap,
			HttpServletResponse response)
	{
		if (entityMap == null)
		{
			throw new UnknownEntityException("Missing entity in body");
		}

		createInternal(entityName, entityMap, response);
	}

	/**
	 * Updates an entity using PUT
	 * <p>
	 * Example url: /api/v1/person/99
	 *
	 * @param entityName
	 * @param untypedId
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = PUT)
	@ResponseStatus(OK)
	public void update(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityName, untypedId, entityMap);
	}

	/**
	 * Updates an entity by tunneling PUT through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=PUT
	 *
	 * @param entityName
	 * @param untypedId
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT")
	@ResponseStatus(OK)
	public void updatePost(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityName, untypedId, entityMap);
	}

	@RequestMapping(value = "/{entityName}/{id}/{attributeName}", method = PUT)
	@ResponseStatus(OK)
	public void updateAttributePut(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName, @PathVariable("id") String untypedId,
			@RequestBody Object paramValue)
	{
		updateAttribute(entityName, attributeName, untypedId, paramValue);
	}

	// TODO alternative for synchronization, for example by adding updatAttribute methods to the REST api
	@RequestMapping(value = "/{entityName}/{id}/{attributeName}", method = POST, params = "_method=PUT")
	@ResponseStatus(OK)
	public synchronized void updateAttribute(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName, @PathVariable("id") String untypedId,
			@RequestBody Object paramValue)
	{
		EntityType entityType = dataService.getEntityType(entityName);
		if (entityType == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " not found");
		}

		Object id = getTypedValue(untypedId, entityType.getIdAttribute());

		Entity entity = dataService.findOneById(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " with id " + id + " not found");
		}

		Attribute attr = entityType.getAttribute(attributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(
					"Attribute '" + attributeName + "' of entity '" + entityName + "' does not exist");
		}

		if (attr.isReadOnly())
		{
			throw new MolgenisDataAccessException(
					"Attribute '" + attributeName + "' of entity '" + entityName + "' is readonly");
		}

		Object value = this.restService.toEntityValue(attr, paramValue);
		entity.set(attributeName, value);
		dataService.update(entityName, entity);
	}

	/**
	 * Updates an entity from a html form post.
	 * <p>
	 * Tunnels PUT through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=PUT
	 *
	 * @param entityName
	 * @param untypedId
	 * @param request
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT", headers = "Content-Type=multipart/form-data")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPostMultiPart(@PathVariable("entityName") String entityName,
			@PathVariable("id") String untypedId, MultipartHttpServletRequest request)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
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
		updateInternal(entityName, untypedId, paramMap);
	}

	/**
	 * Updates an entity from a html form post.
	 * <p>
	 * Tunnels PUT through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=PUT
	 *
	 * @param entityName
	 * @param untypedId
	 * @param request
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPost(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId,
			HttpServletRequest request)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			String[] values = request.getParameterValues(param);
			String value = values != null ? StringUtils.join(values, ',') : null;
			paramMap.put(param, value);
		}

		updateInternal(entityName, untypedId, paramMap);
	}

	/**
	 * Deletes an entity by it's id
	 *
	 * @param entityName
	 * @param untypedId
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void delete(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId)
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
	 * Deletes an entity by it's id but tunnels DELETE through POST
	 * <p>
	 * Example url: /api/v1/person/99?_method=DELETE
	 *
	 * @param entityName
	 * @param untypedId
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deletePost(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId)
	{
		delete(entityName, untypedId);
	}

	/**
	 * Deletes all entities for the given entity name
	 *
	 * @param entityName
	 */
	@RequestMapping(value = "/{entityName}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void deleteAll(@PathVariable("entityName") String entityName)
	{
		dataService.deleteAll(entityName);
	}

	/**
	 * Deletes all entities for the given entity name but tunnels DELETE through POST
	 *
	 * @param entityName
	 */
	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deleteAllPost(@PathVariable("entityName") String entityName)
	{
		dataService.deleteAll(entityName);
	}

	/**
	 * Deletes all entities and entity meta data for the given entity name
	 *
	 * @param entityName
	 */
	@RequestMapping(value = "/{entityName}/meta", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void deleteMeta(@PathVariable("entityName") String entityName)
	{
		deleteMetaInternal(entityName);
	}

	/**
	 * Deletes all entities and entity meta data for the given entity name but tunnels DELETE through POST
	 *
	 * @param entityName
	 */
	@RequestMapping(value = "/{entityName}/meta", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deleteMetaPost(@PathVariable("entityName") String entityName)
	{
		deleteMetaInternal(entityName);
	}

	private void deleteMetaInternal(String entityName)
	{
		dataService.getMeta().deleteEntityType(entityName);
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
	 *
	 * @param login
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	@RunAsSystem
	public LoginResponse login(@Valid @RequestBody LoginRequest login, HttpServletRequest request)
	{
		if (login == null)
		{
			throw new HttpMessageNotReadableException("Missing login");
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login.getUsername(),
				login.getPassword());
		authToken.setDetails(new WebAuthenticationDetails(request));

		// Authenticate the login
		Authentication authentication = authenticationManager.authenticate(authToken);
		if (!authentication.isAuthenticated())
		{
			throw new BadCredentialsException("Unknown username or password");
		}

		User user = dataService
				.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, authentication.getName()), User.class);

		// User authenticated, log the user in
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate a new token for the user
		String token = tokenService.generateAndStoreToken(authentication.getName(), "Rest api login");

		return new LoginResponse(token, user.getUsername(), user.getFirstName(), user.getLastName());
	}

	@RequestMapping("/logout")
	@ResponseStatus(OK)
	public void logout(HttpServletRequest request)
	{
		String token = TokenExtractor.getToken(request);
		if (token == null)
		{
			throw new HttpMessageNotReadableException("Missing token in header");
		}

		tokenService.removeToken(token);
		SecurityContextHolder.getContext().setAuthentication(null);

		if (request.getSession(false) != null)
		{
			request.getSession().invalidate();
		}
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownTokenException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownTokenException(UnknownTokenException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownAttributeException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownAttributeException(UnknownAttributeException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
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
	@ResponseBody
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
	@ResponseBody
	public ErrorMessageResponse handleConversionException(ConversionException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(UNAUTHORIZED)
	@ResponseBody
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
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisReferencedEntityException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisReferencingEntityException(MolgenisReferencedEntityException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@Transactional
	private void updateInternal(String entityName, String untypedId, Map<String, Object> entityMap)
	{
		EntityType meta = dataService.getEntityType(entityName);
		if (meta.getIdAttribute() == null)
		{
			throw new IllegalArgumentException(entityName + " does not have an id attribute");
		}
		Object id = getTypedValue(untypedId, meta.getIdAttribute());

		Entity existing = dataService.findOneById(entityName, id, new Fetch().field(meta.getIdAttribute().getName()));
		if (existing == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " with id " + id + " not found");
		}

		Entity entity = this.restService.toEntity(meta, entityMap);

		dataService.update(entityName, entity);
		restService.updateMappedByEntities(entity, existing);
	}

	@Transactional
	private void createInternal(String entityName, Map<String, Object> entityMap, HttpServletResponse response)
	{
		EntityType entityType = dataService.getEntityType(entityName);
		Entity entity = this.restService.toEntity(entityType, entityMap);

		if (ATTRIBUTE_META_DATA.equals(entityName))
		{
			dataService.getMeta().addAttribute(new Attribute(entity));
		}
		else
		{
			dataService.add(entityName, entity);
		}

		restService.updateMappedByEntities(entity);

		Object id = entity.getIdValue();
		if (id != null)
		{
			response.addHeader("Location", Href.concatEntityHref(RestController.BASE_URI, entityName, id));
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
	}

	private AttributeResponse getAttributePostInternal(String entityName, String attributeName,
			Set<String> attributeSet, Map<String, Set<String>> attributeExpandSet)
	{
		EntityType meta = dataService.getEntityType(entityName);
		Attribute attribute = meta.getAttribute(attributeName);
		if (attribute != null)
		{
			return new AttributeResponse(entityName, meta, attribute, attributeSet, attributeExpandSet,
					molgenisPermissionService, dataService, languageService);
		}
		else
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	private Object retrieveEntityAttributeInternal(String entityName, String untypedId, String refAttributeName,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandSet)
	{
		EntityType meta = dataService.getEntityType(entityName);
		Object id = getTypedValue(untypedId, meta.getIdAttribute());

		// Check if the entity has an attribute with name refAttributeName
		Attribute attr = meta.getAttribute(refAttributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityName + " does not have an attribute named " + refAttributeName);
		}

		// Get the entity
		Entity entity = dataService.findOneById(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		String attrHref = Href
				.concatAttributeHref(RestController.BASE_URI, meta.getName(), entity.getIdValue(), refAttributeName);
		switch (attr.getDataType())
		{
			case COMPOUND:
				Map<String, Object> entityHasAttributeMap = new LinkedHashMap<String, Object>();
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
				List<Entity> mrefEntities = new ArrayList<Entity>();
				for (Entity e : entity.getEntities((attr.getName())))
					mrefEntities.add(e);
				int count = mrefEntities.size();
				int toIndex = request.getStart() + request.getNum();
				mrefEntities = mrefEntities.subList(request.getStart(), toIndex > count ? count : toIndex);

				List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
				for (Entity refEntity : mrefEntities)
				{
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, attr.getRefEntity(), attributesSet,
							attributeExpandSet);
					refEntityMaps.add(refEntityMap);
				}

				EntityPager pager = new EntityPager(request.getStart(), request.getNum(), (long) count, mrefEntities);
				return new EntityCollectionResponse(pager, refEntityMaps, attrHref, null, molgenisPermissionService,
						dataService, languageService);
			case CATEGORICAL:
			case XREF:
				Map<String, Object> entityXrefAttributeMap = getEntityAsMap((Entity) entity.get(refAttributeName),
						attr.getRefEntity(), attributesSet, attributeExpandSet);
				entityXrefAttributeMap.put("href", attrHref);
				return entityXrefAttributeMap;
			default:
				Map<String, Object> entityAttributeMap = new LinkedHashMap<String, Object>();
				entityAttributeMap.put("href", attrHref);
				entityAttributeMap.put(refAttributeName, entity.get(refAttributeName));
				return entityAttributeMap;
		}
	}

	// Handles a Query
	private EntityCollectionResponse retrieveEntityCollectionInternal(String entityName,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandsSet)
	{
		EntityType meta = dataService.getEntityType(entityName);
		Repository<Entity> repository = dataService.getRepository(entityName);

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

		List<QueryRule> queryRules = request.getQ() == null ? Collections.<QueryRule>emptyList() : request.getQ();
		Query<Entity> q = new QueryImpl<>(queryRules).pageSize(request.getNum()).offset(request.getStart()).sort(sort);

		Iterable<Entity> it = () -> dataService.findAll(entityName, q).iterator();
		Long count = repository.count(q);
		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

		List<Map<String, Object>> entities = new ArrayList<>();
		for (Entity entity : it)
		{
			entities.add(getEntityAsMap(entity, meta, attributesSet, attributeExpandsSet));
		}

		return new EntityCollectionResponse(pager, entities, BASE_URI + "/" + entityName, meta,
				molgenisPermissionService, dataService, languageService);
	}

	// Transforms an entity to a Map so it can be transformed to json
	private Map<String, Object> getEntityAsMap(Entity entity, EntityType meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();
		entityMap.put("href", Href.concatEntityHref(RestController.BASE_URI, meta.getName(), entity.getIdValue()));

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
					entityMap.put(attrName, new AttributeResponse(meta.getName(), meta, attr, subAttributesSet, null,
							molgenisPermissionService, dataService, languageService));
				}
				else
				{
					entityMap.put(attrName, Collections.singletonMap("href",
							Href.concatAttributeHref(RestController.BASE_URI, meta.getName(), entity.getIdValue(),
									attrName)));
				}
			}
			else if (attrType == DATE)
			{
				Date date = entity.getDate(attrName);
				entityMap.put(attrName,
						date != null ? MolgenisDateFormat.getDateFormat().format(date) : null);
			}
			else if (attrType == DATE_TIME)
			{
				Date date = entity.getDate(attrName);
				entityMap.put(attrName, date != null ? MolgenisDateFormat.getDateTimeFormat().format(date) : null);
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
					EntityType refEntityType = dataService.getEntityType(attr.getRefEntity().getName());
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityType, subAttributesSet, null);
					entityMap.put(attrName, refEntityMap);
				}
			}
			else if ((attrType == MREF || attrType == CATEGORICAL_MREF || attrType == ONE_TO_MANY)
					&& attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
			{
				EntityType refEntityType = dataService.getEntityType(attr.getRefEntity().getName());
				Iterable<Entity> mrefEntities = entity.getEntities(attr.getName());

				Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
				List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
				for (Entity refEntity : mrefEntities)
				{
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityType, subAttributesSet, null);
					refEntityMaps.add(refEntityMap);
				}

				EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
						(long) refEntityMaps.size(), mrefEntities);

				EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps,
						Href.concatAttributeHref(RestController.BASE_URI, meta.getName(), entity.getIdValue(),
								attrName), null, molgenisPermissionService, dataService, languageService);

				entityMap.put(attrName, ecr);
			}
			else if ((attrType == XREF && entity.get(attr.getName()) != null) || (attrType == CATEGORICAL
					&& entity.get(attr.getName()) != null) || (attrType == FILE && entity.get(attr.getName()) != null)
					|| attrType == MREF || attrType == CATEGORICAL_MREF || attrType == ONE_TO_MANY)
			{
				// Add href to ref field
				Map<String, String> ref = new LinkedHashMap<String, String>();
				ref.put("href", Href.concatAttributeHref(RestController.BASE_URI, meta.getName(), entity.getIdValue(),
						attrName));
				entityMap.put(attrName, ref);
			}
		}

		return entityMap;
	}

	/**
	 * @param attributes
	 * @return set of lower case attribute names
	 */
	private Set<String> toAttributeSet(String[] attributes)
	{
		return attributes != null && attributes.length > 0 ? Sets.newHashSet(
				Iterables.transform(Arrays.asList(attributes), new com.google.common.base.Function<String, String>()
				{
					@Override
					public String apply(String attribute)
					{
						return attribute.toLowerCase();
					}
				})) : null;
	}

	/**
	 * expand is of form 'attr1', 'entity1[attr1]', 'entity1[attr1;attr2]'
	 *
	 * @param expands
	 * @return map from lower case expand names to a attribute set
	 */
	private Map<String, Set<String>> toExpandMap(String[] expands)
	{
		if (expands != null)
		{
			Map<String, Set<String>> expandMap = new HashMap<String, Set<String>>();
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
					attrSet = new HashSet<String>();
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
