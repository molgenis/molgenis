package org.molgenis.security.permission;

import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class PermissionServiceImpl implements PermissionService
{
	private final PermissionEvaluator permissionEvaluator;

	PermissionServiceImpl(PermissionEvaluator permissionEvaluator)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
	}

	@Override
	public boolean hasPermission(String type, String id, org.springframework.security.acls.model.Permission permission)
	{
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return true;
		}
		else
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return authentication != null && permissionEvaluator.hasPermission(authentication, id, type, permission);
		}
	}
}
