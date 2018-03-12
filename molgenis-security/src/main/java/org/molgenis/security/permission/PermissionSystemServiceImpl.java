package org.molgenis.security.permission;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.RepositoryIdentity;
import org.molgenis.data.security.RepositoryPermission;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.security.acl.SidUtils;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.RepositoryPermissionUtils.getCumulativePermission;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

/**
 * @deprecated use {@link org.springframework.security.acls.model.MutableAclService}
 */
@Deprecated
@Component
public class PermissionSystemServiceImpl implements PermissionSystemService
{
	private final MutableAclService mutableAclService;

	public PermissionSystemServiceImpl(MutableAclService mutableAclService)
	{
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public void giveUserWriteMetaPermissions(EntityType entityType)
	{
		giveUserWriteMetaPermissions(singletonList(entityType));
	}

	@Override
	public void giveUserWriteMetaPermissions(Collection<EntityType> entityTypes)
	{
		Sid sid = SidUtils.createSid(getCurrentUsername());
		runAsSystem(() ->
		{
			CumulativePermission permission = getCumulativePermission(RepositoryPermission.WRITEMETA);
			entityTypes.forEach(entityType ->
			{
				MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new RepositoryIdentity(entityType));
				acl.insertAce(acl.getEntries().size(), permission, sid, true);
				mutableAclService.updateAcl(acl);
			});
		});
	}
}
