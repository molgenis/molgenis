package org.molgenis.metadata.manager.service;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.metadata.manager.mapper.AttributeMapper;
import org.molgenis.metadata.manager.mapper.EntityTypeMapper;
import org.molgenis.metadata.manager.mapper.PackageMapper;
import org.molgenis.metadata.manager.model.*;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;
import static org.molgenis.metadata.manager.service.MetadataManagerService.URI;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Service
@RequestMapping(URI)
public class MetadataManagerService
{
	private static final Logger LOG = LoggerFactory.getLogger(MetadataManagerService.class);

	public static final String URI = "metadata-manager-service";

	private final MetaDataService metadataService;
	private final PackageMapper packageMapper;
	private final EntityTypeMapper entityTypeMapper;
	private final AttributeMapper attributeMapper;

	public MetadataManagerService(MetaDataService metadataService, PackageMapper packageMapper,
			EntityTypeMapper entityTypeMapper, AttributeMapper attributeMapper)
	{
		this.metadataService = requireNonNull(metadataService);
		this.packageMapper = requireNonNull(packageMapper);
		this.entityTypeMapper = requireNonNull(entityTypeMapper);
		this.attributeMapper = requireNonNull(attributeMapper);
	}

	@RequestMapping(value = "/editorPackages", method = GET)
	@ResponseBody
	public List<EditorPackageIdentifier> getPackages()
	{
		return createPackageListResponse(metadataService.getPackages());
	}

	@RequestMapping(value = "/entityType/{id:.+}", method = GET)
	@ResponseBody
	public EditorEntityTypeResponse getEntityType(@PathVariable("id") String entityTypeId)
	{
		// metadataService.getEntityType cannot be used due to https://github.com/molgenis/molgenis/issues/5783
		EntityType entityType = metadataService.getRepository(EntityTypeMetadata.ENTITY_TYPE_META_DATA,
				EntityType.class).findOneById(entityTypeId);

		return createEntityTypeResponse(entityType);
	}

	@RequestMapping(value = "/create/entityType", method = GET)
	@ResponseBody
	public EditorEntityTypeResponse createEntityType()
	{
		return createEntityTypeResponse();
	}

	@RequestMapping(value = "/entityType", method = POST, consumes = "application/json")
	@ResponseStatus(OK)
	public void upsertEntityType(@RequestBody EditorEntityType editorEntityType)
	{
		EntityType entityType = entityTypeMapper.toEntityType(editorEntityType);
		metadataService.upsertEntityTypes(newArrayList(entityType));
	}

	@RequestMapping(value = "/create/attribute", method = GET)
	@ResponseBody
	public EditorAttributeResponse createAttribute()
	{
		return createAttributeResponse();
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(
				Collections.singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(
				Collections.singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	private List<EditorPackageIdentifier> createPackageListResponse(List<Package> packages)
	{
		List<EditorPackageIdentifier> response = newArrayList();
		for (Package package_ : packages)
		{
			response.add(packageMapper.toEditorPackage(package_));
		}
		return response;
	}

	private EditorEntityTypeResponse createEntityTypeResponse()
	{
		EditorEntityType editorEntityType = entityTypeMapper.createEditorEntityType();
		return createEntityTypeResponse(editorEntityType);
	}

	private EditorEntityTypeResponse createEntityTypeResponse(EntityType entityType)
	{
		EditorEntityType editorEntityType = entityTypeMapper.toEditorEntityType(entityType);
		return createEntityTypeResponse(editorEntityType);
	}

	private EditorEntityTypeResponse createEntityTypeResponse(EditorEntityType editorEntityType)
	{
		ImmutableList<String> languageCodes = ImmutableList.copyOf(getLanguageCodes().iterator());
		return EditorEntityTypeResponse.create(editorEntityType, languageCodes);
	}

	private EditorAttributeResponse createAttributeResponse()
	{
		EditorAttribute editorAttribute = attributeMapper.createEditorAttribute();
		return createAttributeResponse(editorAttribute);
	}

	private EditorAttributeResponse createAttributeResponse(EditorAttribute editorAttribute)
	{
		ImmutableList<String> languageCodes = ImmutableList.copyOf(getLanguageCodes().iterator());
		return EditorAttributeResponse.create(editorAttribute, languageCodes);
	}
}
