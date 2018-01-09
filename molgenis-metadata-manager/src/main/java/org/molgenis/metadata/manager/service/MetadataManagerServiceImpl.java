package org.molgenis.metadata.manager.service;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.metadata.manager.mapper.AttributeMapper;
import org.molgenis.metadata.manager.mapper.EntityTypeMapper;
import org.molgenis.metadata.manager.mapper.PackageMapper;
import org.molgenis.metadata.manager.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.i18n.LanguageService.getLanguageCodes;

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
		EntityType entityType = metadataService.getEntityTypeBypassingRegistry(entityTypeId);
		if (entityType == null)
		{
			throw new UnknownEntityException(String.format("Unknown EntityType [%s]", entityTypeId));
		}
		return createEntityTypeResponse(entityType, metadataService.getReferringAttributes(entityTypeId)
																   .filter(EntityTypeUtils::isSingleReferenceType)
																   .collect(toList()));
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

	private EditorEntityTypeResponse createEntityTypeResponse(EntityType entityType, List<Attribute> referringAttributes)
	{
		EditorEntityType editorEntityType = entityTypeMapper.toEditorEntityType(entityType, referringAttributes);
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
