package org.molgenis.security.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Arrays.asList;

@Component
public class PermissionRegistry
{
	private SetMultimap<Permission, PermissionSet> mapping = HashMultimap.create();

	public void addMapping(Permission action, PermissionSet... permission)
	{
		mapping.putAll(action, asList(permission));
	}

	public Set<PermissionSet> getPermissions(Permission action)
	{
		return mapping.get(action);
	}
}
