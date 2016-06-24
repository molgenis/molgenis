package org.molgenis.data.mapper.mapping.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.EntityMetaData;

public class MappingTarget
{
	private String identifier;
	private final EntityMetaData target;
	/**
	 * Maps source name to entityMapping for that source to the target
	 */
	private Map<String, EntityMapping> entityMappings;

	/**
	 * Creates a new empty {@link MappingTarget}
	 */
	public MappingTarget(EntityMetaData target)
	{
		this.identifier = null;
		this.target = target;
		this.entityMappings = new LinkedHashMap<String, EntityMapping>();
	}

	/**
	 * Creates a new instance of MappingTarget.
	 */
	public MappingTarget(String identifier, EntityMetaData target, Collection<EntityMapping> entityMappings)
	{
		this.identifier = identifier;
		this.target = target;
		this.entityMappings = new LinkedHashMap<String, EntityMapping>();
		for (EntityMapping mapping : entityMappings)
		{
			this.entityMappings.put(mapping.getName(), mapping);
		}
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void removeIdentifiers()
	{
		identifier = null;
		entityMappings.values().forEach(EntityMapping::removeIdentifiers);
	}

	public EntityMetaData getTarget()
	{
		return target;
	}

	public EntityMapping getMappingForSource(String source)
	{
		return entityMappings.get(source);
	}

	/**
	 * Adds a new {@link EntityMapping} to this target for a certain source.
	 * 
	 * @param source
	 *            {@link EntityMetaData} for the source entity that is mapped to this target
	 * @return the newly created empty {@link EntityMapping}
	 */
	public EntityMapping addSource(EntityMetaData source)
	{
		if (entityMappings.containsKey(source.getName()))
		{
			throw new IllegalStateException("Mapping already present for source " + source.getName());
		}
		EntityMapping result = new EntityMapping(source, target);
		entityMappings.put(source.getName(), result);
		return result;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityMappings == null) ? 0 : entityMappings.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MappingTarget other = (MappingTarget) obj;
		if (entityMappings == null)
		{
			if (other.entityMappings != null) return false;
		}
		else if (!entityMappings.equals(other.entityMappings)) return false;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		if (target == null)
		{
			if (other.target != null) return false;
		}
		else if (!target.equals(other.target)) return false;
		return true;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public String toString()
	{
		return "MappingTarget [identifier=" + identifier + ", target=" + target + ", entityMappings=" + entityMappings
				+ "]";
	}

	public String getName()
	{
		return target.getName();
	}

	public Collection<EntityMapping> getEntityMappings()
	{
		return Lists.newArrayList(entityMappings.values());
	}

	public void removeSource(String source)
	{
		entityMappings.remove(source);
	}

	public boolean hasMappingFor(String name)
	{
		return entityMappings.containsKey(name);
	}
}
