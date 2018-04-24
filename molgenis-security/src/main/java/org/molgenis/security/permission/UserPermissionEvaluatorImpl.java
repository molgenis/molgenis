package org.molgenis.security.permission;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Action;
import org.molgenis.security.core.GeneralPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.meta.ActionPermissionMapping;
import org.molgenis.security.meta.ActionPermissionMappingMetadata;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

@Component
public class UserPermissionEvaluatorImpl implements UserPermissionEvaluator
{
	private final PermissionEvaluator permissionEvaluator;
	private final DataService dataService;

	UserPermissionEvaluatorImpl(PermissionEvaluator permissionEvaluator, DataService dataService)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean hasPermission(ObjectIdentity objectIdentity, Permission permission)
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

	public boolean hasPermission(ObjectIdentity objectIdentity, Action action)
	{
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return true;
		}
		else
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			//FIXME: deal with no result and with multiple results
			GeneralPermission permission = getPermission(action).get(0);
			return authentication != null && permissionEvaluator.hasPermission(authentication,
					objectIdentity.getIdentifier(), objectIdentity.getType(), permission);
		}
	}

	private List<GeneralPermission> getPermission(Action action)
	{
		ActionPermissionMapping actionPermissionMapping = dataService.findOne(
				ActionPermissionMappingMetadata.ACTION_PERMISSION_MAPPING,
				new QueryImpl<ActionPermissionMapping>().eq(ActionPermissionMappingMetadata.ACTION, action.getName()),
				ActionPermissionMapping.class);
		Iterable<org.molgenis.security.meta.Permission> permissions = actionPermissionMapping.getEntities(
				ActionPermissionMappingMetadata.PERMISSIONS, org.molgenis.security.meta.Permission.class);
		List<GeneralPermission> generalPermissions = newArrayList();
		for (org.molgenis.security.meta.Permission permission : permissions)
		{
			generalPermissions.add(new GeneralPermission(permission.getMask(), permission.getCode().charAt(0)));
		}
		return generalPermissions;
	}
}
