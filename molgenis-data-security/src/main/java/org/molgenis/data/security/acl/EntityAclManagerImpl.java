package org.molgenis.data.security.acl;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.security.core.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
public class EntityAclManagerImpl implements EntityAclManager
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityAclManagerImpl.class);

	private final AclService aclService;
	private final ObjectIdMapper objectIdMapper;
	private final AclMapper aclMapper;
	private final SecurityIdMapper securityIdMapper;
	private final PermissionMapper permissionMapper;

	public EntityAclManagerImpl(AclService aclService, AclMapper aclMapper, ObjectIdMapper objectIdMapper,
			SecurityIdMapper securityIdMapper, PermissionMapper permissionMapper)
	{
		this.aclService = requireNonNull(aclService);
		this.objectIdMapper = requireNonNull(objectIdMapper);
		this.aclMapper = requireNonNull(aclMapper);
		this.securityIdMapper = requireNonNull(securityIdMapper);
		this.permissionMapper = requireNonNull(permissionMapper);
	}

	@Override
	public boolean isGranted(Entity entity, List<Permission> permissions, List<SecurityId> securityIds)
	{
		Acl acl = readDomainAcl(entity);
		List<org.springframework.security.acls.model.Permission> domainPermissions = toDomainPermissions(permissions);
		List<Sid> sids = toSids(securityIds);
		try
		{
			return acl.isGranted(domainPermissions, sids, false);
		}
		catch (NotFoundException e)
		{
			return false;
		}
	}

	@Override
	public EntityAcl readAcl(Entity entity)
	{
		return readAcl(entity, null);
	}

	@Override
	public EntityAcl readAcl(Entity entity, List<SecurityId> securityIds)
	{
		Acl acl = readDomainAcl(entity, securityIds);
		return aclMapper.toEntityAcl(acl);
	}

	@Override
	public EntityAcl createAcl(Entity entity, List<EntityAce> entityAces)
	{
		MutableAcl acl = createDomainAcl(entity, entityAces);
		return aclMapper.toEntityAcl(acl);
	}

	private MutableAcl createDomainAcl(Entity entity, List<EntityAce> entityAces)
	{
		ObjectIdentity objectId = objectIdMapper.toObjectIdentity(entity);
		MutableAcl acl = readDomainAcl(entity);
		if (acl == null)
		{
			acl = aclService.createAcl(objectId);
		}
		Attribute attribute = entity.getEntityType().getEntityLevelSecurityInheritance();
		Entity inheritedEntity = attribute != null ? entity.getEntity(attribute.getName()) : null;

		Acl parentAcl;
		if (inheritedEntity != null)
		{
			parentAcl = readDomainAcl(inheritedEntity);
			if (parentAcl == null)
			{
				parentAcl = createDomainAcl(inheritedEntity, emptyList());
			}
		}
		else
		{
			parentAcl = null;
		}
		acl.setParent(parentAcl);

		insertAclAces(entityAces, acl);

		aclService.updateAcl(acl);
		return acl;
	}

	private void insertAclAces(List<EntityAce> entityAces, MutableAcl acl)
	{
		for (int i = 0; i < entityAces.size(); i++)
		{
			EntityAce entityAce = entityAces.get(i);
			org.springframework.security.acls.model.Permission permission = permissionMapper
					.toDomainPermission(entityAce.getPermission());
			Sid sid = securityIdMapper.toSid(entityAce.getSecurityId());
			acl.insertAce(i, permission, sid, entityAce.isGranting());
		}
	}

	@Override
	public void updateAcl(EntityAcl entityAcl)
	{
		MutableAcl acl = readDomainAcl(entityAcl);

		Acl parentAcl = entityAcl.getParent() != null ? readDomainAcl(entityAcl.getParent()) : null;
		acl.setParent(parentAcl);

		Sid owner = securityIdMapper.toSid(entityAcl.getOwner());
		acl.setOwner(owner);

		insertAclAces(entityAcl.getEntries(), acl);

		aclService.updateAcl(acl);
	}

	@Override
	public void deleteAcl(Entity entity)
	{
		ObjectIdentity objectId = objectIdMapper.toObjectIdentity(entity);
		aclService.deleteAcl(objectId, false);
	}

	private MutableAcl readDomainAcl(Entity entity)
	{
		return readDomainAcl(entity, null);
	}

	private MutableAcl readDomainAcl(Entity entity, List<SecurityId> securityIds)
	{
		ObjectIdentity objectId = objectIdMapper.toObjectIdentity(entity);
		return readDomainAcl(objectId, securityIds);
	}

	private MutableAcl readDomainAcl(EntityAcl entityAcl)
	{
		ObjectIdentity objectId = objectIdMapper.toObjectIdentity(entityAcl.getEntityTypeId(), entityAcl.getEntityId());
		return readDomainAcl(objectId, null);
	}

	private MutableAcl readDomainAcl(ObjectIdentity objectIdentity, List<SecurityId> securityIds)
	{
		List<Sid> sids = securityIds != null ? toSids(securityIds) : null;
		MutableAcl acl;
		try
		{
			acl = (MutableAcl) aclService.readAclById(objectIdentity, sids);
		}
		catch (NotFoundException e)
		{
			acl = null;
		}
		return acl;
	}

	private List<org.springframework.security.acls.model.Permission> toDomainPermissions(List<Permission> permissions)
	{
		return permissions.stream().map(permissionMapper::toDomainPermission).collect(toList());
	}

	private List<Sid> toSids(List<SecurityId> securityIds)
	{
		return securityIds.stream().map(securityIdMapper::toSid).collect(toList());
	}
}
