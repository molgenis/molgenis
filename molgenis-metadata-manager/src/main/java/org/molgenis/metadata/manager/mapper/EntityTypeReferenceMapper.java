package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

@Component
class EntityTypeReferenceMapper
{
	private final EntityTypeMetadata entityTypeMetadata;
	private final DataService dataService;

	EntityTypeReferenceMapper(EntityTypeMetadata entityTypeMetadata, DataService dataService)
	{
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
		this.dataService = requireNonNull(dataService);
	}

	EntityType toEntityTypeReference(String entityTypeId)
	{
		if (entityTypeId == null)
		{
			return null;
		}
		return new EntityType(new LazyEntity(entityTypeMetadata, dataService, entityTypeId));
	}

	ImmutableList<EditorEntityTypeIdentifier> toEditorEntityTypeIdentifiers(Iterable<EntityType> extendedBy)
	{
		return ImmutableList.copyOf(
				stream(extendedBy.spliterator(), false).map(this::toEditorEntityTypeIdentifier).iterator());
	}

	EditorEntityTypeIdentifier toEditorEntityTypeIdentifier(EntityType entityType)
	{
		if (entityType == null)
		{
			return null;
		}

		String id = entityType.getId();
		String label = entityType.getLabel();
		return EditorEntityTypeIdentifier.create(id, label);
	}
}
