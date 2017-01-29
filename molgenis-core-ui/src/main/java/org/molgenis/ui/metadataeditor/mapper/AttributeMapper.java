package org.molgenis.ui.metadataeditor.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.DataService;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.ui.metadataeditor.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;

@Component
public class AttributeMapper
{
	private final AttributeFactory attributeFactory;
	private EntityTypeMapper entityTypeMapper;
	private final TagMapper tagMapper;
	private final DataService dataService;

	@Autowired
	AttributeMapper(AttributeFactory attributeFactory, TagMapper tagMapper, DataService dataService)
	{
		this.attributeFactory = requireNonNull(attributeFactory);
		this.tagMapper = requireNonNull(tagMapper);
		this.dataService = requireNonNull(dataService);
	}

	// autowire by method to avoid circular dependency error
	@Autowired
	void setEntityTypeMapper(EntityTypeMapper entityTypeMapper)
	{
		this.entityTypeMapper = requireNonNull(entityTypeMapper);
	}

	private ImmutableMap<String, String> toI18nLabel(Attribute attribute)
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

	private ImmutableMap<String, String> toI18nDescription(Attribute attribute)
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

	Iterable<Attribute> toAttributes(List<EditorAttribute> editorAttributes, EditorEntityType editorEntityType)
	{
		return IntStream.range(0, editorAttributes.size())
				.mapToObj(i -> toAttribute(i, editorAttributes.get(i), editorEntityType)).collect(toList());
	}

	private Attribute toAttribute(int seqNr, EditorAttribute editorAttribute, EditorEntityType editorEntityType)
	{
		if (editorAttribute == null)
		{
			return null;
		}

		Attribute attribute = attributeFactory.create();
		attribute.setIdentifier(editorAttribute.getId());
		attribute.setName(editorAttribute.getName());
		attribute.setEntity(entityTypeMapper.toEntityTypeReference(editorEntityType));
		attribute.setSequenceNumber(seqNr);
		attribute.setDataType(AttributeType.valueOf(editorAttribute.getType()));
		attribute.setIdAttribute(isIdAttribute(editorAttribute, editorEntityType));
		attribute.setLabelAttribute(isLabelAttribute(editorAttribute, editorEntityType));
		attribute.setLookupAttributeIndex(getLookupAttributeIndex(editorAttribute, editorEntityType));
		attribute.setRefEntity(entityTypeMapper.toEntityTypeReference(editorAttribute.getRefEntityType()));
		attribute.setMappedBy(toAttributeReference(editorAttribute.getMappedByAttribute()));
		attribute.setOrderBy(toSort(editorAttribute.getOrderBy()));
		attribute.setExpression(editorAttribute.getExpression());
		attribute.setNillable(editorAttribute.isNullable());
		attribute.setAuto(editorAttribute.isAuto());
		attribute.setVisible(editorAttribute.isVisible());
		attribute.setLabel(editorAttribute.getLabel());
		getLanguageCodes().forEach(
				languageCode -> attribute.setLabel(languageCode, editorAttribute.getLabelI18n().get(languageCode)));
		attribute.setDescription(editorAttribute.getDescription());
		getLanguageCodes().forEach(languageCode -> attribute
				.setDescription(languageCode, editorAttribute.getDescriptionI18n().get(languageCode)));
		attribute.setAggregatable(editorAttribute.isAggregatable());
		attribute.setEnumOptions(editorAttribute.getEnumOptions());
		attribute.setRangeMin(editorAttribute.getRangeMin());
		attribute.setRangeMax(editorAttribute.getRangeMax());
		attribute.setReadOnly(editorAttribute.isReadonly());
		attribute.setUnique(editorAttribute.isUnique());
		attribute.setTags(tagMapper.toTagReferences(editorAttribute.getTags()));
		attribute.setVisibleExpression(editorAttribute.getVisibleExpression());
		attribute.setValidationExpression(editorAttribute.getValidationExpression());
		attribute.setDefaultValue(editorAttribute.getDefaultValue());
		return attribute;
	}

	private Attribute toAttributeReference(EditorAttributeIdentifier editorAttributeIdentifier)
	{
		if (editorAttributeIdentifier == null)
		{
			return null;
		}
		return new Attribute(new LazyEntity(attributeFactory.getAttributeMetadata(), dataService,
				editorAttributeIdentifier.getId()));
	}

