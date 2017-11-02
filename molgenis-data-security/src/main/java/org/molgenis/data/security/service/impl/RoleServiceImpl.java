package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.security.model.RoleEntity;
import org.molgenis.data.security.model.RoleFactory;
import org.molgenis.data.security.model.RoleMetadata;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.model.ConceptualRoles.values;

@Service
public class RoleServiceImpl implements RoleService
{
	private final DataService dataService;
	private final RoleFactory roleFactory;

	public RoleServiceImpl(DataService dataService, RoleFactory roleFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.roleFactory = requireNonNull(roleFactory);
	}

	@Override
	public List<Role> createRolesForGroup(String groupLabel)
	{
		return stream(values()).map(role -> addRoles(groupLabel, role.name())).collect(toList());
	}

	@Override
	public Optional<Role> findRoleById(String roleId)
	{
		return Optional.ofNullable(dataService.findOneById(RoleMetadata.ROLE, roleId, RoleEntity.class))
					   .map(RoleEntity::toRole);
	}

	private Role addRoles(String group, String role)
	{
		RoleEntity roleEntity = roleFactory.create();
		roleEntity.setLabel(role);
		dataService.add(RoleMetadata.ROLE, roleEntity);
		return roleEntity.toRole();
	}

}
