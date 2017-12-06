package org.molgenis.data.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.rest.exception.EntityAlreadyReferencedException;
import org.molgenis.data.rest.exception.FileAttributeUpdateWithoutFileException;
import org.molgenis.data.rest.exception.IllegalAttributeTypeException;
import org.molgenis.data.rest.exception.IncompatibleValueTypeException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.meta.AttributeValidationResult;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.validation.meta.AttributeConstraint.MAPPED_BY_TYPE;
import static org.molgenis.file.model.FileMetaMetaData.FILENAME;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;

@Service
public class RestService
{
	private final DataService dataService;
	private final IdGenerator idGenerator;
	private final FileStore fileStore;
	private final FileMetaFactory fileMetaFactory;
	private final EntityManager entityManager;
	private final ServletUriComponentsBuilderFactory servletUriComponentsBuilderFactory;

	public RestService(DataService dataService, IdGenerator idGenerator, FileStore fileStore,
			FileMetaFactory fileMetaFactory, EntityManager entityManager,
			ServletUriComponentsBuilderFactory servletUriComponentsBuilderFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.fileStore = requireNonNull(fileStore);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.entityManager = requireNonNull(entityManager);
		this.servletUriComponentsBuilderFactory = requireNonNull(servletUriComponentsBuilderFactory);
	}

