package org.molgenis.security.permission;

import org.molgenis.data.security.RepositoryIdentity;
import org.molgenis.data.security.RepositoryPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping("/permission")
public class MolgenisPermissionController
{
	private final UserPermissionEvaluator permissionService;

	public MolgenisPermissionController(UserPermissionEvaluator permissionService)
	{
		this.permissionService = requireNonNull(permissionService);
	}

	@GetMapping("/{entityTypeId}/read")
	@ResponseBody
	public boolean hasReadPermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return permissionService.hasPermission(new RepositoryIdentity(entityTypeId), RepositoryPermission.READ);
	}

	@GetMapping("/{entityTypeId}/write")
	@ResponseBody
	public boolean hasWritePermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return permissionService.hasPermission(new RepositoryIdentity(entityTypeId), RepositoryPermission.WRITE);
	}
}