	private Sort toSort(EditorSort editorSort)
	{
		if (editorSort == null)
		{
			return null;
		}

		List<Sort.Order> orders = editorSort.getOrders().stream().map(this::toOrder).collect(toList());
		return new Sort(orders);
	}

	private Sort.Order toOrder(EditorOrder editorOrder)
	{
		if (editorOrder == null)
		{
			return null;
		}
		return new Sort.Order(editorOrder.getAttributeName(), toDirection(editorOrder.getDirection()));
	}

	private Sort.Direction toDirection(String editorDirection)
	{
		if (editorDirection == null)
		{
			return null;
		}
		return Sort.Direction.valueOf(editorDirection);
	}

	private Integer getLookupAttributeIndex(EditorAttribute editorAttribute, EditorEntityType editorEntityType)
	{
		EditorAttributeIdentifier editorAttributeIdentifier = EditorAttributeIdentifier
				.create(editorAttribute.getId(), editorAttribute.getLabel());
		int index = editorEntityType.getLookupAttributes().indexOf(editorAttributeIdentifier);
		return index != -1 ? index : null;
	}

	private boolean isIdAttribute(EditorAttribute editorAttribute, EditorEntityType editorEntityType)
	{
		return editorEntityType.getIdAttribute() != null && editorEntityType.getIdAttribute().getId()
				.equals(editorAttribute.getId());
	}

	private boolean isLabelAttribute(EditorAttribute editorAttribute, EditorEntityType editorEntityType)
	{
		return editorEntityType.getLabelAttribute() != null && editorEntityType.getLabelAttribute().getId()
				.equals(editorAttribute.getId());
	}

	ImmutableList<EditorAttributeIdentifier> toEditorAttributeIdentifiers(Iterable<Attribute> attributes)
	{
		return ImmutableList
				.copyOf(stream(attributes.spliterator(), false).map(this::toEditorAttributeIdentifier).iterator());
	}

	EditorAttributeIdentifier toEditorAttributeIdentifier(Attribute attribute)
	{
		if (attribute == null)
		{
			return null;
		}
		return EditorAttributeIdentifier.create(attribute.getIdentifier(), attribute.getLabel());
	}

	ImmutableList<EditorAttribute> toEditorAttributes(Iterable<Attribute> attributes)
	{
		return ImmutableList.copyOf(stream(attributes.spliterator(), false).map(this::toEditorAttribute).iterator());
	}

	private EditorAttribute toEditorAttribute(Attribute attribute)
	{
		String id = attribute.getIdentifier();
		String name = attribute.getName();
		String type = attribute.getDataType().toString();
		EditorAttributeIdentifier parent = toEditorAttributeIdentifier(attribute.getParent());
		EditorEntityTypeIdentifier refEntityType = entityTypeMapper
				.toEditorEntityTypeIdentifier(attribute.getRefEntity());
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
		ImmutableList<EditorTagIdentifier> tags = tagMapper.toEditorTags(attribute.getTags());
		String visibleExpression = attribute.getVisibleExpression();
		String validationExpression = attribute.getValidationExpression();
		String defaultValue = attribute.getDefaultValue();
		return EditorAttribute
				.create(id, name, type, parent, refEntityType, mappedByEntityType, orderBy, expression, nullable, auto,
						visible, label, i18nLabel, description, i18nDescription, aggregatable, enumOptions, rangeMin,
						rangeMax, readonly, unique, tags, visibleExpression, validationExpression, defaultValue);
	}

	private EditorSort toEditorSort(Sort sort)
	{
		if (sort == null)
		{
			return null;
		}

		return EditorSort.create(toEditorOrders(sort));
	}

	private ImmutableList<EditorOrder> toEditorOrders(Iterable<Sort.Order> orders)
	{
		return ImmutableList.copyOf(stream(orders.spliterator(), false).map(this::toEditorOrder).iterator());
	}

	private EditorOrder toEditorOrder(Sort.Order order)
	{
		return EditorOrder.create(order.getAttr(), order.getDirection().toString());
	}
}
