package org.molgenis.security.core;

import com.google.common.collect.HashMultimap;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ActionPermissionMappingRegistry
{
	private HashMultimap<Action, Permission> mapping = HashMultimap.create();

	public void addMapping(Action action, Permission permission)
	{
		this.mapping.put(action, permission);
	}

	public Set<Permission> getPermissions(Action action)
	{
		return mapping.get(action);
	}
}
