package org.molgenis.security.core;

import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

import java.util.Map;

public interface PermissionService
{
	void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Sid sid);

	void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Sid sid);
}
