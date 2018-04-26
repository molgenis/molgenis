package org.molgenis.integrationtest.platform;

import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static java.util.Objects.requireNonNull;
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
	public void populate(Map<ObjectIdentity, Permission> entityTypePermissionMap)
	{
		populate(entityTypePermissionMap, SecurityUtils.getCurrentUsername());
	}

	/**
	 * Populate entity type permissions for the current user.
	 */
	@Transactional
	public void populate(Map<ObjectIdentity, Permission> permissions, String username)
	{
		Sid sid = new PrincipalSid(username);
		runAsSystem(() -> permissions.forEach((objectIdentity, permission) ->
		{
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
			acl.insertAce(acl.getEntries().size(), permission, sid, true);
			mutableAclService.updateAcl(acl);
		}));
	}
}
