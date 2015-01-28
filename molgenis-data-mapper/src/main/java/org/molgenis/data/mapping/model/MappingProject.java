package org.molgenis.data.mapping.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.repositories.Repository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapping.MappingService;
import org.molgenis.data.repository.MappingProjectRepository;

/**
 * Created by charbonb on 14/01/15.
 */
public class MappingProject
{
	private String identifier;
	private String name;
	private String owner;
	private Map<String, MappingTarget> mappingTargets;

	/**
	 * Creates a new empty mapping project. Used by the {@link MappingService}.
	 */
	public MappingProject(String name, String owner)
	{
		this.identifier = null;
		this.name = name;
		this.owner = owner;
		this.mappingTargets = new LinkedHashMap<String, MappingTarget>();
	}

	/**
	 * Creates a new instance of {@link MappingProject}. Used by the {@link MappingProjectRepository} when recreating a
	 * MappingProject from the {@link Repository}.
	 */
	public MappingProject(String identifier, String name, String owner, List<MappingTarget> mappingTargets)
	{
		this.identifier = identifier;
		this.name = name;
		this.owner = owner;
		this.mappingTargets = new LinkedHashMap<String, MappingTarget>();
		for (MappingTarget mappingTarget : mappingTargets)
		{
			this.mappingTargets.put(mappingTarget.getTarget().getName(), mappingTarget);
		}
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getOwner()
	{
		return owner;
	}

	public void String(String owner)
	{
		this.owner = owner;
	}

	public Map<String, MappingTarget> getTargets()
	{
		return mappingTargets;
	}

	public MappingTarget addTarget(EntityMetaData target)
	{
		MappingTarget result = new MappingTarget(target);
		mappingTargets.put(target.getName(), result);
		return result;
	}
}
