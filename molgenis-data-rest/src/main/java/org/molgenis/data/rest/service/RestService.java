package org.molgenis.data.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;

@Service
public class RestService
{
	private final DataService dataService;
	private final IdGenerator idGenerator;
	private final FileStore fileStore;
	private final FileMetaFactory fileMetaFactory;
	private final EntityManager entityManager;

	@Autowired
	public RestService(DataService dataService, IdGenerator idGenerator, FileStore fileStore,
			FileMetaFactory fileMetaFactory, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.fileStore = requireNonNull(fileStore);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.entityManager = requireNonNull(entityManager);
	}

	/**
	 * Creates a new entity based from a HttpServletRequest. For file attributes persists the file in the file store
	 * and persist a file meta data entity.
	 *
	 * @param meta    entity meta data
	 * @param request HTTP request parameters
	 * @return entity created from HTTP request parameters
	 */
	public Entity toEntity(final EntityMetaData meta, final Map<String, Object> request)
	{
		final Entity entity = entityManager.create(meta);

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			if (attr.getExpression() == null)
			{
				String paramName = attr.getName();
				final Object paramValue = request.get(paramName);
				final Object value = this.toEntityValue(attr, paramValue);
				entity.set(attr.getName(), value);
			}
		}

		return entity;
	}

	/**
	 * Converts a HTTP request parameter to a entity value of which the type is defined by the attribute. For file
	 * attributes persists the file in the file store and persist a file meta data entity.
	 *
	 * @param attr       attribute
	 * @param paramValue HTTP parameter value
	 * @return Object
	 */
	public Object toEntityValue(AttributeMetaData attr, Object paramValue)
	{
		// Treat empty strings as null
		if (paramValue != null && (paramValue instanceof String) && ((String) paramValue).isEmpty())
		{
			paramValue = null;
		}

		Object value;
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
				value = convertBool(attr, paramValue);
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = convertString(attr, paramValue);
				break;
			case CATEGORICAL:
			case XREF:
				value = convertRef(attr, paramValue);
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				value = convertMref(attr, paramValue);
				break;
			case DATE:
				value = convertDate(attr, paramValue);
				break;
			case DATE_TIME:
				value = convertDateTime(attr, paramValue);
				break;
			case DECIMAL:
				value = convertDecimal(attr, paramValue);
				break;
			case FILE:
				value = convertFile(attr, paramValue);
				break;
			case INT:
				value = convertInt(attr, paramValue);
				break;
			case LONG:
				value = convertLong(attr, paramValue);
				break;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
		return value;
	}

	private static Long convertLong(AttributeMetaData attr, Object paramValue)
	{
		Long value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Long.valueOf((String) paramValue);
			}
			// javascript number converted to double
			else if (paramValue instanceof Number)
			{
				value = ((Number) paramValue).longValue();
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Number.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Integer convertInt(AttributeMetaData attr, Object paramValue)
	{
		Integer value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Integer.valueOf((String) paramValue);
			}
			// javascript number converted to double
			else if ((paramValue instanceof Number))
			{
				value = ((Number) paramValue).intValue();
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Number.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private FileMeta convertFile(AttributeMetaData attr, Object paramValue)
	{
		FileMeta value;
		if (paramValue != null)
		{
			if (!(paramValue instanceof MultipartFile))
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), MultipartFile.class.getSimpleName()));
			}
			MultipartFile multipartFile = (MultipartFile) paramValue;

			String id = idGenerator.generateId();
			try
			{
				fileStore.store(multipartFile.getInputStream(), id);
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(e);
			}

			FileMeta fileEntity = fileMetaFactory.create(id);
			fileEntity.setFilename(multipartFile.getOriginalFilename());
			fileEntity.setContentType(multipartFile.getContentType());
			fileEntity.setSize(multipartFile.getSize());
			fileEntity.setUrl(ServletUriComponentsBuilder.fromCurrentRequest()
					.replacePath(FileDownloadController.URI + '/' + id).replaceQuery(null).build().toUriString());
			dataService.add(FILE_META, fileEntity);

			value = fileEntity;
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Double convertDecimal(AttributeMetaData attr, Object paramValue)
	{
		Double value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Double.valueOf((String) paramValue);
			}
			// javascript number converted to double
			else if (paramValue instanceof Number)
			{
				value = ((Number) paramValue).doubleValue();
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Number.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Date convertDateTime(AttributeMetaData attr, Object paramValue)
	{
		Date value;
		if (paramValue != null)
		{
			if (paramValue instanceof Date)
			{
				value = (Date) paramValue;
			}
			else if (paramValue instanceof String)
			{
				String paramStrValue = (String) paramValue;
				try
				{
					value = getDateTimeFormat().parse(paramStrValue);
				}
				catch (ParseException e)
				{
					throw new MolgenisDataException(
							format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(),
									paramStrValue, MolgenisDateFormat.DATEFORMAT_DATETIME));
				}
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Date.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Date convertDate(AttributeMetaData attr, Object paramValue)
	{
		Date value;
		if (paramValue != null)
		{
			if (paramValue instanceof Date)
			{
				value = (Date) paramValue;
			}
			else if (paramValue instanceof String)
			{
				String paramStrValue = (String) paramValue;
				try
				{
					value = getDateFormat().parse(paramStrValue);
				}
				catch (ParseException e)
				{
					throw new MolgenisDataException(
							format("Attribute [%s] value [%s] does not match date format [%s]", attr.getName(),
									paramStrValue, MolgenisDateFormat.DATEFORMAT_DATE));
				}
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private List<?> convertMref(AttributeMetaData attr, Object paramValue)
	{
		List<?> value;
		if (paramValue != null)
		{
			List<?> mrefParamValues;
			if (paramValue instanceof String)
			{
				mrefParamValues = asList(StringUtils.split((String) paramValue, ','));
			}
			else if (paramValue instanceof List<?>)
			{
				mrefParamValues = (List<?>) paramValue;
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								List.class.getSimpleName()));
			}

			EntityMetaData mrefEntity = attr.getRefEntity();
			AttributeMetaData mrefEntityIdAttr = mrefEntity.getIdAttribute();
			value = mrefParamValues.stream().map(mrefParamValue -> toEntityValue(mrefEntityIdAttr, mrefParamValue))
					.map(mrefIdValue -> entityManager.getReference(mrefEntity, mrefIdValue)).collect(toList());
		}
		else
		{
			value = emptyList();
		}
		return value;
	}

	private Object convertRef(AttributeMetaData attr, Object paramValue)
	{
		Object value;
		if (paramValue != null)
		{
			Object idValue = toEntityValue(attr.getRefEntity().getIdAttribute(), paramValue);
			value = entityManager.getReference(attr.getRefEntity(), idValue);
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static String convertString(AttributeMetaData attr, Object paramValue)
	{
		String value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = (String) paramValue;
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName()));
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Boolean convertBool(AttributeMetaData attr, Object paramValue)
	{
		Boolean value;
		if (paramValue != null)
		{
			if (paramValue instanceof String)
			{
				value = Boolean.valueOf((String) paramValue);
			}
			else if (paramValue instanceof Boolean)
			{
				value = (Boolean) paramValue;
			}
			else
			{
				throw new MolgenisDataException(
						format("Attribute [%s] value is of type [%s] instead of [%s] or [%s]", attr.getName(),
								paramValue.getClass().getSimpleName(), String.class.getSimpleName(),
								Boolean.class.getSimpleName()));
			}
		}
		else
		{
			// boolean false is not posted (http feature), so if null and required, should be false
			value = !attr.isNillable() ? false : null;
		}
		return value;
	}
}
