package org.molgenis.ui.metadataeditor.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.ui.metadataeditor.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;

@Component
public class EntityTypeMapper
{
	private final EntityTypeFactory entityTypeFactory;
	private AttributeMapper attributeMapper;
	private final PackageMapper packageMapper;
	private final TagMapper tagMapper;
	private final DataService dataService;

	@Autowired
	public EntityTypeMapper(EntityTypeFactory entityTypeFactory, PackageMapper packageMapper, TagMapper tagMapper,
			DataService dataService)
	{
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.packageMapper = requireNonNull(packageMapper);
		this.tagMapper = requireNonNull(tagMapper);
		this.dataService = requireNonNull(dataService);
	}

	// autowire by method to avoid circular dependency error
	@Autowired
	void setAttributeMapper(AttributeMapper attributeMapper)
	{
		this.attributeMapper = requireNonNull(attributeMapper);
	}

	public EditorEntityType toEditorEntityType(EntityType entityType)
	{
		String id = entityType.getName();
		String name = entityType.getSimpleName();
		String label = entityType.getLabel();
		ImmutableMap<String, String> i18nLabel = toI18nLabel(entityType);
		String description = entityType.getDescription();
		ImmutableMap<String, String> i18nDescription = toI18nDescription(entityType);
		boolean abstract_ = entityType.isAbstract();
		String backend = entityType.getBackend();
		EditorPackageIdentifier package_ = packageMapper.toEditorPackage(entityType.getPackage());
		EditorEntityTypeParent entityTypeParent = toEditorEntityTypeParent(entityType.getExtends());
		ImmutableList<EditorEntityTypeIdentifier> entityTypeChildren = toEditorEntityTypeIdentifiers(
				entityType.getExtendedBy());
		ImmutableList<EditorAttribute> attributes = attributeMapper
				.toEditorAttributes(entityType.getOwnAllAttributes());
		ImmutableList<EditorTagIdentifier> tags = tagMapper.toEditorTags(entityType.getTags());
		EditorAttributeIdentifier idAttribute = attributeMapper
				.toEditorAttributeIdentifier(entityType.getIdAttribute());
		EditorAttributeIdentifier labelAttribute = attributeMapper
				.toEditorAttributeIdentifier(entityType.getLabelAttribute());
		ImmutableList<EditorAttributeIdentifier> lookupAttributes = attributeMapper.toEditorAttributeIdentifiers(
				entityType.getLookupAttributes());
		return EditorEntityType
				.create(id, name, label, i18nLabel, description, i18nDescription, abstract_, backend, package_,
						entityTypeParent, entityTypeChildren, attributes, tags, idAttribute, labelAttribute,
						lookupAttributes);
	}

	private ImmutableMap<String, String> toI18nDescription(EntityType entityType)
	{
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		getLanguageCodes().forEach(languageCode ->
		{
			// entityType.getDescription cannot be used, since it returns the description in the default language if not available
			String description = entityType
					.getString(getI18nAttributeName(EntityTypeMetadata.DESCRIPTION, languageCode));
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

	public EntityType toEntityType(EditorEntityType editorEntityType)
	{
		EntityType entityType = entityTypeFactory.create();
		entityType.setName(editorEntityType.getId());
		entityType.setSimpleName(editorEntityType.getName());
		entityType.setPackage(packageMapper.toPackageReference(editorEntityType.getPackage()));
		entityType.setLabel(editorEntityType.getLabel());
		getLanguageCodes().forEach(
				languageCode -> entityType.setLabel(languageCode, editorEntityType.getLabelI18n().get(languageCode)));
		entityType.setDescription(editorEntityType.getDescription());
		getLanguageCodes().forEach(languageCode -> entityType
				.setDescription(languageCode, editorEntityType.getDescriptionI18n().get(languageCode)));

		entityType
				.setOwnAllAttributes(attributeMapper.toAttributes(editorEntityType.getAttributes(), editorEntityType));
		entityType.setAbstract(editorEntityType.isAbstract());
		entityType.setExtends(toEntityTypeReference(editorEntityType.getParent()));
		entityType.setTags(tagMapper.toTagReferences(editorEntityType.getTags()));
		entityType.setBackend(editorEntityType.getBackend());
		return entityType;
	}

	EntityType toEntityTypeReference(EditorEntityType editorEntityType)
	{
		if (editorEntityType == null)
		{
			return null;
		}
		return new EntityType(new LazyEntity(entityTypeFactory.getEntityType(), dataService, editorEntityType.getId()));
	}

	EntityType toEntityTypeReference(EditorEntityTypeIdentifier editorEntityTypeIdentifier)
	{
		if (editorEntityTypeIdentifier == null)
		{
			return null;
		}
		return new EntityType(
				new LazyEntity(entityTypeFactory.getEntityType(), dataService, editorEntityTypeIdentifier.getId()));
	}

	private EntityType toEntityTypeReference(EditorEntityTypeParent editorEntityTypeParent)
	{
		if (editorEntityTypeParent == null)
		{
			return null;
		}
		return new EntityType(
				new LazyEntity(entityTypeFactory.getEntityType(), dataService, editorEntityTypeParent.getId()));
	}

	private ImmutableList<EditorEntityTypeIdentifier> toEditorEntityTypeIdentifiers(Iterable<EntityType> extendedBy)
	{
		return ImmutableList
				.copyOf(stream(extendedBy.spliterator(), false).map(this::toEditorEntityTypeIdentifier).iterator());
	}

	EditorEntityTypeIdentifier toEditorEntityTypeIdentifier(EntityType entityType)
	{
		if (entityType == null)
		{
			return null;
		}

		String id = entityType.getName();
		String label = entityType.getLabel();
		return EditorEntityTypeIdentifier.create(id, label);
	}

	private EditorEntityTypeParent toEditorEntityTypeParent(EntityType entityType)
	{
		if (entityType == null)
		{
			return null;
		}

		String id = entityType.getName();
		String label = entityType.getLabel();
		ImmutableList<EditorAttributeIdentifier> attributes = attributeMapper.toEditorAttributeIdentifiers(
				entityType.getOwnAllAttributes());
		EditorEntityTypeParent parent = toEditorEntityTypeParent(entityType.getExtends());
		return EditorEntityTypeParent.create(id, label, attributes, parent);
	}
}
