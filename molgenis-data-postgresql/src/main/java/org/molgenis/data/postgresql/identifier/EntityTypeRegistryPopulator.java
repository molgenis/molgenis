package org.molgenis.data.postgresql.identifier;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.EntityTypeUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.EntityTypeMetadata.BACKEND;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

@Component
public class EntityTypeRegistryPopulator
{
	private final EntityTypeRegistry entityTypeRegistry;
	private final DataService dataService;

	public EntityTypeRegistryPopulator(EntityTypeRegistry entityTypeRegistry, DataService dataService)
	{
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
		this.dataService = requireNonNull(dataService);
	}

	public void populate()
	{
		dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				   .eq(BACKEND, POSTGRESQL)
				   .findAll()
				   .forEach(entityType -> entityTypeRegistry.registerEntityType(entityType.getId(), getReferenceTypeAttributes(entityType)));
	}

	private List<Identifiable> getReferenceTypeAttributes(EntityType entityType)
	{
		return getReferenceTypeAttributes(StreamSupport.stream(entityType.getAllAttributes().spliterator(), false));
	}

	private List<Identifiable> getReferenceTypeAttributes(Stream<Attribute> attributes)
	{
		return attributes.filter(EntityTypeUtils::isReferenceType)
						 .map(attr -> Identifiable.create(attr.getName(), attr.getIdentifier()))
						 .collect(toList());
	}
}
