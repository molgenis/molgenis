package org.molgenis.security.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ActionPermissionMappingRegistry
{
	private SetMultimap<Action, Permission> mapping = HashMultimap.create();

	public void addMapping(Action action, Permission permission)
	{
		this.mapping.put(action, permission);
	}

	public Set<Permission> getPermissions(Action action)
	{
		return mapping.get(action);
	}
}
