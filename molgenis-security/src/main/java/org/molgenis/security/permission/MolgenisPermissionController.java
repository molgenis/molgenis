package org.molgenis.security.permission;

import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Objects.requireNonNull;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/permission")
public class MolgenisPermissionController
{
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public MolgenisPermissionController(MolgenisPermissionService molgenisPermissionService)
	{
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
	}

	@RequestMapping(value = "/{entityTypeId}/read", method = GET)
	@ResponseBody
	public boolean hasReadPermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityTypeId,
				org.molgenis.security.core.Permission.READ);
	}

	@RequestMapping(value = "/{entityTypeId}/write", method = GET)
	@ResponseBody
	public boolean hasWritePermission(@PathVariable("entityTypeId") String entityTypeId)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityTypeId,
				org.molgenis.security.core.Permission.WRITE);
	}
}
