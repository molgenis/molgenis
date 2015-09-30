package org.molgenis.data.rest.v2;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.rest.Href;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileMeta;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

;

@Controller
@RequestMapping(BASE_URI)
class RestControllerV2
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerV2.class);
	private static final int MAX_ENTITIES = 1000;

	public static final String BASE_URI = "/api/v2";

	private final DataService dataService;
	private final RestService restService;
	private final MolgenisPermissionService permissionService;
	private final IdGenerator idGenerator;

	@Autowired
	public RestControllerV2(DataService dataService, MolgenisPermissionService permissionService,
			RestService restService, IdGenerator idGenerator)
	{
		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
		this.restService = requireNonNull(restService);
		this.idGenerator = requireNonNull(idGenerator);
	}

	/**
	 * Retrieve an entity instance by id, optionally specify which attributes to include in the response.
	 * 
	 * @param entityName
	 * @param id
	 * @param attributeFilter
	 * @return
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = GET)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") Object id,
			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter)
	{
		Entity entity = dataService.findOne(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " [" + id + "] not found");
		}

		return createEntityResponse(entity, attributeFilter, true);
	}

	@RequestMapping(value = "/{entityName}/{id:.+}", method = POST, params = "_method=GET")
	@ResponseBody
	public Map<String, Object> retrieveEntityPost(@PathVariable("entityName") String entityName,
			@PathVariable("id") Object id,
			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter)
	{
		Entity entity = dataService.findOne(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " [" + id + "] not found");
		}

		return createEntityResponse(entity, attributeFilter, true);
	}

	@RequestMapping(value = "/{entityName}/{id:.+}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void deleteEntity(@PathVariable("entityName") String entityName, @PathVariable("id") Object id)
	{
		dataService.delete(entityName, id);
	}

	/**
	 * Retrieve an entity collection, optionally specify which attributes to include in the response.
	 * 
	 * @param entityName
	 * @param request
	 * @param attributes
	 * @return
	 */
	@RequestMapping(value = "/{entityName}", method = GET)
	@ResponseBody
	public EntityCollectionResponseV2 retrieveEntityCollection(@PathVariable("entityName") String entityName,
			@Valid EntityCollectionRequestV2 request)
	{
		return createEntityCollectionResponse(entityName, request);
	}

	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET")
	@ResponseBody
	public EntityCollectionResponseV2 retrieveEntityCollectionPost(@PathVariable("entityName") String entityName,
			@Valid EntityCollectionRequestV2 request)
	{
		return createEntityCollectionResponse(entityName, request);
	}

	/**
	 * Try to create multiple entities in one transaction. If one fails all fails.
	 * 
	 * @param entityName
	 *            name of the entity where the entities are going to be added.
	 * @param request
	 *            EntityCollectionCreateRequestV2
	 * @param response
	 *            HttpServletResponse
	 * @return EntityCollectionCreateResponseBodyV2
	 */
	@RequestMapping(value = "/{entityName}", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionBatchResponseBodyV2 createEntities(@PathVariable("entityName") String entityName,
			@RequestBody EntityCollectionBatchRequestV2 request, HttpServletResponse response)
	{
		// TODO
		// Example:
		// http://localhost:8080/api/v2/org_molgenis_test_Person?q=id=in=(AAAACUAZVIJSS6V2QL34UMAAAE,AAAACUAZVJM4E6V2QL34UMAAAE)

		if (request == null)
		{
			throw new UnknownEntityException("Missing entities to create in body");
		}

		if (request.getEntities().size() > MAX_ENTITIES)
		{
			throw new UnknownEntityException("Max " + MAX_ENTITIES + " are allowed");
		}

		final EntityMetaData meta = dataService.getEntityMetaData(entityName);
		final List<Entity> entities = new ArrayList<Entity>();
		final EntityCollectionBatchResponseBodyV2 responseBody = new EntityCollectionBatchResponseBodyV2();
		final List<String> ids = new ArrayList<String>();
		for (Map<String, Object> entity : request.getEntities())
		{
			final Entity e = this.restService.toEntity(meta, entity);
			Object id = e.getIdValue();

			if (null == id)
			{
				boolean isAutoId = meta.getIdAttribute().isAuto();
				if (isAutoId)
				{
					id = idGenerator.generateId();
				}
				else
				{
					throw new MolgenisDataException("The entity: [" + entity
							+ "] is missing an id and therefore cannot be created");
				}
			}

			ids.add(id.toString());
			entities.add(e);
			responseBody.getResources().add(
					new ResourcesResponseV2(id, Href.concatEntityHref(RestControllerV2.BASE_URI, entityName, id)));
		}

		try
		{
			this.dataService.add(entityName, entities);
			response.addHeader("Location", Href.concatEntityCollectionHref(RestControllerV2.BASE_URI, entityName, meta
					.getIdAttribute().getName(), ids));
			response.setStatus(HttpServletResponse.SC_OK);
			return responseBody;
		}
		catch (MolgenisDataAccessException mdae)
		{
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
			return responseBody;
		}

	}

	/**
	 * Try to update multiple entities in one transaction. If one fails all fails.
	 * 
	 * @param entityName
	 *            name of the entity where the entities are going to be added.
	 * @param request
	 *            EntityCollectionCreateRequestV2
	 * @param response
	 *            HttpServletResponse
	 * @return EntityCollectionCreateResponseBodyV2
	 */
	@SuppressWarnings("finally")
	@RequestMapping(value = "/{entityName}", method = PUT, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionBatchResponseBodyV2 updateEntities(@PathVariable("entityName") String entityName,
			@RequestBody EntityCollectionBatchRequestV2 request, HttpServletResponse response)
	{
		if (request == null)
		{
			throw new UnknownEntityException("Missing entities to update in body");
		}

		if (request.getEntities().size() > MAX_ENTITIES)
		{
			throw new UnknownEntityException("Max " + MAX_ENTITIES + " are allowed");
		}

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		List<Entity> entities = new ArrayList<Entity>();
		EntityCollectionBatchResponseBodyV2 responseBody = new EntityCollectionBatchResponseBodyV2();
		List<String> ids = new ArrayList<String>();
		for (Map<String, Object> entity : request.getEntities())
		{
			Entity e = this.restService.toEntity(meta, entity);
			Object id = e.getIdValue();
			if (null == id)
			{
				throw new MolgenisDataException("The entity: [" + entity
						+ "] is missing an id and therefore cannot be updated");
			}
			ids.add(id.toString());
			entities.add(e);
			responseBody.getResources().add(
					new ResourcesResponseV2(id, Href.concatEntityHref(RestControllerV2.BASE_URI, entityName, id)));
		}

		try
		{
			this.dataService.update(entityName, entities);
			response.addHeader("Location", Href.concatEntityCollectionHref(RestControllerV2.BASE_URI, entityName, meta
					.getIdAttribute().getName(), ids));
			response.setStatus(HttpServletResponse.SC_OK);
		}
		catch (MolgenisDataAccessException mdae)
		{
			response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
		}
		finally
		{
			return responseBody;
		}

	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	private EntityCollectionResponseV2 createEntityCollectionResponse(String entityName,
			EntityCollectionRequestV2 request)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);

		Query q = request.getQ() != null ? request.getQ().createQuery(meta) : new QueryImpl();
		q.pageSize(request.getNum()).offset(request.getStart()).sort(request.getSort());

		Iterable<Entity> it = dataService.findAll(entityName, q);
		Long count = dataService.count(entityName, q);
		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

		AttributeFilter attributeFilter = request.getAttrs();
		List<Map<String, Object>> entities = new ArrayList<>();
		for (Entity entity : it)
		{
			Map<String, Object> responseData = new LinkedHashMap<String, Object>();
			createEntityValuesResponse(entity, attributeFilter, responseData);
			entities.add(responseData);
		}

		return new EntityCollectionResponseV2(pager, entities, attributeFilter, BASE_URI + '/' + entityName, meta,
				permissionService);
	}

	private Map<String, Object> createEntityResponse(Entity entity, AttributeFilter attrFilter, boolean includeMetaData)
	{
		Map<String, Object> responseData = new LinkedHashMap<String, Object>();
		if (includeMetaData)
		{
			createEntityMetaResponse(entity.getEntityMetaData(), attrFilter, responseData);
		}
		createEntityValuesResponse(entity, attrFilter, responseData);
		return responseData;
	}

	private void createEntityMetaResponse(EntityMetaData entityMetaData, AttributeFilter attrFilter,
			Map<String, Object> responseData)
	{
		responseData.put("_meta", new EntityMetaDataResponseV2(entityMetaData, attrFilter, permissionService));
	}

	private void createEntityValuesResponse(Entity entity, AttributeFilter attrFilter, Map<String, Object> responseData)
	{
		Iterable<AttributeMetaData> attrs = entity.getEntityMetaData().getAttributes();
		attrFilter = attrFilter != null ? attrFilter : AttributeFilter.ALL_ATTRS_FILTER;
		createEntityValuesResponseRec(entity, attrs, attrFilter, responseData);
	}

	private void createEntityValuesResponseRec(Entity entity, Iterable<AttributeMetaData> attrs,
			AttributeFilter attrFilter, Map<String, Object> responseData)
	{
		responseData.put("_href",
				Href.concatEntityHref(BASE_URI, entity.getEntityMetaData().getName(), entity.getIdValue()));
		for (AttributeMetaData attr : attrs)
		{
			String attrName = attr.getName();
			if (attrFilter.includeAttribute(attr))
			{
				FieldTypeEnum dataType = attr.getDataType().getEnumType();
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
							AttributeFilter refAttrFilter = attrFilter.getAttributeFilter(attr);
							if (refAttrFilter == null)
							{
								refAttrFilter = createDefaultRefAttributeFilter(attr);
							}
							refEntityResponse = createEntityResponse(refEntity, refAttrFilter, false);
						}
						else
						{
							refEntityResponse = null;
						}
						responseData.put(attrName, refEntityResponse);
						break;
					case CATEGORICAL_MREF:
					case MREF:
						Iterable<Entity> refEntities = entity.getEntities(attrName);
						List<Map<String, Object>> refEntityResponses;
						if (refEntities != null)
						{
							refEntityResponses = new ArrayList<Map<String, Object>>();
							AttributeFilter refAttrFilter = attrFilter.getAttributeFilter(attr);
							if (refAttrFilter == null)
							{
								refAttrFilter = createDefaultRefAttributeFilter(attr);
							}
							for (Entity refEntitiesEntity : refEntities)
							{
								refEntityResponses.add(createEntityResponse(refEntitiesEntity, refAttrFilter, false));
							}
						}
						else
						{
							refEntityResponses = null;
						}
						responseData.put(attrName, refEntityResponses);
						break;
					case COMPOUND:
						Iterable<AttributeMetaData> attrParts = attr.getAttributeParts();
						AttributeFilter compoundAttrFilter = new AttributeFilter();
						for (AttributeMetaData attrPart : attrParts)
						{
							compoundAttrFilter.add(attrPart.getName());
						}
						createEntityValuesResponseRec(entity, attrParts, compoundAttrFilter, responseData);
						break;
					case DATE:
						Date dateValue = entity.getDate(attrName);
						String dateValueStr = dateValue != null ? getDateFormat().format(dateValue) : null;
						responseData.put(attrName, dateValueStr);
						break;
					case DATE_TIME:
						Date dateTimeValue = entity.getDate(attrName);
						String dateTimeValueStr = dateTimeValue != null ? getDateTimeFormat().format(dateTimeValue) : null;
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
					case IMAGE:
						throw new UnsupportedOperationException("Unsupported data type [" + dataType + "]");
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

	static AttributeFilter createDefaultRefAttributeFilter(AttributeMetaData attr)
	{
		EntityMetaData refEntityMeta = attr.getRefEntity();
		String idAttrName = refEntityMeta.getIdAttribute().getName();
		String labelAttrName = refEntityMeta.getLabelAttribute().getName();
		AttributeFilter attrFilter = new AttributeFilter().add(idAttrName).add(labelAttrName);
		if (attr.getDataType().getEnumType() == FieldTypeEnum.FILE)
		{
			attrFilter.add(FileMeta.URL);
		}
		return attrFilter;
	}
}
