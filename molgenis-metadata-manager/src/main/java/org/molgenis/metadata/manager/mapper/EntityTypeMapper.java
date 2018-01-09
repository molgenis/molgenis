package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.metadata.manager.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;
import static org.molgenis.i18n.LanguageService.getLanguageCodes;

@Component
public class EntityTypeMapper
{
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeMapper attributeMapper;
	private final AttributeReferenceMapper attributeReferenceMapper;
	private final PackageMapper packageMapper;
	private final TagMapper tagMapper;
	private final EntityTypeParentMapper entityTypeParentMapper;

	public EntityTypeMapper(EntityTypeFactory entityTypeFactory, AttributeMapper attributeMapper,
			AttributeReferenceMapper attributeReferenceMapper, PackageMapper packageMapper, TagMapper tagMapper,
			EntityTypeParentMapper entityTypeParentMapper)
	{
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attributeMapper = requireNonNull(attributeMapper);
		this.attributeReferenceMapper = requireNonNull(attributeReferenceMapper);
		this.packageMapper = requireNonNull(packageMapper);
		this.tagMapper = requireNonNull(tagMapper);
		this.entityTypeParentMapper = requireNonNull(entityTypeParentMapper);
	}

	public EditorEntityType toEditorEntityType(EntityType entityType, List<Attribute> referringAttributes)
	{
		String id = entityType.getId();
		String label = entityType.getLabel();
		ImmutableMap<String, String> i18nLabel = toI18nLabel(entityType);
		String description = entityType.getDescription();
		ImmutableMap<String, String> i18nDescription = toI18nDescription(entityType);
		boolean abstract_ = entityType.isAbstract();
		String backend = entityType.getBackend();
		EditorPackageIdentifier package_ = packageMapper.toEditorPackage(entityType.getPackage());
		EditorEntityTypeParent entityTypeParent = entityTypeParentMapper.toEditorEntityTypeParent(
				entityType.getExtends());
		ImmutableList<EditorAttribute> attributes = attributeMapper.toEditorAttributes(
				entityType.getOwnAllAttributes());
		ImmutableList<EditorAttributeIdentifier> editorReferringAttributes = attributeReferenceMapper.toEditorAttributeIdentifiers(
				referringAttributes);
		ImmutableList<EditorTagIdentifier> tags = tagMapper.toEditorTags(entityType.getTags());
		EditorAttributeIdentifier idAttribute = attributeReferenceMapper.toEditorAttributeIdentifier(
				entityType.getIdAttribute());
		EditorAttributeIdentifier labelAttribute = attributeReferenceMapper.toEditorAttributeIdentifier(
				entityType.getLabelAttribute());
		ImmutableList<EditorAttributeIdentifier> lookupAttributes = attributeReferenceMapper.toEditorAttributeIdentifiers(
				entityType.getLookupAttributes());

		return EditorEntityType.create(id, label, i18nLabel, description, i18nDescription, abstract_, backend, package_,
				entityTypeParent, attributes, editorReferringAttributes, tags, idAttribute, labelAttribute,
				lookupAttributes);
	}

	public EditorEntityType createEditorEntityType()
	{
		EntityType entityType = entityTypeFactory.create();
		return toEditorEntityType(entityType, ImmutableList.of());
	}

	public EntityType toEntityType(EditorEntityType editorEntityType)
	{
		if (editorEntityType.getLabelAttribute() == null || editorEntityType.getIdAttribute() == null)
		{
			throw new MolgenisDataException(
					"ID and Label attribute for EntityType [" + editorEntityType.getLabel() + "] can not be null");
		}

		EntityType entityType = entityTypeFactory.create();
		entityType.setId(editorEntityType.getId());
		entityType.setPackage(packageMapper.toPackageReference(editorEntityType.getPackage()));
		entityType.setLabel(editorEntityType.getLabel());
		if (editorEntityType.getLabelI18n() != null)
		{
			getLanguageCodes().forEach(languageCode -> entityType.setLabel(languageCode,
					editorEntityType.getLabelI18n().get(languageCode)));
		}

		entityType.setDescription(editorEntityType.getDescription());
		if (editorEntityType.getDescriptionI18n() != null)
		{
			getLanguageCodes().forEach(languageCode -> entityType.setDescription(languageCode,
					editorEntityType.getDescriptionI18n().get(languageCode)));
		}

		entityType.setOwnAllAttributes(
				attributeMapper.toAttributes(editorEntityType.getAttributes(), editorEntityType));
		entityType.setAbstract(editorEntityType.isAbstract());
		entityType.setExtends(entityTypeParentMapper.toEntityTypeReference(editorEntityType.getEntityTypeParent()));
		entityType.setTags(tagMapper.toTagReferences(editorEntityType.getTags()));
		entityType.setBackend(editorEntityType.getBackend());

		return entityType;
	}

	private ImmutableMap<String, String> toI18nDescription(EntityType entityType)
	{
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		getLanguageCodes().forEach(languageCode ->
		{
			// entityType.getDescription cannot be used, since it returns the description in the default language if not available
			String description = entityType.getString(
					getI18nAttributeName(EntityTypeMetadata.DESCRIPTION, languageCode));
			if (description != null)
			{
				mapBuilder.put(languageCode, description);
			}
		});
		return mapBuilder.build();
	}

	private ImmutableMap<String, String> toI18nLabel(EntityType entityType)
	{
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		getLanguageCodes().forEach(languageCode ->
		{
			// entityType.getLabel cannot be used, since it returns the description in the default language if not available
			String label = entityType.getString(getI18nAttributeName(EntityTypeMetadata.LABEL, languageCode));
			if (label != null)
			{
				mapBuilder.put(languageCode, label);
			}
		});
		return mapBuilder.build();
	}
}
