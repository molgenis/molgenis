package org.molgenis.data.security.acl;

import org.molgenis.security.core.Permission;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Maps domain object access control list to/from entity access control list.
 */
@Component
class AclMapper
{
	private final ObjectIdMapper objectIdMapper;
	private final PermissionMapper permissionMapper;
	private final SecurityIdMapper securityIdMapper;

	AclMapper(ObjectIdMapper objectIdMapper, PermissionMapper permissionMapper, SecurityIdMapper securityIdMapper)
	{
		this.objectIdMapper = requireNonNull(objectIdMapper);
		this.permissionMapper = requireNonNull(permissionMapper);
		this.securityIdMapper = requireNonNull(securityIdMapper);
	}

	EntityAcl toEntityAcl(Acl acl)
	{
		String entityTypeId = objectIdMapper.toEntityTypeId(acl.getObjectIdentity());
		Object entityId = objectIdMapper.toEntityId(acl.getObjectIdentity());
		SecurityId owner = securityIdMapper.toSecurityId(acl.getOwner());
		Acl parentAcl = acl.getParentAcl();
		EntityAcl parent = parentAcl != null ? toEntityAcl(parentAcl) : null;
		List<EntityAce> entries = toEntries(acl.getEntries());
		return EntityAcl.create(entityTypeId, entityId, owner, parent, entries);
	}

	private List<EntityAce> toEntries(List<AccessControlEntry> entries)
	{
		return entries.stream().map(this::toEntry).collect(toList());
	}

	private EntityAce toEntry(AccessControlEntry accessControlEntry)
	{
		Permission permission = permissionMapper.toPermission(accessControlEntry.getPermission());
		SecurityId securityId = securityIdMapper.toSecurityId(accessControlEntry.getSid());
		return EntityAce.create(permission, securityId, accessControlEntry.isGranting());
	}
}
