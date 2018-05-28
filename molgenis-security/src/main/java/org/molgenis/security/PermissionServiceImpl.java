package org.molgenis.security;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.acl.SidUtils;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

@Component
public class PermissionServiceImpl implements PermissionService
{
	private MutableAclService mutableAclService;

	public PermissionServiceImpl(MutableAclService mutableAclService)
	{
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Role role)
	{
		runAsSystem(() -> objectPermissionMap.forEach(
				(objectIdentity, permissionSet) -> grant(objectIdentity, permissionSet, role)));
	}

	@Override
	public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Role role)
	{
		Sid sid = SidUtils.createRoleSid(role);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
		acl.insertAce(acl.getEntries().size(), permissionSet, sid, true);
		mutableAclService.updateAcl(acl);
	}

	@Override
	public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, User user)
	{
		runAsSystem(() -> objectPermissionMap.forEach(
				(objectIdentity, permissionSet) -> grant(objectIdentity, permissionSet, user)));
	}

	@Override
	public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, User user)
	{
		Sid sid = SidUtils.createUserSid(user);
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
		acl.insertAce(acl.getEntries().size(), permissionSet, sid, true);
		mutableAclService.updateAcl(acl);
	}
}
