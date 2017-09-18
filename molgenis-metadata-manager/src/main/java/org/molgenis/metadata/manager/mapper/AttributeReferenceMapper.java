package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.metadata.manager.model.EditorAttributeIdentifier;
import org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@Component
class AttributeReferenceMapper
{
	private final AttributeMetadata attributeMetadata;
	private final DataService dataService;

	AttributeReferenceMapper(AttributeMetadata attributeMetadata, DataService dataService)
	{
		this.attributeMetadata = requireNonNull(attributeMetadata);
		this.dataService = requireNonNull(dataService);
	}

	ImmutableList<EditorAttributeIdentifier> toEditorAttributeIdentifiers(Iterable<Attribute> attributes)
	{
		return ImmutableList.copyOf(
				stream(attributes.spliterator(), false).map(this::toEditorAttributeIdentifier).iterator());
	}

	EditorAttributeIdentifier toEditorAttributeIdentifier(Attribute attribute)
	{
		if (attribute == null)
		{
			return null;
		}
		return EditorAttributeIdentifier.create(attribute.getIdentifier(), attribute.getLabel(), EditorEntityTypeIdentifier.create(attribute.getEntity().getId(), attribute.getEntity().getLabel()));
	}

	Attribute toAttributeReference(EditorAttributeIdentifier editorAttributeIdentifier)
	{
		if (editorAttributeIdentifier == null)
		{
			return null;
		}
		return new Attribute(new LazyEntity(attributeMetadata, dataService, editorAttributeIdentifier.getId()));
	}
}
