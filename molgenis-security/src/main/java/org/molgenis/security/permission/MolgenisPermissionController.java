package org.molgenis.security.permission;

import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.PermissionService;
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
	private final PermissionService permissionService;

	public MolgenisPermissionController(PermissionService permissionService)
	{
		this.permissionService = requireNonNull(permissionService);
	}

	@GetMapping("/{entityTypeId}/read")
	@ResponseBody
	public boolean hasReadPermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return permissionService.hasPermission(EntityTypeIdentity.TYPE, entityTypeId, EntityTypePermission.READ);
	}

	@GetMapping("/{entityTypeId}/write")
	@ResponseBody
	public boolean hasWritePermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return permissionService.hasPermission(EntityTypeIdentity.TYPE, entityTypeId, EntityTypePermission.WRITE);
	}
}
