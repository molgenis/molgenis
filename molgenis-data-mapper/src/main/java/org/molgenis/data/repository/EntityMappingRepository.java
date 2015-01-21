package org.molgenis.data.repository;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.mapping.EntityMapping;

public interface EntityMappingRepository
{
	public abstract void add(EntityMapping entityMapping);

	public abstract void add(List<EntityMapping> entityMappings);

	public abstract EntityMapping toEntityMappingEntity(Entity entityMappingEntity);
}