package org.molgenis.metadata.manager.service;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.metadata.manager.mapper.AttributeMapper;
import org.molgenis.metadata.manager.mapper.EntityTypeMapper;
import org.molgenis.metadata.manager.mapper.PackageMapper;
import org.molgenis.metadata.manager.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
import static org.molgenis.data.meta.model.AttributeMetadata.TYPE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

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
		EntityType entityType = metadataService.getRepository(ENTITY_TYPE_META_DATA,
				EntityType.class).findOneById(entityTypeId);


		if (entityType == null)
		{
			throw new UnknownEntityException("Unknown EntityType [" + entityTypeId + "]");
		}
		Repository<Attribute> attributeRepository = metadataService.getRepository(ATTRIBUTE_META_DATA,
				Attribute.class);
		//TODO: en nu zonder repository
		Query<Attribute> query = attributeRepository.query().eq(REF_ENTITY_TYPE, entityType).and().eq(TYPE, AttributeType.getValueString(AttributeType.XREF));
		List<Attribute> referringAttributes = attributeRepository.findAll(query).collect(Collectors.toList());
		return createEntityTypeResponse(entityType, referringAttributes);
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
