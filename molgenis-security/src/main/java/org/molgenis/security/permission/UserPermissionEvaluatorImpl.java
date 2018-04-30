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

import java.util.Set;

import static java.util.Objects.requireNonNull;

@Component
public class UserPermissionEvaluatorImpl implements UserPermissionEvaluator
{
	private final PermissionEvaluator permissionEvaluator;
	private final PermissionRegistry actionPermissionMappingRegistry;

	UserPermissionEvaluatorImpl(PermissionEvaluator permissionEvaluator,
			PermissionRegistry actionPermissionMappingRegistry)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
		this.actionPermissionMappingRegistry = requireNonNull(actionPermissionMappingRegistry);
	}

	public boolean hasPermission(ObjectIdentity objectIdentity, PermissionSet permission)
	{
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return true;
		}
		else
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return authentication != null && permissionEvaluator.hasPermission(authentication,
					objectIdentity.getIdentifier(), objectIdentity.getType(), permission);
		}
	}

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

	private CumulativePermission getCumulativePermissionToCheck(Permission action)
	{
		CumulativePermission result = new CumulativePermission();
		Set<PermissionSet> permissionSets = actionPermissionMappingRegistry.getPermissions(action);
		permissionSets.forEach(result::set);
		return result;
	}
}
