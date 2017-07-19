package org.molgenis.security.permission;

import org.molgenis.auth.Role;
import org.molgenis.data.DataService;
import org.molgenis.data.security.acl.SecurityId;
import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.RoleMetadata.ROLE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/permission")
public class MolgenisPermissionController
{
	private final MolgenisPermissionService molgenisPermissionService;
	private final DataService dataService;

	@Autowired
	public MolgenisPermissionController(MolgenisPermissionService molgenisPermissionService, DataService dataService)
	{
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.dataService = dataService;
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

	@RequestMapping(value = "/sid", method = GET)
	@ResponseBody
	public Collection<SecurityId> getSids()
	{
		return dataService.findAll(ROLE, Role.class)
						  .map(role -> SecurityId.createForAuthority(role.getLabel()))
						  .collect(toList());
	}
}
