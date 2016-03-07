package org.molgenis.data.rest.service;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.molgenis.fieldtypes.BoolField;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileMeta;
import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class RestService
{
	private final DataService dataService;
	private final IdGenerator idGenerator;
	private final FileStore fileStore;

	@Autowired
	public RestService(DataService dataService, IdGenerator idGenerator, FileStore fileStore)
	{
		this.dataService = requireNonNull(dataService);
		this.idGenerator = requireNonNull(idGenerator);
		this.fileStore = requireNonNull(fileStore);
	}

	/**
	 * Creates a new MapEntity based from a HttpServletRequest
	 * 
	 * @param meta
	 * @param request
	 * @return
	 */
	public Entity toEntity(final EntityMetaData meta, final Map<String, Object> request)
	{
		final Entity entity = new MapEntity(meta);

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
		if ((paramValue == null) && (attr.getDataType() instanceof BoolField) && !attr.isNillable())
		{
			value = false;
		}

		// Treat null lists as empty lists
		if (paramValue == null
				&& (attr.getDataType().getEnumType() == MREF || attr.getDataType().getEnumType() == CATEGORICAL_MREF))
		{
			value = Collections.emptyList();
		}

		if (paramValue != null)
		{
			if (attr.getDataType().getEnumType() == FieldTypeEnum.FILE)
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

				FileMeta fileEntity = new FileMeta(dataService);
				fileEntity.setId(id);
				fileEntity.setFilename(multipartFile.getOriginalFilename());
				fileEntity.setContentType(multipartFile.getContentType());
				fileEntity.setSize(multipartFile.getSize());
				fileEntity.setUrl(ServletUriComponentsBuilder.fromCurrentRequest()
						.replacePath(FileDownloadController.URI + "/" + id).replaceQuery(null).build().toUriString());
				dataService.add(FileMeta.ENTITY_NAME, fileEntity);

				return fileEntity;
			}

			if (attr.getDataType().getEnumType() == XREF || attr.getDataType().getEnumType() == CATEGORICAL)
			{
				value = dataService.findOne(attr.getRefEntity().getName(), paramValue);
				if (value == null)
				{
					throw new IllegalArgumentException(
							"No " + attr.getRefEntity().getName() + " with id " + paramValue + " found");
				}
			}
			else if (attr.getDataType().getEnumType() == MREF || attr.getDataType().getEnumType() == CATEGORICAL_MREF)
			{
				List<Object> ids = DataConverter.toObjectList(paramValue);
				if ((ids != null) && !ids.isEmpty())
				{
					List<Entity> mrefList = dataService.findAll(attr.getRefEntity().getName(), ids.stream())
							.collect(toList());
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
}
