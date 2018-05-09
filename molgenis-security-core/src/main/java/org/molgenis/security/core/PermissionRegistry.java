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

	public void addMapping(Permission permission, PermissionSet... permissionSet)
	{
		mapping.putAll(permission, asList(permissionSet));
	}

	public Set<PermissionSet> getPermissions(Permission permission)
	{
		return mapping.get(permission);
	}
}
