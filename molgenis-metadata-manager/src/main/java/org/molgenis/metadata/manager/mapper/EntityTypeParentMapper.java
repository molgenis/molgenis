package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.metadata.manager.model.EditorAttributeIdentifier;
import org.molgenis.metadata.manager.model.EditorEntityTypeParent;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
class EntityTypeParentMapper
{
	private final AttributeReferenceMapper attributeReferenceMapper;
	private final EntityTypeMetadata entityTypeMetadata;
	private final DataService dataService;

	public EntityTypeParentMapper(AttributeReferenceMapper attributeReferenceMapper,
			EntityTypeMetadata entityTypeMetadata, DataService dataService)
	{
		this.attributeReferenceMapper = requireNonNull(attributeReferenceMapper);
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
		this.dataService = requireNonNull(dataService);
	}

	EntityType toEntityTypeReference(EditorEntityTypeParent editorEntityTypeParent)
	{
		if (editorEntityTypeParent == null)
		{
			return null;
		}
		return new EntityType(new LazyEntity(entityTypeMetadata, dataService, editorEntityTypeParent.getId()));
	}

	EditorEntityTypeParent toEditorEntityTypeParent(EntityType entityType)
	{
		if (entityType == null)
		{
			return null;
		}

		String id = entityType.getId();
		String label = entityType.getLabel();
		ImmutableList<EditorAttributeIdentifier> attributes = attributeReferenceMapper.toEditorAttributeIdentifiers(
				entityType.getOwnAllAttributes());
		EditorEntityTypeParent parent = toEditorEntityTypeParent(entityType.getExtends());
		return EditorEntityTypeParent.create(id, label, attributes, parent);
	}
}
