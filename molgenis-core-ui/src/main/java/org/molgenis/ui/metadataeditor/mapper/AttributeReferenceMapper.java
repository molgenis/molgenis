package org.molgenis.ui.metadataeditor.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.ui.metadataeditor.model.EditorAttributeIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@Component
class AttributeReferenceMapper
{
	private final AttributeMetadata attributeMetadata;
	private final DataService dataService;

	@Autowired
	AttributeReferenceMapper(AttributeMetadata attributeMetadata, DataService dataService)
	{

		this.attributeMetadata = requireNonNull(attributeMetadata);
		this.dataService = requireNonNull(dataService);
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

	Attribute toAttributeReference(EditorAttributeIdentifier editorAttributeIdentifier)
	{
		if (editorAttributeIdentifier == null)
		{
			return null;
		}
		return new Attribute(new LazyEntity(attributeMetadata, dataService, editorAttributeIdentifier.getId()));
	}
}
