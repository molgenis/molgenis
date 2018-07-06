package org.molgenis.data.security;

import org.molgenis.data.security.auth.Group;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class GroupIdentity extends ObjectIdentityImpl
{
	public static final String GROUP = "group";

	public GroupIdentity(Group group)
	{
		super(GROUP, group.getName());
	}
}