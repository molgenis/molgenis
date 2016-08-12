package org.molgenis.security.permission;

import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Objects.requireNonNull;

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

	@RequestMapping(value = "/{entityName}/read", method = RequestMethod.GET)
	@ResponseBody
	public boolean hasReadPermission(@PathVariable("entityName") String entityName)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityName, org.molgenis.security.core.Permission.READ);
	}

	@RequestMapping(value = "/{entityName}/write", method = RequestMethod.GET)
	@ResponseBody
	public boolean hasWritePermission(@PathVariable("entityName") String entityName)
	{
		return molgenisPermissionService.hasPermissionOnEntity(entityName, org.molgenis.security.core.Permission.WRITE);
	}
}
