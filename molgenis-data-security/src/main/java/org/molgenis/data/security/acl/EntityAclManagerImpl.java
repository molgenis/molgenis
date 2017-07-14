package org.molgenis.data.security.acl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.EntityUtils;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

@Service
public class EntityAclManagerImpl implements EntityAclManager
{
	private final AclService aclService;
	private final SidRetrievalStrategy sidRetrievalStrategy;
	private final DataService dataService;

	public EntityAclManagerImpl(AclService aclService, SidRetrievalStrategy sidRetrievalStrategy,
			DataService dataService)
	{
		this.aclService = requireNonNull(aclService);
		this.sidRetrievalStrategy = requireNonNull(sidRetrievalStrategy);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean isGranted(EntityIdentity entityIdentity, Permission permission)
	{
		Acl acl = readDomainAcl(entityIdentity);
		List<org.springframework.security.acls.model.Permission> domainPermissions = toDomainPermissions(
				expandPermissions(permission));
		List<Sid> sids = getCurrentUserSids();
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
	public EntityAcl readAcl(EntityIdentity entityIdentity)
	{
		Acl acl = readDomainAcl(entityIdentity);
		return toEntityAcl(acl);
	}

	@Override
	public EntityAcl createAcl(Entity entity)
	{
		List<EntityAce> entityAces;
		if (!currentUserIsSuOrSystem())
		{
			SecurityId securityId = SecurityId.create(SecurityUtils.getCurrentUsername(), null);
			EntityAce entityAce = EntityAce.create(Permission.WRITE, securityId, true);
			entityAces = singletonList(entityAce);
		}
		else
		{
			entityAces = emptyList();
		}
		MutableAcl acl = createDomainAcl(entity, entityAces);
		return toEntityAcl(acl);
	}

	@Override
	public void updateAcl(EntityAcl entityAcl)
	{
		MutableAcl acl = readDomainAcl(entityAcl.getEntityIdentity());

		Acl parentAcl = entityAcl.getParent() != null ? readDomainAcl(entityAcl.getParent().getEntityIdentity()) : null;
		acl.setParent(parentAcl);

		Sid owner = toSid(entityAcl.getOwner());
		acl.setOwner(owner);

		insertAclAces(entityAcl.getEntries(), acl);

		aclService.updateAcl(acl);
	}

	@Override
	public void deleteAcl(EntityIdentity entityIdentity)
	{
		ObjectIdentity objectId = toObjectIdentity(entityIdentity);
		aclService.deleteAcl(objectId, false);
	}

	private MutableAcl createDomainAcl(Entity entity, List<EntityAce> entityAces)
	{
		MutableAcl acl = readDomainAcl(entity);
		if (acl == null)
		{
			ObjectIdentity objectIdentity = toObjectIdentity(entity);
			acl = aclService.createAcl(objectIdentity);
		}
		Attribute attribute = entity.getEntityType().getEntityLevelSecurityInheritance();
		Acl parentAcl;
		if (attribute != null)
		{
			Entity inheritedEntity = attribute != null ? entity.getEntity(attribute.getName()) : null;

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
		}
		else
		{
			parentAcl = createDomainAcl(entity.getEntityType(), emptyList());
		}
		acl.setParent(parentAcl);

		insertAclAces(entityAces, acl);

		aclService.updateAcl(acl);
		return acl;
	}

	private void insertAclAces(List<EntityAce> entityAces, MutableAcl acl)
	{
		int nrEntityAces = entityAces.size();
		for (int i = 0; i < nrEntityAces; i++)
		{
			EntityAce entityAce = entityAces.get(i);
			org.springframework.security.acls.model.Permission permission = toDomainPermission(
					entityAce.getPermission());
			Sid sid = toSid(entityAce.getSecurityId());
			acl.insertAce(i, permission, sid, entityAce.isGranting());
		}
	}

	private MutableAcl readDomainAcl(Entity entity)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(entity.getEntityType().getId(), entity.getIdValue());
		return readDomainAcl(entityIdentity);
	}

	private MutableAcl readDomainAcl(EntityIdentity entityIdentity)
	{
		ObjectIdentity objectIdentity = toObjectIdentity(entityIdentity);

		List<Sid> sids = getCurrentUserSids();
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
		return permissions.stream().map(this::toDomainPermission).collect(toList());
	}

	private List<Sid> getCurrentUserSids()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return sidRetrievalStrategy.getSids(authentication);
	}

