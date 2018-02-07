package org.molgenis.security.permission;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

@Component
public class PermissionSystemServiceImpl implements PermissionSystemService
{
	private final MutableAclService aclService;

	public PermissionSystemServiceImpl(MutableAclService aclService)
	{
		this.aclService = requireNonNull(aclService);
	}

	@Override
	public void giveUserWriteMetaPermissions(EntityType entityType)
	{
		giveUserWriteMetaPermissions(singleton(entityType));
	}

	@Override
	public void giveUserWriteMetaPermissions(Collection<EntityType> entityTypes)
	{
		// superusers and system user have all permissions by default
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return;
		}

		Sid sid = new PrincipalSid(SecurityContextHolder.getContext().getAuthentication());
		runAsSystem(() -> insertAces(sid, entityTypes.stream().map(EntityTypeIdentity::new).collect(toList()),
				EntityTypePermission.WRITEMETA));
	}

	private void insertAces(Sid sid, Collection<ObjectIdentity> objectIdentities,
			org.springframework.security.acls.model.Permission permission)
	{
		for (ObjectIdentity id : objectIdentities)
		{
			MutableAcl acl = (MutableAcl) aclService.readAclById(id, singletonList(sid));
			acl.insertAce(0, permission, sid, true);
			aclService.updateAcl(acl);
		}
	}
}
