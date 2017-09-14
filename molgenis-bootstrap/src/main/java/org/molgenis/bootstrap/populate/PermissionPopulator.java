package org.molgenis.bootstrap.populate;

import org.springframework.context.ApplicationContext;

/**
 * Populates {@link org.molgenis.data.security.acl.EntityAclManager ACL service} with entity access control entries during bootstrapping.
 */
public interface PermissionPopulator
{
	void populate(ApplicationContext ctx);
}
