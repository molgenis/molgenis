package org.molgenis.data.security.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.security.model.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.model.ConceptualRoles.values;

@Service
public class RoleServiceImpl implements RoleService
{
	private final DataService dataService;
	private final RoleFactory roleFactory;

	public RoleServiceImpl(DataService dataService, RoleFactory roleFactory) {
		this.dataService = requireNonNull(dataService);
		this.roleFactory = requireNonNull(roleFactory);
	}

	@Override
	public List<Role> createRolesForGroup(String group) {
		return stream(values()).map(role -> addRoles(group, role.name())).collect(Collectors.toList());
	}

	@Override
	public Optional<Role> findRoleById(String roleId)
	{
		return Optional.ofNullable(dataService.findOneById(RoleMetadata.ROLE, roleId, RoleEntity.class))
				.map(RoleEntity::toRole);
	}

	private Role addRoles(String group, String role) {
		RoleEntity roleEntity = roleFactory.create();
		roleEntity.setLabel(group + "-" + role);
		dataService.add(RoleMetadata.ROLE, roleEntity);
		return roleEntity.toRole();
	}


}
