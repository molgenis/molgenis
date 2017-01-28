package org.molgenis.ui.metadataeditor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
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
	private final EntityTypeMetadata entityTypeMetadata;
	private final AttributeMetadata attributeMetadata;
	private final PackageMetadata packageMetadata;
	private final TagMetadata tagMetadata;

	@Autowired
	public EntityTypeMapper(EntityTypeMetadata entityTypeMetadata, AttributeMetadata attributeMetadata,
			PackageMetadata packageMetadata, TagMetadata tagMetadata)
	{

		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
		this.attributeMetadata = requireNonNull(attributeMetadata);
		this.packageMetadata = requireNonNull(packageMetadata);
		this.tagMetadata = requireNonNull(tagMetadata);
	}

	// TODO i18n
	static EditorEntityType toEditorEntityType(EntityType entityType)
	{
		String id = entityType.getName();
		String name = entityType.getSimpleName();
		String label = entityType.getLabel();
		ImmutableMap<String, String> i18nLabel = toI18nLabel(entityType);
		String description = entityType.getDescription();
		ImmutableMap<String, String> i18nDescription = toI18nDescription(entityType);
		boolean abstract_ = entityType.isAbstract();
		String backend = entityType.getBackend();
		EditorPackage package_ = toEditorPackage(entityType.getPackage());
		EditorEntityTypeParent entityTypeParent = toEditorEntityTypeParent(entityType.getExtends());
		ImmutableList<EditorEntityTypeIdentifier> entityTypeChildren = toEditorEntityTypeIdentifiers(
				entityType.getExtendedBy());
		ImmutableList<EditorAttribute> attributes = toEditorAttributes(entityType.getOwnAllAttributes());
		ImmutableList<EditorTag> tags = toEditorTags(entityType.getTags());
		EditorAttributeIdentifier idAttribute = toEditorAttributeIdentifier(entityType.getIdAttribute());
		EditorAttributeIdentifier labelAttribute = toEditorAttributeIdentifier(entityType.getLabelAttribute());
		ImmutableList<EditorAttributeIdentifier> lookupAttributes = toEditorAttributeIdentifiers(
				entityType.getLookupAttributes());
		return EditorEntityType
				.create(id, name, label, i18nLabel, description, i18nDescription, abstract_, backend, package_,
						entityTypeParent, entityTypeChildren, attributes, tags, idAttribute, labelAttribute,
						lookupAttributes);
	}

	private static ImmutableMap<String, String> toI18nDescription(EntityType entityType)
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

	private static ImmutableMap<String, String> toI18nLabel(Attribute attribute)
	{
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		getLanguageCodes().forEach(languageCode ->
		{
			// attribute.getLabel cannot be used, since it returns the description in the default language if not available
			String label = attribute.getString(getI18nAttributeName(AttributeMetadata.LABEL, languageCode));
			if (label != null)
			{
				mapBuilder.put(languageCode, label);
			}
		});
		return mapBuilder.build();
	}

	private static ImmutableMap<String, String> toI18nDescription(Attribute attribute)
	{
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		getLanguageCodes().forEach(languageCode ->
		{
			// attribute.getDescription cannot be used, since it returns the description in the default language if not available
			String description = attribute.getString(getI18nAttributeName(AttributeMetadata.DESCRIPTION, languageCode));
			if (description != null)
			{
				mapBuilder.put(languageCode, description);
			}
		});
		return mapBuilder.build();
	}

	private static ImmutableMap<String, String> toI18nLabel(EntityType entityType)
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

	EntityType toEntityType(EditorEntityType editorEntityType)
	{
		return null;// TODO
	}

	private static ImmutableList<EditorEntityTypeIdentifier> toEditorEntityTypeIdentifiers(
			Iterable<EntityType> extendedBy)
	{
		return ImmutableList
				.copyOf(stream(extendedBy.spliterator(), false).map(EntityTypeMapper::toEditorEntityTypeIdentifier)
						.iterator());
	}

	private static EditorPackage toEditorPackage(Package package_)
	{
		if (package_ == null)
		{
			return null;
		}
		return EditorPackage.create(package_.getName(), package_.getLabel());
	}

	private static EditorEntityTypeIdentifier toEditorEntityTypeIdentifier(EntityType entityType)
	{
		if (entityType == null)
		{
			return null;
		}

		String id = entityType.getName();
		String label = entityType.getLabel();
		return EditorEntityTypeIdentifier.create(id, label);
	}

	private static EditorEntityTypeParent toEditorEntityTypeParent(EntityType entityType)
	{
		if (entityType == null)
		{
			return null;
		}

		String id = entityType.getName();
		String label = entityType.getLabel();
		ImmutableList<EditorAttributeIdentifier> attributes = toEditorAttributeIdentifiers(
				entityType.getOwnAllAttributes());
		EditorEntityTypeParent parent = toEditorEntityTypeParent(entityType.getExtends());
		return EditorEntityTypeParent.create(id, label, attributes, parent);
	}

	private static ImmutableList<EditorAttributeIdentifier> toEditorAttributeIdentifiers(Iterable<Attribute> attributes)
	{
		return ImmutableList
				.copyOf(stream(attributes.spliterator(), false).map(EntityTypeMapper::toEditorAttributeIdentifier)
						.iterator());
	}

	private static EditorAttributeIdentifier toEditorAttributeIdentifier(Attribute attribute)
	{
		if (attribute == null)
		{
			return null;
		}
		return EditorAttributeIdentifier.create(attribute.getIdentifier(), attribute.getLabel());
	}

	private static ImmutableList<EditorAttribute> toEditorAttributes(Iterable<Attribute> attributes)
	{
		return ImmutableList
				.copyOf(stream(attributes.spliterator(), false).map(EntityTypeMapper::toEditorAttribute).iterator());
	}

	private static EditorAttribute toEditorAttribute(Attribute attribute)
	{
		String id = attribute.getIdentifier();
		String name = attribute.getName();
		String type = attribute.getDataType().toString();
		EditorAttributeIdentifier parent = toEditorAttributeIdentifier(attribute.getParent());
		EditorEntityTypeIdentifier refEntityType = toEditorEntityTypeIdentifier(attribute.getRefEntity());
		EditorAttributeIdentifier mappedByEntityType = toEditorAttributeIdentifier(attribute.getMappedBy());
		EditorSort orderBy = toEditorSort(attribute.getOrderBy());
		String expression = attribute.getExpression();
		boolean nullable = attribute.isNillable();
		boolean auto = attribute.isAuto();
		boolean visible = attribute.isVisible();
		String label = attribute.getLabel();
		ImmutableMap<String, String> i18nLabel = toI18nLabel(attribute);
		String description = attribute.getDescription();
		ImmutableMap<String, String> i18nDescription = toI18nDescription(attribute);
		boolean aggregatable = attribute.isAggregatable();
		ImmutableList<String> enumOptions = ImmutableList.copyOf(attribute.getEnumOptions());
		Long rangeMin = attribute.getRangeMin();
		Long rangeMax = attribute.getRangeMax();
		boolean readonly = attribute.isReadOnly();
		boolean unique = attribute.isUnique();
		ImmutableList<EditorTag> tags = toEditorTags(attribute.getTags());
		String visibleExpression = attribute.getVisibleExpression();
		String validationExpression = attribute.getValidationExpression();
		String defaultValue = attribute.getDefaultValue();
		return EditorAttribute
				.create(id, name, type, parent, refEntityType, mappedByEntityType, orderBy, expression, nullable, auto,
						visible, label, i18nLabel, description, i18nDescription, aggregatable, enumOptions, rangeMin,
						rangeMax, readonly, unique, tags, visibleExpression, validationExpression, defaultValue);
	}

	private static EditorSort toEditorSort(Sort sort)
	{
		if (sort == null)
		{
			return null;
		}

		return EditorSort.create(toEditorOrders(sort));
	}

	private static ImmutableList<EditorOrder> toEditorOrders(Iterable<Sort.Order> orders)
	{
		return ImmutableList
				.copyOf(stream(orders.spliterator(), false).map(EntityTypeMapper::toEditorOrder).iterator());
	}

	private static EditorOrder toEditorOrder(Sort.Order order)
	{
		return EditorOrder.create(order.getAttr(), order.getDirection().toString());
	}

	private static ImmutableList<EditorTag> toEditorTags(Iterable<Tag> tags)
	{
		return ImmutableList.copyOf(stream(tags.spliterator(), false).map(EntityTypeMapper::toEditorTags).iterator());
	}

	private static EditorTag toEditorTags(Tag tag)
	{
		return EditorTag.create(tag.getId());
	}
}
