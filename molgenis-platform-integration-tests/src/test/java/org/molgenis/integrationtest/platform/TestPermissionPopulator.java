package org.molgenis.integrationtest.platform;

import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.EntityTypePermissionUtils.getCumulativePermission;
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
	public void populate(Map<String, EntityTypePermission> entityTypePermissionMap)
	{
		populate(entityTypePermissionMap, SecurityUtils.getCurrentUsername());
	}

	/**
	 * Populate entity type permissions for the current user.
	 */
	@Transactional
	public void populate(Map<String, EntityTypePermission> entityTypePermissionMap, String username)
	{
		Sid sid = new PrincipalSid(username);
		runAsSystem(() -> entityTypePermissionMap.forEach((entityTypeId, permission) ->
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(new EntityTypeIdentity(entityTypeId));
			acl.insertAce(acl.getEntries().size(), getCumulativePermission(permission), sid, true);
			mutableAclService.updateAcl(acl);
		}));
	}
}
