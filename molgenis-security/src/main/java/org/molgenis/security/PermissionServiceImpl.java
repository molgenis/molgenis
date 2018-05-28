package org.molgenis.security;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.acl.SidUtils.createRoleSid;
import static org.molgenis.security.acl.SidUtils.createUserSid;

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
		grant(objectPermissionMap, createRoleSid(role));
	}

	@Override
	public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Role role)
	{
		grant(objectIdentity, permissionSet, createRoleSid(role));
	}

	@Override
	public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, User user)
	{
		grant(objectPermissionMap, createUserSid(user));
	}

	@Override
	public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, User user)
	{
		grant(objectIdentity, permissionSet, createUserSid(user));
	}

	@Override
	public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Sid sid)
	{
		objectPermissionMap.forEach((objectIdentity, permissionSet) -> grant(objectIdentity, permissionSet, sid));
	}

	@Override
	public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Sid sid)
	{
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
		acl.insertAce(acl.getEntries().size(), permissionSet, sid, true);
		mutableAclService.updateAcl(acl);
	}
}
