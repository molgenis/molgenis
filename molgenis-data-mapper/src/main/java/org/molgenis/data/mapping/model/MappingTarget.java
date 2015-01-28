package org.molgenis.data.mapping.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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
		this.identifier = null;
		this.target = target;
		this.entityMappings = new LinkedHashMap<String, EntityMapping>();
		for (EntityMapping mapping : entityMappings)
		{
			this.entityMappings.put(mapping.getSourceEntityMetaData().getName(), mapping);
		}
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public EntityMetaData getTarget()
	{
		return target;
	}

	public Map<String, EntityMapping> getEntityMappings()
	{
		return entityMappings;
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
		// TODO: check existence?
		EntityMapping result = new EntityMapping(source, target);
		entityMappings.put(source.getName(), result);
		return result;
	}

}
