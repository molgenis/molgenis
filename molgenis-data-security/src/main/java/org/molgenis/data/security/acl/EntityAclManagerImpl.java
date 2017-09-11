package org.molgenis.data.security.acl;

import com.google.common.collect.ImmutableSet;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.EntityUtils;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

@Service
public class EntityAclManagerImpl implements EntityAclManager
{
	private static final int BATCH_SIZE = 1000;

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
	@Transactional(readOnly = true)
	public boolean isGranted(EntityIdentity entityIdentity, Permission permission)
	{
		Acl acl = readDomainAcl(entityIdentity);
		List<org.springframework.security.acls.model.Permission> domainPermissions = singletonList(
				toDomainPermission(permission));
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
	@Transactional(readOnly = true)
	public EntityAcl readAcl(EntityIdentity entityIdentity)
	{
		Acl acl = readDomainAcl(entityIdentity);
		return toEntityAcl(acl);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<EntityAcl> readAcls(Collection<EntityIdentity> entityIdentities)
	{
		return entityIdentities.stream().map(this::readAcl).collect(toList());
	}

	@Override
	@Transactional
	public void createAcl(Entity entity)
	{
		List<EntityAce> entityAces;
		if (!currentUserIsSuOrSystem())
		{
			SecurityId securityId = SecurityId.createForUsername(SecurityUtils.getCurrentUsername());
			EntityAce entityAce = EntityAce.create(ImmutableSet.of(Permission.WRITE, Permission.READ), securityId,
					true);
			entityAces = singletonList(entityAce);
		}
		else
		{
			entityAces = emptyList();
		}
		createDomainAcl(entity, entityAces);
	}

	@Override
	public boolean hasAclClass(EntityType entityType)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME implement
	}

	@Override
	public String getAclClassParent(EntityType entityType)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME implement
	}

	@Override
	@Transactional
	public void createAclClass(EntityType entityType)
	{
		// TODO do not create ACL class on demand when calling aclService.createAcl but create when calling this method
	}

	@Override
	@Transactional
	public void deleteAclClass(EntityType entityType)
	{
		Fetch entityIdFetch = new Fetch().field(entityType.getIdAttribute().getName());
		dataService.getMeta().getRepository(entityType).forEachBatched(entityIdFetch, this::deleteAcls, BATCH_SIZE);
	}

	@Override
	@Transactional
	public void createAcls(Collection<Entity> entities)
	{
		entities.forEach(this::createAcl);
	}

	@Override
	@Transactional
	public void updateAcl(EntityAcl entityAcl)
	{
		MutableAcl acl = readDomainAcl(entityAcl.getEntityIdentity());

		Acl parentAcl = entityAcl.getParent() != null ? readDomainAcl(entityAcl.getParent().getEntityIdentity()) : null;
		acl.setParent(parentAcl);

		Sid owner = toSid(entityAcl.getOwner());
		acl.setOwner(owner);

		for (int aceIndex = acl.getEntries().size() - 1; aceIndex >= 0; aceIndex--)
		{
			acl.deleteAce(aceIndex);
		}
		insertAclAces(entityAcl.getEntries(), acl);

		aclService.updateAcl(acl);
	}

	@Override
	@Transactional
	public void updateAcls(Collection<EntityAcl> entityAcls)
	{
		entityAcls.forEach(this::updateAcl);
	}

	@Override
	@Transactional
	public void deleteAcl(Entity entity)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(entity.getEntityType().getId(), entity.getIdValue());
		deleteAcl(entityIdentity);
	}

	@Override
	@Transactional
	public void deleteAcl(EntityIdentity entityIdentity)
	{
		ObjectIdentity objectId = toObjectIdentity(entityIdentity);
		aclService.deleteAcl(objectId, false);
	}

	@Override
	@Transactional
	public void deleteAcls(Collection<Entity> entities)
	{
		entities.forEach(this::deleteAcl);
	}

	private MutableAcl createDomainAcl(Entity entity, List<EntityAce> entityAces)
	{
		MutableAcl acl = readDomainAcl(entity);
		if (acl == null)
		{
			ObjectIdentity objectIdentity = toObjectIdentity(entity);
			acl = aclService.createAcl(objectIdentity);
		}
		String aclClassParent = getAclClassParent(entity.getEntityType());
		Attribute attribute = entity.getEntityType().getAttribute(aclClassParent);
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
		int nrEntityAces = entityAces.size();
		for (int i = 0; i < nrEntityAces; i++)
		{
			EntityAce entityAce = entityAces.get(i);
			org.springframework.security.acls.model.Permission permission = toDomainPermission(
					entityAce.getPermissions());
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
		Set<Permission> permissions = toPermission(accessControlEntry.getPermission());
		SecurityId securityId = toSecurityId(accessControlEntry.getSid());
		return EntityAce.create(permissions, securityId, accessControlEntry.isGranting());
	}

	private SecurityId toSecurityId(Sid sid)
	{
		if (sid instanceof PrincipalSid)
		{
			String principal = ((PrincipalSid) sid).getPrincipal();
			return SecurityId.createForUsername(principal);
		}
		if (sid instanceof GrantedAuthoritySid)
		{
			String grantedAuthority = ((GrantedAuthoritySid) sid).getGrantedAuthority();
			return SecurityId.createForAuthority(grantedAuthority);
		}
		throw new IllegalArgumentException(String.format("Unknown Sid type '%s'", sid.getClass().getSimpleName()));
	}

	private org.springframework.security.acls.model.Permission toDomainPermission(Set<Permission> permissions)
	{
		CumulativePermission result = new CumulativePermission();
		for (Permission permission : permissions)
		{
			if (permission != null) // GSON serializes unknown enum values to null
			{
				result.set(toDomainPermission(permission));
			}
		}
		return result;
	}

	private org.springframework.security.acls.model.Permission toDomainPermission(Permission permission)
	{
		switch (permission)
		{
			case COUNT: // FIXME treat COUNT differently from READ
			case READ:
				return BasePermission.READ;
			case WRITE:
				return BasePermission.WRITE;
			case NONE:
				throw new IllegalArgumentException(String.format("Illegal permission '%s'", permission.toString()));
			case WRITEMETA:
				return BasePermission.ADMINISTRATION;
			default:
				throw new RuntimeException(String.format("Unknown permission '%s'", permission.toString()));
		}
	}

	private Set<Permission> toPermission(org.springframework.security.acls.model.Permission permission)
	{
		ImmutableSet.Builder<Permission> result = ImmutableSet.builder();
		if ((permission.getMask() & BasePermission.READ.getMask()) > 0)
		{
			result.add(Permission.READ);
		}
		if ((permission.getMask() & BasePermission.WRITE.getMask()) > 0)
		{
			result.add(Permission.WRITE);
		}
		//TODO still relevant?
		if ((permission.getMask() & BasePermission.ADMINISTRATION.getMask()) > 0)
		{
			result.add(Permission.WRITEMETA);
		}
		return result.build();
	}
}
