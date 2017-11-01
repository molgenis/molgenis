package org.molgenis.security.core.service;

import org.molgenis.security.core.model.Role;

import java.util.List;

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

}