	/**
	 * Creates a new entity based from a HttpServletRequest. For file attributes persists the file in the file store
	 * and persist a file meta data entity.
	 *
	 * @param meta    entity meta data
	 * @param request HTTP request parameters
	 * @return entity created from HTTP request parameters
	 */
	public Entity toEntity(final EntityType meta, final Map<String, Object> request)
	{
		final Entity entity = entityManager.create(meta, POPULATE);

		for (Attribute attr : meta.getAtomicAttributes())
		{
			if (attr.getExpression() == null)
			{
				String paramName = attr.getName();
				if (request.containsKey(paramName))
				{
					final Object paramValue = request.get(paramName);
					Attribute idAttribute = meta.getIdAttribute();
					Object idValue = request.get(idAttribute.getName());
					final Object value = this.toEntityValue(attr, paramValue, idValue);
					entity.set(attr.getName(), value);
				}
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
	public Object toEntityValue(Attribute attr, Object paramValue, Object id)
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
				value = convertFile(attr, paramValue, id);
				break;
			case INT:
				value = convertInt(attr, paramValue);
				break;
			case LONG:
				value = convertLong(attr, paramValue);
				break;
			case COMPOUND:
				throw new IllegalAttributeTypeException(attrType);
			default:
				throw new UnexpectedEnumException(attrType);
		}
		return value;
	}

	private static Long convertLong(Attribute attr, Object paramValue)
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
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), Number.class.getSimpleName() });
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Integer convertInt(Attribute attr, Object paramValue)
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
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), Number.class.getSimpleName() });
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private FileMeta convertFile(Attribute attr, Object paramValue, Object entityId)
	{
		FileMeta value;
		if (paramValue != null)
		{
			/*
			 * If an entity is updated and no new file is passed, use the old file value
			 */
			if (!(paramValue instanceof MultipartFile))
			{

				EntityType entityType = attr.getEntity();
				Attribute idAttribute = entityType.getIdAttribute();
				Object idValue = this.toEntityValue(idAttribute, entityId, null);
				Entity oldEntity = dataService.findOneById(entityType.getId(), idValue);

				if (paramValue instanceof String)
				{
					FileMeta entity = (FileMeta) oldEntity.getEntity(attr.getName());
					if (entity.get(FILENAME).equals(paramValue))
					{
						value = entity;
					}
					else
					{
						throw new FileAttributeUpdateWithoutFileException();
					}
				}
				else
				{
					throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
							new String[] { MultipartFile.class.getSimpleName() });
				}
			}
			else
			{
				MultipartFile multipartFile = (MultipartFile) paramValue;

				String id = idGenerator.generateId();
				try
				{
					fileStore.store(multipartFile.getInputStream(), id);
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}

				FileMeta fileEntity = fileMetaFactory.create(id);
				fileEntity.setFilename(multipartFile.getOriginalFilename());
				fileEntity.setContentType(multipartFile.getContentType());
				fileEntity.setSize(multipartFile.getSize());
				ServletUriComponentsBuilder currentRequest = servletUriComponentsBuilderFactory.fromCurrentRequest();
				UriComponents downloadUri = currentRequest.replacePath(FileDownloadController.URI + '/' + id)
														  .replaceQuery(null)
														  .build();
				fileEntity.setUrl(downloadUri.toUriString());
				dataService.add(FILE_META, fileEntity);

				value = fileEntity;
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Double convertDecimal(Attribute attr, Object paramValue)
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
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), Number.class.getSimpleName() });
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Instant convertDateTime(Attribute attr, Object paramValue)
			throws java.time.format.DateTimeParseException
	{
		Instant value;
		if (paramValue != null)
		{
			if (paramValue instanceof Instant)
			{
				value = (Instant) paramValue;
			}
			else if (paramValue instanceof String)
			{
				String paramStrValue = (String) paramValue;
				try
				{
					value = parseInstant(paramStrValue);
				}
				catch (java.time.format.DateTimeParseException e)
				{
					throw new DateParseException(attr, paramStrValue);
				}
			}
			else
			{
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), Instant.class.getSimpleName() });
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static LocalDate convertDate(Attribute attr, Object paramValue)
	{
		LocalDate value;
		if (paramValue != null)
		{
			if (paramValue instanceof LocalDate)
			{
				value = (LocalDate) paramValue;
			}
			else if (paramValue instanceof String)
			{
				String paramStrValue = (String) paramValue;
				try
				{
					value = parseLocalDate(paramStrValue);
				}
				catch (java.time.format.DateTimeParseException e)
				{
					throw new DateParseException(attr, paramStrValue);
				}
			}
			else
			{
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), LocalDate.class.getSimpleName() });
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private List<?> convertMref(Attribute attr, Object paramValue)
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
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), List.class.getSimpleName() });
			}

			EntityType mrefEntity = attr.getRefEntity();
			Attribute mrefEntityIdAttr = mrefEntity.getIdAttribute();
			value = mrefParamValues.stream()
								   .map(mrefParamValue -> toEntityValue(mrefEntityIdAttr, mrefParamValue, null))
								   .map(mrefIdValue -> entityManager.getReference(mrefEntity, mrefIdValue))
								   .collect(toList());
		}
		else
		{
			value = emptyList();
		}
		return value;
	}

	private Object convertRef(Attribute attr, Object paramValue)
	{
		Object value;
		if (paramValue != null)
		{
			Object idValue = toEntityValue(attr.getRefEntity().getIdAttribute(), paramValue, null);
			value = entityManager.getReference(attr.getRefEntity(), idValue);
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static String convertString(Attribute attr, Object paramValue)
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
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName() });
			}
		}
		else
		{
			value = null;
		}
		return value;
	}

	private static Boolean convertBool(Attribute attr, Object paramValue)
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
				throw new IncompatibleValueTypeException(attr, paramValue.getClass().getSimpleName(),
						new String[] { String.class.getSimpleName(), Boolean.class.getSimpleName() });
			}
		}
		else
		{
			// boolean false is not posted (http feature), so if null and required, should be false
			value = !attr.isNillable() ? false : null;
		}
		return value;
	}

	/**
	 * For entities with attributes that are part of a bidirectional relationship update the other side of the relationship.
	 *
	 * @param entity created entity
	 */
	public void updateMappedByEntities(@Nonnull Entity entity)
	{
		updateMappedByEntities(entity, null);
	}

	/**
	 * For entities with attributes that are part of a bidirectional relationship update the other side of the relationship.
	 *
	 * @param entity         created or updated entity
	 * @param existingEntity existing entity
	 */
	public void updateMappedByEntities(@Nonnull Entity entity, @Nullable Entity existingEntity)
	{
		entity.getEntityType().getMappedByAttributes().forEach(mappedByAttr ->
		{
			AttributeType type = mappedByAttr.getDataType();
			switch (type)
			{
				case ONE_TO_MANY:
					updateMappedByEntitiesOneToMany(entity, existingEntity, mappedByAttr);
					break;
				default:
					AttributeValidationResult attributeValidationResult = AttributeValidationResult.create(mappedByAttr,
							Collections.singleton(MAPPED_BY_TYPE));
					throw new ValidationException(attributeValidationResult);
			}
		});
	}

	/**
	 * For entities with the given attribute that is part of a bidirectional one-to-many relationship update the other side of the relationship.
	 *
	 * @param entity         created or updated entity
	 * @param existingEntity existing entity
	 * @param attr           bidirectional one-to-many attribute
	 */
	private void updateMappedByEntitiesOneToMany(@Nonnull Entity entity, @Nullable Entity existingEntity,
			@Nonnull Attribute attr)
	{
		// update ref entities of created/updated entity
		Attribute refAttr = attr.getMappedBy();
		Stream<Entity> stream = stream(entity.getEntities(attr.getName()).spliterator(), false);
		if (existingEntity != null)
		{
			// filter out unchanged ref entities
			Set<Object> refEntityIds = stream(existingEntity.getEntities(attr.getName()).spliterator(), false).map(
					Entity::getIdValue).collect(toSet());
			stream = stream.filter(refEntity -> !refEntityIds.contains(refEntity.getIdValue()));
		}
		List<Entity> updatedRefEntities = stream.map(refEntity ->
		{
			if (refEntity.getEntity(refAttr.getName()) != null)
			{
				throw new EntityAlreadyReferencedException(attr.getRefEntity(), refAttr, entity.getEntityType());
			}

			refEntity.set(refAttr.getName(), entity);
			return refEntity;
		}).collect(toList());

		// update ref entities of existing entity
		if (existingEntity != null)
		{
			Set<Object> refEntityIds = stream(entity.getEntities(attr.getName()).spliterator(), false).map(
					Entity::getIdValue).collect(toSet());
			List<Entity> updatedRefEntitiesExistingEntity = stream(
					existingEntity.getEntities(attr.getName()).spliterator(), false).filter(
					refEntity -> !refEntityIds.contains(refEntity.getIdValue())).map(refEntity ->
			{
				refEntity.set(refAttr.getName(), null);
				return refEntity;
			}).collect(toList());

			updatedRefEntities = Stream.concat(updatedRefEntities.stream(), updatedRefEntitiesExistingEntity.stream())
									   .collect(toList());
		}

		if (!updatedRefEntities.isEmpty())
		{
			dataService.update(attr.getRefEntity().getId(), updatedRefEntities.stream());
		}
	}
}
