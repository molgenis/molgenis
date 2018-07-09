package org.molgenis.data.security.auth;

import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
public class RoleService
{
	private final DataService dataService;
	private final RoleMetadata roleMetadata;

	public RoleService(DataService dataService, RoleMetadata roleMetadata)
	{
		this.dataService = requireNonNull(dataService);
		this.roleMetadata = requireNonNull(roleMetadata);
	}

	public Role getRole(String roleName)
	{
		Role role = dataService.query(RoleMetadata.ROLE, Role.class).eq(RoleMetadata.NAME, roleName).findOne();
		if (role == null)
		{
			throw new UnknownEntityException(roleMetadata, roleMetadata.getAttribute(RoleMetadata.NAME), roleName);
		}
		return role;
	}
}
