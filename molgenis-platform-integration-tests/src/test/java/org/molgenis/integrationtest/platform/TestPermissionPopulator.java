package org.molgenis.integrationtest.platform;

import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.RepositoryIdentity;
import org.molgenis.data.security.RepositoryPermission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.RepositoryPermissionUtils.getCumulativePermission;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

@Component
public class TestPermissionPopulator
{
	private final MutableAclService mutableAclService;

	TestPermissionPopulator(MutableAclService mutableAclService)
	{
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	/**
	 * Populate entity type permissions for the current user.
	 */
	@Transactional
	public void populate(Map<String, RepositoryPermission> entityTypePermissionMap)
	{
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
		runAsSystem(() -> entityTypePermissionMap.forEach((entityTypeId, permission) ->
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new RepositoryIdentity(entityTypeId));
			acl.insertAce(acl.getEntries().size(), getCumulativePermission(permission), sid, true);
			mutableAclService.updateAcl(acl);

			//RLS permission on entityType
			MutableAcl rlsAcl = (MutableAcl) mutableAclService.readAclById(
					new EntityIdentity(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityTypeId));
			rlsAcl.insertAce(rlsAcl.getEntries().size(), EntityPermission.READ, sid, true);
			mutableAclService.updateAcl(rlsAcl);
		}));
	}
}
