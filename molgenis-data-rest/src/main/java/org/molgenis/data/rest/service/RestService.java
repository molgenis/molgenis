package org.molgenis.data.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.molgenis.file.model.FileMetaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;

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
		this.fileMetaFactory = fileMetaFactory;
		this.entityManager = entityManager;
	}

	/**
	 * Creates a new entity based from a HttpServletRequest
	 * 
	 * @param meta
	 * @param request
	 * @return
	 */
	public Entity toEntity(final EntityMetaData meta, final Map<String, Object> request)
	{
		final Entity entity = entityManager.create(meta);

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			String paramName = attr.getName();
			final Object paramValue = request.get(paramName);
			final Object value = this.toEntityValue(attr, paramValue);
			entity.set(attr.getName(), value);
		}

		return entity;
	}

	/**
	 * Transform the request values to match the metadata
	 * 
	 * @param attr
	 * @param paramValue
	 * @return Object
	 */
	public Object toEntityValue(AttributeMetaData attr, Object paramValue)
	{
		Object value = null;

		// Treat empty strings as null
		if ((paramValue != null) && (paramValue instanceof String) && StringUtils.isEmpty((String) paramValue))
		{
			paramValue = null;
		}

		// boolean false is not posted (http feature), so if null and required, should be false
		if ((paramValue == null) && (attr.getDataType() == BOOL) && !attr.isNillable())
		{
			value = false;
		}

		// Treat null lists as empty lists
		if (paramValue == null && (attr.getDataType() == MREF || attr.getDataType() == CATEGORICAL_MREF))
		{
			value = Collections.emptyList();
		}

		if (paramValue != null)
		{
			if (attr.getDataType() == AttributeType.FILE)
			{
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
						.replacePath(FileDownloadController.URI + "/" + id).replaceQuery(null).build().toUriString());
				dataService.add(FILE_META, fileEntity);

				return fileEntity;
			}

			if (attr.getDataType() == XREF || attr.getDataType() == CATEGORICAL)
			{
				value = dataService.findOneById(attr.getRefEntity().getName(), paramValue);
				if (value == null)
				{
					throw new IllegalArgumentException(
							"No " + attr.getRefEntity().getName() + " with id " + paramValue + " found");
				}
			}
			else if (attr.getDataType() == MREF || attr.getDataType() == CATEGORICAL_MREF)
			{
				List<Object> ids = DataConverter.toObjectList(paramValue);
				if ((ids != null) && !ids.isEmpty())
				{
					AttributeMetaData refIdAttr = attr.getRefEntity().getIdAttribute();
					List<Entity> mrefList = dataService.findAll(attr.getRefEntity().getName(),
							ids.stream().map(id -> convert(refIdAttr, id))).collect(toList());
					if (mrefList.size() != ids.size())
					{
						throw new IllegalArgumentException("Could not find all referencing ids for  " + attr.getName());
					}

					value = mrefList;
				}
			}
			else
			{
				value = DataConverter.convert(paramValue, attr);
			}
		}
		return value;
	}

	private static Object convert(AttributeMetaData attr, Object value)
	{
		FieldType fieldType = MolgenisFieldTypes.getType(getValueString(attr.getDataType()));
		return fieldType.convert(value);
	}
}
