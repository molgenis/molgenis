package org.molgenis.security.core.service;

import org.molgenis.security.core.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService
{
	/**
	 *
	 *
	 * <p>Create roles from {@link org.molgenis.security.core.model.ConceptualRoles} for {@link org.molgenis.security.core.model.Group}</p>
	 *
	 * @param group groupId
	 *
	 */
	List<Role> createRolesForGroup(String group);

	/**
	 *
	 * <p>Return {@link Role}</p>
	 *
	 * @param roleId role id parameter
	 *
	 * @return Role
	 */
	Optional<Role> findRoleById(String roleId);

}
