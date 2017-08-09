package org.molgenis.data.mapper.mapping.model;

import com.google.common.collect.Lists;
import org.elasticsearch.repositories.Repository;
import org.molgenis.auth.User;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.model.EntityType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class MappingProject
{
	private String identifier;
	private String name;
	private User owner;
	private Map<String, MappingTarget> mappingTargets;

	/**
	 * Creates a new empty mapping project. Used by the {@link MappingService}.
	 */
	public MappingProject(String name, User owner)
	{
		this.identifier = null;
		this.name = name;
		this.owner = owner;
		this.mappingTargets = new LinkedHashMap<>();
	}

	/**
	 * Creates a new instance of {@link MappingProject}. Used by the {@link MappingProjectRepository} when recreating a
	 * MappingProject from the {@link Repository}.
	 */
	public MappingProject(String identifier, String name, User owner, List<MappingTarget> mappingTargets)
	{
		this.identifier = identifier;
		this.name = name;
		this.owner = owner;
		this.mappingTargets = new LinkedHashMap<>();
		for (MappingTarget mappingTarget : mappingTargets)
		{
			if (mappingTarget != null)
			{
				this.mappingTargets.put(mappingTarget.getTarget().getId(), mappingTarget);
			}
		}
	}

	public void removeIdentifiers()
	{
		this.identifier = null;
		mappingTargets.values().forEach(MappingTarget::removeIdentifiers);
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

	public User getOwner()
	{
		// For owner lazy load the molgenis username as system so that Freemarker is able to access it
		runAsSystem(() -> owner.getUsername());
		return owner;
	}

	public void setOwner(User owner)
	{
		this.owner = owner;
	}

	public List<MappingTarget> getMappingTargets()
	{
		return Lists.newArrayList(mappingTargets.values());
	}

	public MappingTarget getMappingTarget(String name)
	{
		return mappingTargets.get(name);
	}

	public MappingTarget addTarget(EntityType target)
	{
		if (mappingTargets.containsKey(target.getId()))
		{
			throw new IllegalStateException("Cannot have multiple mappings for same target.");
		}
		MappingTarget result = new MappingTarget(target);
		mappingTargets.put(target.getId(), result);
		return result;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((mappingTargets == null) ? 0 : mappingTargets.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MappingProject other = (MappingProject) obj;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		if (mappingTargets == null)
		{
			if (other.mappingTargets != null) return false;
		}
		else if (!mappingTargets.equals(other.mappingTargets)) return false;
		if (name == null)
		{
			if (other.name != null) return false;
		}
		else if (!name.equals(other.name)) return false;
		if (owner == null)
		{
			if (other.owner != null) return false;
		}
		else if (!owner.equals(other.owner)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "MappingProject [identifier=" + identifier + ", name=" + name + ", owner=" + owner + ", mappingTargets="
				+ mappingTargets + "]";
	}

}