	private static List<Permission> expandPermissions(Permission permission)
	{
		switch (permission)
		{
			case NONE:
				return singletonList(Permission.NONE);
			case COUNT:
				return Arrays.asList(Permission.COUNT, Permission.READ, Permission.WRITE, Permission.WRITEMETA);
			case READ:
				return Arrays.asList(Permission.READ, Permission.WRITE, Permission.WRITEMETA);
			case WRITE:
				return Arrays.asList(Permission.WRITE, Permission.WRITEMETA);
			case WRITEMETA:
				return singletonList(Permission.WRITEMETA);
			default:
				throw new RuntimeException(String.format("Unknown permission '%s'", permission.toString()));
		}
	}

	private ObjectIdentity toObjectIdentity(Entity entity)
	{
		return new ObjectIdentityImpl(entity.getEntityType().getId(), entity.getIdValue().toString());
	}

	private ObjectIdentity toObjectIdentity(EntityIdentity entityIdentity)
	{
		return new ObjectIdentityImpl(entityIdentity.getEntityTypeId(), entityIdentity.getEntityId().toString());
	}

	private EntityIdentity toEntityIdentity(ObjectIdentity objectIdentity)
	{
		String entityTypeId = objectIdentity.getType();

		EntityType entityType = dataService.getEntityType(entityTypeId);
		String untypedIdentifier = objectIdentity.getIdentifier().toString();
		Object entityId = EntityUtils.getTypedValue(untypedIdentifier, entityType.getIdAttribute());

		return EntityIdentity.create(entityTypeId, entityId);
	}

	private Sid toSid(SecurityId securityId)
	{
		if (securityId.getUsername() != null)
		{
			return new PrincipalSid(securityId.getUsername());
		}
		else
		{
			return new GrantedAuthoritySid(securityId.getAuthority());
		}
	}

	private EntityAcl toEntityAcl(Acl acl)
	{
		EntityIdentity entityIdentity = toEntityIdentity(acl.getObjectIdentity());
		SecurityId owner = toSecurityId(acl.getOwner());
		Acl parentAcl = acl.getParentAcl();
		EntityAcl parent = parentAcl != null ? toEntityAcl(parentAcl) : null;
		List<EntityAce> entries = toEntries(acl.getEntries());
		return EntityAcl.create(entityIdentity, owner, parent, entries);
	}

	private List<EntityAce> toEntries(List<AccessControlEntry> entries)
	{
		return entries.stream().map(this::toEntry).collect(toList());
	}

	private EntityAce toEntry(AccessControlEntry accessControlEntry)
	{
		Permission permission = toPermission(accessControlEntry.getPermission());
		SecurityId securityId = toSecurityId(accessControlEntry.getSid());
		return EntityAce.create(permission, securityId, accessControlEntry.isGranting());
	}

	private SecurityId toSecurityId(Sid sid)
	{
		if (sid instanceof PrincipalSid)
		{
			String principal = ((PrincipalSid) sid).getPrincipal();
			return SecurityId.create(principal, null);
		}
		if (sid instanceof GrantedAuthoritySid)
		{
			String grantedAuthority = ((GrantedAuthoritySid) sid).getGrantedAuthority();
			return SecurityId.create(null, grantedAuthority);
		}
		throw new IllegalArgumentException(String.format("Unknown Sid type '%s'", sid.getClass().getSimpleName()));
	}

	private org.springframework.security.acls.model.Permission toDomainPermission(Permission permission)
	{
		switch (permission)
		{
			case READ:
				return BasePermission.READ;
			case WRITE:
				return BasePermission.WRITE;
			case COUNT:
			case NONE:
				throw new IllegalArgumentException(String.format("Illegal permission '%s'", permission.toString()));
			case WRITEMETA:
				return BasePermission.ADMINISTRATION;
			default:
				throw new RuntimeException(String.format("Unknown permission '%s'", permission.toString()));
		}
	}

	private Permission toPermission(org.springframework.security.acls.model.Permission permission)
	{
		if (!(permission instanceof BasePermission))
		{
			throw new RuntimeException("Permission is not a BasePermission");
		}

		BasePermission basePermission = (BasePermission) permission;
		if (basePermission == BasePermission.READ)
		{
			return Permission.READ;
		}
		else if (basePermission == BasePermission.WRITE)
		{
			return Permission.WRITE;
		}
		else
		{
			throw new RuntimeException(
					String.format("BasePermission '%s' cannot be mapped to Permission", basePermission));
		}
	}
}
