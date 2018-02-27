package org.molgenis.security.permission;

import org.molgenis.security.acl.SidUtils;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.*;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserPermissionEvaluatorImpl implements UserPermissionEvaluator
{
	private final PermissionEvaluator permissionEvaluator;
	private final MutableAclService mutableAclService;
	private final UserAccountService userAccountService;

	UserPermissionEvaluatorImpl(PermissionEvaluator permissionEvaluator, MutableAclService mutableAclService,
			UserAccountService userAccountService)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.userAccountService = requireNonNull(userAccountService);
	}

	@Override
	public boolean hasPermission(ObjectIdentity objectIdentity, Permission permission)
	{
		if (SecurityUtils.currentUserIsSuOrSystem() || isOwner(objectIdentity))
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

	private boolean isOwner(ObjectIdentity objectIdentity)
	{
		Acl acl = mutableAclService.readAclById(objectIdentity);
		Sid owner = acl.getOwner();
		Sid currentUser = SidUtils.createSid(userAccountService.getCurrentUser());
		return owner.equals(currentUser);
	}
}
