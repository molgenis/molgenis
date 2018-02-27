package org.molgenis.security.acl;

import java.util.Collection;

public interface AclClassService
{
	boolean hasAclClass(String type);

	Collection<String> getAclClasses();
}
