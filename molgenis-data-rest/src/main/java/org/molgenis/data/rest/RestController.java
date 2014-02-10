package org.molgenis.data.rest;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.EntityNotFoundException;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Lists;

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
	private final DataService dataService;

	@Autowired
	public RestController(DataService dataService)
	{
		this.dataService = dataService;
	}

	/**
	 * Gets the metadata for an entity
	 * 
	 * Example url: /api/v1/person/meta
	 * 
	 * @param entityNameRaw
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityMetaData getMetaData(@PathVariable("entityName")
	String entityNameRaw)
	{
		String entityName = getEntityName(entityNameRaw);
		Repository repo = dataService.getRepositoryByEntityName(entityName);

		DefaultEntityMetaData meta = new DefaultEntityMetaData(repo.getName());
		meta.setDescription(repo.getDescription());
		meta.setLabel(repo.getLabel());

		for (AttributeMetaData attr : repo.getAttributes())
		{
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				DefaultAttributeMetaData copy = new DefaultAttributeMetaData(attr.getName(), attr.getDataType()
						.getEnumType());
				copy.setDefaultValue(attr.getDefaultValue());
				copy.setDescription(attr.getDescription());
				copy.setIdAttribute(attr.isIdAtrribute());
				copy.setLabel(attr.getLabel());
				copy.setLabelAttribute(attr.isLabelAttribute());
				copy.setNillable(attr.isNillable());
				copy.setReadOnly(attr.isReadonly());

				if (attr.getRefEntity() != null)
				{
					copy.setRefEntity(attr.getRefEntity());
				}

				copy.setVisible(attr.isVisible());

				meta.addAttributeMetaData(copy);
			}
		}

		return meta;
	}

	/**
	 * TODO JJ 
	 * Gets the metadata for an entity
	 * 
	 * Example url: /api/v1/person/meta
	 * 
	 * @param entityNameRaw
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta/tree", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityMetaData getMetaDataTree(@PathVariable("entityName") String inputEntityName)
	{
		String entityName = getEntityName(inputEntityName);
		Repository repo = dataService.getRepositoryByEntityName(entityName);
		
		//TODO JJ REMOVE
		logger.info("TODO REMOVE DEBUG INFO!!! entityNameRaw: " + entityName);

		DefaultEntityMetaData meta = new DefaultEntityMetaData(repo.getName());
		meta.setDescription(repo.getDescription());
		meta.setLabel(repo.getLabel());

		for (AttributeMetaData attr : repo.getLevelOneAttributes())
		{
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				DefaultAttributeMetaData copy = new DefaultAttributeMetaData(attr.getName(), attr.getDataType()
						.getEnumType());
				copy.setDefaultValue(attr.getDefaultValue());
				copy.setDescription(attr.getDescription());
				copy.setIdAttribute(attr.isIdAtrribute());
				copy.setLabel(attr.getLabel());
				copy.setLabelAttribute(attr.isLabelAttribute());
				copy.setNillable(attr.isNillable());
				copy.setReadOnly(attr.isReadonly());

				if (attr.getRefEntity() != null)
				{
					copy.setRefEntity(attr.getRefEntity());
				}

				copy.setVisible(attr.isVisible());

				meta.addAttributeMetaData(copy);
			}
		}
		return meta;

	}

	/**
	 * Get's an entity by it's id
	 * 
	 * Examples:
	 * 
	 * /api/v1/person/99 Retrieves a person with id 99
	 * 
	 * 
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @param expandFields
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieve(@PathVariable("entityName")
	String entityNameRaw, @PathVariable("id")
	Integer id, @RequestParam(value = "expand", required = false)
	String... expandFields)
	{
		String entityName = getEntityName(entityNameRaw);// Be backwards compatible
		EntityMetaData meta = dataService.getRepositoryByEntityName(entityName);
		Entity entity = dataService.findOne(entityName, id);

		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		Set<String> expandFieldSet = expandFields == null ? Collections.<String> emptySet() : new HashSet<String>(
				Arrays.asList(expandFields));

		return getEntityAsMap(entity, meta, expandFieldSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * 
	 * Example:
	 * 
	 * /api/v1/person/99/address
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @param refAttributeName
	 * @param request
	 * @param expandFields
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}/{refAttributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object retrieveRef(@PathVariable("entityName")
	String entityNameRaw, @PathVariable("id")
	Integer id, @PathVariable("refAttributeName")
	String refAttributeName, @Valid
	EntityCollectionRequest request, @RequestParam(value = "expand", required = false)
	String... expandFields)
	{
		String entityName = getEntityName(entityNameRaw);// Be backwards compatible
		Set<String> expandFieldSet = expandFields == null ? Collections.<String> emptySet() : new HashSet<String>(
				Arrays.asList(expandFields));
		EntityMetaData meta = dataService.getRepositoryByEntityName(entityName);

		// Check if the entity has an attribute with name refAttributeName
		AttributeMetaData attr = meta.getAttribute(refAttributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityName + " does not have an attribute named " + refAttributeName);
		}

		// Check if it is of type XREF or MREF
		if (attr.getRefEntity() == null)
		{
			throw new UnknownAttributeException(refAttributeName + " of entity " + entityName
					+ " is not of type XREF or MREF.");
		}

		// Get the entity
		Entity entity = dataService.findOne(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		// If it's XREF return the referencing entity
		if (attr.getDataType().getEnumType() == XREF)
		{
			return getEntityAsMap((Entity) entity.get(refAttributeName), attr.getRefEntity(), expandFieldSet);
		}

		// It's an MREF
		@SuppressWarnings("unchecked")
		List<Entity> mrefEntities = (List<Entity>) entity.get(attr.getName());
		int count = mrefEntities.size();
		int toIndex = request.getStart() + request.getNum();
		mrefEntities = mrefEntities.subList(request.getStart(), toIndex > count ? count : toIndex);

		List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
		for (Entity refEntity : mrefEntities)
		{
			Map<String, Object> refEntityMap = getEntityAsMap(refEntity, attr.getRefEntity(), expandFieldSet);
			refEntityMaps.add(refEntityMap);
		}

		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), (long) count, mrefEntities);

		String uri = String.format(BASE_URI + "/%s/%s/%s", meta.getName().toLowerCase(), entity.getIdValue(),
				refAttributeName);
		EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps, uri);

		return ecr;
	}

	/**
	 * Do a query
	 * 
	 * Returns json
	 * 
	 * @param entityNameRaw
	 * @param request
	 * @param expandFields
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollection(
			@PathVariable("entityName") String entityNameRaw, 
			@Valid EntityCollectionRequest request, 
			@RequestParam(value = "expand", required = false) String... expandFields)
	{
		String entityName = getEntityName(entityNameRaw);// Be backwards compatible
		Set<String> expandFieldSet = expandFields == null ? Collections.<String> emptySet() : new HashSet<String>(
				Arrays.asList(expandFields));

		return retrieveEntityCollectionInternal(entityName, request, expandFieldSet);
	}

	/**
	 * Same as retrieveEntityCollection (GET) only tunneled through POST.
	 * 
	 * Example url: /api/v1/person?_method=GET
	 * 
	 * Returns json
	 * 
	 * @param entityNameRaw
	 * @param request
	 * @param expandFields
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollectionPost(@PathVariable("entityName")
	String entityNameRaw, @Valid
	@RequestBody
	EntityCollectionRequest request, @RequestParam(value = "expand", required = false)
	String... expandFields)
	{
		String entityName = getEntityName(entityNameRaw);// Be backwards compatible
		Set<String> expandFieldSet = expandFields == null ? Collections.<String> emptySet() : new HashSet<String>(
				Arrays.asList(expandFields));
		request = request != null ? request : new EntityCollectionRequest();

		return retrieveEntityCollectionInternal(entityName, request, expandFieldSet);
	}

	/**
	 * Creates a new entity from a html form post.
	 * 
	 * @param entityNameRaw
	 * @param request
	 * @param response
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = POST, headers = "Content-Type=application/x-www-form-urlencoded")
	public void createFromFormPost(@PathVariable("entityName")
	String entityNameRaw, HttpServletRequest request, HttpServletResponse response)
	{
		String entityName = getEntityName(entityNameRaw);

		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			paramMap.put(param, request.getParameter(param));
		}

		createInternal(entityName, paramMap, response);
	}

	@RequestMapping(value = "/{entityName}", method = POST)
	public void create(@PathVariable("entityName")
	String entityNameRaw, @RequestBody
	Map<String, Object> entityMap, HttpServletResponse response) throws EntityNotFoundException
	{
		if (entityMap == null)
		{
			throw new UnknownEntityException("Missing entity in body");
		}

		String entityName = getEntityName(entityNameRaw);
		createInternal(entityName, entityMap, response);
	}

	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = PUT)
	@ResponseStatus(OK)
	public void update(@PathVariable("entityName")
	String entityNameRaw, @PathVariable("id")
	Integer id, @RequestBody
	Map<String, Object> entityMap)
	{
		String entityName = getEntityName(entityNameRaw);
		updateInternal(entityName, id, entityMap);
	}

	/**
	 * Updates an entity by tunneling PUT through POST
	 * 
	 * Example url: /api/v1/person/99?_method=PUT
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT")
	@ResponseStatus(OK)
	public void updatePost(@PathVariable("entityName")
	String entityNameRaw, @PathVariable("id")
	Integer id, @RequestBody
	Map<String, Object> entityMap)
	{
		String entityName = getEntityName(entityNameRaw);
		updateInternal(entityName, id, entityMap);
	}

	/**
	 * Updates an entity from a html form post.
	 * 
	 * Tunnels PUT through POST
	 * 
	 * Example url: /api/v1/person/99?_method=PUT
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @param request
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPost(@PathVariable("entityName")
	String entityNameRaw, @PathVariable("id")
	Integer id, HttpServletRequest request)
	{
		String entityName = getEntityName(entityNameRaw);

		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			paramMap.put(param, request.getParameter(param));
		}

		updateInternal(entityName, id, paramMap);
	}

	/**
	 * Deletes an entity by it's id
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void delete(@PathVariable("entityName")
	String entityNameRaw, @PathVariable
	Integer id)
	{
		String entityName = getEntityName(entityNameRaw);
		dataService.delete(entityName, id);
	}

	/**
	 * Deletes an entity by it's id but tunnels DELETE through POST
	 * 
	 * Example url: /api/v1/person/99?_method=DELETE
	 * 
	 * @param entityNameRaw
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deletePost(@PathVariable("entityName")
	String entityName, @PathVariable
	Integer id)
	{
		delete(entityName, id);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		logger.error("", e);
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

	@ExceptionHandler(ConversionException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleConversionException(ConversionException e)
	{
		logger.error("", e);
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

	private void updateInternal(String entityName, Integer id, Map<String, Object> entityMap)
	{
		EntityMetaData meta = dataService.getRepositoryByEntityName(entityName);
		if (!(meta instanceof Updateable))
		{
			throw new IllegalArgumentException(entityName + " is not updateable");
		}

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
		EntityMetaData meta = dataService.getRepositoryByEntityName(entityName);
		if (!(meta instanceof Writable))
		{
			throw new IllegalArgumentException(entityName + " is not writeable");
		}

		Entity entity = toEntity(meta, entityMap);

		Integer id = dataService.add(entityName, entity);
		if (id != null)
		{
			response.addHeader("Location", String.format(BASE_URI + "/%s/%d", entityName.toLowerCase(), id));
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
	}

	// Creates a new MapEntity based from a HttpServletRequest
	private Entity toEntity(EntityMetaData meta, Map<String, Object> request)
	{
		Entity entity = new MapEntity();

		for (AttributeMetaData attr : meta.getAttributes())
		{
			String paramName = StringUtils.uncapitalize(attr.getName());
			Object paramValue = request.get(paramName);
			Object value = null;

			if (paramValue != null)
			{
				if (attr.getDataType().getEnumType() == XREF)
				{
					Integer id = DataConverter.toInt(paramValue);
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
					List<Integer> ids = DataConverter.toIntList(paramValue);
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
			EntityCollectionRequest request, Set<String> expandFields)
	{
		EntityMetaData meta = dataService.getRepositoryByEntityName(entityName);

		// TODO non queryable
		List<QueryRule> queryRules = request.getQ() == null ? Collections.<QueryRule> emptyList() : request.getQ();
		Query q = new QueryImpl(resolveRefIdentifiers(queryRules, meta)).pageSize(request.getNum()).offset(
				request.getStart());

		Iterable<Entity> it = dataService.findAll(entityName, q);
		Long count = ((Queryable) meta).count(q);
		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

		List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		for (Entity entity : it)
		{
			entities.add(getEntityAsMap(entity, meta, expandFields));
		}

		return new EntityCollectionResponse(pager, entities, BASE_URI + "/" + entityName);
	}

	// Transforms an entity to a Map so it can be transformed to json
	private Map<String, Object> getEntityAsMap(Entity entity, EntityMetaData meta, Set<String> expandFields)
	{
		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();
		entityMap.put("href", String.format(BASE_URI + "/%s/%s", meta.getName().toLowerCase(), entity.getIdValue()));

		// TODO system fields
		for (AttributeMetaData attr : meta.getAttributes())
		{
			if (attr.isVisible() && !attr.getName().equals("__Type"))// TODO remove __Type from jpa entities
			{
				String attrName = getAttributeName(attr);
				FieldTypeEnum attrType = attr.getDataType().getEnumType();

				if (attrType != XREF && attrType != MREF)
				{
					entityMap.put(attrName, entity.get(attr.getName()));
				}
				else if (attrType == XREF && expandFields.contains(attrName))
				{
					Entity refEntity = (Entity) entity.get(attr.getName());

					if (refEntity != null)
					{
						EntityMetaData refEntityMetaData = dataService.getRepositoryByEntityName(attr.getRefEntity()
								.getName());
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								Collections.<String> emptySet());
						entityMap.put(attrName, refEntityMap);
					}
				}
				else if (attrType == MREF && expandFields.contains(attrName))
				{
					EntityMetaData refEntityMetaData = dataService.getRepositoryByEntityName(attr.getRefEntity()
							.getName());

					@SuppressWarnings("unchecked")
					Iterable<Entity> mrefEntities = (Iterable<Entity>) entity.get(attr.getName());

					List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
					for (Entity refEntity : mrefEntities)
					{
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								Collections.<String> emptySet());
						refEntityMaps.add(refEntityMap);
					}

					EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
							(long) refEntityMaps.size(), mrefEntities);

					String uri = String.format(BASE_URI + "/%s/%s/%s", meta.getName().toLowerCase(),
							entity.getIdValue(), attrName);
					EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps, uri);
					entityMap.put(attrName, ecr);
				}
				else if ((attrType == XREF && entity.get(attr.getName()) != null) || attrType == MREF)
				{
					// Add href to ref field
					Map<String, String> ref = new LinkedHashMap<String, String>();
					ref.put("href", String.format(BASE_URI + "/%s/%s/%s", meta.getName().toLowerCase(),
							entity.getIdValue(), attrName));
					entityMap.put(attrName, ref);
				}

			}

		}

		return entityMap;
	}

	// In the old EntityControllers all attribute names started with a lowercase, be compatable with this
	private String getAttributeName(AttributeMetaData attr)
	{
		return StringUtils.uncapitalize(attr.getName());
	}

	// In the old EntityControllers entitynames where lowercase, be compatable with this
	private String getEntityName(String inputEntityName) throws UnknownEntityException
	{
		for (String entityName : dataService.getEntityNames())
		{
			if (entityName.equalsIgnoreCase(inputEntityName))
			{
				return entityName;
			}
		}

		throw new UnknownEntityException("Unknown entity " + inputEntityName);
	}

	// Handle a bit of lagacy, handle query like 'SELECT FROM Category WHERE observableFeature_Identifier=xxx'
	// Resolve xref ids.
	// TODO Do this in a cleaner way and support more operators, Move to util class or remove this completely?
	private List<QueryRule> resolveRefIdentifiers(List<QueryRule> rules, EntityMetaData meta)
	{
		for (QueryRule r : rules)
		{
			if (r.getField() != null)
			{
				if (r.getField().endsWith("_Identifier"))
				{
					String entityName = StringUtils.capitalize(r.getField().substring(0,
							r.getField().length() - "_Identifier".length()));
					r.setField(entityName);

					Object value = dataService.findOne(entityName, new QueryImpl().eq("Identifier", r.getValue()));
					r.setValue(value);
				}
				else
				{
					// Resolve xref, mref fields
					AttributeMetaData attr = meta.getAttribute(r.getField());

					if (attr.getDataType().getEnumType() == MolgenisFieldTypes.FieldTypeEnum.XREF)
					{
						if (r.getOperator() == Operator.IN)
						{
							Iterable<?> values = dataService.findAll(
									attr.getRefEntity().getName(),
									new QueryImpl().in(attr.getRefEntity().getIdAttribute().getName(),
											(Iterable<?>) r.getValue()));
							r.setValue(Lists.newArrayList(values));
						}
						else
						{
							Object value = dataService.findOne(attr.getRefEntity().getName(),
									new QueryImpl().eq(attr.getRefEntity().getIdAttribute().getName(), r.getValue()));
							r.setValue(value);
						}
					}
				}
			}

		}

		return rules;
	}
}
