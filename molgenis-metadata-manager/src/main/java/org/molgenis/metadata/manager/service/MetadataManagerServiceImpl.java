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
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;

@Component
public class MetadataManagerServiceImpl implements MetadataManagerService
{
	private final MetaDataService metadataService;
	private final PackageMapper packageMapper;
	private final EntityTypeMapper entityTypeMapper;
	private final AttributeMapper attributeMapper;

	public MetadataManagerServiceImpl(MetaDataService metadataService, PackageMapper packageMapper,
			EntityTypeMapper entityTypeMapper, AttributeMapper attributeMapper)
	{
		this.metadataService = requireNonNull(metadataService);
		this.packageMapper = requireNonNull(packageMapper);
		this.entityTypeMapper = requireNonNull(entityTypeMapper);
		this.attributeMapper = requireNonNull(attributeMapper);
	}

	@Override
	public List<EditorPackageIdentifier> getEditorPackages()
	{
		return createPackageListResponse(metadataService.getPackages());
	}

	@Override
	public EditorEntityTypeResponse getEditorEntityType(String entityTypeId)
	{
		// metadataService.getEditorEntityType cannot be used due to https://github.com/molgenis/molgenis/issues/5783
		EntityType entityType = metadataService
				.getRepository(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class).findOneById(entityTypeId);

		if (entityType == null)
		{
			throw new UnknownEntityException("Unknown EntityType [" + entityTypeId + "]");
		}

		return createEntityTypeResponse(entityType);
	}

	@Override
	public EditorEntityTypeResponse createEditorEntityType()
	{
		return createEntityTypeResponse();
	}

	@Override
	public void upsertEntityType(EditorEntityType editorEntityType)
	{
		EntityType entityType = entityTypeMapper.toEntityType(editorEntityType);
		metadataService.upsertEntityTypes(newArrayList(entityType));
	}

	@Override
	public EditorAttributeResponse createEditorAttribute()
	{
		return createAttributeResponse();
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
