package org.molgenis.data.rest;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.*;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.*;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.framework.db.EntityNotFoundException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.security.token.TokenService;
import org.molgenis.security.token.UnknownTokenException;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Rest endpoint for the DataService
 * 
 * Query, create, update and delete entities.
 * 
 * If a repository isn't capable of doing the requested operation an error is thrown.
 * 
 * Response is json.
 * 
 * @author erwin
 */
@Controller
@RequestMapping(BASE_URI)
public class RestController
{
	public static final String BASE_URI = "/api/v1";
	private static final Logger logger = Logger.getLogger(RestController.class);
	private static final Pattern PATTERN_EXPANDS = Pattern.compile("([^\\[^\\]]+)(?:\\[(.+)\\])?");
	private final DataService dataService;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public RestController(DataService dataService, TokenService tokenService,
			AuthenticationManager authenticationManager)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (tokenService == null) throw new IllegalArgumentException("tokenService is null");
		if (authenticationManager == null) throw new IllegalArgumentException("authenticationManager is null");
		this.dataService = dataService;
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
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
			dataService.getRepositoryByEntityName(entityName);
			return true;
		}
		catch (UnknownEntityException e)
		{
			return false;
		}
	}

	/**
	 * Gets the metadata for an entity
	 * 
	 * Example url: /api/v1/person/meta
	 * 
	 * @param entityName
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityMetaDataResponse getEntityMetaData(@PathVariable("entityName") String entityName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		return new EntityMetaDataResponse(meta, attributeSet, attributeExpandSet);
	}

	/**
	 * Example url: /api/v1/person/meta/emailaddresses
	 * 
	 * @param entityName
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeMetaDataResponse getAttributeMetaData(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		AttributeMetaData attributeMetaData = meta.getAttribute(attributeName);
		if (attributeMetaData != null)
		{
			return new AttributeMetaDataResponse(entityName, attributeMetaData, attributeSet, attributeExpandSet);
		}
		else
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	/**
	 * Get's an entity by it's id
	 * 
	 * Examples:
	 * 
	 * /api/v1/person/99 Retrieves a person with id 99
	 * 
	 * @param entityName
	 * @param id
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") Object id, @RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		Entity entity = dataService.findOne(entityName, id);

		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * 
	 * Example:
	 * 
	 * /api/v1/person/99/address
	 * 
	 * @param entityName
	 * @param id
	 * @param refAttributeName
	 * @param request
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}/{refAttributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object retrieveEntityAttribute(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@PathVariable("refAttributeName") String refAttributeName, @Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);

		// Check if the entity has an attribute with name refAttributeName
		AttributeMetaData attr = meta.getAttribute(refAttributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityName + " does not have an attribute named " + refAttributeName);
		}

		// Get the entity
		Entity entity = dataService.findOne(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		String attrHref = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), refAttributeName);
		switch (attr.getDataType().getEnumType())
		{
			case COMPOUND:
				Map<String, Object> entityHasAttributeMap = new LinkedHashMap<String, Object>();
				entityHasAttributeMap.put("href", attrHref);
				@SuppressWarnings("unchecked")
				Iterable<AttributeMetaData> attributeParts = (Iterable<AttributeMetaData>) entity.get(refAttributeName);
				for (AttributeMetaData attributeMetaData : attributeParts)
				{
					String attrName = attributeMetaData.getName();
					entityHasAttributeMap.put(attrName, entity.get(attrName));
				}
				return entityHasAttributeMap;
			case MREF:
				@SuppressWarnings("unchecked")
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
				return new EntityCollectionResponse(pager, refEntityMaps, attrHref);
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

	/**
	 * Do a query
	 * 
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
	 * 
	 * Example url: /api/v1/person?_method=GET
	 * 
	 * Returns json
	 * 
	 * @param request
	 * @param attributes
	 * @param attributeExpands
	 * @return
	 */
	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollectionPost(@PathVariable("entityName") String entityName,
			@Valid @RequestBody EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		request = request != null ? request : new EntityCollectionRequest();

		return retrieveEntityCollectionInternal(entityName, request, attributesSet, attributeExpandSet);
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
			String value = request.getParameter(param);
			if (StringUtils.isNotBlank(value))
			{
				paramMap.put(param, value);
			}
		}

		createInternal(entityName, paramMap, response);
	}

	@RequestMapping(value = "/{entityName}", method = POST)
	public void create(@PathVariable("entityName") String entityName, @RequestBody Map<String, Object> entityMap,
			HttpServletResponse response) throws EntityNotFoundException
	{
		if (entityMap == null)
		{
			throw new UnknownEntityException("Missing entity in body");
		}

		createInternal(entityName, entityMap, response);
	}

	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = PUT)
	@ResponseStatus(OK)
	public void update(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityName, id, entityMap);
	}

	/**
	 * Updates an entity by tunneling PUT through POST
	 * 
	 * Example url: /api/v1/person/99?_method=PUT
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT")
	@ResponseStatus(OK)
	public void updatePost(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityName, id, entityMap);
	}

	/**
	 * Updates an entity from a html form post.
	 * 
	 * Tunnels PUT through POST
	 * 
	 * Example url: /api/v1/person/99?_method=PUT
	 * 
	 * @param entityName
	 * @param id
	 * @param request
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPost(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			HttpServletRequest request)
	{
        Object typedId = dataService.getRepositoryByEntityName(entityName).getEntityMetaData().getIdAttribute().getDataType().convert(id);

        Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			paramMap.put(param, request.getParameter(param));
		}

		updateInternal(entityName, typedId, paramMap);
	}

	/**
	 * Deletes an entity by it's id
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void delete(@PathVariable("entityName") String entityName, @PathVariable Object id)
	{
        Object typedId = dataService.getRepositoryByEntityName(entityName).getEntityMetaData().getIdAttribute().getDataType().convert(id);
		dataService.delete(entityName, typedId);
	}

	/**
	 * Deletes an entity by it's id but tunnels DELETE through POST
	 * 
	 * Example url: /api/v1/person/99?_method=DELETE
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deletePost(@PathVariable("entityName") String entityName, @PathVariable Object id)
	{
		delete(entityName, id);
	}

	/**
	 * Login to the api.
	 * 
	 * Returns a json object with a token on correct login else throws an AuthenticationException. Clients can use this
	 * token when calling the api.
	 * 
	 * Example:
	 * 
	 * Request: {username:admin,password:xxx}
	 * 
	 * Response: {token: b4fd94dc-eae6-4d9a-a1b7-dd4525f2f75d}
	 * 
	 * @param login
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
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

		// User authenticated, log the user in
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate a new token for the user
		String token = tokenService.generateAndStoreToken(authentication.getName());

		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, authentication.getName()), MolgenisUser.class);

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
		request.getSession().invalidate();
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownTokenException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownTokenException(UnknownTokenException e)
	{
		logger.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		logger.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownAttributeException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownAttributeException(UnknownAttributeException e)
	{
		logger.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisValidationException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisValidationException(MolgenisValidationException e)
	{
		logger.info("", e);

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
		logger.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(UNAUTHORIZED)
	@ResponseBody
	public ErrorMessageResponse handleAuthenticationException(AuthenticationException e)
	{
		logger.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataAccessException.class)
	@ResponseStatus(UNAUTHORIZED)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
		logger.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	private void updateInternal(String entityName, Object id, Map<String, Object> entityMap)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		if (meta.getIdAttribute() == null)
		{
			throw new IllegalArgumentException(entityName + " does not have a id attribute");
		}

		Entity existing = dataService.findOne(entityName, id);
		if (existing == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " with id " + id + " not found");
		}

		Entity entity = toEntity(meta, entityMap);
		entity.set(meta.getIdAttribute().getName(), existing.getIdValue());

		dataService.update(entityName, entity);
	}

	private void createInternal(String entityName, Map<String, Object> entityMap, HttpServletResponse response)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);

		Entity entity = toEntity(meta, entityMap);

		dataService.add(entityName, entity);
        Object id = entity.getIdValue();
		if (id != null)
		{
			response.addHeader("Location", String.format(BASE_URI + "/%s/%s", entityName, id));
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
	}

	// Creates a new MapEntity based from a HttpServletRequest
	private Entity toEntity(EntityMetaData meta, Map<String, Object> request)
	{
		Entity entity = new MapEntity();
        if(meta.getIdAttribute() != null) entity = new MapEntity(meta.getIdAttribute().getName());

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			String paramName = attr.getName();
			Object paramValue = request.get(paramName);
			Object value = null;

			// Treat empty strings as null
			if ((paramValue != null) && (paramValue instanceof String) && StringUtils.isEmpty((String) paramValue))
			{
				paramValue = null;
			}

			if (paramValue != null)
			{
				if (attr.getDataType().getEnumType() == XREF)
				{
					Object id = paramValue;
					if (id != null)
					{
						value = dataService.findOne(attr.getRefEntity().getName(), id);
						if (value == null)
						{
							throw new IllegalArgumentException("No " + attr.getRefEntity().getName() + " with id "
									+ paramValue + " found");
						}
					}
				}
				else if (attr.getDataType().getEnumType() == MREF)
				{
					List<Object> ids = DataConverter.toObjectList(paramValue);
					if ((ids != null) && !ids.isEmpty())
					{
						Iterable<Entity> mrefs = dataService.findAll(attr.getRefEntity().getName(), ids);
						List<Entity> mrefList = Lists.newArrayList(mrefs);
						if (mrefList.size() != ids.size())
						{
							throw new IllegalArgumentException("Could not find all referencing ids for  "
									+ attr.getName());
						}

						value = mrefList;
					}
				}
				else
				{
					value = paramValue;
				}
			}

			entity.set(attr.getName(), value);
		}

		return entity;
	}

	// Handles a Query
	private EntityCollectionResponse retrieveEntityCollectionInternal(String entityName,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandsSet)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		Repository repository = dataService.getRepositoryByEntityName(entityName);

		// TODO non queryable
		List<QueryRule> queryRules = request.getQ() == null ? Collections.<QueryRule> emptyList() : request.getQ();
		Query q = new QueryImpl(queryRules).pageSize(request.getNum()).offset(request.getStart())
				.sort(request.getSort());

		Iterable<Entity> it = dataService.findAll(entityName, q);
		Long count = ((Queryable) repository).count(q);
		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

		List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		for (Entity entity : it)
		{
			entities.add(getEntityAsMap(entity, meta, attributesSet, attributeExpandsSet));
		}

		return new EntityCollectionResponse(pager, entities, BASE_URI + "/" + entityName);
	}

	// Transforms an entity to a Map so it can be transformed to json
	private Map<String, Object> getEntityAsMap(Entity entity, EntityMetaData meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();
		entityMap.put("href", String.format(BASE_URI + "/%s/%s", meta.getName(), entity.getIdValue()));

		// TODO system fields
		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			// filter fields
			if (attributesSet != null && !attributesSet.contains(attr.getName().toLowerCase())) continue;

			if (attr.isVisible() && !attr.getName().equals("__Type"))// TODO remove __Type from jpa entities
			{
				String attrName = attr.getName();
				FieldTypeEnum attrType = attr.getDataType().getEnumType();

				if (attrType == COMPOUND)
				{
					if (attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						entityMap.put(attrName, new AttributeMetaDataResponse(meta.getName(), attr, subAttributesSet,
								null));
					}
					else
					{
						String attrHref = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(),
								attrName);
						entityMap.put(attrName, Collections.singletonMap("href", attrHref));
					}
				}
				else if (attrType != XREF && attrType != CATEGORICAL && attrType != MREF)
				{
					entityMap.put(attrName, entity.get(attr.getName()));
				}
				else if ((attrType == XREF || attrType == CATEGORICAL) && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					Entity refEntity = entity.getEntity(attr.getName());
					if (refEntity != null)
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						entityMap.put(attrName, refEntityMap);
					}
				}
				else if (attrType == MREF && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
					Iterable<Entity> mrefEntities = entity.getEntities(attr.getName());

					Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
					List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
					for (Entity refEntity : mrefEntities)
					{
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						refEntityMaps.add(refEntityMap);
					}

					EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
							(long) refEntityMaps.size(), mrefEntities);

					String uri = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName);
					EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps, uri);
					entityMap.put(attrName, ecr);
				}
				else if ((attrType == XREF && entity.get(attr.getName()) != null)
						|| (attrType == CATEGORICAL && entity.get(attr.getName()) != null) || attrType == MREF)
				{
					// Add href to ref field
					Map<String, String> ref = new LinkedHashMap<String, String>();
					ref.put("href",
							String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName));
					entityMap.put(attrName, ref);
				}

			}

		}

		return entityMap;
	}

	/**
	 * 
	 * @param attributes
	 * @return set of lower case attribute names
	 */
	private Set<String> toAttributeSet(String[] attributes)
	{
		return attributes != null ? Sets.newHashSet(Iterables.transform(Arrays.asList(attributes),
				new Function<String, String>()
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
