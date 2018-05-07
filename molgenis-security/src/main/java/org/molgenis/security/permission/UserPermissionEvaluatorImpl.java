package org.molgenis.security.permission;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class UserPermissionEvaluatorImpl implements UserPermissionEvaluator
{
	private final PermissionEvaluator permissionEvaluator;
	private final PermissionRegistry permissionRegistry;

	UserPermissionEvaluatorImpl(PermissionEvaluator permissionEvaluator, PermissionRegistry permissionRegistry)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
		this.permissionRegistry = requireNonNull(permissionRegistry);
	}

	@Override
	public boolean hasPermission(ObjectIdentity objectIdentity, Permission action)
	{
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return true;
		}
		else
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return authentication != null && permissionEvaluator.hasPermission(authentication,
					objectIdentity.getIdentifier(), objectIdentity.getType(), getCumulativePermissionToCheck(action));
		}
	}

	@Override
	public boolean hasPermission(ObjectIdentity objectIdentity, List<Permission> permissions)
	{
		return permissions.stream().allMatch(permission -> hasPermission(objectIdentity, permission));
	}

	@Override
	public Set<Permission> getPermissions(ObjectIdentity objectIdentity, Permission[] permissions)
	{
		return Arrays.stream(permissions)
					 .filter(permission -> hasPermission(objectIdentity, permission))
					 .collect(Collectors.toSet());
	}

	private CumulativePermission getCumulativePermissionToCheck(Permission permission)
	{
		CumulativePermission result = new CumulativePermission();
		Set<PermissionSet> permissionSets = permissionRegistry.getPermissions(permission);
		permissionSets.forEach(result::set);
		return result;
	}
}
