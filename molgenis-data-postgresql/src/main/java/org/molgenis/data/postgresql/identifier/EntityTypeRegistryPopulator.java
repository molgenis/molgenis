package org.molgenis.data.postgresql.identifier;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
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
				   .forEach(entityTypeRegistry::registerEntityType);
	}
}
